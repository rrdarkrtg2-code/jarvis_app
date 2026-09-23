package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.components.OrbState
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary

@Composable
fun OnboardingScreen(onFinishOnboarding: () -> Unit) {
    var userName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(JarvisCard)
                .border(1.dp, Color(0xFFFF2A85).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            JarvisCoreOrb(state = OrbState.SPEAKING, size = 150.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WELCOME TO MAYA AI 💖",
                color = Color(0xFFFF4081),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Aapki sweet aur caring AI girlfriend companion. Main aapke phone ke saare kaam sambhaal sakti hoon aur aapse dher saari baatein kar sakti hoon! ✨",
                color = JarvisTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("What should Maya call you? (Your Name)", fontSize = 12.sp) },
                placeholder = { Text("e.g. Yash or Boss", color = JarvisTextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF4081),
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onFinishOnboarding,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A85)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("START WITH MAYA 🥰", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
