package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLogEntity
import com.example.data.model.AlertEntity
import com.example.data.model.DetectionRuleConfigEntity
import com.example.data.model.EducationalContentEntity
import com.example.data.model.FoodLocationEntity
import com.example.data.model.FoodReportEntity
import com.example.data.model.MerchantChecklistEntity
import com.example.data.model.ReportReviewEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM food_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<FoodReportEntity>>

    @Query("SELECT * FROM food_reports WHERE id = :id")
    suspend fun getReportById(id: Long): FoodReportEntity?

    @Query("SELECT * FROM food_reports WHERE reportCode = :code")
    suspend fun getReportByCode(code: String): FoodReportEntity?

    @Query("SELECT * FROM food_reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatus(status: String): Flow<List<FoodReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: FoodReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<FoodReportEntity>)

    @Update
    suspend fun updateReport(report: FoodReportEntity)

    @Query("UPDATE food_reports SET status = :newStatus WHERE id = :reportId")
    suspend fun updateStatus(reportId: Long, newStatus: String)

    @Query("DELETE FROM food_reports WHERE isSimulationData = 1")
    suspend fun deleteSimulationReports()

    @Query("SELECT COUNT(*) FROM food_reports")
    fun getTotalReportsCount(): Flow<Int>
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getAlertById(id: Long): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Update
    suspend fun updateAlert(alert: AlertEntity)

    @Query("DELETE FROM alerts")
    suspend fun clearAllAlerts()
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM report_reviews WHERE reportId = :reportId ORDER BY reviewTimestamp DESC")
    fun getReviewsForReport(reportId: Long): Flow<List<ReportReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReportReviewEntity): Long
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM food_locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<FoodLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<FoodLocationEntity>)
}

@Dao
interface EducationDao {
    @Query("SELECT * FROM educational_contents ORDER BY id ASC")
    fun getAllContents(): Flow<List<EducationalContentEntity>>

    @Query("SELECT * FROM educational_contents WHERE category = :category ORDER BY id ASC")
    fun getContentsByCategory(category: String): Flow<List<EducationalContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContents(contents: List<EducationalContentEntity>)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long
}

@Dao
interface RuleConfigDao {
    @Query("SELECT * FROM detection_rule_config WHERE id = 1")
    fun getConfigFlow(): Flow<DetectionRuleConfigEntity?>

    @Query("SELECT * FROM detection_rule_config WHERE id = 1")
    suspend fun getConfig(): DetectionRuleConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: DetectionRuleConfigEntity)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM merchant_checklists ORDER BY id ASC")
    fun getAllChecklist(): Flow<List<MerchantChecklistEntity>>

    @Update
    suspend fun updateItem(item: MerchantChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(items: List<MerchantChecklistEntity>)

    @Query("UPDATE merchant_checklists SET isCompleted = 0")
    suspend fun resetAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}
