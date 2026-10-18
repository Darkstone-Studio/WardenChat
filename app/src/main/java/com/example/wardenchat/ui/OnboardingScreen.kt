package com.example.wardenchat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wardenchat.ui.theme.AccentBlue
import com.example.wardenchat.ui.theme.DangerRed
import com.example.wardenchat.ui.theme.DarkBackground
import com.example.wardenchat.ui.theme.DarkBorder
import com.example.wardenchat.ui.theme.DarkSurfaceVariant
import com.example.wardenchat.ui.theme.TextDark
import com.example.wardenchat.ui.theme.TextPrimary
import com.example.wardenchat.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    viewModel: LinkUpViewModel,
    onComplete: () -> Unit
) {
    // Intercept back button so user cannot exit onboarding without setting a username
    BackHandler(enabled = true) {}

    var nameInput by remember { mutableStateOf("") }
    var hasInteracted by remember { mutableStateOf(false) }

    val trimmedName = nameInput.trim()
    val isValid = LinkUpViewModel.isValidDisplayName(nameInput)
    val showError = hasInteracted && !isValid

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title / Logo Branding
            Text(
                text = "Warden Chat",
                style = TextStyle(
                    color = AccentBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hoş geldin",
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Karşı tarafın seni göreceği adı seç. Gerçek adını yazmak zorunda değilsin.",
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    if (it.length <= 25) {
                        nameInput = it
                        hasInteracted = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Kullanıcı adı",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 15.sp
                    )
                },
                singleLine = true,
                isError = showError,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder,
                    errorBorderColor = DangerRed,
                    cursorColor = AccentBlue
                )
            )

            if (showError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kullanıcı adı en az 2 karakter olmalıdır.",
                    style = TextStyle(
                        color = DangerRed,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    hasInteracted = true
                    if (isValid) {
                        viewModel.saveDisplayName(trimmedName)
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = TextDark,
                    disabledContainerColor = DarkSurfaceVariant,
                    disabledContentColor = TextSecondary.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "Devam Et",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
