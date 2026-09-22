package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.components.OrbState
import com.jarvis.assistant.ui.components.WaveformVisualizer
import com.jarvis.assistant.ui.theme.JarvisAmber
import com.jarvis.assistant.ui.theme.JarvisAmberBright
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisMagenta
import com.jarvis.assistant.ui.theme.JarvisPurple
import com.jarvis.assistant.ui.theme.JarvisSuccess
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenOwnerMenu: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as JarvisApp
    val state by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    val batteryText = app.deviceController.getBatteryLevel().filter { it.isDigit() }.take(2).let { if (it.isNotEmpty()) "$it%" else "85%" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP TACTICAL HUD HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Owner Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(JarvisCard)
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenOwnerMenu)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(JarvisSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BOSS RTGYASH", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            // Central Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "J.A.R.V.I.S.",
                    color = when (state.orbState) {
                        OrbState.THINKING -> JarvisAmberBright
                        OrbState.SPEAKING -> JarvisMagenta
                        else -> JarvisCyan
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "J.A.R.V.I.S. NEURAL CORE",
                    color = JarvisTextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Settings Button
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(JarvisCard)
                    .border(1.dp, JarvisCardBorder, CircleShape)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = JarvisCyan, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. HUD TELEMETRY PILLS (Sleek horizontal row instead of bulky boxes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HudPill(label = "STATUS", value = "ONLINE", valueColor = JarvisSuccess)
            HudPill(label = "BATTERY", value = batteryText, valueColor = JarvisCyan)
            HudPill(label = "TIME", value = timeFormat, valueColor = JarvisTextPrimary)
            HudPill(label = "MODE", value = "OFFLINE", valueColor = JarvisAmberBright)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. CORE HOLOGRAPHIC ORB DISPLAY
        when (state.orbState) {
            OrbState.THINKING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.THINKING, size = 220.dp)
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(JarvisCard)
                            .border(1.dp, JarvisAmber.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("> Processing request locally...", color = JarvisAmberBright, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text("> Executing command...", color = JarvisTextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                color = JarvisAmberBright,
                                trackColor = JarvisCardBorder
                            )
                        }
                    }
                }
            }

            OrbState.SPEAKING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.SPEAKING, audioLevel = state.audioLevel, size = 220.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    WaveformVisualizer(
                        audioLevel = state.audioLevel,
                        isActive = true,
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = state.lastResponse.ifEmpty { "At your service, boss." },
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(JarvisMagenta.copy(alpha = 0.2f))
                            .border(2.dp, JarvisMagenta, CircleShape)
                            .clickable { viewModel.stopSpeaking() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = JarvisMagenta, modifier = Modifier.size(24.dp))
                    }
                }
            }

            OrbState.LISTENING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.LISTENING, audioLevel = state.audioLevel, size = 220.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    WaveformVisualizer(
                        audioLevel = state.audioLevel,
                        isActive = true,
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Listening... Speak now, boss", color = JarvisCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.onMicrophoneClicked() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = JarvisTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel", color = JarvisTextPrimary, fontSize = 12.sp)
                    }
                }
            }

            else -> {
                // IDLE STATE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.IDLE, size = 220.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("J.A.R.V.I.S. READY", color = JarvisCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp)
                    Text("\"How may I assist you today, Boss?\"", color = JarvisTextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pulse Voice Mic Trigger
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(JarvisBlue)
                            .border(2.5.dp, JarvisCyan, CircleShape)
                            .clickable { viewModel.onMicrophoneClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Speak", tint = JarvisTextPrimary, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. ACTION COMMAND DOCK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionDockButton("Voice", Icons.Default.Mic) { viewModel.onMicrophoneClicked() }
            ActionDockButton("Chat", Icons.Default.Chat, onNavigateToChat)
            ActionDockButton("Apps", Icons.Default.Apps, onNavigateToTools)
            ActionDockButton("Screen", Icons.Default.Visibility) { viewModel.processQuery("see screen") }
            ActionDockButton("Tools", Icons.Default.Widgets, onOpenOwnerMenu)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. QUICK COMMAND INPUT
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = { Text("Ask J.A.R.V.I.S. or control screen...", color = JarvisTextSecondary, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                if (textInput.isNotEmpty()) {
                    IconButton(onClick = {
                        val q = textInput
                        textInput = ""
                        viewModel.processQuery(q)
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = JarvisCyan)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisCardBorder,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                focusedContainerColor = JarvisCard,
                unfocusedContainerColor = JarvisCard
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 6. FOOTER BRAND
        Text(
            text = "INTELLIGENCE • ASSISTANCE • AUTONOMY",
            color = JarvisTextSecondary.copy(alpha = 0.5f),
            fontSize = 9.sp,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun HudPill(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = JarvisTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionDockButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = title, tint = JarvisCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = JarvisTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
