package com.example.wardenchat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wardenchat.ui.theme.AccentBlue
import com.example.wardenchat.ui.theme.DangerRed
import com.example.wardenchat.ui.theme.DarkBackground
import com.example.wardenchat.ui.theme.DarkBorder
import com.example.wardenchat.ui.theme.DarkSurface
import com.example.wardenchat.ui.theme.DarkSurfaceVariant
import com.example.wardenchat.ui.theme.TextDark
import com.example.wardenchat.ui.theme.TextPrimary
import com.example.wardenchat.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    viewModel: LinkUpViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isAddingNewChat by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Text(
                text = "MESAJLAR",
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            // Added spacing between header and top action area
            Spacer(modifier = Modifier.height(8.dp))

            // Pinned Top Action Area (Dashed Button or Inline Input)
            if (!isAddingNewChat) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp)) // Matched with chat item corner radius
                        .background(DarkSurface)
                        .dashedBorder(
                            color = AccentBlue,
                            strokeWidth = 1.5.dp,
                            cornerRadius = 12.dp,
                            dashLength = 12.dp,
                            gapLength = 8.dp
                        )
                        .clickable { isAddingNewChat = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yeni",
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Yeni kişi Ekle...",
                            style = TextStyle(
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            } else {
                // Inline Add Chat Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "YENİ KİŞİ EKLE",
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { newValue ->
                                inputCode = viewModel.formatConnectInput(newValue)
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "45H-68Y-U8T",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            ),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DarkBorder,
                                cursorColor = AccentBlue
                            )
                        )

                        Button(
                            onClick = {
                                val success = viewModel.startChatWithPeer(inputCode)
                                if (success) {
                                    val peerId = inputCode.trim().uppercase()
                                    inputCode = ""
                                    isAddingNewChat = false
                                    onNavigateToChat(peerId)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = TextDark
                            ),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text(
                                text = "Başlat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                isAddingNewChat = false
                                inputCode = ""
                            }
                        ) {
                            Text(text = "İptal", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz sohbetin yok. Yukarıdan yeni kişi ekleyebilirsin.",
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts, key = { it.peerId }) { contact ->
                        ChatPreviewItem(
                            peerId = contact.peerId,
                            lastMessage = contact.lastMessage,
                            lastMessageTime = contact.lastMessageTime,
                            onClick = { onNavigateToChat(contact.peerId) },
                            onDelete = { viewModel.deleteContact(contact.peerId) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom modifier to draw a dashed border around a rounded rectangle shape.
 */
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp,
    dashLength: Dp = 10.dp,
    gapLength: Dp = 10.dp
) = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val cornerRadiusPx = cornerRadius.toPx()
    val dashLengthPx = dashLength.toPx()
    val gapLengthPx = gapLength.toPx()

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLengthPx, gapLengthPx), 0f)
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect
        ),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}

@Composable
private fun ChatPreviewItem(
    peerId: String,
    lastMessage: String?,
    lastMessageTime: Long?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val avatarInitial = if (peerId.isNotEmpty()) peerId.first().toString() else "?"
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = lastMessageTime?.let { timeFormat.format(Date(it)) } ?: ""
    val previewText = lastMessage ?: "Sohbet başlatıldı"

    var isLongPressed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val backgroundColor = if (isLongPressed) DangerRed.copy(alpha = 0.15f) else DarkSurface
    val borderColor = if (isLongPressed) DangerRed.copy(alpha = 0.4f) else DarkBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .pointerInput(peerId) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        isLongPressed = true
                        showDeleteDialog = true
                    }
                )
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitial,
                style = TextStyle(
                    color = TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }

        // Info column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = peerId,
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = timeString,
                    style = TextStyle(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = previewText,
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                isLongPressed = false
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Kişiyi Sil",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            },
            text = {
                Text(
                    text = "Kişiyi silmek istediğinize emin misiniz?",
                    style = TextStyle(fontSize = 13.sp, color = TextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isLongPressed = false
                        onDelete()
                    }
                ) {
                    Text(text = "Sil", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isLongPressed = false
                    }
                ) {
                    Text(text = "İptal", color = AccentBlue)
                }
            }
        )
    }
}
