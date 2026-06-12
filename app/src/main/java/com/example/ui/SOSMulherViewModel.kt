package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SOSMulherViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = SOSMulherRepository(db)

    // UI Navigation State: Starts as loading, then moves to onboarding or home based on profile
    private val _currentScreen = MutableStateFlow("loading")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Last selected tab for bottom bar (helps restore state from detailed screens)
    private val _lastSelectedTab = MutableStateFlow("home")
    val lastSelectedTab: StateFlow<String> = _lastSelectedTab.asStateFlow()

    // SOS Trigger state
    private val _countdownActive = MutableStateFlow(false)
    val countdownActive: StateFlow<Boolean> = _countdownActive.asStateFlow()

    private val _countdownValue = MutableStateFlow(3)
    val countdownValue: StateFlow<Int> = _countdownValue.asStateFlow()

    // GPS coordinate representation
    val lastLocationCoordinates = MutableStateFlow<String?>(null)

    // Check-In Safety Timer variables
    private var checkInJob: Job? = null
    
    private val _checkInTimeRemaining = MutableStateFlow(0) // seconds
    val checkInTimeRemaining: StateFlow<Int> = _checkInTimeRemaining.asStateFlow()

    private val _isCheckInActive = MutableStateFlow(false)
    val isCheckInActive: StateFlow<Boolean> = _isCheckInActive.asStateFlow()

    // Fake notebook state for disguise mode
    private val _fakeNotes = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "Receitas saudáveis" to "Ingredientes:\n1. Banana amassada\n2. Aveia em flocos\n3. Mel\nMisturar tudo e colocar no forno por 15 minutos.",
            "Lista de compras" to "- Leite de amêndoas\n- Tomates cereja\n- Papel toalha\n- Café moído\n- Pão integral",
            "Anotações de estudos" to "Revisar os capítulos 3 e 4 do livro de Administração Básica na segunda de manhã.\nFazer mapa mental dos conceitos de liderança situacional."
        )
    )
    val fakeNotes: StateFlow<List<Pair<String, String>>> = _fakeNotes.asStateFlow()

    // Expose flows from db
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<AlertHistory>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bulletins: StateFlow<List<DigitalBulletin>> = repository.allBulletins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Pre-fill contacts and community on first opening
            repository.insertDefaultContactsIfEmpty()
            repository.insertDefaultPostsIfEmpty()
            
            // Check if profile exists; if not, direct to onboarding exactly ONCE on start!
            val profile = repository.getProfileDirect()
            if (profile == null || !profile.onboardingCompleted) {
                _currentScreen.value = "onboarding"
            } else {
                _currentScreen.value = "home"
                // Sync active checkin if VM was recreated
                if (profile.isCheckInActive) {
                    _isCheckInActive.value = true
                    val timeDiff = (profile.checkInEndTime - System.currentTimeMillis()) / 1000
                    if (timeDiff > 0) {
                        startLocalCheckInCountdown(timeDiff.toInt())
                    } else {
                        // Already triggered
                        _isCheckInActive.value = false
                        triggerSOSAlert("Check-in Expirou")
                    }
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        if (screen in listOf("home", "comunidade", "historico", "perfil")) {
            _lastSelectedTab.value = screen
        }
    }

    // Onboarding and User Profile edits
    fun registerProfile(name: String, email: String, cpf: String, gender: String) {
        viewModelScope.launch {
            val updated = UserProfile(
                id = 1,
                name = name,
                email = email,
                cpf = cpf,
                gender = gender,
                onboardingCompleted = true
            )
            repository.saveProfile(updated)
            navigateTo("home")
        }
    }

    // Contacts CRUD
    fun addContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            repository.insertContact(Contact(name = name, phone = phone, relationship = relationship, isPriority = true))
        }
    }

    fun editContact(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    // Community post submit
    fun submitPost(content: String, location: String) {
        viewModelScope.launch {
            val user = userProfile.value?.name ?: "Usuária Anônima"
            repository.insertPost(CommunityPost(author = user, content = content, location = location))
        }
    }

    // Alert submission
    fun recordAlert(source: String) {
        viewModelScope.launch {
            val loc = lastLocationCoordinates.value ?: "Localização desconhecida"
            val contactCount = contacts.value.size
            repository.insertAlert(
                AlertHistory(
                    location = loc,
                    status = "Notificados via WhatsApp ($source)",
                    contactsNotifiedCount = contactCount
                )
            )
        }
    }

    // Submit Digital Police Bulletins
    fun submitBulletin(incidentType: String, description: String, location: String) {
        viewModelScope.launch {
            repository.insertBulletin(
                DigitalBulletin(
                    incidentType = incidentType,
                    description = description,
                    location = location
                )
            )
        }
    }

    // Simple counts
    fun addFakeNote(title: String, content: String) {
        val list = _fakeNotes.value.toMutableList()
        list.add(0, title to content)
        _fakeNotes.value = list
    }

    // SOS Countdown logic
    private var countdownJob: Job? = null

    fun startSOSCountdown() {
        if (_countdownActive.value) return
        _countdownActive.value = true
        _countdownValue.value = 3
        countdownJob = viewModelScope.launch {
            while (_countdownValue.value > 0) {
                delay(1000)
                _countdownValue.value -= 1
            }
            // Trigger alert when finished
            triggerSOSAlert("Botão SOS Ativado")
            _countdownActive.value = false
        }
    }

    fun cancelSOSCountdown() {
        countdownJob?.cancel()
        _countdownActive.value = false
        _countdownValue.value = 3
    }

    private val _sosSimulationActive = MutableStateFlow(false)
    val sosSimulationActive: StateFlow<Boolean> = _sosSimulationActive.asStateFlow()

    fun dismissSimulation() {
        _sosSimulationActive.value = false
    }

    fun triggerSOSAlert(source: String) {
        recordAlert(source)
        _sosSimulationActive.value = true
    }

    fun triggerImmediateSOS() {
        triggerSOSAlert("Botão SOS Imediato")
    }

    // Check-in timer setup
    fun startCheckInTimer(minutes: Int) {
        val seconds = minutes * 60
        viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (seconds * 1000L)
            _isCheckInActive.value = true
            _checkInTimeRemaining.value = seconds
            
            // Persist to Room so it survives closing
            val currentProf = userProfile.value ?: UserProfile(name = "Usuária", email = "", cpf = "", gender = "")
            val updated = currentProf.copy(
                isCheckInActive = true,
                checkInEndTime = endTime,
                checkInTimerSeconds = seconds
            )
            repository.saveProfile(updated)

            startLocalCheckInCountdown(seconds)
        }
    }

    private fun startLocalCheckInCountdown(initSeconds: Int) {
        checkInJob?.cancel()
        _checkInTimeRemaining.value = initSeconds
        checkInJob = viewModelScope.launch {
            while (_checkInTimeRemaining.value > 0) {
                delay(1000)
                _checkInTimeRemaining.value -= 1
            }
            // Timer finished, user may be in absolute danger! Trigger emergency alert
            _isCheckInActive.value = false
            triggerSOSAlert("Check-in expirado")
            
            // Reset state in db
            val currentProf = userProfile.value
            if (currentProf != null) {
                repository.saveProfile(currentProf.copy(isCheckInActive = false))
            }
        }
    }

    fun cancelCheckInTimer() {
        checkInJob?.cancel()
        _isCheckInActive.value = false
        _checkInTimeRemaining.value = 0
        viewModelScope.launch {
            val currentProf = userProfile.value
            if (currentProf != null) {
                repository.saveProfile(currentProf.copy(isCheckInActive = false))
            }
        }
    }
}
