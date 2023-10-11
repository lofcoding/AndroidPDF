package com.loc.androidpdf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.List
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.ListNumberingType
import com.itextpdf.layout.property.TextAlignment
import com.loc.androidpdf.ui.theme.AndroidPDFTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pdfDocumentsGenerator = PdfDocumentsGenerator(this)
        setContent {
            AndroidPDFTheme {
                // A surface container using the 'background' color from the theme
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val createDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                    onResult = {
                        it.data?.data?.let {
                            context.contentResolver.openOutputStream(it)?.let {
                                scope.launch {
                                    pdfDocumentsGenerator.generateInvoicePdf(
                                        outputStream = it,
                                        invoice = Invoice(
                                            number = 7877859L,
                                            price = 885.0f,
                                            link = "https://www.google.com",
                                            date = "Tue 4th Aug, 2020",
                                            from = PersonInfo(
                                                name = "Leslie Alexander",
                                                address = "2972 Westheimer Rd. Santa Ana, IIlinois 85486"
                                            ),
                                            to = PersonInfo(
                                                name = "Marvin McKinney",
                                                address = "2972 Westheimer Rd. Santa Ana, IIlinois 85486"
                                            ),
                                            listOf(
                                                Product(
                                                    description = "Dashboard Design",
                                                    rate = 779.58f,
                                                    quantity = 1
                                                ),
                                                Product(
                                                    description = "Logo Design",
                                                    rate = 106.58f,
                                                    quantity = 2
                                                ),
                                                Product(
                                                    description = "Thumbnail Design",
                                                    rate = 22.3f,
                                                    quantity = 1
                                                ),
                                            ),
                                            signatureUrl = "https://i.ibb.co/JqN6cFN/download.png"
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = {
                            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_TITLE,"invoice.pdf")
                            }.also {
                                createDocumentLauncher.launch(it)
                            }
                        }) {
                            Text(text = "Save")
                        }
                    }
                }
            }
        }
    }
}












