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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
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
    val state by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER: Avatar + Maya + Status + Time + Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(JarvisCard).border(1.5.dp, JarvisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = JarvisCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Maya", color = JarvisTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (state.isMicListening) "● Listening..." else "● Systems operational. Standing by",
                        color = if (state.isMicListening) JarvisCyan else JarvisSuccess,
                        fontSize = 10.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeFormat, color = JarvisTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // QUANTUM CYBER REACTOR ORB
        JarvisCoreOrb(state = state.orbState, audioLevel = state.audioLevel, size = 210.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // CONVERSATION DISPLAY CARD (From Reference Video)
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(JarvisCard).border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp)).padding(14.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CONVERSATION", color = JarvisCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(timeFormat, color = JarvisTextSecondary, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (state.lastResponse.isNotEmpty()) state.lastResponse else "Systems operational. Ready for your command, Boss.",
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3-PILL ACTION BAR (From Reference Video)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(JarvisCard).border(1.dp, JarvisSuccess.copy(alpha = 0.6f), RoundedCornerShape(20.dp)).padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(JarvisSuccess))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ONLINE", color = JarvisSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier.weight(1.1f).clip(RoundedCornerShape(20.dp)).background(JarvisCard).border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp)).clickable { viewModel.processQuery("What is on my screen?") }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = "Vision", tint = JarvisCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VISION ON", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(JarvisCard).border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp)).clickable(onClick = onNavigateToChat).padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = JarvisCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HISTORY", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // BOTTOM MIC & COMMAND INPUT BAR
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask Maya or say a command...", color = JarvisTextSecondary, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisCard,
                    unfocusedContainerColor = JarvisCard
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(if (state.isMicListening) JarvisCyan else JarvisCard).border(1.5.dp, JarvisCyan, CircleShape).clickable {
                    if (textInput.isNotBlank()) {
                        val q = textInput
                        textInput = ""
                        viewModel.processQuery(q)
                    } else {
                        viewModel.onMicrophoneClicked()
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (textInput.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = if (state.isMicListening) JarvisBackground else JarvisCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
