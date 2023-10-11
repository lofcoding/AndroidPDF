package com.loc.androidpdf

import android.content.Context
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.Leading
import com.itextpdf.layout.property.Property
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.net.URL

class PdfDocumentsGenerator(
    private val context: Context
) {

    suspend fun generateInvoicePdf(invoice: Invoice, outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize(650f, 700f)).apply {
                setMargins(50f, 13f, 13f, 13f)
                setProperty(
                    Property.LEADING,
                    Leading(Leading.MULTIPLIED, 1f)
                )
            }.setWordSpacing(0f)

            val page = pdfDocument.addNewPage()

            // Invoice number
            val invoiceText = Paragraph("Invoice #${invoice.number}")
                .setBold()
                .setFontSize(32f)

            // Date
            val dateText = createLightTextParagraph(invoice.date)

            // Pay button
            val payLink = Link("Pay ${invoice.price}", PdfAction.createURI(invoice.link))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(DeviceRgb.WHITE)
            val payParagraph = Paragraph()
                .add(payLink)
                .setBackgroundColor(DeviceRgb(0, 92, 230))
                .setPadding(12f)
                .setWidth(100f)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setTextAlignment(TextAlignment.CENTER)

            // Top Section Table
            val topSectionTable = Table(2, false)
                .setMarginLeft(15f)
                .setMarginRight(15f)
                .setWidth(page.pageSize.width - 30f - 26f)
            topSectionTable.addCell(createNoBorderCell(invoiceText))
            topSectionTable.addCell(createNoBorderCell(payParagraph))
            topSectionTable.addCell(createNoBorderCell(dateText))

            // Top Section Separator
            val line = LineSeparator(
                SolidLine().apply {
                    color = DeviceRgb(204, 204, 204)
                }
            ).setMarginTop(20f)

            document.add(topSectionTable)
            document.add(line)


            // People information
            val from = createLightTextParagraph("From")
            val to = createLightTextParagraph("To").setTextAlignment(TextAlignment.RIGHT)
            val fromName =
                createBoldTextParagraph(invoice.from.name).setTextAlignment(TextAlignment.LEFT)
            val toName =
                createBoldTextParagraph(invoice.to.name).setTextAlignment(TextAlignment.RIGHT)
            val fromAddress =
                createLightTextParagraph(invoice.from.address).setTextAlignment(TextAlignment.LEFT)
            val toAddress =
                createLightTextParagraph(invoice.to.address).setTextAlignment(TextAlignment.RIGHT)

            val peopleTable = Table(2, true).apply {
                setMarginLeft(15f)
                setMarginRight(15f)
                setMarginTop(50f)
            }
            peopleTable.addCell(createNoBorderCell(from))
            peopleTable.addCell(createNoBorderCell(to))
            peopleTable.addCell(createNoBorderCell(fromName))
            peopleTable.addCell(createNoBorderCell(toName))
            peopleTable.addCell(createNoBorderCell(fromAddress))
            peopleTable.addCell(createNoBorderCell(toAddress))
            document.add(peopleTable)

            // Products Table
            val description =
                createBoldTextParagraph("Description").setTextAlignment(TextAlignment.LEFT)
            val rate = createBoldTextParagraph("Rate").setTextAlignment(TextAlignment.CENTER)
            val qty = createBoldTextParagraph("QTY").setTextAlignment(TextAlignment.CENTER)
            val subtotal = createBoldTextParagraph("SUBTOTAL").setTextAlignment(TextAlignment.RIGHT)

            val productsTable = Table(4, true).apply {
                setMarginLeft(15f)
                setMarginRight(15f)
                setMarginTop(50f)
            }

            productsTable.addCell(createProductTableCell(description))
            productsTable.addCell(createProductTableCell(rate))
            productsTable.addCell(createProductTableCell(qty))
            productsTable.addCell(createProductTableCell(subtotal))

            val lighterBlack = DeviceRgb(64, 64, 64)
            invoice.products.forEach {
                val pDescription = createBoldTextParagraph(
                    it.description,
                    lighterBlack
                ).setTextAlignment(TextAlignment.LEFT)

                val pRate = createBoldTextParagraph(
                    "$${it.rate}",
                    lighterBlack
                ).setTextAlignment(TextAlignment.CENTER)

                val pQTY = createBoldTextParagraph(
                    it.quantity.toString(),
                    lighterBlack
                ).setTextAlignment(TextAlignment.CENTER)

                val pSubtotal = createBoldTextParagraph(
                    "$${it.rate * it.quantity}",
                    lighterBlack
                ).setTextAlignment(TextAlignment.RIGHT)

                productsTable.addCell(createProductTableCell(pDescription))
                productsTable.addCell(createProductTableCell(pRate))
                productsTable.addCell(createProductTableCell(pQTY))
                productsTable.addCell(createProductTableCell(pSubtotal))
            }

            val grantTotal = Paragraph("Grand Total")
                .setFontColor(DeviceRgb(166, 166, 166))
                .setFontSize(16f)
                .setTextAlignment(TextAlignment.RIGHT)
            val grandTotalCell = Cell(1, 4).add(grantTotal).setBorder(null)
                .setBorderBottom(SolidBorder(DeviceRgb(204, 204, 204), 2f))
                .setPaddingTop(20f)
                .setPaddingBottom(20f)
            productsTable.addCell(grandTotalCell)


            invoice.signatureUrl?.let {
                val imageData = ImageDataFactory.create(URL(it))
                val image = Image(imageData)
                image.setTextAlignment(TextAlignment.LEFT).setWidth(50f).setHeight(50f)
                productsTable.addCell(Cell(1, 2).add(image).setPaddingTop(10f).setBorder(null))
            }

            val totalPrice = createBoldTextParagraph("$${getTotalPrice(invoice.products)}")
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold()
            val totalPriceCell =
                Cell(1, if (invoice.signatureUrl == null) 4 else 2).setPaddingTop(10f)
                    .setBorder(null)
                    .add(totalPrice)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
            productsTable.addCell(totalPriceCell)

            document.add(productsTable)
            document.close()
        }
    }

    private fun getTotalPrice(product: List<Product>): Float {
        var price = 0f
        product.forEach {
            price += it.rate * it.quantity
        }
        return price
    }

    private fun createProductTableCell(paragraph: Paragraph): Cell {
        return Cell().add(paragraph).apply {
            setPaddingBottom(20f)
            setPaddingTop(15f)
            setBorder(null)
            setBorderBottom(SolidBorder(DeviceRgb(204, 204, 204), 1f))
        }
    }

    private fun createLightTextParagraph(text: String): Paragraph {
        val lightTextStyle = Style().apply {
            setFontSize(12f)
            setFontColor(DeviceRgb(166, 166, 166))
        }
        return Paragraph(text).addStyle(lightTextStyle)
    }

    private fun createBoldTextParagraph(text: String, color: Color = DeviceRgb.BLACK): Paragraph {
        val boldTextStyle = Style().apply {
            setFontSize(16f)
            setFontColor(color)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
        }
        return Paragraph(text).addStyle(boldTextStyle)
    }

    private fun createNoBorderCell(paragraph: Paragraph): Cell {
        return Cell().add(paragraph).setBorder(null)
    }


}

