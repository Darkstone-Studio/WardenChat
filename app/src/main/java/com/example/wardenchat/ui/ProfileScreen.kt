package com.example.wardenchat.ui

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.wardenchat.data.AppConstants
import com.example.wardenchat.data.MailboxCheckWorker
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
    val displayName by viewModel.displayName.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showRegenerateDialog by remember { mutableStateOf(false) }
    var showVanityModal by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }

    val avatarInitial = if (displayName.isNullOrEmpty()) {
        if (myId.isNotEmpty()) myId.first().toString() else "W"
    } else {
        displayName!!.first().uppercaseChar().toString()
    }

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

            // Profile Header Card (Avatar + Username + ID Code in single Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar circle (64dp)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                        .border(1.5.dp, AccentBlue.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitial,
                        style = TextStyle(
                            color = TextDark,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Info Column (Username + Edit Icon & ID Code)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Username + Edit icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                editNameInput = displayName ?: ""
                                showEditNameDialog = true
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = displayName ?: "İsimsiz",
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = AccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // ID Code (clickable to copy)
                    Text(
                        text = myId.ifEmpty { "--- --- ---" },
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                if (myId.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(myId))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("ID kopyalandı")
                                    }
                                }
                            }
                            .padding(vertical = 2.dp)
                    )
                }
            }

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

                    // Gizlilik Politikası
                    SettingsItem(
                        icon = Icons.Default.Shield,
                        iconTint = AccentBlue,
                        title = "Gizlilik Politikası",
                        subtitle = "Veri gizliliği ve güvenlik politikamız",
                        onClick = {
                            val customTabsIntent = CustomTabsIntent.Builder().build()
                            customTabsIntent.launchUrl(
                                context,
                                Uri.parse(AppConstants.PRIVACY_POLICY_URL)
                            )
                        }
                    )

                    // Bildirimi Test Et (Temporary Test)
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconTint = AccentBlue,
                        title = "Bildirimi Test Et",
                        subtitle = "Arka plan bildirimini hemen tetikle",
                        onClick = {
                            val testWorkRequest = OneTimeWorkRequestBuilder<MailboxCheckWorker>()
                                .setInputData(workDataOf("force" to true))
                                .build()
                            WorkManager.getInstance(context).enqueue(testWorkRequest)
                            Toast.makeText(context, "Kontrol başlatıldı", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Hakkında
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = TextSecondary,
                        title = "Hakkında",
                        subtitle = "Warden Chat v1.8.0 · Anonim Mesajlaşma",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Edit Display Name Dialog
    if (showEditNameDialog) {
        var hasInteracted by remember { mutableStateOf(false) }
        val trimmed = editNameInput.trim()
        val isValid = trimmed.length in 2..20
        val showError = hasInteracted && !isValid

        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Kullanıcı Adını Düzenle",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = {
                            if (it.length <= 25) {
                                editNameInput = it
                                hasInteracted = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        shape = RoundedCornerShape(8.dp),
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
                        Text(
                            text = "Kullanıcı adı en az 2 karakter olmalıdır.",
                            style = TextStyle(color = DangerRed, fontSize = 11.sp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        hasInteracted = true
                        if (isValid) {
                            viewModel.saveDisplayName(trimmed)
                            showEditNameDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Kullanıcı adı güncellendi")
                            }
                        }
                    }
                ) {
                    Text(text = "Kaydet", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(text = "İptal", color = TextSecondary)
                }
            }
        )
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
