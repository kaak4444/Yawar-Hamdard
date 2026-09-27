package com.example.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClaimEntity
import com.example.data.model.AppLanguage
import com.example.ui.common.ClaimStatusBadge
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.ChipBg
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun ClaimsScreen(
    viewModel: YawarViewModel,
    modifier: Modifier = Modifier
) {
    val claims by viewModel.claims.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showNewClaimDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Claims & Coverage Desk",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Cashless Pre-Authorization & Reimbursement Management",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = YawarBlue,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Tab bar: Active Claims (2) | Cashless Pre-Auth | Guidance Checklist
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = YawarNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ClinicalGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "My Claims (${claims.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) YawarNavy else Slate
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Claims Guidance",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) YawarNavy else Slate
                        )
                    }
                )
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedTab == 0) {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(claims) { claim ->
                            ClaimCard(claim = claim, language = language)
                        }
                    }
                } else {
                    ClaimsGuidanceView()
                }
            }
        }

        // FAB to submit claim
        FloatingActionButton(
            onClick = { showNewClaimDialog = true },
            containerColor = ClinicalGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 16.dp, 16.dp, 80.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Submit Claim")
        }

        if (showNewClaimDialog) {
            NewClaimSubmissionDialog(
                onDismiss = { showNewClaimDialog = false },
                onSubmit = { type, provider, amount, notes ->
                    viewModel.submitNewClaim(type, provider, amount, notes)
                    showNewClaimDialog = false
                }
            )
        }
    }
}

@Composable
fun ClaimCard(
    claim: ClaimEntity,
    language: AppLanguage
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = claim.id,
                        fontWeight = FontWeight.Bold,
                        color = YawarNavy,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${claim.claimType}",
                        color = Ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ClaimStatusBadge(status = claim.status, language = language)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = claim.providerOrHospital,
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 16.sp
            )

            Text(
                text = "Submitted: ${claim.submissionDate} • Patient: ${claim.patientName}",
                color = Slate,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(PaleBlue)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Invoiced Amount", color = Slate, fontSize = 11.sp)
                    Text("${claim.invoiceAmountAf.toInt()} AFN", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Approved Settlement", color = Slate, fontSize = 11.sp)
                    Text(
                        text = if (claim.approvedAmountAf > 0) "${claim.approvedAmountAf.toInt()} AFN" else "Under Review",
                        color = if (claim.approvedAmountAf > 0) DeepGreen else YawarBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (claim.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Coordinator Notes: ${claim.remarks}",
                    color = Slate,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ClaimsGuidanceView() {
    LazyColumn(
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Cashless Settlement Checklist",
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "For pre-approved treatments at partner hospitals, YHCS settles costs directly.",
                        color = Slate,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GuidanceStepItem(
                        step = "1",
                        title = "Obtain Pre-Approval",
                        desc = "Submit your doctor's treatment order at least 24 hours prior to scheduled admission."
                    )
                    GuidanceStepItem(
                        step = "2",
                        title = "Present Member ID Card",
                        desc = "Show your digital or physical YHCS Membership ID at the hospital admission desk."
                    )
                    GuidanceStepItem(
                        step = "3",
                        title = "Direct Invoicing",
                        desc = "The hospital bills YHCS directly; you only settle non-covered personal co-pays."
                    )
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Reimbursement Document Checklist",
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Upload complete documentation within 30 days of medical service.",
                        color = Slate,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DocumentCheckItem("Itemized hospital or clinic invoice with official stamp")
                    DocumentCheckItem("Treating doctor's signed clinical summary and prescription")
                    DocumentCheckItem("Diagnostic laboratory and radiology investigation reports")
                    DocumentCheckItem("Official pharmacy receipts showing stamped pricing")
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(PaleBlue)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Claims Assistance", tint = YawarNavy, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Dedicated Claims Support Desk", fontWeight = FontWeight.Bold, color = YawarNavy, fontSize = 13.sp)
                        Text("callcenter@yawarconsulting.com • Subject: Claims Assistance", color = YawarBlue, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GuidanceStepItem(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(PaleGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(step, fontWeight = FontWeight.Bold, color = DeepGreen, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
            Text(desc, fontSize = 11.sp, color = Slate)
        }
    }
}

@Composable
fun DocumentCheckItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Check", tint = ClinicalGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Ink)
    }
}

@Composable
fun NewClaimSubmissionDialog(
    onDismiss: () -> Unit,
    onSubmit: (type: String, provider: String, amount: Double, notes: String) -> Unit
) {
    var claimType by remember { mutableStateOf("Reimbursement") }
    var provider by remember { mutableStateOf("FMIC Tertiary Care Hospital") }
    var amountText by remember { mutableStateOf("4500") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Submit Claim / Cashless Pre-Auth", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column {
                Text("Select Request Type", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { claimType = "Cashless Pre-Auth" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (claimType == "Cashless Pre-Auth") ClinicalGreen else ChipBg,
                            contentColor = if (claimType == "Cashless Pre-Auth") Color.White else Ink
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("Cashless", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { claimType = "Reimbursement" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (claimType == "Reimbursement") ClinicalGreen else ChipBg,
                            contentColor = if (claimType == "Reimbursement") Color.White else Ink
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("Reimbursement", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Hospital or Clinic Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Estimated or Invoiced Amount (AFN)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Remarks / Diagnosis Summary") },
                    placeholder = { Text("e.g. Outpatient consultation & cardiac echo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Notification will be sent to callcenter@yawarconsulting.com",
                    fontSize = 11.sp,
                    color = YawarBlue
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onSubmit(claimType, provider, amount, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
