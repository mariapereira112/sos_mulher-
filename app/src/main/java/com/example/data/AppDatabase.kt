package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET isCheckInActive = :active, checkInEndTime = :endTime, checkInTimerSeconds = :seconds WHERE id = 1")
    suspend fun updateCheckInState(active: Boolean, endTime: Long, seconds: Int)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)
    
    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Query("SELECT * FROM contacts")
    suspend fun getContactsDirect(): List<Contact>
}

@Dao
interface AlertHistoryDao {
    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertHistory)
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Query("UPDATE community_posts SET likesCount = likesCount + (CASE WHEN :like != 0 THEN 1 ELSE -1 END) WHERE id = :id")
    suspend fun updateLikes(id: Int, like: Boolean)

    @Query("SELECT COUNT(*) FROM community_posts")
    suspend fun getPostsCount(): Int
}

@Dao
interface DigitalBulletinDao {
    @Query("SELECT * FROM digital_bulletins ORDER BY timestamp DESC")
    fun getAllBulletins(): Flow<List<DigitalBulletin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulletin(bulletin: DigitalBulletin)
}

@Database(
    entities = [
        UserProfile::class,
        Contact::class,
        AlertHistory::class,
        CommunityPost::class,
        DigitalBulletin::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun contactDao(): ContactDao
    abstract fun alertHistoryDao(): AlertHistoryDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun digitalBulletinDao(): DigitalBulletinDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sos_mulher_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
