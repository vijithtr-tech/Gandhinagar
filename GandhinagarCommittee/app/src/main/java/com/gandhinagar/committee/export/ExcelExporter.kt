package com.gandhinagar.committee.export

import android.content.Context
import com.gandhinagar.committee.data.AppDatabase
import com.gandhinagar.committee.data.Calc
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    private val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    private fun d(ts: Long?): String = ts?.let { df.format(Date(it)) } ?: "-"

    /** Builds the xlsx file and returns it. Saved under app external files /exports. */
    suspend fun export(context: Context): File {
        val db = AppDatabase.get(context)
        val members = db.memberDao().getAll()
        val loans = db.loanDao().getAll()
        val settings = db.settingsDao().get()
        val now = System.currentTimeMillis()

        val wb = XSSFWorkbook()

        val header = wb.createCellStyle().apply {
            val f = wb.createFont().apply { bold = true }
            setFont(f)
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Summary sheet
        val totalOpenPrincipal = loans.filter { !it.isClosed }.sumOf { it.principal }
        val opening = settings?.openingBalance ?: 0.0
        wb.createSheet("Summary").let { s ->
            var r = 0
            s.createRow(r++).createCell(0).setCellValue("GANDHINAGAR COMMITTEE")
            s.createRow(r++).apply { createCell(0).setCellValue("Generated"); createCell(1).setCellValue(df.format(Date(now))) }
            s.createRow(r++).apply { createCell(0).setCellValue("Opening Balance"); createCell(1).setCellValue(opening) }
            s.createRow(r++).apply { createCell(0).setCellValue("Total Open Loans"); createCell(1).setCellValue(totalOpenPrincipal) }
            s.createRow(r++).apply { createCell(0).setCellValue("Current Balance"); createCell(1).setCellValue(opening - totalOpenPrincipal) }
            s.createRow(r++).apply { createCell(0).setCellValue("Loan Limit (per member)"); createCell(1).setCellValue(settings?.loanLimit ?: 10000.0) }
        }

        // Members sheet
        wb.createSheet("Members").let { s ->
            val h = s.createRow(0)
            listOf("ID","Name","Phone","Membership Amount","Due Date","Paid","Paid Date","Penalty","Total Due")
                .forEachIndexed { i, t -> h.createCell(i).apply { setCellValue(t); cellStyle = header } }
            members.forEachIndexed { idx, m ->
                val row = s.createRow(idx + 1)
                row.createCell(0).setCellValue(m.id.toDouble())
                row.createCell(1).setCellValue(m.name)
                row.createCell(2).setCellValue(m.phone)
                row.createCell(3).setCellValue(m.membershipAmount)
                row.createCell(4).setCellValue(d(m.membershipDueDate))
                row.createCell(5).setCellValue(if (m.membershipPaid) "PAID" else "UNPAID")
                row.createCell(6).setCellValue(d(m.membershipPaidDate))
                row.createCell(7).setCellValue(Calc.membershipPenalty(m, now))
                row.createCell(8).setCellValue(Calc.membershipTotalDue(m, now))
            }
        }

        // Loans sheet
        val byId = members.associateBy { it.id }
        wb.createSheet("Loans").let { s ->
            val h = s.createRow(0)
            listOf("Loan ID","Member","Principal","Interest %","Monthly Interest","Start Date","Close Date","Status","Interest Accrued","Note")
                .forEachIndexed { i, t -> h.createCell(i).apply { setCellValue(t); cellStyle = header } }
            loans.forEachIndexed { idx, l ->
                val row = s.createRow(idx + 1)
                row.createCell(0).setCellValue(l.id.toDouble())
                row.createCell(1).setCellValue(byId[l.memberId]?.name ?: "#${l.memberId}")
                row.createCell(2).setCellValue(l.principal)
                row.createCell(3).setCellValue(l.interestRatePercent)
                row.createCell(4).setCellValue(l.monthlyInterest)
                row.createCell(5).setCellValue(d(l.startDate))
                row.createCell(6).setCellValue(d(l.closeDate))
                row.createCell(7).setCellValue(if (l.isClosed) "CLOSED" else "OPEN")
                row.createCell(8).setCellValue(Calc.loanInterestAccrued(l, now))
                row.createCell(9).setCellValue(l.note)
            }
        }

        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date(now))
        val file = File(dir, "committee_backup_$stamp.xlsx")
        file.outputStream().use { wb.write(it) }
        wb.close()
        return file
    }
}
