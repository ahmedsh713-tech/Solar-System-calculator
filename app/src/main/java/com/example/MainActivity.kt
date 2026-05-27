package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

// -- Languages Definitions --

enum class Language { EN, AR }

class TransStr(val en: String, val ar: String) {
    fun get(lang: Language): String = if (lang == Language.AR) ar else en
}

object AppText {
    val appTitle = TransStr("Solar Sizer Pro", "برو لسيزر شمسي")
    val titleAr = TransStr("Solar Calculator", "حاسبة الطاقة الشمسية")
    val subtitle = TransStr("Professional design & client records", "التصميم المهني للأجهزة وسجلات العملاء")
    val toggleLang = TransStr("العربية", "English")
    
    val tabLoads = TransStr("Loads", "الأحمال")
    val tabConfig = TransStr("Settings", "الإعدادات")
    val tabResults = TransStr("Sizing Results", "النتائج")
    val tabLogs = TransStr("Client Logs", "سجلات العملاء")

    val totalPeakLoad = TransStr("Total Load Peak:", "إجمالي الحمل الأقصى:")
    val totalDailyEnergy = TransStr("Daily Energy Needed:", "الاستهلاك اليومي الكلي:")
    val daytimeEnergy = TransStr("Daytime Use:", "استهلاك النهار:")
    val nighttimeEnergy = TransStr("Nighttime Use:", "استهلاك الليل:")
    
    val addAppliance = TransStr("Add New Appliance", "إضافة حمل جديد")
    val editAppliance = TransStr("Edit Appliance Info", "تعديل بيانات الحمل")
    val applianceName = TransStr("Appliance Name", "اسم الجهاز / الحمل")
    val powerWatts = TransStr("Power Consumption (Watts)", "قدرة الجهاز (واط)")
    val quantity = TransStr("Quantity", "الكمية")
    val daytimeHours = TransStr("Daytime Working Hours", "ساعات الاستهلاك بالنهار")
    val nighttimeHours = TransStr("Nighttime Working Hours", "ساعات الاستهلاك بالليل")
    
    val save = TransStr("Save", "حفظ")
    val cancel = TransStr("Cancel", "إلغاء")
    val delete = TransStr("Delete", "حذف")
    val update = TransStr("Update", "تحديث")
    
    // Config Translates
    val configHeader = TransStr("System Coefficients & Parameters", "ثوابت ومواصفات النظام الشمسية")
    val sysVoltage = TransStr("DC System Voltage (V)", "جهد التيار المستمر للنظام (فولت)")
    val peakSunHrs = TransStr("Peak Sun Hours (Hours/Day)", "ساعات الشمس القصوى اليومية (ساعة)")
    val battDod = TransStr("Battery Depth of Discharge (DoD)", "عمق تفريغ البطارية المسموح (DoD)")
    val reserveDays = TransStr("Days of Autonomy (Reserve)", "الاحتياطي أيام بدون شمس (يوم)")
    val safetyFactor = TransStr("System Safety Factor (Losses)", "معامل الأمان لفاقد الأسلاك والغبار")
    val invEfficiency = TransStr("Inverter DC-AC Efficiency (%)", "كفاءة العاكس لنقل التيار (%)")
    val singlePanelW = TransStr("Panel Capacity Used (Watts)", "قدرة اللوح الشمسي الواحد (واط)")
    val singleBattAh = TransStr("Battery Capacity (Ah)", "سعة البطارية الواحدة (أمبير)")
    val singleBattV = TransStr("Battery Unit Voltage (Volt)", "جهد البطارية الواحدة (فولت)")

    // Sizing results Translates
    val resultTitle = TransStr("Equipment Recommendations", "المواصفات الفنية الموصى بها للأجهزة")
    val pvHeader = TransStr("Solar Panels Matrix", "مصفوفة الألواح الشمسية")
    val pvPowerNeeded = TransStr("Total Matrix Power Needed:", "إجمالي قدرة المصفوفة الكهروضوئية:")
    val panelsCountNeeded = TransStr("Required Panels Count:", "عدد الألواح المطلوبة:")
    val panelSpec = TransStr("Assumed Panel Type:", "نوع اللوح المفترض للحساب:")
    
    val battHeader = TransStr("Battery Storage Storage Bank", "بنك بطاريات التخزين")
    val requiredAh = TransStr("Required capacity in AH:", "السعة الإجمالية المطلوبة (أمبير):")
    val totalBattUnits = TransStr("Required Battery Units:", "العدد الإجمالي للبطاريات:")
    val battWiring = TransStr("Suggested Bank Connection:", "طريقة توصيل بنك البطاريات:")
    val battSpec = TransStr("Assumed Battery Size:", "مواصفات البطارية المفترضة:")

    val invHeader = TransStr("Inverter Rating (DC to AC)", "العاكس الكهربائي (Inverter)")
    val minInvPower = TransStr("Minimum Inverter Rating:", "الحد الأدنى المطلوب لقدرة العاكس:")
    val invEfficiencyInfo = TransStr("Based on configured efficiency & 1.25 surge factor", "شامل عامل الأمان لبدء الأحمال وكفاءة العاكس")

    val ccHeader = TransStr("Solar Charge Controller Rate", "منظم شحن الألواح")
    val controllerRating = TransStr("Minimum rated current capacity:", "الحد الأدنى الموصى به لسعة شحن المنظم:")
    val controllerDisclaimer = TransStr("For MPPT types, handles full short-circuit current safely", "شامل عامل الأمان للتعامل مع ارتفاع جهد اللوح")

    val cableHeader = TransStr("Wire Gauge Connections (DC Line)", "توصيلات كابلات النحاس")
    val rawCalculatedAmps = TransStr("Continuous Current (Amperes):", "الأمبير المستمر على خط الجهد:")
    val suggestedWireCross = TransStr("Suggested copper cross-section:", "قطر كابل التوصيل المقترح (نحاس):")
    val mm2Unit = TransStr("mm²", "مم مربّع")
    val textAmps = TransStr("Amperes", "أمبير")

    val btnSaveProject = TransStr("Save Sizing Calculations Report", "حفظ تقرير الحسابات للعميل")
    val labelEnterName = TransStr("Save Client Project Report", "حفظ تقرير العميل الجديد")
    val clientPromptName = TransStr("Client / Project Name", "اسم العميل أو اسم المشروع")
    val clientNameErr = TransStr("Please enter a valid name", "يرجى كتابة اسم صحيح للعميل")
    val saveSuccess = TransStr("Calculations report saved successfully", "تم حفظ تقرير الحسابات بنجاح")

    val tabReportsHistory = TransStr("History Logs", "السجلات التاريخية")
    val clearAllHistory = TransStr("Clear All History Logs", "مسح جميع السجلات التاريخية")
    val deleteRecord = TransStr("Remove Saved Calculation", "حذف مشروع العميل")
    val noHistoryText = TransStr(
        "No historical client logs found. Click 'Save Project' on the results page to capture reports!",
        "لا توجد سجلات محفوظة للعملاء حتى الآن. قم بحفظ أول تقرير بالضغط على 'حفظ تقرير الحسابات' في صفحة نتائج الحساب!"
    )
    val projectDetails = TransStr("Client Project Summary Details", "تفاصيل تقرير حسابات العميل")
    val back = TransStr("Back", "رجوع")
}

// -- ViewModel implementation --

class SolarViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SolarDatabase.getDatabase(application)
    val repository = SolarRepository(database)

    val appliances: StateFlow<List<SolarAppliance>> = repository.appliancesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val config: StateFlow<SystemConfig> = repository.configFlow
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemConfig()
        )

    val calculations: StateFlow<List<SavedCalculation>> = repository.calculationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentLanguage = MutableStateFlow(Language.AR) // Default to Arabic for user comfort

    fun toggleLanguage() {
        currentLanguage.update { if (it == Language.AR) Language.EN else Language.AR }
    }

    fun updateConfig(newConfig: SystemConfig) {
        viewModelScope.launch {
            repository.saveConfig(newConfig)
        }
    }

    fun addAppliance(name: String, powerWatts: Double, quantity: Int, daytimeHours: Double, nighttimeHours: Double) {
        viewModelScope.launch {
            repository.insertAppliance(
                SolarAppliance(
                    name = name,
                    powerWatts = powerWatts,
                    quantity = quantity,
                    daytimeHours = daytimeHours,
                    nighttimeHours = nighttimeHours
                )
            )
        }
    }

    fun updateAppliance(appliance: SolarAppliance) {
        viewModelScope.launch {
            repository.updateAppliance(appliance)
        }
    }

    fun deleteAppliance(appliance: SolarAppliance) {
        viewModelScope.launch {
            repository.deleteAppliance(appliance)
        }
    }

    fun clearAllAppliances() {
        viewModelScope.launch {
            repository.clearAppliances()
        }
    }

    fun saveProjectReport(clientName: String, results: SizingResults) {
        viewModelScope.launch {
            repository.insertCalculation(
                SavedCalculation(
                    clientName = clientName,
                    totalDailyEnergyWh = results.totalDailyEnergyWh,
                    totalNighttimeEnergyWh = results.totalNighttimeEnergyWh,
                    peakLoadWatts = results.peakLoadWatts,
                    totalPanelsPowerRequiredWatts = results.totalPanelsPowerRequiredWatts,
                    panelsCountNeeded = results.panelsCountNeeded,
                    panelWattageUsed = results.panelWattageUsed,
                    totalBatteryAhRequired = results.totalBatteryAhRequired,
                    batteriesCountNeeded = results.batteriesCountNeeded,
                    batteryCapacityAhUsed = results.batteryCapacityAhUsed,
                    systemVoltageUsed = results.systemVoltageUsed,
                    inverterRatingWatts = results.inverterRatingWatts,
                    chargeControllerAmps = results.chargeControllerAmps,
                    suggestedCableSizeMm2 = results.suggestedCableSizeMm2,
                    calculatedAmps = results.calculatedAmps,
                    batteryChemistryUsed = results.batteryChemistryUsed,
                    isHybridInverterUsed = results.isHybridInverterUsed,
                    mpptMaxVocUsed = results.mpptMaxVocUsed,
                    mpptMinVmpUsed = results.mpptMinVmpUsed,
                    panelVocUsed = results.panelVocUsed,
                    panelVmpUsed = results.panelVmpUsed
                )
            )
        }
    }

    fun deleteReport(calc: SavedCalculation) {
        viewModelScope.launch {
            repository.deleteCalculation(calc)
        }
    }

    fun clearAllReports() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

// -- Main Sizing Sizing Formulas --

data class SizingResults(
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
    val suggestedCableSizeMm2: Double, // DC Copper Cable
    val calculatedAmps: Double, // DC Currents
    val batteriesInSeries: Int,
    val batteriesInParallel: Int,
    // Custom support & AC sizing properties
    val acVoltageUsed: Double,
    val continuousACLineAmps: Double,
    val suggestedCableSizeAcMm2: Double,
    val tiltAngleUsed: Double,
    val panelPhysicalLengthUsed: Double,
    val rearSupportHeightMeters: Double,
    val baseFloorLengthMeters: Double,
    // Lithium & Hybrid parameters
    val batteryChemistryUsed: String,
    val isHybridInverterUsed: Boolean,
    val mpptMaxVocUsed: Double,
    val mpptMinVmpUsed: Double,
    val panelVocUsed: Double,
    val panelVmpUsed: Double,
    val suggestedMpptSeries: Int,
    val suggestedMpptParallel: Int,
    val totalPVVoc: Double,
    val totalPVVmp: Double,
    val batteryMaxSafeDischargeAmps: Double,
    val batteryCycleLifeYears: Double,
    val bmsRatingAmps: Double
)

fun calculateSizing(appliances: List<SolarAppliance>, config: SystemConfig): SizingResults {
    val totalDailyEnergyWh = appliances.sumOf { it.totalDailyEnergyWh }
    val totalNighttimeEnergyWh = appliances.sumOf { it.totalNighttimeEnergyWh }
    val peakLoadWatts = appliances.sumOf { it.totalPower }

    // PV panels matrix size computation
    val correctedDailyEnergy = totalDailyEnergyWh * config.safetyFactor
    val panelTotalPowerNeeded = if (config.averageSunHours > 0) correctedDailyEnergy / config.averageSunHours else 0.0
    val panelsNeeded = if (config.panelWattage > 0) ceil(panelTotalPowerNeeded / config.panelWattage).toInt() else 0
    val totalPanelsPower = panelsNeeded * config.panelWattage

    // Battery system design logic
    val rawBatteryWh = (totalNighttimeEnergyWh * config.batteryAutonomyDays)
    val totalBatteryStorageWhNeeded = if (config.batteryDod > 0) rawBatteryWh / config.batteryDod else 0.0
    val totalBatteryAhNeeded = if (config.systemVoltage > 0) totalBatteryStorageWhNeeded / config.systemVoltage else 0.0
    
    val seriesCount = if (config.batteryVoltage > 0) ceil(config.systemVoltage.toDouble() / config.batteryVoltage).toInt().coerceAtLeast(1) else 1
    val parallelCount = if (config.batteryCapacityAh > 0) ceil(totalBatteryAhNeeded / config.batteryCapacityAh).toInt().coerceAtLeast(0) else 0
    val totalBatteries = seriesCount * parallelCount

    // Inverter maximum limits
    val minInverterWatts = if (config.inverterEfficiency > 0) (peakLoadWatts * 1.25) / config.inverterEfficiency else peakLoadWatts * 1.25

    // Charge Regulator sizing (with 25% extra safe room)
    val chargeControllerRatingAmps = if (config.systemVoltage > 0) (totalPanelsPower / config.systemVoltage) * 1.25 else 0.0

    // Cable Copper Dimensions: DC Wire
    val continuousDCLineAmps = if (config.systemVoltage > 0) peakLoadWatts / config.systemVoltage else 0.0
    val safeCableRawDc = continuousDCLineAmps / 4.0 // Safe design density is 4 Amp/mm² for DC
    val suggestedWireDcMm2 = when {
        safeCableRawDc <= 2.5 -> 2.5
        safeCableRawDc <= 4.0 -> 4.0
        safeCableRawDc <= 6.0 -> 6.0
        safeCableRawDc <= 10.0 -> 10.0
        safeCableRawDc <= 16.0 -> 16.0
        safeCableRawDc <= 25.0 -> 25.0
        else -> 35.0
    }

    // Cable Copper Dimensions: AC Wire
    val continuousACLineAmps = if (config.acVoltage > 0) peakLoadWatts / config.acVoltage else 0.0
    val safeCableRawAc = continuousACLineAmps / 4.0 // Safe design density is 4 Amp/mm² for AC too
    val suggestedWireAcMm2 = when {
        safeCableRawAc <= 1.5 -> 1.5
        safeCableRawAc <= 2.5 -> 2.5
        safeCableRawAc <= 4.0 -> 4.0
        safeCableRawAc <= 6.0 -> 6.0
        safeCableRawAc <= 10.0 -> 10.0
        safeCableRawAc <= 16.0 -> 16.0
        else -> 25.0
    }

    // Mounting Frame / Panel Support calculations based on Tilt Angle
    val MathRad = Math.toRadians(config.tiltAngle)
    val rearSupportHeight = config.panelPhysicalLength * Math.sin(MathRad)
    val baseFloorLength = config.panelPhysicalLength * Math.cos(MathRad)

    // MPPT Hybrid Inverter & PV String Design logic
    val isHybrid = config.isHybridInverter
    val pVoc = config.panelVoc
    val pVmp = config.panelVmp
    val mpptMax = config.mpptMaxVoc
    val mpptMin = config.mpptMinVmp

    // Temperature safety coefficient of +12%
    val correctedPanelVoc = pVoc * 1.12
    val maxSeries = if (correctedPanelVoc > 0.0) floor(mpptMax / correctedPanelVoc).toInt().coerceAtLeast(1) else 1
    val minSeries = if (pVmp > 0.0) ceil(mpptMin / pVmp).toInt().coerceAtLeast(1) else 1

    var suggestedMpptSeries = 1
    var suggestedMpptParallel = 1
    
    if (panelsNeeded > 0) {
        val seriesLowerBound = minOf(minSeries, panelsNeeded).coerceAtLeast(1)
        val seriesUpperBound = minOf(maxSeries, panelsNeeded).coerceAtLeast(1)
        
        var bestS = 1
        var bestDiff = Double.MAX_VALUE
        
        for (s in seriesLowerBound..seriesUpperBound) {
            val p = ceil(panelsNeeded.toDouble() / s.toDouble()).toInt()
            val totalAssigned = s * p
            val diff = (totalAssigned - panelsNeeded).toDouble()
            if (diff < bestDiff) {
                bestDiff = diff
                bestS = s
            }
        }
        suggestedMpptSeries = bestS
        suggestedMpptParallel = ceil(panelsNeeded.toDouble() / bestS.toDouble()).toInt().coerceAtLeast(1)
    }
    
    val totalPVVoc = suggestedMpptSeries * pVoc
    val totalPVVmp = suggestedMpptSeries * pVmp

    // Lithium batteries support:
    val chemistry = config.batteryChemistry
    val cycleYears = if (chemistry.lowercase() == "lithium") 11.5 else 1.3
    val dischargeFactor = if (chemistry.lowercase() == "lithium") 1.0 else 0.2
    val safeDischargeA = config.batteryCapacityAh * dischargeFactor * parallelCount.coerceAtLeast(1)
    val bmsAmps = if (config.systemVoltage > 0) (peakLoadWatts / config.systemVoltage) * 1.25 else 0.0

    return SizingResults(
        totalDailyEnergyWh = totalDailyEnergyWh,
        totalNighttimeEnergyWh = totalNighttimeEnergyWh,
        peakLoadWatts = peakLoadWatts,
        totalPanelsPowerRequiredWatts = totalPanelsPower,
        panelsCountNeeded = panelsNeeded,
        panelWattageUsed = config.panelWattage,
        totalBatteryAhRequired = totalBatteryAhNeeded,
        batteriesCountNeeded = totalBatteries,
        batteryCapacityAhUsed = config.batteryCapacityAh,
        systemVoltageUsed = config.systemVoltage,
        inverterRatingWatts = minInverterWatts,
        chargeControllerAmps = chargeControllerRatingAmps,
        suggestedCableSizeMm2 = suggestedWireDcMm2,
        calculatedAmps = continuousDCLineAmps,
        batteriesInSeries = seriesCount,
        batteriesInParallel = parallelCount,
        acVoltageUsed = config.acVoltage,
        continuousACLineAmps = continuousACLineAmps,
        suggestedCableSizeAcMm2 = suggestedWireAcMm2,
        tiltAngleUsed = config.tiltAngle,
        panelPhysicalLengthUsed = config.panelPhysicalLength,
        rearSupportHeightMeters = rearSupportHeight,
        baseFloorLengthMeters = baseFloorLength,
        batteryChemistryUsed = chemistry,
        isHybridInverterUsed = isHybrid,
        mpptMaxVocUsed = mpptMax,
        mpptMinVmpUsed = mpptMin,
        panelVocUsed = pVoc,
        panelVmpUsed = pVmp,
        suggestedMpptSeries = suggestedMpptSeries,
        suggestedMpptParallel = suggestedMpptParallel,
        totalPVVoc = totalPVVoc,
        totalPVVmp = totalPVVmp,
        batteryMaxSafeDischargeAmps = safeDischargeA,
        batteryCycleLifeYears = cycleYears,
        bmsRatingAmps = bmsAmps
    )
}

// -- Host Activity --

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: SolarViewModel = viewModel()
                AppContent(viewModel)
            }
        }
    }
}

@Composable
fun AppContent(viewModel: SolarViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val appliances by viewModel.appliances.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val reportsHistory by viewModel.calculations.collectAsStateWithLifecycle()

    val sizingResults = calculateSizing(appliances, config)
    val context = LocalContext.current

    // Navigation state variables
    var currentTab by remember { mutableStateOf(0) }
    var showAddApplianceDialog by remember { mutableStateOf(false) }
    var selectedApplianceToEdit by remember { mutableStateOf<SolarAppliance?>(null) }
    var showSaveProjectDialog by remember { mutableStateOf(false) }
    var showExportPdfDialog by remember { mutableStateOf(false) }

    // Support genuine layout direction flipping dynamically (RTL for Arabic / LTR for English)
    val targetDirection = if (currentLang == Language.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides targetDirection) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val tabs = listOf(
                        Triple(0, AppText.tabLoads.get(currentLang), Icons.Filled.List),
                        Triple(1, AppText.tabConfig.get(currentLang), Icons.Filled.Settings),
                        Triple(2, AppText.tabResults.get(currentLang), Icons.Filled.Info),
                        Triple(3, AppText.tabLogs.get(currentLang), Icons.Filled.DateRange)
                    )
                    
                    tabs.forEach { (index, title, icon) ->
                        NavigationBarItem(
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = SolarPrimary,
                                indicatorColor = SolarPrimary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(BackgroundDark, BackgroundDark.copy(alpha = 0.95f))
                        )
                    )
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Area
                    HeaderArea(
                        currentLang = currentLang,
                        onLanguageChanged = { viewModel.toggleLanguage() }
                    )

                    // Quick Summary Bar
                    QuickSummaryBar(
                        sizing = sizingResults,
                        lang = currentLang
                    )

                    // Tab View Router
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "tab_fade"
                        ) { index ->
                            when (index) {
                                0 -> LoadsTab(
                                    appliances = appliances,
                                    currentLang = currentLang,
                                    onAddClick = { showAddApplianceDialog = true },
                                    onEditClick = { selectedApplianceToEdit = it },
                                    onDeleteClick = { viewModel.deleteAppliance(it) },
                                    onClearAll = { viewModel.clearAllAppliances() }
                                )
                                1 -> ConfigTab(
                                    config = config,
                                    currentLang = currentLang,
                                    onConfigUpdate = { viewModel.updateConfig(it) }
                                )
                                2 -> SizingResultsTab(
                                    results = sizingResults,
                                    currentLang = currentLang,
                                    onSaveReportClick = { showSaveProjectDialog = true },
                                    onExportPdfClick = { showExportPdfDialog = true }
                                )
                                3 -> HistoryReportsTab(
                                    reports = reportsHistory,
                                    currentLang = currentLang,
                                    onDeleteReport = { viewModel.deleteReport(it) },
                                    onClearAllReports = { viewModel.clearAllReports() }
                                )
                            }
                        }
                    }
                }

                // Add Load Dialog Sheet
                if (showAddApplianceDialog) {
                    ApplianceFormDialog(
                        appliance = null,
                        currentLang = currentLang,
                        onDismiss = { showAddApplianceDialog = false },
                        onConfirm = { name, power, qty, day, night ->
                            viewModel.addAppliance(name, power, qty, day, night)
                            showAddApplianceDialog = false
                            Toast.makeText(context, if (currentLang == Language.AR) "تمت إضافة الحمل لجدول الاستهلاك" else "Appliance load added successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Edit Load Dialog Sheet
                selectedApplianceToEdit?.let { item ->
                    ApplianceFormDialog(
                        appliance = item,
                        currentLang = currentLang,
                        onDismiss = { selectedApplianceToEdit = null },
                        onConfirm = { name, power, qty, day, night ->
                            viewModel.updateAppliance(item.copy(name = name, powerWatts = power, quantity = qty, daytimeHours = day, nighttimeHours = night))
                            selectedApplianceToEdit = null
                            Toast.makeText(context, if (currentLang == Language.AR) "تم تعديل مواصفات الحمل بنجاح" else "Appliance updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Save Project Dialog
                if (showSaveProjectDialog) {
                    SaveClientReportDialog(
                        currentLang = currentLang,
                        onDismiss = { showSaveProjectDialog = false },
                        onConfirm = { clientName ->
                            viewModel.saveProjectReport(clientName, sizingResults)
                            showSaveProjectDialog = false
                            currentTab = 3 // Move automatically to records history tab!
                            Toast.makeText(context, AppText.saveSuccess.get(currentLang), Toast.LENGTH_LONG).show()
                        }
                    )
                }

                // Export PDF Dialog
                if (showExportPdfDialog) {
                    ExportClientReportDialog(
                        currentLang = currentLang,
                        onDismiss = { showExportPdfDialog = false },
                        onConfirm = { clientName ->
                            PdfExporter.exportSizingToPdf(context, clientName, sizingResults, currentLang)
                            showExportPdfDialog = false
                        }
                    )
                }
            }
        }
    }
}

// -- Header Component --

@Composable
fun HeaderArea(
    currentLang: Language,
    onLanguageChanged: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = SolarPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppText.titleAr.get(currentLang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Text(
                text = AppText.subtitle.get(currentLang),
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Button(
            onClick = onLanguageChanged,
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceVariantDark,
                contentColor = SolarSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("lang_toggle")
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = AppText.toggleLang.get(currentLang),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -- Quick Live Sizing Bar --

@Composable
fun QuickSummaryBar(
    sizing: SizingResults,
    lang: Language
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(AppText.totalPeakLoad.get(lang), fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = "${sizing.peakLoadWatts.roundToInt()} W",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = SolarSecondary
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(BorderDark)
                    .align(Alignment.CenterVertically)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(AppText.totalDailyEnergy.get(lang), fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = "${String.format("%.1f", sizing.totalDailyEnergyWh / 1000.0)} kWh",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = SolarPrimary
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(BorderDark)
                    .align(Alignment.CenterVertically)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(AppText.panelsCountNeeded.get(lang), fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = "${sizing.panelsCountNeeded}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentOrange
                )
            }
        }
    }
}

// -- Subscreen 1: LOADS TAB --

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoadsTab(
    appliances: List<SolarAppliance>,
    currentLang: Language,
    onAddClick: () -> Unit,
    onEditClick: (SolarAppliance) -> Unit,
    onDeleteClick: (SolarAppliance) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${AppText.tabLoads.get(currentLang)} (${appliances.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row {
                if (appliances.isNotEmpty()) {
                    IconButton(
                        onClick = onClearAll,
                        modifier = Modifier.testTag("clear_appliances_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear all", tint = Color.Red)
                    }
                }
                
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("add_appliance_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppText.addAppliance.get(currentLang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        if (appliances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = null,
                        tint = BorderDark,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentLang == Language.AR) 
                            "قائمة الأجهزة فارغة. أضف أجهزتك الكهربائية لحساب حجم الأحمال وإجمالي الطاقة لتوليدها وبطاريات تخزينها." 
                            else "Load breakdown is empty. Add your electrical gadgets to compute energy totals.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(items = appliances, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, BorderDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.quantity}x",
                                    color = SolarSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "${item.powerWatts.roundToInt()} W",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "|",
                                        color = BorderDark,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "☼¹ ${String.format("%.1f", item.daytimeHours)}h / ☾² ${String.format("%.1f", item.nighttimeHours)}h",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.0f", item.totalDailyEnergyWh)} Wh",
                                    color = SolarPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentLang == Language.AR) "طاقة اليوم" else "Daily Energy",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onEditClick(item) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteClick(item) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -- Subscreen 2: CONFIGURATION TAB --

@Composable
fun ConfigTab(
    config: SystemConfig,
    currentLang: Language,
    onConfigUpdate: (SystemConfig) -> Unit
) {
    var sysV by remember(config) { mutableStateOf(config.systemVoltage) }
    var sunH by remember(config) { mutableStateOf(config.averageSunHours) }
    var dod by remember(config) { mutableStateOf(config.batteryDod) }
    var reserveD by remember(config) { mutableStateOf(config.batteryAutonomyDays) }
    var safety by remember(config) { mutableStateOf(config.safetyFactor) }
    var panelW by remember(config) { mutableStateOf(config.panelWattage) }
    var battAh by remember(config) { mutableStateOf(config.batteryCapacityAh) }
    var battV by remember(config) { mutableStateOf(config.batteryVoltage) }
    var invEff by remember(config) { mutableStateOf(config.inverterEfficiency * 100) }
    var acV by remember(config) { mutableStateOf(config.acVoltage) }
    var tiltA by remember(config) { mutableStateOf(config.tiltAngle) }
    var pLength by remember(config) { mutableStateOf(config.panelPhysicalLength) }

    var chem by remember(config) { mutableStateOf(config.batteryChemistry) }
    var isHybrid by remember(config) { mutableStateOf(config.isHybridInverter) }
    var mpptMax by remember(config) { mutableStateOf(config.mpptMaxVoc) }
    var mpptMin by remember(config) { mutableStateOf(config.mpptMinVmp) }
    var pVoc by remember(config) { mutableStateOf(config.panelVoc) }
    var pVmp by remember(config) { mutableStateOf(config.panelVmp) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = AppText.configHeader.get(currentLang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // System Voltage Selector
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppText.sysVoltage.get(currentLang),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(12, 24, 48).forEach { volt ->
                            val isSelected = sysV == volt
                            Button(
                                onClick = {
                                    sysV = volt
                                    onConfigUpdate(config.copy(systemVoltage = volt))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SolarSecondary else SurfaceVariantDark,
                                    contentColor = if (isSelected) Color.Black else Color.Gray
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sys_v_$volt")
                            ) {
                                Text("$volt V", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Battery Type / Chemistry Selector
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (currentLang == Language.AR) "نوع وتقنية البطارية المستخدمة" else "Battery Chemistry / Technology Type",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Lithium", "Lead-Acid").forEach { tech ->
                            val isSelected = chem == tech
                            val displayTech = if (tech == "Lithium") {
                                if (currentLang == Language.AR) "ليثيوم (LiFePO4)" else "Lithium (LiFePO4)"
                            } else {
                                if (currentLang == Language.AR) "رصاص مغلق (Lead-Acid)" else "Lead-Acid (AGM/Gel)"
                            }
                            Button(
                                onClick = {
                                    chem = tech
                                    val suggestedDod = if (tech == "Lithium") 0.80 else 0.50
                                    dod = suggestedDod
                                    onConfigUpdate(config.copy(
                                        batteryChemistry = tech,
                                        batteryDod = suggestedDod
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SolarSecondary else SurfaceVariantDark,
                                    contentColor = if (isSelected) Color.Black else Color.Gray
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chem_$tech")
                            ) {
                                Text(displayTech, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Small informative tip based on selected type
                    Text(
                        text = if (chem == "Lithium") {
                            if (currentLang == Language.AR) 
                                "💡 تقنية الليثيوم توفر كفاءة شحن 95٪ وتفريغ آمن حتى 80٪ وعمر افتراضي يصل إلى 10 سنوات (أكثر من 4000 دورة)."
                            else 
                                "💡 Lithium technology provides 95% charge efficiency, safe discharge up to 80% DoD, and lasts over 11 years (4000+ cycles)."
                        } else {
                            if (currentLang == Language.AR) 
                                "⚠️ بطاريات الرصاص يفضل تفريغها بنسبة 50٪ كحد أقصى وعمرها الافتراضي قصير يتراوح بين سنة إلى سنتين (300 دورة)."
                            else 
                                "⚠️ Lead-acid batteries should only be discharged to 50% max, with a shorter lifespan of 1-2 years (approx 300 cycles)."
                        },
                        color = if (chem == "Lithium") SolarPrimary else Color.Yellow.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // MPPT Hybrid Inverter Setup
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentLang == Language.AR) "عاكس هجين ذكي بمتحكم MPPT" else "Smart MPPT Hybrid Inverter Option",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentLang == Language.AR) "متحمل ومخطط توصيلات ذكي للألواح" else "Enables premium series-parallel solar matrix calculations",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isHybrid,
                            onCheckedChange = { isChecked ->
                                isHybrid = isChecked
                                onConfigUpdate(config.copy(isHybridInverter = isChecked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SolarSecondary,
                                checkedTrackColor = SolarSecondary.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = SurfaceVariantDark
                            ),
                            modifier = Modifier.testTag("switch_hybrid")
                        )
                    }

                    if (isHybrid) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (currentLang == Language.AR) "إعدادات متحكم الشحن MPPT داخل العاكس:" else "Inverter Built-in MPPT Tracker Limits:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = mpptMax.toString(),
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                                    if (clean != null && clean > 0.0) {
                                        mpptMax = clean
                                        onConfigUpdate(config.copy(mpptMaxVoc = clean))
                                    }
                                },
                                label = { Text(if (currentLang == Language.AR) "أقصى فولت مقاس (Voc)" else "Max MPPT Voc (V)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolarSecondary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = mpptMin.toString(),
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                                    if (clean != null && clean > 0.0) {
                                        mpptMin = clean
                                        onConfigUpdate(config.copy(mpptMinVmp = clean))
                                    }
                                },
                                label = { Text(if (currentLang == Language.AR) "أدنى فولت تشغيل (Vmp)" else "Min MPPT Vmp (V)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolarSecondary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (currentLang == Language.AR) "الخصائص الكهربائية للوح المختار:" else "Chosen Solar Panel Electrical Specs:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = pVoc.toString(),
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                                    if (clean != null && clean > 0.0) {
                                        pVoc = clean
                                        onConfigUpdate(config.copy(panelVoc = clean))
                                    }
                                },
                                label = { Text(if (currentLang == Language.AR) "فولت الدائرة المفتوحة (Voc)" else "Panel Voc (V)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolarSecondary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = pVmp.toString(),
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                                    if (clean != null && clean > 0.0) {
                                        pVmp = clean
                                        onConfigUpdate(config.copy(panelVmp = clean))
                                    }
                                },
                                label = { Text(if (currentLang == Language.AR) "فولت أقصى قدرة (Vmp)" else "Panel Vmp (V)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SolarSecondary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Peak Sun Hours Slider
        item {
            ConfigSliderContainer(
                title = AppText.peakSunHrs.get(currentLang),
                valueStr = "${String.format("%.1f", sunH)}h",
                progressValue = (sunH - 2.0).toFloat() / 6f, // Range from 2h to 8h
                onProgressChanged = { pct ->
                    val newVal = 2.0 + (pct * 6.0)
                    sunH = (newVal * 10.0).roundToInt() / 10.0
                    onConfigUpdate(config.copy(averageSunHours = sunH))
                }
            )
        }

        // Battery DOD Slider
        item {
            ConfigSliderContainer(
                title = AppText.battDod.get(currentLang),
                valueStr = "${(dod * 100).roundToInt()}%",
                progressValue = (dod - 0.20).toFloat() / 0.60f, // Range 20% to 80%
                onProgressChanged = { pct ->
                    val newVal = 0.20 + (pct * 0.60)
                    dod = (newVal * 100.0).roundToInt() / 100.0
                    onConfigUpdate(config.copy(batteryDod = dod))
                }
            )
        }

        // Days of autonomy Slider
        item {
            ConfigSliderContainer(
                title = AppText.reserveDays.get(currentLang),
                valueStr = "$reserveD",
                progressValue = (reserveD - 1).toFloat() / 4f, // Range 1 to 5 days
                onProgressChanged = { pct ->
                    val newVal = 1 + (pct * 4).roundToInt()
                    reserveD = newVal
                    onConfigUpdate(config.copy(batteryAutonomyDays = reserveD))
                }
            )
        }

        // Safety Multiplier Card (Losses & dirt)
        item {
            ConfigSliderContainer(
                title = AppText.safetyFactor.get(currentLang),
                valueStr = "x ${String.format("%.2f", safety)}",
                progressValue = (safety - 1.0).toFloat() / 0.60f, // Range 1.0 to 1.6
                onProgressChanged = { pct ->
                    val newVal = 1.0 + (pct * 0.60)
                    safety = (newVal * 100.0).roundToInt() / 100.0
                    onConfigUpdate(config.copy(safetyFactor = safety))
                }
            )
        }

        // Technical Specs inputs: Single Panel Wattage, Battery AH size, Inverter efficiency
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (currentLang == Language.AR) "مكونات وأجهزة التصميم الفنية" else "Hardware System Values",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Field for panel capacity
                    OutlinedTextField(
                        value = panelW.toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                            if (clean != null && clean > 0) {
                                panelW = clean
                                onConfigUpdate(config.copy(panelWattage = clean))
                            }
                        },
                        label = { Text(AppText.singlePanelW.get(currentLang), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field for single battery AH
                    OutlinedTextField(
                        value = battAh.toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                            if (clean != null && clean > 0) {
                                battAh = clean
                                onConfigUpdate(config.copy(batteryCapacityAh = clean))
                            }
                        },
                        label = { Text(AppText.singleBattAh.get(currentLang), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field for individual battery V
                    OutlinedTextField(
                        value = battV.toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                            if (clean != null && clean > 0) {
                                battV = clean
                                onConfigUpdate(config.copy(batteryVoltage = clean))
                            }
                        },
                        label = { Text(AppText.singleBattV.get(currentLang), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Inverter efficiency input
                    OutlinedTextField(
                        value = invEff.roundToInt().toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }.toDoubleOrNull()
                            if (clean != null && clean in 50.0..100.0) {
                                invEff = clean
                                onConfigUpdate(config.copy(inverterEfficiency = clean / 100.0))
                            }
                        },
                        label = { Text(AppText.invEfficiency.get(currentLang), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field for AC output load voltage
                    OutlinedTextField(
                        value = acV.toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                            if (clean != null && clean > 0) {
                                acV = clean
                                onConfigUpdate(config.copy(acVoltage = clean))
                            }
                        },
                        label = { Text(if (currentLang == Language.AR) "جهد التيار المتردد للحمل (فولت AC)" else "AC Output System Voltage (Volt)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field for custom support structure Tilt Angle (Degrees)
                    OutlinedTextField(
                        value = tiltA.toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                            if (clean != null && clean >= 0.0 && clean <= 90.0) {
                                tiltA = clean
                                onConfigUpdate(config.copy(tiltAngle = clean))
                            }
                        },
                        label = { Text(if (currentLang == Language.AR) "زاوية ميل اللوح الشمسي (درجة °)" else "Panel Mounting Support Tilt Angle (°)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field for standard Panel Length (m)
                    OutlinedTextField(
                        value = pLength.toString(),
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                            if (clean != null && clean > 0) {
                                pLength = clean
                                onConfigUpdate(config.copy(panelPhysicalLength = clean))
                            }
                        },
                        label = { Text(if (currentLang == Language.AR) "طول اللوح الشمسي الفعلي (متر L)" else "Solar Panel Physical Length (m)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigSliderContainer(
    title: String,
    valueStr: String,
    progressValue: Float, // bounded between 0 and 1
    onProgressChanged: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = valueStr,
                    color = SolarPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Slider(
                value = progressValue.coerceIn(0f, 1f),
                onValueChange = onProgressChanged,
                colors = SliderDefaults.colors(
                    thumbColor = SolarPrimary,
                    activeTrackColor = SolarPrimary,
                    inactiveTrackColor = SurfaceVariantDark
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -- Subscreen 3: COMPUTATIONAL SIZING RESULTS --

@Composable
fun SizingResultsTab(
    results: SizingResults,
    currentLang: Language,
    onSaveReportClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppText.resultTitle.get(currentLang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSaveReportClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarSecondary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("save_results_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (currentLang == Language.AR) "حفظ" else "Save",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = onExportPdfClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("export_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (currentLang == Language.AR) "تصدير" else "PDF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Section 1: PV matrix
        item {
            ResultCard(
                header = AppText.pvHeader.get(currentLang),
                icon = Icons.Outlined.Share,
                accentColor = SolarPrimary
            ) {
                ResultRow(label = AppText.panelsCountNeeded.get(currentLang), value = "${results.panelsCountNeeded}", valueColor = SolarPrimary)
                ResultRow(label = AppText.pvPowerNeeded.get(currentLang), value = "${results.totalPanelsPowerRequiredWatts.roundToInt()} W")
                ResultRow(label = AppText.panelSpec.get(currentLang), value = "${results.panelWattageUsed.roundToInt()} W (Mono-Si)")

                if (results.isHybridInverterUsed && results.panelsCountNeeded > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = if (currentLang == Language.AR) "تصميم مصفوفة وتوصيلات عاكس الهجين (MPPT):" else "MPPT Hybrid Inverter String Design:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ResultRow(
                        label = if (currentLang == Language.AR) "توزيع الألواح (التوالي × التوازي):" else "String Grouping (Series x Parallel):",
                        value = "${results.suggestedMpptSeries}S  x  ${results.suggestedMpptParallel}P",
                        valueColor = SolarSecondary
                    )
                    ResultRow(
                        label = if (currentLang == Language.AR) "إجمالي فولت الألواح (Voc):" else "Array Open-Circuit Voltage (Voc):",
                        value = "${String.format("%.1f", results.totalPVVoc)} V"
                    )
                    ResultRow(
                        label = if (currentLang == Language.AR) "إجمالي فولت التشغيل (Vmp):" else "Array Operation Voltage (Vmp):",
                        value = "${String.format("%.1f", results.totalPVVmp)} V"
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Simple technical feedback
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceVariantDark)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (results.totalPVVoc * 1.12 > results.mpptMaxVocUsed) {
                                if (currentLang == Language.AR)
                                    "🚨 تحذير: فولت الألواح Voc قد يتجاوز كفاءة العاكس القصوى في الشتاء (${results.mpptMaxVocUsed}V)! الرجاء زيادة توازي السلاسل."
                                else
                                    "🚨 Warning: Cold-weather Voc exceeds inverter MPPT limit (${results.mpptMaxVocUsed}V)! Increase parallel strings."
                            } else {
                                if (currentLang == Language.AR)
                                    "⚙️ الجهد متوافق بالكامل ومحمي ضمن نطاق تتبع الـ MPPT للعاكس (${results.mpptMinVmpUsed}V - ${results.mpptMaxVocUsed}V)."
                                else
                                    "⚙️ Voltages are safe and optimized within the inverter's MPPT tracking range (${results.mpptMinVmpUsed}V - ${results.mpptMaxVocUsed}V)."
                            },
                            color = if (results.totalPVVoc * 1.12 > results.mpptMaxVocUsed) Color.Red else SolarPrimary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                // PV Matrix physical grid draw helper
                if (results.panelsCountNeeded > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == Language.AR) "ترتيب وتخطيط مصفوفة التوليد المقترح:" else "Visual layout array recommendation:",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..results.panelsCountNeeded) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp, 44.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SolarSkyBlue)
                                    .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Battery Storage Book
        item {
            ResultCard(
                header = AppText.battHeader.get(currentLang),
                icon = Icons.Outlined.Check,
                accentColor = SolarSecondary
            ) {
                ResultRow(label = AppText.totalBattUnits.get(currentLang), value = "${results.batteriesCountNeeded}", valueColor = SolarSecondary)
                ResultRow(label = AppText.requiredAh.get(currentLang), value = "${results.totalBatteryAhRequired.roundToInt()} Ah")
                ResultRow(
                    label = AppText.battWiring.get(currentLang), 
                    value = "${results.batteriesInSeries}S  x  ${results.batteriesInParallel}P"
                )
                
                val unitVolt = if (results.batteriesInSeries > 0) (results.systemVoltageUsed / results.batteriesInSeries) else 12
                ResultRow(label = AppText.battSpec.get(currentLang), value = "${results.batteryCapacityAhUsed.roundToInt()} Ah @ $unitVolt V")
                ResultRow(
                    label = if (currentLang == Language.AR) "نوع البطارية المعتمد:" else "Selected Battery Chemistry:",
                    value = if (results.batteryChemistryUsed.lowercase() == "lithium") {
                        if (currentLang == Language.AR) "ليثيوم حديد فوسفات LiFePO4" else "Lithium (LiFePO4)"
                    } else {
                        if (currentLang == Language.AR) "رصاص مغلق أسيد AGM/Gel" else "Sealed Lead-Acid (AGM/Gel)"
                    },
                    valueColor = if (results.batteryChemistryUsed.lowercase() == "lithium") SolarPrimary else Color.Yellow
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (currentLang == Language.AR) "الحسابات والمؤشرات الفنية للبطارية:" else "Battery Technical Analysis & Accounts:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))

                ResultRow(
                    label = if (currentLang == Language.AR) "العمر الافتراضي المتوقع:" else "Expected Battery Lifespan:",
                    value = if (results.batteryChemistryUsed.lowercase() == "lithium") {
                        if (currentLang == Language.AR) "~11.5 سنة (4000+ دورة شحن)" else "~11.5 Years (4000+ cycles)"
                    } else {
                        if (currentLang == Language.AR) "~1.3 سنة (350 دورة شحن)" else "~1.3 Years (350 cycles)"
                    },
                    valueColor = SolarPrimary
                )

                ResultRow(
                    label = if (currentLang == Language.AR) "أقصى تيار تفريغ آمن ومستمر:" else "Max Safe Continuous Discharge Rate:",
                    value = "${results.batteryMaxSafeDischargeAmps.roundToInt()} A",
                    valueColor = SolarSecondary
                )

                ResultRow(
                    label = if (currentLang == Language.AR) "الحد الأدنى المقترح لنظام BMS:" else "Suggested Protection BMS Rating:",
                    value = "${results.bmsRatingAmps.roundToInt()} A"
                )

                if (results.batteriesCountNeeded > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == Language.AR) "تظليل بنك البطاريات الكلي:" else "Visual battery cells depiction:",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..results.batteriesCountNeeded) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp, 34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceVariantDark)
                                    .border(1.5.dp, SolarSecondary.copy(0.4f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp, 12.dp).background(Color.Red))
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Box(modifier = Modifier.size(6.dp, 12.dp).background(Color.Black))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Inverter Device
        item {
            ResultCard(
                header = AppText.invHeader.get(currentLang),
                icon = Icons.Outlined.Info,
                accentColor = AccentOrange
            ) {
                ResultRow(label = AppText.minInvPower.get(currentLang), value = "${String.format("%.0f", results.inverterRatingWatts)} W", valueColor = AccentOrange)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppText.invEfficiencyInfo.get(currentLang),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }

        // Section 4: Charge Controller size
        item {
            ResultCard(
                header = AppText.ccHeader.get(currentLang),
                icon = Icons.Outlined.LocationOn,
                accentColor = SolarPrimary
            ) {
                ResultRow(label = AppText.controllerRating.get(currentLang), value = "${String.format("%.1f", results.chargeControllerAmps)} Amps", valueColor = SolarPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppText.controllerDisclaimer.get(currentLang),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }

        // Section 5: Cable Dimension copper (handling AC & DC Current streams)
        item {
            ResultCard(
                header = if (currentLang == Language.AR) "أقطاب ومقاسات كابلات النحاس (AC / DC)" else "Copper Wire Gauge Sizing (AC & DC Lines)",
                icon = Icons.Outlined.Notifications,
                accentColor = SolarSecondary
            ) {
                Text(
                    text = if (currentLang == Language.AR) "خط التيار المستمر (من الخلايا للبطاريات/المنظم):" else "DC Current Line (PV Array to Battery/Inverter):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                ResultRow(label = AppText.rawCalculatedAmps.get(currentLang), value = "${String.format("%.2f", results.calculatedAmps)} A")
                ResultRow(label = AppText.suggestedWireCross.get(currentLang), value = "${results.suggestedCableSizeMm2} ${AppText.mm2Unit.get(currentLang)}", valueColor = SolarSecondary)
                
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (currentLang == Language.AR) "خط التيار المتردد (من خارج العاكس للأجهزة المنزلية):" else "AC Current Line (from Inverter to AC home loads):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                ResultRow(label = if (currentLang == Language.AR) "أمبير التيار المتردد المستمر:" else "AC Continuous Stream current:", value = "${String.format("%.2f", results.continuousACLineAmps)} A")
                ResultRow(label = if (currentLang == Language.AR) "قطر كابل النحاس المتردد المقترح:" else "Suggested AC copper wire gauge:", value = "${results.suggestedCableSizeAcMm2} ${AppText.mm2Unit.get(currentLang)}", valueColor = SolarPrimary)
            }
        }

        // Section 5.5: Panel Mounting Bracket & Support Geometry Sizing
        item {
            ResultCard(
                header = if (currentLang == Language.AR) "هيكل وطول قوائم تثبيت الألواح" else "Solar Panel Mounting Frame & Brackets Sizing",
                icon = Icons.Outlined.Build,
                accentColor = AccentOrange
            ) {
                Text(
                    text = if (currentLang == Language.AR) "حسابات مثلث التثبيت المعتمدة على زاوية الميل:" else "Triangular frame calculations based on tilt angle:",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                ResultRow(label = if (currentLang == Language.AR) "زاوية ميل اللوح الشمسي:" else "Tilt Angle Used:", value = "${results.tiltAngleUsed}°")
                ResultRow(label = if (currentLang == Language.AR) "طول اللوح الفرضي بالميل (L):" else "Panel Physical Length (L):", value = "${String.format("%.2f", results.panelPhysicalLengthUsed)} m")
                
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow(
                    label = if (currentLang == Language.AR) "ارتفاع عمود التثبيت الخلفي (H):" else "Rear Support Column Height (H):",
                    value = "${String.format("%.2f", results.rearSupportHeightMeters)} m",
                    valueColor = AccentOrange
                )
                ResultRow(
                    label = if (currentLang == Language.AR) "طول مسافة التثبيت الأرضية (Depth):" else "Horizontal Base Ground Footprint (D):",
                    value = "${String.format("%.2f", results.baseFloorLengthMeters)} m",
                    valueColor = SolarPrimary
                )

                // Simple ASCII art representation of the triangle for visual richness
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        val rearHStr = String.format("%.2fm", results.rearSupportHeightMeters)
                        val baseDStr = String.format("%.2fm", results.baseFloorLengthMeters)
                        val tiltStr = "${results.tiltAngleUsed}°"
                        Text(
                            text = if (currentLang == Language.AR) "رسم هندسي تقريبي لمثلث الحامل:" else "Geometric preview frame:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Visual Drawing of vertical vs horizontal triangle line
                        Text(
                            text = """
                                  /\
                                 /  \    [L = ${String.format("%.1fm", results.panelPhysicalLengthUsed)}]
                                /    \
                               /______\  [H = $rearHStr]
                            Tilt = $tiltStr  [Base depth = $baseDStr]
                            """.trimIndent(),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = SolarSecondary
                        )
                    }
                }
            }
        }

        // Section 6: Action button block bottom page save project
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSaveReportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarSecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_save_client_calculation")
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLang == Language.AR) "حفظ التقرير" else "Save Report",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Button(
                    onClick = onExportPdfClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_export_pdf_calculation")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLang == Language.AR) "تصدير PDF" else "Export PDF",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

// Result Card Wrapper
@Composable
fun ResultCard(
    header: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = header,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

// Result Row
@Composable
fun ResultRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.LightGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}

// -- Subscreen 4: HISTORICAL CALCULATIONS LOG --

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryReportsTab(
    reports: List<SavedCalculation>,
    currentLang: Language,
    onDeleteReport: (SavedCalculation) -> Unit,
    onClearAllReports: () -> Unit
) {
    val context = LocalContext.current
    var selectedReportForDetails by remember { mutableStateOf<SavedCalculation?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${AppText.tabReportsHistory.get(currentLang)} (${reports.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (reports.isNotEmpty()) {
                Button(
                    onClick = onClearAllReports,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("clear_history_btn")
                ) {
                    Text(
                        text = if (currentLang == Language.AR) "مسح السجل" else "Clear History",
                        fontSize = 11.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = BorderDark,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = AppText.noHistoryText.get(currentLang),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(items = reports, key = { it.id }) { item ->
                    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                    val dateFormatted = sdf.format(Date(item.dateLong))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                            .clickable { selectedReportForDetails = item },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.clientName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantDark)
                                        .clickable { onDeleteReport(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LabelInfoCol(
                                    label = if (currentLang == Language.AR) "ألواح شمسية" else "Panels Array",
                                    info = "${item.panelsCountNeeded} x ${item.panelWattageUsed.roundToInt()}W",
                                    iconColor = SolarPrimary
                                )
                                LabelInfoCol(
                                    label = if (currentLang == Language.AR) "البطاريات" else "Batteries AH",
                                    info = "${item.batteriesCountNeeded} x ${item.batteryCapacityAhUsed.roundToInt()}Ah",
                                    iconColor = SolarSecondary
                                )
                                LabelInfoCol(
                                    label = if (currentLang == Language.AR) "العاكس" else "Inverter Power",
                                    info = "${item.inverterRatingWatts.roundToInt()}W",
                                    iconColor = AccentOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail modal dialog
    selectedReportForDetails?.let { item ->
        ReportDetailsDialog(
            report = item,
            currentLang = currentLang,
            onDismiss = { selectedReportForDetails = null }
        )
    }
}

@Composable
fun LabelInfoCol(label: String, info: String, iconColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(iconColor))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 9.sp, color = Color.Gray)
        }
        Text(info, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

// -- Detailed saved report dialogue --

@Composable
fun ReportDetailsDialog(
    report: SavedCalculation,
    currentLang: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundDark),
            border = BorderStroke(1.5.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = AppText.projectDetails.get(currentLang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                ResultRow(label = AppText.clientPromptName.get(currentLang), value = report.clientName, valueColor = SolarSecondary)
                ResultRow(
                    label = if (currentLang == Language.AR) "تاريخ الحساب" else "Calculated Date", 
                    value = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(report.dateLong))
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (currentLang == Language.AR) "معطيات الأحمال المحسوبة" else "Stored Load Information",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                ResultRow(label = AppText.totalPeakLoad.get(currentLang), value = "${report.peakLoadWatts.roundToInt()} W")
                ResultRow(label = AppText.totalDailyEnergy.get(currentLang), value = "${report.totalDailyEnergyWh.roundToInt()} Wh")
                ResultRow(label = AppText.nighttimeEnergy.get(currentLang), value = "${report.totalNighttimeEnergyWh.roundToInt()} Wh")

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (currentLang == Language.AR) "تفاصيل الأجهزة والنظام الموفرة" else "Stored Sizing Equipment Report",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                ResultRow(label = AppText.panelsCountNeeded.get(currentLang), value = "${report.panelsCountNeeded} x ${report.panelWattageUsed.roundToInt()}W", valueColor = SolarPrimary)
                ResultRow(label = AppText.pvPowerNeeded.get(currentLang), value = "${report.totalPanelsPowerRequiredWatts.roundToInt()} W")
                ResultRow(label = AppText.totalBattUnits.get(currentLang), value = "${report.batteriesCountNeeded} x ${report.batteryCapacityAhUsed.roundToInt()}Ah", valueColor = SolarSecondary)
                ResultRow(label = AppText.requiredAh.get(currentLang), value = "${report.totalBatteryAhRequired.roundToInt()} Ah")
                ResultRow(label = AppText.minInvPower.get(currentLang), value = "${report.inverterRatingWatts.roundToInt()} W", valueColor = AccentOrange)
                ResultRow(label = AppText.controllerRating.get(currentLang), value = "${report.chargeControllerAmps.roundToInt()} A")
                ResultRow(label = AppText.suggestedWireCross.get(currentLang), value = "${report.suggestedCableSizeMm2} ${AppText.mm2Unit.get(currentLang)}", valueColor = SolarSecondary)

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(AppText.back.get(currentLang), fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            PdfExporter.exportSizingToPdf(
                                context,
                                report.clientName,
                                report.toSizingResults(),
                                currentLang
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_export_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == Language.AR) "تصدير PDF" else "Export PDF",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportClientReportDialog(
    currentLang: Language,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var clientNameInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.5.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (currentLang == Language.AR) "تصدير تقرير العميل بصيغة PDF" else "Export Sizing Report PDF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = clientNameInput,
                    onValueChange = {
                        clientNameInput = it
                        if (it.isNotBlank()) inputError = null
                    },
                    label = { Text(AppText.clientPromptName.get(currentLang)) },
                    isError = inputError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_client_name_text_input")
                )
                if (inputError != null) {
                    Text(inputError!!, color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(AppText.cancel.get(currentLang), color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (clientNameInput.isBlank()) {
                                inputError = AppText.clientNameErr.get(currentLang)
                            } else {
                                onConfirm(clientNameInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("confirm_export_client_btn")
                    ) {
                        Text(
                            text = if (currentLang == Language.AR) "تصدير" else "Export",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -- Dialogue component for saving a client project record --

@Composable
fun SaveClientReportDialog(
    currentLang: Language,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var clientNameInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.5.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = AppText.labelEnterName.get(currentLang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = clientNameInput,
                    onValueChange = {
                        clientNameInput = it
                        if (it.isNotBlank()) inputError = null
                    },
                    label = { Text(AppText.clientPromptName.get(currentLang)) },
                    isError = inputError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("client_name_text_input")
                )
                if (inputError != null) {
                    Text(inputError!!, color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(AppText.cancel.get(currentLang), color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (clientNameInput.isBlank()) {
                                inputError = AppText.clientNameErr.get(currentLang)
                            } else {
                                onConfirm(clientNameInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("confirm_save_client_btn")
                    ) {
                        Text(AppText.save.get(currentLang), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -- Dialogue components for adding/editing appliances --

@Composable
fun ApplianceFormDialog(
    appliance: SolarAppliance?, // null if adding
    currentLang: Language,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, Double, Double) -> Unit // name, power, qty, daytime, nighttime
) {
    var nameInput by remember { mutableStateOf(appliance?.name ?: "") }
    var powerInput by remember { mutableStateOf(appliance?.powerWatts?.roundToInt()?.toString() ?: "") }
    var qtyInput by remember { mutableStateOf(appliance?.quantity?.toString() ?: "1") }
    var daytimeInput by remember { mutableStateOf(appliance?.daytimeHours?.toString() ?: "4") }
    var nighttimeInput by remember { mutableStateOf(appliance?.nighttimeHours?.toString() ?: "4") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundDark),
            border = BorderStroke(1.5.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (appliance == null) AppText.addAppliance.get(currentLang) else AppText.editAppliance.get(currentLang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Suggestion row helper
                Text(
                    text = if (currentLang == Language.AR) "اقتراحات سريعة لنوع الحمل:" else "Quick preset load templates:",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Pair("مصباح LED", 15),
                        Pair("مروحة", 75),
                        Pair("شاشة تلفاز", 120),
                        Pair("ثلاجة", 250),
                        Pair("شاحن هاتف", 18),
                        Pair("كمبيوتر", 300),
                        Pair("مكيف هواء", 1500),
                        Pair("مضخة مياه", 750)
                    )
                    presets.forEach { (pName, pPower) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .border(0.5.dp, BorderDark, RoundedCornerShape(8.dp))
                                .clickable {
                                    nameInput = pName
                                    powerInput = pPower.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(pName, fontSize = 11.sp, color = SolarSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Name
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(AppText.applianceName.get(currentLang)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("appliance_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Input Power (Watts)
                OutlinedTextField(
                    value = powerInput,
                    onValueChange = { powerInput = it },
                    label = { Text(AppText.powerWatts.get(currentLang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("appliance_power_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Hours row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text(AppText.quantity.get(currentLang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).testTag("appliance_qty_input")
                    )

                    OutlinedTextField(
                        value = daytimeInput,
                        onValueChange = { daytimeInput = it },
                        label = { Text(AppText.daytimeHours.get(currentLang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1.3f).testTag("appliance_daytime_input")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Nighttime input
                OutlinedTextField(
                    value = nighttimeInput,
                    onValueChange = { nighttimeInput = it },
                    label = { Text(AppText.nighttimeHours.get(currentLang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("appliance_nighttime_input")
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentLang == Language.AR) "يرجى ملء جميع الحقول بشكل صحيح وبأرقام صالحة" else "Please check all fields and enter proper numbers",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(AppText.cancel.get(currentLang), color = Color.White)
                    }

                    Button(
                        onClick = {
                            val power = powerInput.toDoubleOrNull()
                            val qty = qtyInput.toIntOrNull()
                            val dayH = daytimeInput.toDoubleOrNull()
                            val nightH = nighttimeInput.toDoubleOrNull()

                            if (nameInput.isNotBlank() && power != null && qty != null && dayH != null && nightH != null && power > 0 && qty > 0 && dayH >= 0 && nightH >= 0) {
                                hasError = false
                                onConfirm(nameInput, power, qty, dayH, nightH)
                            } else {
                                hasError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("appliance_submit_btn")
                    ) {
                        Text(
                            text = if (appliance == null) AppText.save.get(currentLang) else AppText.update.get(currentLang),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
