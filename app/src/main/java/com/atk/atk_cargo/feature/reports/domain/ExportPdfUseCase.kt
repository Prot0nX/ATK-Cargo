package com.atk.atk_cargo.feature.reports.domain

import android.annotation.SuppressLint
import android.app.Application
import android.os.Environment
import com.atk.atk_cargo.data.model.FilteredSummary
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont.IDENTITY_H
import com.itextpdf.text.pdf.BaseFont.createFont
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExportPdfUseCase(private val application: Application) {
    
    suspend operator fun invoke(data: FilteredSummary): String = withContext(Dispatchers.IO) {
        val fileName = "report_${System.currentTimeMillis()}.pdf"
        val file = File(application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            file.outputStream().use { outputStream ->
                val document = Document(PageSize.A4, 30f, 30f, 40f, 30f)
                val writer = PdfWriter.getInstance(document, outputStream)
                document.open()

                val persianFontManager = PersianFontManager()
                val fonts = persianFontManager.loadFonts()
                val colorScheme = PdfColorScheme()

                addModernHeader(document, fonts.headerFont, colorScheme)
                addEnhancedSummaryInfo(document, data, fonts, colorScheme)

                val table = createEnhancedMainTable(data, fonts, colorScheme)
                document.add(table)

                addPersianFooter(document, writer, fonts.normalFont, colorScheme)
                document.close()
            }
        } catch (e: Exception) {
            throw e
        }
        fileName
    }

    private fun addModernHeader(document: Document, font: Font, colorScheme: PdfColorScheme) {
        val headerTable = PdfPTable(1)
        headerTable.widthPercentage = 100f
        headerTable.spacingAfter = 15f

        val headerCell = PdfPCell()
        headerCell.backgroundColor = colorScheme.primary
        headerCell.border = Rectangle.NO_BORDER
        headerCell.paddingTop = 20f
        headerCell.paddingBottom = 20f
        headerCell.paddingLeft = 15f
        headerCell.paddingRight = 15f

        val whiteHeaderFont = Font(font.baseFont, font.size, font.style)
        whiteHeaderFont.color = BaseColor.WHITE

        addPersianText(headerCell, "گزارش خلاصه حواله‌ها", whiteHeaderFont, Element.ALIGN_CENTER, false)
        headerTable.addCell(headerCell)

        val separatorTable = PdfPTable(1)
        separatorTable.widthPercentage = 100f
        separatorTable.spacingAfter = 10f

        val separatorCell = PdfPCell()
        separatorCell.backgroundColor = colorScheme.accent
        separatorCell.border = Rectangle.NO_BORDER
        separatorCell.fixedHeight = 3f
        separatorTable.addCell(separatorCell)

        document.add(headerTable)
        document.add(separatorTable)
    }

    private fun addEnhancedSummaryInfo(
        document: Document,
        data: FilteredSummary,
        fonts: PdfFonts,
        colorScheme: PdfColorScheme
    ) {
        val infoTable = PdfPTable(2)
        infoTable.widthPercentage = 100f
        infoTable.setWidths(floatArrayOf(1.2f, 0.8f))
        infoTable.spacingAfter = 20f

        fun addModernInfoRow(label: String, value: String, isHighlight: Boolean = false) {
            val valueCell = PdfPCell()
            valueCell.backgroundColor = colorScheme.white
            valueCell.border = Rectangle.BOX
            valueCell.borderColor = colorScheme.lightGray
            valueCell.borderWidth = 1f
            valueCell.paddingTop = 12f
            valueCell.paddingBottom = 12f
            valueCell.paddingLeft = 15f
            valueCell.paddingRight = 15f

            val valueFont = if (isHighlight) fonts.boldFont else fonts.normalFont
            valueFont.color = colorScheme.text
            addPersianText(valueCell, value, valueFont, Element.ALIGN_RIGHT)
            infoTable.addCell(valueCell)

            val labelCell = PdfPCell()
            labelCell.backgroundColor = if (isHighlight) colorScheme.accent else colorScheme.secondary
            labelCell.border = Rectangle.NO_BORDER
            labelCell.paddingTop = 12f
            labelCell.paddingBottom = 12f
            labelCell.paddingLeft = 15f
            labelCell.paddingRight = 15f

            val labelFont = if (isHighlight) fonts.boldFont else fonts.normalFont
            if (isHighlight) labelFont.color = BaseColor.WHITE
            addPersianText(labelCell, label, labelFont, Element.ALIGN_LEFT, false)
            infoTable.addCell(labelCell)
        }

        addModernInfoRow("شماره کوتاژ:", data.quotaNumber)
        
        val formattedStartDate = if (data.startDate.isNotBlank()) convertToShamsiDate(data.startDate) else "بدون تاریخ"
        val formattedStartTime = if (data.startTime.isNotBlank()) convertToPersianNumbers(data.startTime) else "۰:۰۰"
        addModernInfoRow("از تاریخ و ساعت:", "$formattedStartDate - $formattedStartTime")

        val formattedEndDate = if (data.endDate.isNotBlank()) convertToShamsiDate(data.endDate) else "بدون تاریخ"
        val formattedEndTime = if (data.endTime.isNotBlank()) convertToPersianNumbers(data.endTime) else "۰:۰۰"
        addModernInfoRow("تا تاریخ و ساعت:", "$formattedEndDate - $formattedEndTime")
        
        addModernInfoRow("تعداد کل حواله‌ها:", convertToPersianNumbers(data.voucherCount.toString()), true)
        addModernInfoRow("وزن خالص کل:", "${formatPersianNumber(data.totalNetWeight.toInt())} کیلوگرم", true)

        document.add(infoTable)
    }

    private fun formatPersianNumber(number: Int): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(number)
        return convertToPersianNumbers(formatted)
    }

    private fun createEnhancedMainTable(
        data: FilteredSummary,
        fonts: PdfFonts,
        colorScheme: PdfColorScheme
    ): PdfPTable {
        val table = PdfPTable(6)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2.2f, 2f, 2f, 1.8f, 1.8f, 2.5f))
        table.spacingBefore = 10f

        fun createModernCell(
            content: String,
            isHeader: Boolean = false,
            isPersian: Boolean = true,
            isNumeric: Boolean = false
        ): PdfPCell {
            val cell = PdfPCell()
            cell.paddingTop = if (isHeader) 15f else 10f
            cell.paddingBottom = if (isHeader) 15f else 10f
            cell.paddingLeft = 8f
            cell.paddingRight = 8f

            if (isHeader) {
                cell.backgroundColor = colorScheme.primary
                cell.border = Rectangle.BOX
                cell.borderColor = colorScheme.white
                cell.borderWidth = 1f

                val headerFont = Font(fonts.boldFont.baseFont, 11f, Font.BOLD)
                headerFont.color = BaseColor.WHITE
                addPersianText(cell, content, headerFont, Element.ALIGN_CENTER, false)
            } else {
                cell.backgroundColor = colorScheme.white
                cell.border = Rectangle.BOX
                cell.borderColor = colorScheme.lightGray
                cell.borderWidth = 0.5f

                val cellFont = fonts.normalFont
                cellFont.color = colorScheme.text

                if (isPersian) {
                    val processedContent = if (isNumeric) convertToPersianNumbers(content) else content
                    addPersianText(cell, processedContent, cellFont, Element.ALIGN_CENTER)
                } else {
                    cell.phrase = Phrase(content, cellFont)
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                }
            }
            return cell
        }

        table.addCell(createModernCell("شماره قبض", isHeader = true))
        table.addCell(createModernCell("وزن خالص (کیلوگرم)", isHeader = true))
        table.addCell(createModernCell("تاریخ خروج", isHeader = true))
        table.addCell(createModernCell("ساعت خروج", isHeader = true))
        table.addCell(createModernCell("ساعت ورود", isHeader = true))
        table.addCell(createModernCell("شماره حواله", isHeader = true))

        data.voucherDetails.forEachIndexed { index, detail ->
            val isEvenRow = index % 2 == 0
            val rowColor = if (isEvenRow) colorScheme.white else colorScheme.lightGray

            table.addCell(createModernCell(detail.scaleReceiptNumber, isPersian = false).apply {
                backgroundColor = rowColor
            })

            table.addCell(createModernCell(formatPersianNumber(detail.netWeight.toInt()), isPersian = true).apply {
                backgroundColor = rowColor
            })

            val exitDate = detail.exitDate.ifBlank { "" }
            table.addCell(createModernCell(if (exitDate.isNotBlank()) convertToShamsiDate(exitDate) else "---", isPersian = true).apply {
                backgroundColor = rowColor
            })

            val exitTime = detail.exitTime.ifBlank { "" }
            table.addCell(createModernCell(if (exitTime.isNotBlank()) exitTime else "---", isPersian = true, isNumeric = true).apply {
                backgroundColor = rowColor
            })

            val entryTime = detail.entryTime.ifBlank { "" }
            table.addCell(createModernCell(if (entryTime.isNotBlank()) entryTime else "---", isPersian = true, isNumeric = true).apply {
                backgroundColor = rowColor
            })

            table.addCell(createModernCell(detail.trackingNumber, isPersian = false).apply {
                backgroundColor = rowColor
            })
        }
        return table
    }

    private class PersianFontManager {
        fun loadFonts(): PdfFonts {
            return try {
                val regularFont = createFont("assets/fonts/main/Vazirmatn-Regular.ttf", IDENTITY_H, true)
                val boldFontBase = createFont("assets/fonts/main/Vazirmatn-Bold.ttf", IDENTITY_H, true)
                val fallbackFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)

                PdfFonts(
                    normalFont = Font(regularFont, 11f, Font.NORMAL),
                    boldFont = Font(boldFontBase, 12f, Font.BOLD),
                    headerFont = Font(boldFontBase, 20f, Font.BOLD),
                    titleFont = Font(boldFontBase, 16f, Font.BOLD),
                    subtitleFont = Font(boldFontBase, 14f, Font.BOLD),
                    fallbackFont = fallbackFont
                )
            } catch (e: Exception) {
                val fallback = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
                PdfFonts(
                    normalFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL),
                    boldFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD),
                    headerFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD),
                    titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD),
                    subtitleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD),
                    fallbackFont = fallback
                )
            }
        }
    }

    private data class PdfFonts(
        val normalFont: Font,
        val boldFont: Font,
        val headerFont: Font,
        val titleFont: Font,
        val subtitleFont: Font,
        val fallbackFont: Font
    )

    private class PdfColorScheme {
        val primary = BaseColor(0, 96, 100)
        val secondary = BaseColor(240, 248, 255)
        val accent = BaseColor(255, 193, 7)
        val text = BaseColor(33, 37, 41)
        val lightGray = BaseColor(248, 249, 250)
        val white = BaseColor.WHITE
    }

    private fun addPersianText(
        cell: PdfPCell,
        text: String,
        font: Font,
        alignment: Int = Element.ALIGN_RIGHT,
        convertNumbers: Boolean = true
    ) {
        val processedText = if (convertNumbers) convertToPersianNumbers(text) else text
        val column = ColumnText(null)
        column.runDirection = PdfWriter.RUN_DIRECTION_RTL
        column.alignment = alignment
        column.addElement(Paragraph(processedText, font))
        cell.column = column
    }

    private fun convertToPersianNumbers(text: String): String {
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        var result = text
        for (i in 0..9) {
            result = result.replace(i.toString(), persianDigits[i])
        }
        return result
    }

    private fun convertToShamsiDate(date: String): String {
        return try {
            if (date.isBlank()) return ""
            val normalizedDate = date.replace(Regex("[^0-9]"), "/")
            val parts = normalizedDate.split("/").filter { it.isNotBlank() }
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()

                if (year in 1300..1500) {
                    val yearStr = year.toString()
                    val monthStr = month.toString().padStart(2, '0')
                    val dayStr = day.toString().padStart(2, '0')
                    return "${convertToPersianNumbers(yearStr)}/${convertToPersianNumbers(monthStr)}/${convertToPersianNumbers(dayStr)}"
                }

                val shamsiDate = gregorianToShamsi(year, month, day)
                val yearStr = shamsiDate.year.toString()
                val monthStr = shamsiDate.month.toString().padStart(2, '0')
                val dayStr = shamsiDate.day.toString().padStart(2, '0')
                "${convertToPersianNumbers(yearStr)}/${convertToPersianNumbers(monthStr)}/${convertToPersianNumbers(dayStr)}"
            } else {
                convertToPersianNumbers(date)
            }
        } catch (e: Exception) {
            convertToPersianNumbers(date)
        }
    }

    private fun convertGregorianToShamsi(gregorianDate: Date): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = gregorianDate
            val gregorianYear = calendar.get(Calendar.YEAR)
            val gregorianMonth = calendar.get(Calendar.MONTH) + 1
            val gregorianDay = calendar.get(Calendar.DAY_OF_MONTH)

            val shamsiDate = gregorianToShamsi(gregorianYear, gregorianMonth, gregorianDay)
            val year = shamsiDate.year.toString()
            val month = shamsiDate.month.toString().padStart(2, '0')
            val day = shamsiDate.day.toString().padStart(2, '0')
            "${convertToPersianNumbers(year)}/${convertToPersianNumbers(month)}/${convertToPersianNumbers(day)}"
        } catch (e: Exception) {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            convertToPersianNumbers(dateFormat.format(gregorianDate))
        }
    }

    private data class ShamsiDate(val year: Int, val month: Int, val day: Int)

    private fun gregorianToShamsi(gYear: Int, gMonth: Int, gDay: Int): ShamsiDate {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gMonth > 2) gYear + 1 else gYear
        var days = 355666 + (365 * gYear) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gDay + g_d_m[gMonth - 1]
        
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return ShamsiDate(jy, jm, jd)
    }

    @SuppressLint("SimpleDateFormat", "DefaultLocale")
    private fun addPersianFooter(
        document: Document,
        writer: PdfWriter,
        font: Font,
        colorScheme: PdfColorScheme
    ) {
        val currentDate = Date()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val persianDate = convertGregorianToShamsi(currentDate)
        val persianTime = convertToPersianNumbers(timeFormat.format(currentDate))

        for (pageNumber in 1..writer.pageNumber) {
            val footerTable = PdfPTable(3)
            footerTable.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
            footerTable.setWidths(floatArrayOf(1f, 1.5f, 1f))

            val pageCell = PdfPCell()
            pageCell.border = Rectangle.NO_BORDER
            pageCell.paddingTop = 10f
            pageCell.paddingBottom = 5f
            val pageText = "صفحه ${convertToPersianNumbers(pageNumber.toString())} از ${convertToPersianNumbers(writer.pageNumber.toString())}"
            addPersianText(pageCell, pageText, font, Element.ALIGN_LEFT)
            footerTable.addCell(pageCell)

            val dateTimeCell = PdfPCell()
            dateTimeCell.border = Rectangle.NO_BORDER
            dateTimeCell.paddingTop = 10f
            dateTimeCell.paddingBottom = 5f
            val dateTimeText = "تاریخ ایجاد گزارش: $persianDate - ساعت: $persianTime"
            addPersianText(dateTimeCell, dateTimeText, font, Element.ALIGN_CENTER)
            footerTable.addCell(dateTimeCell)

            val companyCell = PdfPCell()
            companyCell.border = Rectangle.NO_BORDER
            companyCell.paddingTop = 10f
            companyCell.paddingBottom = 5f
            addPersianText(companyCell, "امین تجار خوزستان", font, Element.ALIGN_RIGHT, false)
            footerTable.addCell(companyCell)

            footerTable.writeSelectedRows(
                0, -1,
                document.leftMargin(),
                document.bottomMargin() + 30f,
                writer.directContent
            )
        }
    }
}
