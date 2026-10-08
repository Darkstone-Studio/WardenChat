package com.example.wardenchat.ui

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wardenchat.ui.theme.AccentBlue
import com.example.wardenchat.ui.theme.AccentBlue15Alpha
import com.example.wardenchat.ui.theme.DangerRed
import com.example.wardenchat.ui.theme.DarkBackground
import com.example.wardenchat.ui.theme.DarkBorder
import com.example.wardenchat.ui.theme.DarkSurface
import com.example.wardenchat.ui.theme.DarkSurfaceVariant
import com.example.wardenchat.ui.theme.TextDark
import com.example.wardenchat.ui.theme.TextPrimary
import com.example.wardenchat.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: LinkUpViewModel
) {
    val myId by viewModel.myId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showRegenerateDialog by remember { mutableStateOf(false) }
    var showVanityModal by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val avatarInitial = if (myId.isNotEmpty()) myId.first().toString() else "W"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title
            Text(
                text = "PROFİL VE KİMLİK",
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 1. Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(AccentBlue)
                    .border(2.dp, AccentBlue.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarInitial,
                    style = TextStyle(
                        color = TextDark,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // 2. 9-digit ID Code
            Text(
                text = myId.ifEmpty { "--- --- ---" },
                style = TextStyle(
                    color = AccentBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            )

            // Action Buttons (Copy & Regenerate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(myId))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("ID kopyalandı")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopyala",
                        modifier = Modifier.size(16.dp),
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Kopyala", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { showRegenerateDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yeni Kimlik",
                        modifier = Modifier.size(16.dp),
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Yeni Kimlik", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // Gizliliği Destekle
                    SettingsItem(
                        icon = Icons.Default.Favorite,
                        iconTint = DangerRed,
                        title = "Gizliliği Destekle",
                        subtitle = "Bağış yaparak projeye katkıda bulunun",
                        onClick = {
                            val customTabsIntent = CustomTabsIntent.Builder().build()
                            customTabsIntent.launchUrl(
                                context,
                                Uri.parse("https://github.com/sponsors/mazyLeyn")
                            )
                        }
                    )

                    // Özel ID Al
                    SettingsItem(
                        icon = Icons.Default.Star,
                        iconTint = AccentBlue,
                        title = "Özel ID Al",
                        subtitle = "Kişiselleştirilmiş vanity ID oluştur",
                        onClick = { showVanityModal = true }
                    )

                    // Hakkında
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = TextSecondary,
                        title = "Hakkında",
                        subtitle = "Warden Chat v1.5.1 · Anonim Mesajlaşma",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Regenerate ID
    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Yeni Kimlik Oluştur",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            },
            text = {
                Text(
                    text = "Yeni kimlik oluşturursan mevcut bağlantıların sıfırlanır, emin misin?",
                    style = TextStyle(fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegenerateDialog = false
                        viewModel.generateNewId()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Yeni kimlik oluşturuldu")
                        }
                    }
                ) {
                    Text(text = "Evet", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateDialog = false }) {
                    Text(text = "İptal", color = AccentBlue)
                }
            }
        )
    }

    // Vanity Code Modal
    if (showVanityModal) {
        VanityCodeModal(onDismiss = { showVanityModal = false })
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Warden Chat Hakkında",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            },
            text = {
                Text(
                    text = "Warden Chat, telefon numarası veya e-posta gerektirmeden, her kullanıcıya rastgele bir kimlik kodu veren anonim bir mesajlaşma uygulamasıdır. Mesajlar yalnızca karşı tarafa ulaşana kadar geçici olarak bekler, teslim edildiği anda sunucudan silinir. Hiçbir kayıt, hiçbir kalıcı iz tutulmaz.",
                    style = TextStyle(fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(text = "Kapat", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Git",
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun VanityCodeModal(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Özel ID (Vanity Code)",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentBlue15Alpha)
                        .border(1.dp, AccentBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Yakında",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                    )
                }
            }
        },
        text = {
            Text(
                text = "Standart kimlik kodun rastgele üretilir (örn. 45H-68Y-U8T). Kendi seçeceğin özel bir kod (örn. BOSS-007) alma seçeneği opsiyonel bağış veya üyelik sonrası açılacak bir özelliktir.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Kapat",
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
