package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun SavedCalculation.toSizingResults(): SizingResults {
    val batteriesInSeries = if (this.batteryCapacityAhUsed > 0 && this.systemVoltageUsed > 0) {
        val individualV = 12.0
        kotlin.math.ceil(this.systemVoltageUsed.toDouble() / individualV).toInt().coerceAtLeast(1)
    } else 1
    val batteriesInParallel = if (batteriesInSeries > 0) this.batteriesCountNeeded / batteriesInSeries else this.batteriesCountNeeded

    val cycleYears = if (this.batteryChemistryUsed.lowercase() == "lithium") 11.5 else 1.3
    val dischargeFactor = if (this.batteryChemistryUsed.lowercase() == "lithium") 1.0 else 0.2
    val safeDischargeA = this.batteryCapacityAhUsed * dischargeFactor * batteriesInParallel.coerceAtLeast(1)
    val bmsAmps = if (this.systemVoltageUsed > 0) (this.peakLoadWatts / this.systemVoltageUsed) * 1.25 else 0.0

    val tilt = 30.0
    val panelLength = 1.7
    val MathRad = Math.toRadians(tilt)
    val rearSupportHeight = panelLength * Math.sin(MathRad)
    val baseFloorLength = panelLength * Math.cos(MathRad)

    val correctedPanelVoc = this.panelVocUsed * 1.12
    val maxSeries = if (correctedPanelVoc > 0.0) kotlin.math.floor(this.mpptMaxVocUsed / correctedPanelVoc).toInt().coerceAtLeast(1) else 1
    val minSeries = if (this.panelVmpUsed > 0.0) kotlin.math.ceil(this.mpptMinVmpUsed / this.panelVmpUsed).toInt().coerceAtLeast(1) else 1

    var suggestedMpptSeries = 1
    var suggestedMpptParallel = 1
    
    if (this.panelsCountNeeded > 0) {
        val seriesLowerBound = minOf(minSeries, this.panelsCountNeeded).coerceAtLeast(1)
        val seriesUpperBound = minOf(maxSeries, this.panelsCountNeeded).coerceAtLeast(1)
        
        var bestS = 1
        var bestDiff = Double.MAX_VALUE
        
        for (s in seriesLowerBound..seriesUpperBound) {
            val p = kotlin.math.ceil(this.panelsCountNeeded.toDouble() / s.toDouble()).toInt()
            val totalAssigned = s * p
            val diff = (totalAssigned - this.panelsCountNeeded).toDouble()
            if (diff < bestDiff) {
                bestDiff = diff
                bestS = s
            }
        }
        suggestedMpptSeries = bestS
        suggestedMpptParallel = kotlin.math.ceil(this.panelsCountNeeded.toDouble() / bestS.toDouble()).toInt().coerceAtLeast(1)
    }

    return SizingResults(
        totalDailyEnergyWh = this.totalDailyEnergyWh,
        totalNighttimeEnergyWh = this.totalNighttimeEnergyWh,
        peakLoadWatts = this.peakLoadWatts,
        totalPanelsPowerRequiredWatts = this.totalPanelsPowerRequiredWatts,
        panelsCountNeeded = this.panelsCountNeeded,
        panelWattageUsed = this.panelWattageUsed,
        totalBatteryAhRequired = this.totalBatteryAhRequired,
        batteriesCountNeeded = this.batteriesCountNeeded,
        batteryCapacityAhUsed = this.batteryCapacityAhUsed,
        systemVoltageUsed = this.systemVoltageUsed,
        inverterRatingWatts = this.inverterRatingWatts,
        chargeControllerAmps = this.chargeControllerAmps,
        suggestedCableSizeMm2 = this.suggestedCableSizeMm2,
        calculatedAmps = this.calculatedAmps,
        batteriesInSeries = batteriesInSeries,
        batteriesInParallel = batteriesInParallel,
        acVoltageUsed = 220.0,
        continuousACLineAmps = this.peakLoadWatts / 220.0,
        suggestedCableSizeAcMm2 = 2.5,
        tiltAngleUsed = tilt,
        panelPhysicalLengthUsed = panelLength,
        rearSupportHeightMeters = rearSupportHeight,
        baseFloorLengthMeters = baseFloorLength,
        batteryChemistryUsed = this.batteryChemistryUsed,
        isHybridInverterUsed = this.isHybridInverterUsed,
        mpptMaxVocUsed = this.mpptMaxVocUsed,
        mpptMinVmpUsed = this.mpptMinVmpUsed,
        panelVocUsed = this.panelVocUsed,
        panelVmpUsed = this.panelVmpUsed,
        suggestedMpptSeries = suggestedMpptSeries,
        suggestedMpptParallel = suggestedMpptParallel,
        totalPVVoc = suggestedMpptSeries * this.panelVocUsed,
        totalPVVmp = suggestedMpptSeries * this.panelVmpUsed,
        batteryMaxSafeDischargeAmps = safeDischargeA,
        batteryCycleLifeYears = cycleYears,
        bmsRatingAmps = bmsAmps
    )
}

object PdfExporter {
    fun exportSizingToPdf(
        context: Context,
        clientName: String,
        results: SizingResults,
        lang: Language
    ) {
        try {
            val document = PdfDocument()
            // A4 page is 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val backgroundPaint = Paint().apply {
                color = Color.rgb(255, 255, 255)
            }
            canvas.drawRect(0f, 0f, 595f, 842f, backgroundPaint)

            val primarySlate = Color.rgb(30, 41, 59)
            val primaryOrange = Color.rgb(249, 115, 22)
            val greySecondary = Color.rgb(100, 116, 139)
            val lineGrey = Color.rgb(226, 232, 240)

            // Header Background accent
            val accentHeaderPaint = Paint().apply {
                color = primarySlate
            }
            canvas.drawRect(0f, 0f, 595f, 100f, accentHeaderPaint)

            // Header Top Bar Title
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                isFakeBoldText = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = if (lang == Language.AR) Paint.Align.RIGHT else Paint.Align.LEFT
            }
            val appTitleStr = if (lang == Language.AR) "سيزر شمسي برو - تقرير تصميم الأجهزة" else "SolarWise Pro - Recommendations Report"
            if (lang == Language.AR) {
                canvas.drawText(appTitleStr, 595f - 40f, 45f, titlePaint)
            } else {
                canvas.drawText(appTitleStr, 40f, 45f, titlePaint)
            }

            // Subtitle
            val subtitlePaint = Paint().apply {
                color = primaryOrange
                textSize = 11f
                isFakeBoldText = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = if (lang == Language.AR) Paint.Align.RIGHT else Paint.Align.LEFT
            }
            val subStr = if (lang == Language.AR) "مواصفات وحسابات أحجام الأنظمة الشمسية المحترفة" else "Professional Solar Load Sizing & Design Blueprint"
            if (lang == Language.AR) {
                canvas.drawText(subStr, 595f - 40f, 65f, subtitlePaint)
            } else {
                canvas.drawText(subStr, 40f, 65f, subtitlePaint)
            }

            // Info bar (client name & date) below header
            val infoPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                textSize = 9f
                textAlign = if (lang == Language.AR) Paint.Align.RIGHT else Paint.Align.LEFT
            }
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val dateFormatted = sdf.format(Date())
            val clientStr = if (lang == Language.AR) "العميل / المشروع: $clientName" else "Project Client: $clientName"
            val dateStr = if (lang == Language.AR) "تاريخ التصدير: $dateFormatted" else "Generated On: $dateFormatted"

            if (lang == Language.AR) {
                canvas.drawText(clientStr, 595f - 40f, 88f, infoPaint)
                canvas.drawText(dateStr, 40f, 88f, infoPaint.apply { textAlign = Paint.Align.LEFT })
            } else {
                canvas.drawText(clientStr, 40f, 88f, infoPaint)
                canvas.drawText(dateStr, 595f - 40f, 88f, infoPaint.apply { textAlign = Paint.Align.RIGHT })
            }

            var y = 140f
            val margin = 40f

            fun drawSection(title: String) {
                // Draw Section Banner block
                val bannerPaint = Paint().apply {
                    color = Color.rgb(241, 245, 249)
                }
                canvas.drawRect(margin, y - 18f, 595f - margin, y + 8f, bannerPaint)

                // Draw section accent line
                val accentLinePaint = Paint().apply {
                    color = primaryOrange
                    strokeWidth = 3f
                }
                if (lang == Language.AR) {
                    canvas.drawLine(595f - margin, y - 18f, 595f - margin, y + 8f, accentLinePaint)
                } else {
                    canvas.drawLine(margin, y - 18f, margin, y + 8f, accentLinePaint)
                }

                val secTitlePaint = Paint().apply {
                    color = primarySlate
                    textSize = 12f
                    isFakeBoldText = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = if (lang == Language.AR) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                if (lang == Language.AR) {
                    canvas.drawText(title, 595f - margin - 15f, y - 1f, secTitlePaint)
                } else {
                    canvas.drawText(title, margin + 15f, y - 1f, secTitlePaint)
                }
                y += 20f
            }

            val labelPaint = Paint().apply {
                color = greySecondary
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = if (lang == Language.AR) Paint.Align.RIGHT else Paint.Align.LEFT
            }

            val valuePaint = Paint().apply {
                color = primarySlate
                textSize = 10.5f
                isFakeBoldText = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = if (lang == Language.AR) Paint.Align.LEFT else Paint.Align.RIGHT
            }

            val linePaint = Paint().apply {
                color = lineGrey
                strokeWidth = 0.5f
            }

            fun drawItemRow(label: String, valStr: String) {
                if (lang == Language.AR) {
                    canvas.drawText(label, 595f - margin - 15f, y, labelPaint)
                    canvas.drawText(valStr, margin + 15f, y, valuePaint)
                } else {
                    canvas.drawText(label, margin + 15f, y, labelPaint)
                    canvas.drawText(valStr, 595f - margin - 15f, y, valuePaint)
                }
                y += 6f
                canvas.drawLine(margin + 15f, y, 595f - margin - 15f, y, linePaint)
                y += 14f
            }

            // Section 1: Power Requirements
            val secLoadsTitle = if (lang == Language.AR) "أحمال المشروع والطاقة اليومية" else "Summary Load Profiling"
            drawSection(secLoadsTitle)
            drawItemRow(
                if (lang == Language.AR) "إجمالي قدرة الأحمال (حمل الذروة):" else "Combined Appliances Peak Load (Watts):",
                "${results.peakLoadWatts.roundToInt()} W"
            )
            drawItemRow(
                if (lang == Language.AR) "الاستهلاك اليومي الكلي المطلوب:" else "Total Daily Energy Needs (Wh):",
                "${results.totalDailyEnergyWh.roundToInt()} Wh"
            )
            drawItemRow(
                if (lang == Language.AR) "الاستهلاك في فترات الليل:" else "Nighttime Load Energy Consumption (Wh):",
                "${results.totalNighttimeEnergyWh.roundToInt()} Wh"
            )

            // Section 2: solar panel array
            val secPVTitle = if (lang == Language.AR) "مصفوفة وتوصيلات الألواح الشمسية (PV)" else "Solar Panel Array recommendations"
            drawSection(secPVTitle)
            drawItemRow(
                if (lang == Language.AR) "إجمالي قدرة مصفوفة الألواح المطلوبة:" else "Required solar array power capacity:",
                "${results.totalPanelsPowerRequiredWatts.roundToInt()} W"
            )
            drawItemRow(
                if (lang == Language.AR) "عدد الألواح الشمسية المطلوبة بالتحديد:" else "Prescribed Solar Panels count:",
                "${results.panelsCountNeeded} units"
            )
            drawItemRow(
                if (lang == Language.AR) "قدرة اللوح الفردي المفترض:" else "Individual panel capacity size:",
                "${results.panelWattageUsed.roundToInt()} W (Mono-Si)"
            )
            if (results.isHybridInverterUsed && results.panelsCountNeeded > 0) {
                drawItemRow(
                    if (lang == Language.AR) "طريقة التوصيل المقترحة (التوالي × التوازي):" else "MPPT Array String Connection Layout:",
                    "${results.suggestedMpptSeries}S  x  ${results.suggestedMpptParallel}P"
                )
                drawItemRow(
                    if (lang == Language.AR) "جهد الدائرة المفتوحة الإجمالي (Voc):" else "Total Open-circuit Array Voltage (Voc):",
                    "${String.format("%.1f", results.totalPVVoc)} V"
                )
                drawItemRow(
                    if (lang == Language.AR) "جهد التشغيل الإجمالي الأقصى (Vmp):" else "Maximum Power Array Voltage (Vmp):",
                    "${String.format("%.1f", results.totalPVVmp)} V"
                )
            }

            // Section 3: Batteries Bank
            val secBattTitle = if (lang == Language.AR) "بنك بطاريات التخزين الكهربائي" else "Storage Batteries Bank configuration"
            drawSection(secBattTitle)
            drawItemRow(
                if (lang == Language.AR) "عدد بطاريات التخزين الإجمالي:" else "Consolidated Battery bank units count:",
                "${results.batteriesCountNeeded} units"
            )
            drawItemRow(
                if (lang == Language.AR) "سعة بنك البطاريات الكلية بالـ AH:" else "Combined battery storage capacity required:",
                "${results.totalBatteryAhRequired.roundToInt()} Ah"
            )
            drawItemRow(
                if (lang == Language.AR) "سعة وجهد البطارية المفترضة:" else "Subsumed single battery specification:",
                "${results.batteryCapacityAhUsed.roundToInt()} Ah @ ${if (results.batteriesInSeries > 0) results.systemVoltageUsed / results.batteriesInSeries else 12}V"
            )
            drawItemRow(
                if (lang == Language.AR) "مخطط التوصيل المقترح (التوالي × التوازي):" else "Suggested Series-Parallel connections scheme:",
                "${results.batteriesInSeries}S  x  ${results.batteriesInParallel}P"
            )
            drawItemRow(
                if (lang == Language.AR) "التقنية الكيميائية وعمر البطارية الفرضي:" else "Selected Chemistry / Expected lifespan years:",
                "${results.batteryChemistryUsed} (~${results.batteryCycleLifeYears} Years)"
            )
            drawItemRow(
                if (lang == Language.AR) "الحد الأقصى للتفريغ الآمن المستمر:" else "Recommended max safe continuous discharge rate:",
                "${results.batteryMaxSafeDischargeAmps.roundToInt()} A"
            )

            // Section 4: Inverter & controller specs
            val secDevicesTitle = if (lang == Language.AR) "الأجهزة المساعدة وعاكس التيار" else "Inverter and Charger Controllers"
            drawSection(secDevicesTitle)
            drawItemRow(
                if (lang == Language.AR) "الحد الأدنى لقدرة عاكس التيار (Inverter):" else "Minimum Inverter power rating threshold:",
                "${results.inverterRatingWatts.roundToInt()} W"
            )
            drawItemRow(
                if (lang == Language.AR) "الحد الأدنى لسعة منظم الشحن:" else "Solar Charge Controller minimum rating current:",
                "${results.chargeControllerAmps.roundToInt()} A"
            )

            // Section 5: Cables & Geometric
            val secCablesTitle = if (lang == Language.AR) "مقاسات كابلات النحاس والزوايا الهندسية" else "Cables Gauges & Mechanical Sizing"
            drawSection(secCablesTitle)
            drawItemRow(
                if (lang == Language.AR) "تيار ومقاس كابل خط الجهد المستمر (DC):" else "Continuous DC line amperage & recommended wire size:",
                "${String.format("%.1f", results.calculatedAmps)} A  →  ${results.suggestedCableSizeMm2} mm²"
            )
            drawItemRow(
                if (lang == Language.AR) "تيار ومقاس كابل التيار المتردد (AC):" else "Continuous AC load amperage & recommended wire size:",
                "${String.format("%.1f", results.continuousACLineAmps)} A  →  ${results.suggestedCableSizeAcMm2} mm²"
            )
            drawItemRow(
                if (lang == Language.AR) "زاوية ميل اللوح المفترضة لتصميم الحامل:" else "Solar panels recommended mounting tilt pitch angle:",
                "${results.tiltAngleUsed}°"
            )
            drawItemRow(
                if (lang == Language.AR) "مقاسات قوائم التثبيت (الارتفاع H × العمق D):" else "Bracket triangular geometry (Height H x Footprint depth D):",
                "H: ${String.format("%.2f", results.rearSupportHeightMeters)}m , D: ${String.format("%.2f", results.baseFloorLengthMeters)}m"
            )

            // Footer and professional copyright details
            val footerLinePaint = Paint().apply {
                color = primarySlate
                strokeWidth = 1.5f
            }
            canvas.drawLine(margin, 790f, 595f - margin, 790f, footerLinePaint)

            val footerPaint = Paint().apply {
                color = greySecondary
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
            }
            val footerText1 = if (lang == Language.AR) "هذا التقرير تم إنشاؤه تلقائيًا عبر تطبيق سيزر شمسي برو للتصميم والمقايسات الهندسية" else "Report generated automatically by Solar Sizer Pro - Renewable Energy Advisor"
            val footerText2 = if (lang == Language.AR) "إنتاج الطاقة الشمسية يختلف باختلاف الفصول والأحوال الجوية، كابلات النحاس تفترض مسافة أقصاها 10 أمتار" else "Solar production varies with weather and seasons. Copper cables calculated assuming max 10-meter runs."
            canvas.drawText(footerText1, 595f / 2, 808f, footerPaint)
            canvas.drawText(footerText2, 595f / 2, 822f, footerPaint)

            document.finishPage(page)

            // Write document to file
            val outputDir = File(context.cacheDir, "shared_pdfs").apply { mkdirs() }
            val formattedClient = clientName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val file = File(outputDir, "SolarSizerReport_${formattedClient}.pdf")

            val fileOutputStream = FileOutputStream(file)
            document.writeTo(fileOutputStream)
            document.close()
            fileOutputStream.flush()
            fileOutputStream.close()

            // Open share intent
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, if (lang == Language.AR) "تقرير التوصيات الفنية للأجهزة - $clientName" else "Equipment recommendations solar report - $clientName")
                putExtra(Intent.EXTRA_TEXT, if (lang == Language.AR) "مرفق تقرير الحسابات الهندسية ومواصفات الأجهزة لمشروع: $clientName" else "Enclosed is the professional sizing recommendations solar report for project: $clientName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, if (lang == Language.AR) "مشاركه التقرير PDF" else "Share Sizing Report PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate report PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
