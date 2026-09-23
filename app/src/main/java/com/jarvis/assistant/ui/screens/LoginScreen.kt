package com.jarvis.assistant.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.components.OrbState
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as JarvisApp
    val usageMgr = app.usageLimitManager
    val scope = rememberCoroutineScope()

    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        JarvisCoreOrb(state = OrbState.IDLE, size = 160.dp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Maya SECURITY",
            color = JarvisCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Enter Owner PIN for unrestricted access",
            color = JarvisTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisCard)
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 8) {
                            pinText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Owner PIN (Default: 1234)", color = JarvisTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCardBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = JarvisError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val ok = usageMgr.verifyAndUnlockOwner(pinText)
                            if (ok) {
                                onLoginSuccess(true)
                            } else {
                                errorMessage = "Incorrect PIN. Try default: 1234"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = JarvisBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock as Owner", color = JarvisBackground, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            usageMgr.lockOwner()
                            onLoginSuccess(false)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisTextSecondary)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Guest", tint = JarvisTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue as Guest")
                }
            }
        }
    }
}
