package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SOSMulherRepository(private val db: AppDatabase) {

    // User Profile
    val userProfile: Flow<UserProfile?> = db.userProfileDao().getProfile()
    
    suspend fun getProfileDirect(): UserProfile? = withContext(Dispatchers.IO) {
        db.userProfileDao().getProfileDirect()
    }
    
    suspend fun saveProfile(profile: UserProfile): Unit = withContext(Dispatchers.IO) {
        db.userProfileDao().saveProfile(profile)
    }

    suspend fun updateCheckInState(active: Boolean, endTime: Long, seconds: Int): Unit = withContext(Dispatchers.IO) {
        db.userProfileDao().updateCheckInState(active, endTime, seconds)
    }

    // Contacts
    val allContacts: Flow<List<Contact>> = db.contactDao().getAllContacts()
    
    suspend fun getContactsDirect(): List<Contact> = withContext(Dispatchers.IO) {
        db.contactDao().getContactsDirect()
    }

    suspend fun insertContact(contact: Contact): Unit = withContext(Dispatchers.IO) {
        db.contactDao().insertContact(contact)
    }

    suspend fun updateContact(contact: Contact): Unit = withContext(Dispatchers.IO) {
        db.contactDao().updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact): Unit = withContext(Dispatchers.IO) {
        db.contactDao().deleteContact(contact)
    }

    suspend fun insertDefaultContactsIfEmpty(): Unit = withContext(Dispatchers.IO) {
        val direct = db.contactDao().getContactsDirect()
        if (direct.isEmpty()) {
            db.contactDao().insertContact(Contact(name = "Patrulha Maria da Penha", phone = "180", relationship = "Serviço Público", isPriority = true))
            db.contactDao().insertContact(Contact(name = "Polícia Militar", phone = "190", relationship = "Serviço de Emergência", isPriority = true))
            db.contactDao().insertContact(Contact(name = "Contato de Apoio (Exemplo)", phone = "+5511999999999", relationship = "Família/Amiga", isPriority = true))
        }
    }

    // Alert History
    val allAlerts: Flow<List<AlertHistory>> = db.alertHistoryDao().getAllAlerts()

    suspend fun insertAlert(alert: AlertHistory): Unit = withContext(Dispatchers.IO) {
        db.alertHistoryDao().insertAlert(alert)
    }

    // Community Posts
    val allPosts: Flow<List<CommunityPost>> = db.communityPostDao().getAllPosts()

    suspend fun insertPost(post: CommunityPost): Unit = withContext(Dispatchers.IO) {
        db.communityPostDao().insertPost(post)
    }

    suspend fun insertDefaultPostsIfEmpty(): Unit = withContext(Dispatchers.IO) {
        val count = db.communityPostDao().getPostsCount()
        if (count == 0) {
            db.communityPostDao().insertPost(CommunityPost(author = "Conselho Mulher", content = "Lembre-se: Você não está sozinha. Ative o Check-in de segurança ao sair à noite sozinha.", location = "São Paulo, SP"))
            db.communityPostDao().insertPost(CommunityPost(author = "Patrulha Maria da Penha", content = "Nova ronda ativa no centro de apoio. Qualquer ocorrência ou suspeita pode ser registrada no boletim digital.", location = "Rio de Janeiro, RJ"))
        }
    }

    // Digital Bulletins
    val allBulletins: Flow<List<DigitalBulletin>> = db.digitalBulletinDao().getAllBulletins()

    suspend fun insertBulletin(bulletin: DigitalBulletin): Unit = withContext(Dispatchers.IO) {
        db.digitalBulletinDao().insertBulletin(bulletin)
    }
}
