package com.jarvis.assistant.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.services.FloatingBubbleService
import com.jarvis.assistant.services.JarvisBackgroundService
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisSuccess
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToDiagnostics: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as JarvisApp
    val securityMgr = app.securityManager
    val settingsRepo = app.settingsRepository
    val usageMgr = app.usageLimitManager
    val scope = rememberCoroutineScope()

    val providers = listOf(
        Constants.PROVIDER_GROK,
        Constants.PROVIDER_GEMINI,
        Constants.PROVIDER_OPENAI,
        Constants.PROVIDER_OPENROUTER,
        Constants.PROVIDER_LOCAL_LAN
    )

    val fallbackProviders = listOf("None") + providers

    var selectedProvider by remember { mutableStateOf(Constants.PROVIDER_GEMINI) }
    var selectedFallback by remember { mutableStateOf("None") }
    var apiKey by remember { mutableStateOf("") }
    var fallbackApiKey by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    var primaryDropdownExpanded by remember { mutableStateOf(false) }
    var fallbackDropdownExpanded by remember { mutableStateOf(false) }

    var speechRate by remember { mutableFloatStateOf(1.0f) }
    var pitch by remember { mutableFloatStateOf(1.0f) }
    var backgroundServiceEnabled by remember { mutableStateOf(false) }
    var floatingBubbleEnabled by remember { mutableStateOf(false) }

    // Owner lock status
    var isOwnerUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Rewarded Ad Simulation
    val remainingSeconds by usageMgr.remainingSecondsFlow.collectAsState()
    var isWatchingAd by remember { mutableStateOf(false) }
    var adCountdown by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        selectedProvider = settingsRepo.getString(Constants.KEY_AI_PROVIDER, Constants.PROVIDER_GEMINI)
        selectedFallback = settingsRepo.getString(Constants.KEY_AI_PROVIDER_FALLBACK, "None")
        apiKey = securityMgr.getApiKey(selectedProvider)
        fallbackApiKey = securityMgr.getApiKey("${selectedFallback}_fallback")
        speechRate = settingsRepo.getFloat(Constants.KEY_VOICE_SPEECH_RATE, 1.0f)
        pitch = settingsRepo.getFloat(Constants.KEY_VOICE_PITCH, 1.0f)
        backgroundServiceEnabled = settingsRepo.getBoolean(Constants.KEY_BACKGROUND_SERVICE, false)
        floatingBubbleEnabled = settingsRepo.getBoolean(Constants.KEY_FLOATING_BUBBLE, false)
        isOwnerUnlocked = usageMgr.isOwnerUnlocked()
    }

    // Rewarded Ad Countdown Timer
    LaunchedEffect(isWatchingAd) {
        if (isWatchingAd) {
            adCountdown = 5
            while (adCountdown > 0) {
                delay(1000)
                adCountdown--
            }
            usageMgr.rewardAddOneHour()
            isWatchingAd = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SYSTEM SETTINGS",
            color = JarvisCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Manage voice engine, talk quotas, and device preferences",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: AI TALK TIME QUOTA & REWARD AD
        SettingsSectionCard(title = "AI TALK TIME QUOTA") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Free Talk Time Remaining", color = JarvisTextSecondary, fontSize = 12.sp)
                    Text(
                        text = if (isOwnerUnlocked) "Unlimited (Owner Mode Active)" else usageMgr.formatRemainingTime(remainingSeconds),
                        color = if (remainingSeconds > 600 || isOwnerUnlocked) JarvisSuccess else JarvisError,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { isWatchingAd = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Watch Ad (+1 Hour)", color = JarvisBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: Local device commands (Opening apps, YouTube, Google, battery, flashlight, calculator, time) are always 100% FREE and never use your quota.",
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: OWNER API VAULT (HIDDEN BEHIND PIN)
        SettingsSectionCard(title = if (isOwnerUnlocked) "OWNER MASTER CONTROL (MULTI-TIER API)" else "OWNER / ADMIN ACCESS") {
            if (!isOwnerUnlocked) {
                Text(
                    text = "API keys and Multi-Tier provider routing are protected so normal users cannot alter owner credentials.",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showPinDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enter Owner PIN to Unlock APIs", color = JarvisCyan, fontWeight = FontWeight.SemiBold)
                }
            } else {
                // PRIMARY API
                Text("API 1 (Primary AI Provider)", color = JarvisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = primaryDropdownExpanded,
                    onExpandedChange = { primaryDropdownExpanded = !primaryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProvider,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = primaryDropdownExpanded,
                        onDismissRequest = { primaryDropdownExpanded = false }
                    ) {
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider) },
                                onClick = {
                                    selectedProvider = provider
                                    primaryDropdownExpanded = false
                                    scope.launch {
                                        settingsRepo.setString(Constants.KEY_AI_PROVIDER, provider)
                                        apiKey = securityMgr.getApiKey(provider)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("Enter $selectedProvider API Key...", color = JarvisTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCardBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // FALLBACK API
                Text("API 2 (Automatic Fallback Provider)", color = JarvisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("If API 1 runs out of credits or encounters an error, J.A.R.V.I.S. automatically routes to API 2.", color = JarvisTextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = fallbackDropdownExpanded,
                    onExpandedChange = { fallbackDropdownExpanded = !fallbackDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFallback,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fallbackDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = fallbackDropdownExpanded,
                        onDismissRequest = { fallbackDropdownExpanded = false }
                    ) {
                        fallbackProviders.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider) },
                                onClick = {
                                    selectedFallback = provider
                                    fallbackDropdownExpanded = false
                                    scope.launch {
                                        settingsRepo.setString(Constants.KEY_AI_PROVIDER_FALLBACK, provider)
                                        fallbackApiKey = securityMgr.getApiKey("${provider}_fallback")
                                    }
                                }
                            )
                        }
                    }
                }

                if (selectedFallback != "None") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fallbackApiKey,
                        onValueChange = { fallbackApiKey = it },
                        placeholder = { Text("Enter $selectedFallback Backup Key...", color = JarvisTextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            securityMgr.saveApiKey(selectedProvider, apiKey)
                            if (selectedFallback != "None") {
                                securityMgr.saveApiKey("${selectedFallback}_fallback", fallbackApiKey)
                            }
                            testResult = "Owner Keys Securely Encrypted & Saved."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save All Keys", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isTesting = true
                            scope.launch {
                                val provider = app.getActiveAIProvider()
                                val ok = provider?.testConnection() ?: false
                                isTesting = false
                                testResult = if (ok) "Multi-Tier Test Successful!" else "Connection Failed. Check Keys."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isTesting) "Testing..." else "Test Connection")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                usageMgr.lockOwner()
                                isOwnerUnlocked = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lock Vault")
                    }
                }

                testResult?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = msg, color = if (msg.contains("Successful") || msg.contains("Saved")) JarvisSuccess else JarvisError, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: VOICE SYNTHESIS
        SettingsSectionCard(title = "VOICE ENGINE") {
            Text("Speech Rate: ${String.format("%.2f", speechRate)}x", color = JarvisTextPrimary, fontSize = 13.sp)
            Slider(
                value = speechRate,
                onValueChange = {
                    speechRate = it
                    scope.launch { settingsRepo.setFloat(Constants.KEY_VOICE_SPEECH_RATE, it) }
                },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Pitch: ${String.format("%.2f", pitch)}x", color = JarvisTextPrimary, fontSize = 13.sp)
            Slider(
                value = pitch,
                onValueChange = {
                    pitch = it
                    scope.launch { settingsRepo.setFloat(Constants.KEY_VOICE_PITCH, it) }
                },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { app.voiceEngine.speak("Voice synthesis calibrated.", speechRate, pitch) },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Test Voice Synthesis")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: AUTOMATION & OVERLAY SERVICES
        SettingsSectionCard(title = "SERVICES & AUTOMATION") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Background Service", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Maintains J.A.R.V.I.S. active notification", color = JarvisTextSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = backgroundServiceEnabled,
                    onCheckedChange = { enable ->
                        backgroundServiceEnabled = enable
                        scope.launch { settingsRepo.setBoolean(Constants.KEY_BACKGROUND_SERVICE, enable) }
                        val intent = Intent(context, JarvisBackgroundService::class.java)
                        if (enable) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
                        } else context.stopService(intent)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisCardBorder)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Floating J-Orb Bubble", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Always-on-top draggable assistant orb", color = JarvisTextSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = floatingBubbleEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                        } else {
                            floatingBubbleEnabled = enable
                            scope.launch { settingsRepo.setBoolean(Constants.KEY_FLOATING_BUBBLE, enable) }
                            val intent = Intent(context, FloatingBubbleService::class.java)
                            if (enable) context.startService(intent) else context.stopService(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisCardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 5: DIAGNOSTICS & RESET
        SettingsSectionCard(title = "DIAGNOSTICS & CACHE") {
            Button(
                onClick = onNavigateToDiagnostics,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Developer Diagnostics Console")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        app.database.memoryDao().clearAll()
                        app.database.conversationDao().clearAll()
                        app.database.reminderDao().clearAll()
                        app.database.auditLogDao().clearLogs()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisError.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Clear Local Memory & Conversations", color = JarvisTextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Owner PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Owner Verification", color = JarvisCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your Owner Admin PIN to view and configure multi-tier API keys.", color = JarvisTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it },
                        placeholder = { Text("Default PIN: 1234") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    pinError?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, color = JarvisError, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val ok = usageMgr.verifyAndUnlockOwner(enteredPin)
                            if (ok) {
                                isOwnerUnlocked = true
                                showPinDialog = false
                                enteredPin = ""
                                pinError = null
                            } else {
                                pinError = "Incorrect PIN. (Default is 1234)"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("Unlock", color = JarvisBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showPinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = JarvisCard
        )
    }

    // Rewarded Ad Simulation Dialog
    if (isWatchingAd) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Partner Rewarded Ad", color = JarvisCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Watching partner advertisement...", color = JarvisTextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$adCountdown",
                        color = JarvisCyan,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reward: +1 Hour of AI Talk Time", color = JarvisSuccess, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {},
            containerColor = JarvisCard
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = JarvisCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
