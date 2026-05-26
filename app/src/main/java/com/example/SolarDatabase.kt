package com.example

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// -- Entities --

@Entity(tableName = "system_config")
data class SystemConfig(
    @PrimaryKey val id: Int = 1,
    val systemVoltage: Int = 24, // 12V, 24V, 48V
    val averageSunHours: Double = 5.0, // Peak sun hours per day
    val batteryDod: Double = 0.50, // Depth of Discharge (e.g. 50%)
    val batteryAutonomyDays: Int = 2, // Days without sun
    val safetyFactor: Double = 1.25, // Security multiplier (e.g. 1.25)
    val inverterEfficiency: Double = 0.85, // Efficiency ratio (e.g. 85%)
    val panelWattage: Double = 400.0, // Default panel watt size (e.g. 400W)
    val batteryCapacityAh: Double = 200.0, // AH size of single battery
    val batteryVoltage: Double = 12.0, // Voltage of single battery unit
    val acVoltage: Double = 220.0, // Standard AC Voltage (V)
    val tiltAngle: Double = 30.0, // Tilt angle of support structure (degrees)
    val panelPhysicalLength: Double = 1.7, // Panel physical length along tilt (meters)
    val batteryChemistry: String = "Lithium", // "Lithium" or "Lead-Acid"
    val isHybridInverter: Boolean = true, // Has MPPT Hybrid Inverter?
    val mpptMaxVoc: Double = 150.0, // MPPT Max input Voc limit (Volt)
    val mpptMinVmp: Double = 30.0, // MPPT Min tracker threshold start voltage (Volt)
    val panelVoc: Double = 49.5, // Voc of the chosen panel type (Volt)
    val panelVmp: Double = 41.2 // Vmp of the chosen panel type (Volt)
)

@Entity(tableName = "appliances")
data class SolarAppliance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val powerWatts: Double,
    val quantity: Int,
    val daytimeHours: Double,
    val nighttimeHours: Double
) {
    val totalPower: Double get() = powerWatts * quantity
    val totalDailyEnergyWh: Double get() = totalPower * (daytimeHours + nighttimeHours)
    val totalDaytimeEnergyWh: Double get() = totalPower * daytimeHours
    val totalNighttimeEnergyWh: Double get() = totalPower * nighttimeHours
}

@Entity(tableName = "saved_calculations")
data class SavedCalculation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val dateLong: Long = System.currentTimeMillis(),
    val totalDailyEnergyWh: Double,
    val totalNighttimeEnergyWh: Double,
    val peakLoadWatts: Double,
    val totalPanelsPowerRequiredWatts: Double,
    val panelsCountNeeded: Int,
    val panelWattageUsed: Double,
    val totalBatteryAhRequired: Double,
    val batteriesCountNeeded: Int,
    val batteryCapacityAhUsed: Double,
    val systemVoltageUsed: Int,
    val inverterRatingWatts: Double,
    val chargeControllerAmps: Double,
    val suggestedCableSizeMm2: Double,
    val calculatedAmps: Double,
    // Hybrid and lithium details
    val batteryChemistryUsed: String = "Lithium",
    val isHybridInverterUsed: Boolean = true,
    val mpptMaxVocUsed: Double = 150.0,
    val mpptMinVmpUsed: Double = 30.0,
    val panelVocUsed: Double = 49.5,
    val panelVmpUsed: Double = 41.2
)


// -- DAOs --

@Dao
interface ConfigDao {
    @Query("SELECT * FROM system_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<SystemConfig?>

    @Query("SELECT * FROM system_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): SystemConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: SystemConfig)
}

@Dao
interface ApplianceDao {
    @Query("SELECT * FROM appliances ORDER BY id DESC")
    fun getAllAppliancesFlow(): Flow<List<SolarAppliance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliance(appliance: SolarAppliance)

    @Update
    suspend fun updateAppliance(appliance: SolarAppliance)

    @Delete
    suspend fun deleteAppliance(appliance: SolarAppliance)

    @Query("DELETE FROM appliances")
    suspend fun clearAll()
}

@Dao
interface SavedCalculationDao {
    @Query("SELECT * FROM saved_calculations ORDER BY dateLong DESC")
    fun getAllCalculationsFlow(): Flow<List<SavedCalculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: SavedCalculation)

    @Delete
    suspend fun deleteCalculation(calculation: SavedCalculation)

    @Query("DELETE FROM saved_calculations")
    suspend fun clearHistory()
}

// -- Database --

@Database(
    entities = [SystemConfig::class, SolarAppliance::class, SavedCalculation::class],
    version = 5,
    exportSchema = false
)
abstract class SolarDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun applianceDao(): ApplianceDao
    abstract fun savedCalculationDao(): SavedCalculationDao

    companion object {
        @Volatile
        private var INSTANCE: SolarDatabase? = null

        fun getDatabase(context: Context): SolarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SolarDatabase::class.java,
                    "solar_calculator_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// -- Repository --

class SolarRepository(private val db: SolarDatabase) {
    private val configDao = db.configDao()
    private val applianceDao = db.applianceDao()
    private val savedCalculationDao = db.savedCalculationDao()

    val configFlow: Flow<SystemConfig?> = configDao.getConfigFlow()
    val appliancesFlow: Flow<List<SolarAppliance>> = applianceDao.getAllAppliancesFlow()
    val calculationsFlow: Flow<List<SavedCalculation>> = savedCalculationDao.getAllCalculationsFlow()

    suspend fun saveConfig(config: SystemConfig) {
        configDao.insertOrUpdate(config)
    }

    suspend fun getConfig(): SystemConfig {
        return configDao.getConfig() ?: SystemConfig()
    }

    suspend fun insertAppliance(appliance: SolarAppliance) {
        applianceDao.insertAppliance(appliance)
    }

    suspend fun updateAppliance(appliance: SolarAppliance) {
        applianceDao.updateAppliance(appliance)
    }

    suspend fun deleteAppliance(appliance: SolarAppliance) {
        applianceDao.deleteAppliance(appliance)
    }

    suspend fun clearAppliances() {
        applianceDao.clearAll()
    }

    suspend fun insertCalculation(calculation: SavedCalculation) {
        savedCalculationDao.insertCalculation(calculation)
    }

    suspend fun deleteCalculation(calculation: SavedCalculation) {
        savedCalculationDao.deleteCalculation(calculation)
    }

    suspend fun clearHistory() {
        savedCalculationDao.clearHistory()
    }
}
