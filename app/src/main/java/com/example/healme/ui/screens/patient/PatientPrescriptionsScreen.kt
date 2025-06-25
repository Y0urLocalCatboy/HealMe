package com.example.healme.ui.screens.patient

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.Prescription
import com.example.healme.ui.theme.Crimson
import com.example.healme.ui.theme.DarkGreen
import com.example.healme.ui.theme.DarkTurquoise
import com.example.healme.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Composable function to display the Patient's Prescriptions screen.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param patientViewModel ViewModel for managing patient's data and operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientPrescriptionsScreen(
    navController: NavController,
    patientViewModel: PatientViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    var prescriptions by remember { mutableStateOf<List<Prescription>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val noDataMessage = stringResource(R.string.patient_prescriptions_no_data)
    val errorLoadingMessage = stringResource(R.string.patient_prescriptions_error_message)

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            isLoading = true
            errorMessage = null
            patientViewModel.getPrescriptionsForPatient(currentUserId) { result ->
                if (result != null) {
                    val updatedPrescriptions = result.map { prescription ->
                        if (prescription.status.equals("Active", ignoreCase = true)) {
                            try {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val issueDate = LocalDate.parse(prescription.dateIssued, formatter)
                                val daysSinceIssued =
                                    ChronoUnit.DAYS.between(issueDate, LocalDate.now())
                                if (daysSinceIssued > 7) {
                                    prescription.copy(status = "Expired")
                                } else {
                                    prescription
                                }
                            } catch (e: Exception) {
                                prescription
                            }
                        } else {
                            prescription
                        }
                    }

                    val statusOrder = mapOf("active" to 1, "expired" to 2, "filled" to 3)
                    prescriptions = updatedPrescriptions.sortedWith(
                        compareBy { statusOrder[it.status.lowercase()] ?: 4 }
                    )
                } else {
                    prescriptions = null
                }
                isLoading = false
                if (result == null) {
                    errorMessage = noDataMessage
                }
            }
        } else {
            isLoading = false
            errorMessage = errorLoadingMessage
        }
    }

    Scaffold{
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.patient_prescriptions_loading))
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: stringResource(R.string.patient_prescriptions_error),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                prescriptions.isNullOrEmpty() -> {
                    Text(
                        text = stringResource(R.string.patient_prescriptions_no_prescriptions),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(prescriptions!!) { prescription ->
                            PrescriptionCard(
                                prescription = prescription,
                                patientViewModel = patientViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function to display a single prescription card.
 *
 * @param prescription The prescription data to display.
 * @param patientViewModel ViewModel for managing patient's data and operations.
 * @param navController Navigation controller for navigating between screens.
 */
@Composable
fun PrescriptionCard(prescription: Prescription,
                     patientViewModel: PatientViewModel,
                     navController: NavController)
{
    val context = LocalContext.current

    val pdfGeneratedToast = stringResource(R.string.patient_prescriptions_pdf_generated_toast)
    val pdfGenerationFailedToast =
        stringResource(R.string.patient_prescriptions_pdf_generation_failed)
    val statusUpdateFailedToast =
        stringResource(R.string.patient_prescriptions_status_update_failed)

    var status by remember(prescription.status) { mutableStateOf(prescription.status) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = prescription.medicationName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(
                label = stringResource(
                    R.string.patient_prescriptions_issued_by,
                    prescription.doctorName
                )
            )
            InfoRow(
                label = stringResource(
                    R.string.patient_prescriptions_date_issued,
                    prescription.dateIssued
                )
            )
            InfoRow(
                label = stringResource(
                    R.string.patient_prescriptions_dosage,
                    prescription.dosage
                )
            )

            if (prescription.instructions.isNotBlank()) {
                InfoRow(
                    label = stringResource(
                        R.string.patient_prescriptions_instructions,
                        prescription.instructions
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            stringResource(
                                R.string.patient_prescriptions_status,
                                prescription.status
                            )
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (prescription.status.lowercase()) {
                            "active" -> DarkGreen.copy(alpha = 0.2f)
                            "filled" -> DarkTurquoise.copy(alpha = 0.2f)
                            "expired" -> Crimson.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        labelColor = when (prescription.status.lowercase()) {
                            "active" -> DarkGreen
                            "filled" -> DarkTurquoise
                            "expired" -> Crimson
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if(prescription.status == "Active") {
                    Button(
                        onClick = {
                            try {
                                val pdfDocument = PdfDocument()
                                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                                val page = pdfDocument.startPage(pageInfo)
                                val canvas = page.canvas
                                val paint = android.graphics.Paint()

                                paint.textSize = 20f
                                paint.isFakeBoldText = true
                                canvas.drawText("Prescription Details", 40f, 50f, paint)
                                paint.isFakeBoldText = false
                                paint.textSize = 14f

                                var yPos = 80f
                                val lineSpacing = 20f

                                canvas.drawText(
                                    "Medication: ${prescription.medicationName}",
                                    40f,
                                    yPos,
                                    paint
                                ); yPos += lineSpacing
                                canvas.drawText(
                                    "Dosage: ${prescription.dosage}",
                                    40f,
                                    yPos,
                                    paint
                                ); yPos += lineSpacing
                                canvas.drawText(
                                    "Instructions: ${prescription.instructions}",
                                    40f,
                                    yPos,
                                    paint
                                ); yPos += lineSpacing
                                canvas.drawText(
                                    "Date Issued: ${prescription.dateIssued}",
                                    40f,
                                    yPos,
                                    paint
                                ); yPos += lineSpacing
                                canvas.drawText(
                                    "Issued by: Dr. ${prescription.doctorName}",
                                    40f,
                                    yPos,
                                    paint
                                ); yPos += lineSpacing
                                canvas.drawText("Status: Active", 40f, yPos, paint)

                                val logoBitmap = BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.logohealme
                                )
                                val scaledLogo = logoBitmap.scale(80, 80, false)
                                canvas.drawBitmap(
                                    scaledLogo,
                                    (pageInfo.pageWidth - scaledLogo.width - 40).toFloat(),
                                    (pageInfo.pageHeight - scaledLogo.height - 40).toFloat(),
                                    null
                                )

                                pdfDocument.finishPage(page)

                                val outputDir =
                                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                val file = File(outputDir, "Prescription_${prescription.id}.pdf")
                                pdfDocument.writeTo(FileOutputStream(file))
                                pdfDocument.close()

                                val fileUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(fileUri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                                Toast.makeText(context, pdfGeneratedToast, Toast.LENGTH_LONG).show()

                                if (!status.equals("Filled", ignoreCase = true)) {
                                    patientViewModel.updatePrescriptionStatus(
                                        prescription.id,
                                        "Filled"
                                    ) { success ->
                                        if (success) {
                                            status = "Filled"
                                        } else {
                                            Toast.makeText(
                                                context,
                                                statusUpdateFailedToast,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }

                                navController.popBackStack()
                                navController.navigate("patient")

                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No PDF viewer found.", Toast.LENGTH_SHORT)
                                    .show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "$pdfGenerationFailedToast: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.patient_prescriptions_generate_pdf_button))
                    }
                }
            }
        }
    }
}

/** Composable function to display a row with label and optional value.
 *
 * @param label The label text to display.
 * @param value The value text to display, if any.
 */
@Composable
private fun InfoRow(label: String, value: String? = null) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}