package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.NetworkSecurityScanner
import com.example.ui.SecurityViewModel
import com.example.ui.theme.*

// --- SMS SECURITY SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSecurityScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    var senderInput by remember { mutableStateOf("+1 800-555-0199") }
    var smsText by remember { mutableStateOf("Urgent: Your account is suspended. Verify immediately at http://bit.ly/claim-bank-auth") }
    val lastResult by viewModel.lastSmsResult.collectAsState()

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("SMS Scam & Phishing Protection", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldDarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = senderInput,
                onValueChange = { senderInput = it },
                label = { Text("SMS Sender ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("sms_sender_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShieldCyanPrimary, unfocusedBorderColor = ShieldBorder)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = smsText,
                onValueChange = { smsText = it },
                label = { Text("SMS Message Content") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("sms_body_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShieldCyanPrimary, unfocusedBorderColor = ShieldBorder)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.scanSms(senderInput, smsText) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("scan_sms_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
            ) {
                Text("SCAN MESSAGE FOR PHISHING", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            lastResult?.let { res ->
                val color = if (res.threatLevel == "SAFE") ShieldSafeGreen else ShieldCriticalRed
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SMS THREAT ASSESSMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShieldTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(res.threatLevel, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                        Text("Risk Score: ${res.riskScore}/100", fontSize = 13.sp, color = color)
                        Spacer(modifier = Modifier.height(10.dp))
                        res.reasons.forEach { r ->
                            Text("• $r", fontSize = 12.sp, color = ShieldTextPrimary, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- NETWORK SECURITY SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSecurityScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val netResult = remember { NetworkSecurityScanner.scanNetwork(context) }

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Network & Wi-Fi Security", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldDarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = ShieldCyanPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(netResult.target, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ShieldTextPrimary)
                            Text("Status: ${netResult.threatLevel}", color = if (netResult.threatLevel == "SAFE") ShieldSafeGreen else ShieldWarningAmber, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Network Indicators:", fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 13.sp)

                    netResult.technicalDetails.forEach { (key, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(key, color = ShieldTextSecondary, fontSize = 13.sp)
                            Text(value, color = ShieldTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// --- EMAIL SECURITY INTEGRATION SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSecurityScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val connectedEmails by viewModel.connectedEmails.collectAsState()
    var emailInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Connected Email Security", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldDarkBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("OAuth Email Protection", fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Connect Gmail or Outlook via official OAuth scopes to continuously audit inbox messages for fake invoices, spoofed headers, and phishing attachments.", color = ShieldTextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("email_connect_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShieldCyanPrimary, unfocusedBorderColor = ShieldBorder)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (emailInput.isNotBlank()) {
                                        viewModel.connectEmail(emailInput, "GMAIL")
                                        emailInput = ""
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
                            ) {
                                Text("Connect Gmail", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (emailInput.isNotBlank()) {
                                        viewModel.connectEmail(emailInput, "OUTLOOK")
                                        emailInput = ""
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldSurfaceVariant)
                            ) {
                                Text("Connect Outlook", color = ShieldCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text("Connected Inboxes", fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 16.sp)
            }

            items(connectedEmails) { integration ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = ShieldCyanPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(integration.email, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                            Text("Provider: ${integration.provider} • Scanned ${integration.scannedMessagesCount} msgs", color = ShieldTextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { viewModel.disconnectEmail(integration.email) }) {
                            Icon(Icons.Default.DisconnectHide, contentDescription = "Disconnect", tint = ShieldCriticalRed)
                        }
                    }
                }
            }
        }
    }
}

// --- PERMISSION AUDITOR SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionAuditorScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val apps by viewModel.installedApps.collectAsState()

    val accessibilityApps = remember(apps) { apps.filter { it.hasAccessibilityPermission } }
    val overlayApps = remember(apps) { apps.filter { it.hasOverlayPermission } }

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Permission Auditor", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldDarkBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Critical Privilege Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ShieldTextPrimary)
                Text("Accessibility Services and Display Overlay permissions are frequently leveraged by Android banking trojans to log keystrokes and display fake login dialogs.", fontSize = 12.sp, color = ShieldTextSecondary)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Accessibility Service Apps (${accessibilityApps.size})", fontWeight = FontWeight.Bold, color = ShieldWarningAmber)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (accessibilityApps.isEmpty()) {
                            Text("✓ No third-party apps holding Accessibility privileges.", color = ShieldSafeGreen, fontSize = 12.sp)
                        } else {
                            accessibilityApps.forEach { a ->
                                Text("• ${a.appName} (${a.packageName})", color = ShieldTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Display Overlay / Draw Over Apps (${overlayApps.size})", fontWeight = FontWeight.Bold, color = ShieldCyanPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (overlayApps.isEmpty()) {
                            Text("✓ No non-system apps holding Overlay permissions.", color = ShieldSafeGreen, fontSize = 12.sp)
                        } else {
                            overlayApps.forEach { a ->
                                Text("• ${a.appName}", color = ShieldTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SETTINGS SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Security Settings", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldDarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingSwitchRow("Daily Automatic Scan", "Automated background scan via Android WorkManager", settings.dailyScanEnabled) {
                viewModel.updateSettings(settings.copy(dailyScanEnabled = it))
            }

            SettingSwitchRow("Real-Time Security Alerts", "Instant system notifications upon threat detection", settings.realTimeAlertsEnabled) {
                viewModel.updateSettings(settings.copy(realTimeAlertsEnabled = it))
            }

            SettingSwitchRow("Spam & Scam Protection", "Intercept suspicious phone numbers & robocalls", settings.spamProtectionEnabled) {
                viewModel.updateSettings(settings.copy(spamProtectionEnabled = it))
            }

            SettingSwitchRow("SMS Phishing Protection", "Scan incoming SMS messages for scam links", settings.smsScanningEnabled) {
                viewModel.updateSettings(settings.copy(smsScanningEnabled = it))
            }

            SettingSwitchRow("Emergency Lockdown Mode", "Enforce high-frequency scanning & max alert state", settings.emergencyLockdownMode) {
                viewModel.updateSettings(settings.copy(emergencyLockdownMode = it))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Security Summary Report Generated.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE SECURITY AUDIT REPORT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ShieldSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 14.sp)
                Text(subtitle, color = ShieldTextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = ShieldCyanPrimary,
                    uncheckedThumbColor = ShieldTextSecondary,
                    uncheckedTrackColor = ShieldSurfaceVariant
                )
            )
        }
    }
}
