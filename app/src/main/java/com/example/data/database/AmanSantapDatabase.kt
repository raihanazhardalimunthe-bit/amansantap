package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ActivityDao
import com.example.data.dao.AlertDao
import com.example.data.dao.ChecklistDao
import com.example.data.dao.EducationDao
import com.example.data.dao.LocationDao
import com.example.data.dao.ReportDao
import com.example.data.dao.ReviewDao
import com.example.data.dao.RuleConfigDao
import com.example.data.dao.UserDao
import com.example.data.model.ActivityLogEntity
import com.example.data.model.AlertEntity
import com.example.data.model.DetectionRuleConfigEntity
import com.example.data.model.EducationalContentEntity
import com.example.data.model.FoodLocationEntity
import com.example.data.model.FoodReportEntity
import com.example.data.model.MerchantChecklistEntity
import com.example.data.model.ReportReviewEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        FoodLocationEntity::class,
        FoodReportEntity::class,
        AlertEntity::class,
        ReportReviewEntity::class,
        EducationalContentEntity::class,
        ActivityLogEntity::class,
        DetectionRuleConfigEntity::class,
        MerchantChecklistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AmanSantapDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun alertDao(): AlertDao
    abstract fun reviewDao(): ReviewDao
    abstract fun locationDao(): LocationDao
    abstract fun educationDao(): EducationDao
    abstract fun activityDao(): ActivityDao
    abstract fun ruleConfigDao(): RuleConfigDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AmanSantapDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AmanSantapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmanSantapDatabase::class.java,
                    "amansantap_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        DatabaseSeeder.seedInitialData(database)
                    }
                }
            }
        }
    }
}
