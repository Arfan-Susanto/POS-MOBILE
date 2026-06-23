package com.possaas.FiturKasir.PaymentSuccess

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.possaas.R
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDFExportHelper(private val context: Context) {

    fun generatePDF(data: TransactionDataParcel): String? {
        return try {
            val fileName = "Receipt_${data.invoiceId}.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            addHeader(document, data)
            addInvoiceInfo(document, data)
            addTransactionDetails(document, data)
            addMenuTable(document, data)
            addSummary(document, data)

            document.close()

            Toast.makeText(context, "PDF berhasil dibuat: $fileName", Toast.LENGTH_SHORT).show()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun addHeader(document: Document, data: TransactionDataParcel) {
        val headerTable = Table(floatArrayOf(100f, 200f))
        headerTable.useAllAvailableWidth()

        try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.logo_pos)
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                val imageData = ImageDataFactory.create(stream.toByteArray())
                val image = Image(imageData)
                image.scaleAbsolute(150f, 60f)
                val logoCell = Cell()
                logoCell.add(image)
                logoCell.setTextAlignment(TextAlignment.CENTER)
                headerTable.addCell(logoCell)
            } else {
                val logoCell = Cell()
                logoCell.add(Paragraph("POS"))
                logoCell.setTextAlignment(TextAlignment.CENTER)
                headerTable.addCell(logoCell)
            }
        } catch (e: Exception) {
            val logoCell = Cell()
            logoCell.add(Paragraph("POS"))
            logoCell.setTextAlignment(TextAlignment.CENTER)
            headerTable.addCell(logoCell)
        }

        val invoiceCell = Cell()
        invoiceCell.add(
            Paragraph("ID Invoice\n${data.invoiceId}")
                .setTextAlignment(TextAlignment.RIGHT)
        )
        headerTable.addCell(invoiceCell)

        document.add(headerTable)
        document.add(Paragraph("\n"))
    }

    private fun addInvoiceInfo(document: Document, data: TransactionDataParcel) {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        val dateString = dateFormat.format(Date(data.timestamp))

        val infoTable = Table(floatArrayOf(100f, 200f))
        infoTable.useAllAvailableWidth()

        val dateCell1 = Cell()
        dateCell1.add(Paragraph("Tanggal").setBold())
        infoTable.addCell(dateCell1)

        val dateCell2 = Cell()
        dateCell2.add(Paragraph(dateString))
        infoTable.addCell(dateCell2)

        val nameCell1 = Cell()
        nameCell1.add(Paragraph("Nama Lengkap").setBold())
        infoTable.addCell(nameCell1)

        val nameCell2 = Cell()
        nameCell2.add(Paragraph(data.customerName))
        infoTable.addCell(nameCell2)

        val typeCell1 = Cell()
        typeCell1.add(Paragraph("Tipe Order").setBold())
        infoTable.addCell(typeCell1)

        val typeCell2 = Cell()
        val orderTypeText = if (data.orderType == "dine_in") {
            "Dine In, Meja ${data.tableNumber}"
        } else {
            "Take Away"
        }
        typeCell2.add(Paragraph(orderTypeText))
        infoTable.addCell(typeCell2)

        val paymentCell1 = Cell()
        paymentCell1.add(Paragraph("Metode Pembayaran").setBold())
        infoTable.addCell(paymentCell1)

        val paymentCell2 = Cell()
        paymentCell2.add(Paragraph(data.paymentMethod))
        infoTable.addCell(paymentCell2)

        document.add(infoTable)
        document.add(Paragraph("\n"))
    }

    private fun addTransactionDetails(document: Document, data: TransactionDataParcel) {
        val detailTable = Table(floatArrayOf(150f, 150f))
        detailTable.useAllAvailableWidth()

        val statusCell1 = Cell()
        statusCell1.add(Paragraph("Status Pembayaran").setBold())
        detailTable.addCell(statusCell1)

        val statusCell2 = Cell()
        statusCell2.add(Paragraph("Berhasil"))
        detailTable.addCell(statusCell2)

        document.add(detailTable)
        document.add(Paragraph("\n"))
    }

    private fun addMenuTable(document: Document, data: TransactionDataParcel) {
        val table = Table(floatArrayOf(40f, 100f, 50f, 90f))
        table.useAllAvailableWidth()

        val headers = listOf("Nomor", "Nama Menu", "Jumlah", "Harga")
        val greenCell = DeviceRgb(57, 211, 33)

        for (header in headers) {
            val cell = Cell()
            cell.add(Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
            cell.setBackgroundColor(greenCell)
            table.addCell(cell)
        }
        val rupiah = NumberFormat.getInstance(Locale("id", "ID"))
        data.menuItems.forEachIndexed { index, item ->
            table.addCell(Cell().add(Paragraph("${index + 1}")))
            table.addCell(Cell().add(Paragraph(item.nama)))
            table.addCell(Cell().add(Paragraph(item.jumlah.toString())))
            table.addCell(Cell().add(Paragraph("Rp ${rupiah.format(item.hargaSatuan * item.jumlah)}")))
        }
        val totalCell1 = Cell()
        totalCell1.add(Paragraph("Total Harga").setBold().setFontColor(ColorConstants.WHITE))
        totalCell1.setBackgroundColor(greenCell)
        table.addCell(totalCell1)

        repeat(2) {
            val emptyCell = Cell()
            emptyCell.setBackgroundColor(greenCell)
            table.addCell(emptyCell)
        }

        val totalCell2 = Cell()
        totalCell2.add(Paragraph("Rp ${rupiah.format(data.totalPrice)}").setBold().setFontColor(ColorConstants.WHITE))
        totalCell2.setBackgroundColor(greenCell)
        table.addCell(totalCell2)

        val paidCell1 = Cell()
        paidCell1.add(Paragraph("Terbayar").setBold().setFontColor(ColorConstants.WHITE))
        paidCell1.setBackgroundColor(greenCell)
        table.addCell(paidCell1)

        repeat(2) {
            val emptyCell = Cell()
            emptyCell.setBackgroundColor(greenCell)
            table.addCell(emptyCell)
        }

        val paidCell2 = Cell()
        paidCell2.add(Paragraph("Rp ${rupiah.format(data.cashAmount)}").setBold().setFontColor(ColorConstants.WHITE))
        paidCell2.setBackgroundColor(greenCell)
        table.addCell(paidCell2)

        val changeCell1 = Cell()
        changeCell1.add(Paragraph("Kembalian").setBold().setFontColor(ColorConstants.WHITE))
        changeCell1.setBackgroundColor(greenCell)
        table.addCell(changeCell1)

        repeat(2) {
            val emptyCell = Cell()
            emptyCell.setBackgroundColor(greenCell)
            table.addCell(emptyCell)
        }

        val change = data.cashAmount - data.totalPrice
        val changeCell2 = Cell()
        changeCell2.add(Paragraph("Rp ${rupiah.format(change)}").setBold().setFontColor(ColorConstants.WHITE))
        changeCell2.setBackgroundColor(greenCell)
        table.addCell(changeCell2)

        document.add(table)
    }

    private fun addSummary(document: Document, data: TransactionDataParcel) {
        document.add(Paragraph("\n"))
        document.add(
            Paragraph("Terima kasih telah berbelanja!")
                .setTextAlignment(TextAlignment.CENTER)
        )
    }
}