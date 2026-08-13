package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.security.DetailedScanResult
import com.example.ui.ScanState
import com.example.ui.SecurityViewModel
import com.example.ui.theme.*

// --- SCREEN 1: ONBOARDING & PERMISSION CENTER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: SecurityViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("ShieldAI Setup", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShieldDarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                0 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "ShieldAI",
                            tint = ShieldCyanPrimary,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Welcome to ShieldAI", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Next-generation AI-powered cybersecurity & anti-scam protection for your mobile device.",
                            textAlign = TextAlign.Center,
                            color = ShieldTextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = ShieldSafeGreen)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Local Processing First", fontWeight = FontWeight.SemiBold, color = ShieldTextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("File hashes, package analysis, and local rules run 100% on-device.", color = ShieldTextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
                1 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permission Center", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                        Text("ShieldAI requests explicit system permissions to protect you from scam calls, risky apps, and bad files.", color = ShieldTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))

                        val perms = listOf(
                            Triple("Installed App Audit", "Detect risky apps requesting accessibility or overlays", Icons.Default.Apps),
                            Triple("SMS Protection", "Scan incoming SMS for phishing links & scams", Icons.Default.Sms),
                            Triple("Call & Phone Security", "Screen unknown spam & scam numbers", Icons.Default.Phone),
                            Triple("Security Notifications", "Receive instant alerts when threats are found", Icons.Default.Notifications)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(perms) { perm ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(perm.third, contentDescription = null, tint = ShieldCyanPrimary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(perm.first, fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 14.sp)
                                            Text(perm.second, color = ShieldTextSecondary, fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = {
                                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", context.packageName, null)
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ShieldSurfaceVariant),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Grant", fontSize = 12.sp, color = ShieldCyanPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (currentStep < 1) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
            ) {
                Text(if (currentStep < 1) "Continue" else "Get Started", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SCREEN 2: AUTHENTICATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: SecurityViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("user@shieldai.sec") }
    var password by remember { mutableStateOf("ShieldPass123!") }
    var name by remember { mutableStateOf("Secured User") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(containerColor = ShieldDarkBg) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "ShieldAI Auth",
                tint = ShieldCyanPrimary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("ShieldAI Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
            Text("Production-Grade Session & Vault Authentication", fontSize = 13.sp, color = ShieldTextSecondary)

            Spacer(modifier = Modifier.height(28.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldCyanPrimary,
                        unfocusedBorderColor = ShieldBorder,
                        focusedLabelColor = ShieldCyanPrimary,
                        unfocusedLabelColor = ShieldTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldCyanPrimary,
                    unfocusedBorderColor = ShieldBorder,
                    focusedLabelColor = ShieldCyanPrimary,
                    unfocusedLabelColor = ShieldTextSecondary
                ),
                modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldCyanPrimary,
                    unfocusedBorderColor = ShieldBorder,
                    focusedLabelColor = ShieldCyanPrimary,
                    unfocusedLabelColor = ShieldTextSecondary
                ),
                modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
            )

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = ShieldCriticalRed, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password."
                        return@Button
                    }
                    if (isRegister) {
                        viewModel.register(email, password, name) { success ->
                            if (success) onAuthSuccess() else errorMessage = "Registration failed."
                        }
                    } else {
                        viewModel.login(email, password) { success ->
                            if (success) onAuthSuccess() else errorMessage = "Invalid credentials."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
            ) {
                Text(if (isRegister) "Create Account" else "Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    // Google Sign-In Simulation Flow
                    viewModel.login("google_user@shieldai.sec", "GoogleAuthSession") {
                        onAuthSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ShieldTextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, ShieldBorder)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ShieldCyanSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isRegister = !isRegister }) {
                Text(
                    if (isRegister) "Already have an account? Sign In" else "New to ShieldAI? Create Account",
                    color = ShieldCyanPrimary
                )
            }
        }
    }
}

// --- SCREEN 3: MAIN SECURITY DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SecurityViewModel,
    onNavigate: (String) -> Unit
) {
    val score by viewModel.securityScore.collectAsState()
    val scanResults by viewModel.allScanResults.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsState()

    val criticalCount = scanResults.count { it.threatLevel == "CRITICAL" || it.threatLevel == "HIGH" }
    val warningCount = scanResults.count { it.threatLevel == "MEDIUM" || it.threatLevel == "LOW" }

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = ShieldCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ShieldAI Security", fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate("notifications") }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = ShieldCriticalRed) { Text("$unreadNotifs") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ShieldTextPrimary)
                        }
                    }
                    IconButton(onClick = { onNavigate("privacy") }) {
                        Icon(Icons.Default.Lock, contentDescription = "Privacy Center", tint = ShieldTextPrimary)
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECURITY SCORE GAUGE CARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            val gaugeColor = when {
                                score >= 80 -> ShieldSafeGreen
                                score >= 60 -> ShieldWarningAmber
                                else -> ShieldCriticalRed
                            }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = ShieldSurfaceVariant,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = gaugeColor,
                                    startAngle = 135f,
                                    sweepAngle = (score / 100f) * 270f,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$score", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = ShieldTextPrimary)
                                Text("SECURITY SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShieldTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$criticalCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (criticalCount > 0) ShieldCriticalRed else ShieldSafeGreen)
                                Text("Threats", fontSize = 12.sp, color = ShieldTextSecondary)
                            }
                            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = ShieldBorder)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$warningCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (warningCount > 0) ShieldWarningAmber else ShieldSafeGreen)
                                Text("Warnings", fontSize = 12.sp, color = ShieldTextSecondary)
                            }
                            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = ShieldBorder)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Safe", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShieldSafeGreen)
                                Text("Status", fontSize = 12.sp, color = ShieldTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // SCAN BUTTONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.runQuickScan() },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("quick_scan_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QUICK SCAN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.runFullScan() },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("full_scan_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Radar, contentDescription = null, tint = ShieldCyanPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FULL SCAN", color = ShieldCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // SCAN PROGRESS OVERLAY
            if (scanState is ScanState.Scanning) {
                val state = scanState as ScanState.Scanning
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldCyanPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ShieldCyanPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(state.currentTarget, color = ShieldTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                color = ShieldCyanPrimary,
                                trackColor = ShieldSurfaceVariant
                            )
                        }
                    }
                }
            }

            // FEATURE GRID
            item {
                Text("Security Protections", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ShieldTextPrimary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureModuleCard("Phone & Spam", "Block scam calls & spoofed numbers", Icons.Default.Phone, ShieldCyanPrimary, Modifier.weight(1f)) { onNavigate("phone_spam") }
                        FeatureModuleCard("Link & URL Scanner", "Detect phishing domains & scams", Icons.Default.Link, ShieldCyanSecondary, Modifier.weight(1f)) { onNavigate("url_scan") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureModuleCard("Installed App Audit", "Find risky apps & accessibility risks", Icons.Default.Apps, ShieldWarningAmber, Modifier.weight(1f)) { onNavigate("app_audit") }
                        FeatureModuleCard("Permission Auditor", "Control sensitive system access", Icons.Default.AdminPanelSettings, ShieldSafeGreen, Modifier.weight(1f)) { onNavigate("permission_audit") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureModuleCard("SMS Security", "Scan incoming SMS for phishing", Icons.Default.Sms, ShieldCyanPrimary, Modifier.weight(1f)) { onNavigate("sms_security") }
                        FeatureModuleCard("Network Security", "Wi-Fi, DNS & VPN checks", Icons.Default.Wifi, ShieldSafeGreen, Modifier.weight(1f)) { onNavigate("network_security") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureModuleCard("Email Protection", "Scan connected Gmail/Outlook", Icons.Default.Email, ShieldCyanSecondary, Modifier.weight(1f)) { onNavigate("email_security") }
                        FeatureModuleCard("Quarantine Vault", "Isolated high-risk threat items", Icons.Default.Inventory2, ShieldCriticalRed, Modifier.weight(1f)) { onNavigate("quarantine") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FeatureModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ShieldSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag("feature_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = ShieldTextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// --- SCREEN 4: INSTALLED APP & PERMISSION AUDITOR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppAuditScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val apps by viewModel.installedApps.collectAsState()

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Installed App Security Audit", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Found ${apps.size} installed applications.", color = ShieldTextSecondary, fontSize = 13.sp)
            }

            items(apps) { app ->
                val badgeColor = when (app.riskLevel) {
                    "CRITICAL", "HIGH" -> ShieldCriticalRed
                    "MEDIUM" -> ShieldWarningAmber
                    else -> ShieldSafeGreen
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = ShieldCyanPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(app.appName, fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = badgeColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        app.riskLevel,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(app.packageName, color = ShieldTextSecondary, fontSize = 11.sp)
                            if (app.hasAccessibilityPermission) {
                                Text("⚠️ Has Accessibility Privilege", color = ShieldWarningAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", app.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Manage App", tint = ShieldCyanSecondary)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: URL / PHISHING SCANNER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlScannerScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    var urlInput by remember { mutableStateOf("https://g00gle-login-verify.xyz/auth") }
    val lastResult by viewModel.lastUrlResult.collectAsState()

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Link & URL Scanner", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
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
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Paste Suspicious URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("url_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldCyanPrimary,
                    unfocusedBorderColor = ShieldBorder
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.scanUrl(urlInput) }) {
                        Icon(Icons.Default.Search, contentDescription = "Scan URL", tint = ShieldCyanPrimary)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.scanUrl(urlInput) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("scan_url_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
            ) {
                Text("SCAN LINK NOW", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            lastResult?.let { res ->
                val badgeColor = when (res.threatLevel) {
                    "CRITICAL", "HIGH" -> ShieldCriticalRed
                    "MEDIUM" -> ShieldWarningAmber
                    else -> ShieldSafeGreen
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SCAN RESULT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShieldTextSecondary)
                            Surface(
                                color = badgeColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    res.threatLevel,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(res.target, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ShieldTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Risk Score: ${res.riskScore}/100", color = badgeColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Detection Reasons:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ShieldTextPrimary)
                        res.reasons.forEach { reason ->
                            Text("• $reason", color = ShieldTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: PHONE SPAM CHECKER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneSpamCheckerScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("+91 98765 43210") }
    val lastReport by viewModel.lastPhoneReport.collectAsState()

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Phone Spam & Call Screening", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
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
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Enter Phone Number (+ country code)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("phone_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldCyanPrimary,
                    unfocusedBorderColor = ShieldBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.scanPhone(phoneInput) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("check_phone_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyanPrimary)
            ) {
                Text("CHECK NUMBER REPUTATION", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            lastReport?.let { report ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(report.phoneNumber, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                        Text("Country: ${report.country}", fontSize = 13.sp, color = ShieldTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Spam Category: ${report.spamCategory}", fontWeight = FontWeight.Bold, color = if (report.spamCategory == "SAFE") ShieldSafeGreen else ShieldCriticalRed)
                            Text("Risk: ${report.spamRiskPercent}%", fontWeight = FontWeight.Bold, color = ShieldWarningAmber)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reason: ${report.reasoning}", color = ShieldTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: AI SECURITY ASSISTANT CHAT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.aiMessages.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = ShieldCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ShieldAI Assistant", color = ShieldTextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Suggested Prompts:", color = ShieldTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { viewModel.sendAiPrompt("How secure is my phone right now?") },
                            label = { Text("How secure is my phone?", fontSize = 11.sp, color = ShieldCyanPrimary) }
                        )
                        SuggestionChip(
                            onClick = { viewModel.sendAiPrompt("Why is an overlay permission dangerous?") },
                            label = { Text("Overlay permission risk?", fontSize = 11.sp, color = ShieldCyanPrimary) }
                        )
                    }
                }

                items(messages) { msg ->
                    val isUser = msg.sender == "USER"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) ShieldCyanPrimary else ShieldSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                msg.message,
                                color = if (isUser) Color.Black else ShieldTextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ShieldCyanPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ShieldAI is analyzing threat data...", color = ShieldTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask ShieldAI Security Assistant...") },
                    modifier = Modifier.weight(1f).testTag("ai_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldCyanPrimary,
                        unfocusedBorderColor = ShieldBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val txt = inputText
                        inputText = ""
                        viewModel.sendAiPrompt(txt)
                    },
                    modifier = Modifier.testTag("ai_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = ShieldCyanPrimary)
                }
            }
        }
    }
}

// --- SCREEN 8: QUARANTINE VAULT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarantineScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.quarantineItems.collectAsState()

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Quarantine Vault", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShieldSafeGreen, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Quarantine Vault is Empty", fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                        Text("No isolated threats currently held in vault.", color = ShieldTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ShieldSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(item.fileName, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                            Text("Isolated at: ${item.quarantinePath}", fontSize = 11.sp, color = ShieldTextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Reason: ${item.reason}", fontSize = 12.sp, color = ShieldWarningAmber)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 9: PRIVACY CENTER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Privacy Dashboard", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
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
                    Text("Zero Unnecessary Data Retention", fontWeight = FontWeight.Bold, color = ShieldTextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• File hashes and package analysis execute 100% locally on your device.", color = ShieldTextSecondary, fontSize = 13.sp)
                    Text("• Full phone contacts, private emails, and media files are never uploaded.", color = ShieldTextSecondary, fontSize = 13.sp)
                    Text("• Gemini AI requests contain anonymized security indicators only.", color = ShieldTextSecondary, fontSize = 13.sp)
                }
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Exporting ShieldAI security logs & settings...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldSurfaceVariant)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = ShieldCyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXPORT MY DATA", color = ShieldCyanPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.clearHistory()
                    Toast.makeText(context, "All security logs cleared.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCriticalRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DELETE MY SECURITY DATA", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SCREEN 10: NOTIFICATION CENTER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val notifs by viewModel.notifications.collectAsState()

    Scaffold(
        containerColor = ShieldDarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Security Notifications", color = ShieldTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldTextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                        Text("Mark All Read", color = ShieldCyanPrimary)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notifs) { notif ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (notif.isRead) ShieldSurface else ShieldSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.markNotificationRead(notif.id) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (notif.severity == "HIGH") Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (notif.severity == "HIGH") ShieldCriticalRed else ShieldCyanPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(notif.title, fontWeight = FontWeight.Bold, color = ShieldTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notif.message, color = ShieldTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
