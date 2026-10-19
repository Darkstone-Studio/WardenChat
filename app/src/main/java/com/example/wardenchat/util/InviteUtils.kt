package com.example.wardenchat.util

import android.net.Uri

object InviteUtils {
    private val VALID_CODE_REGEX = Regex("^[2-9A-HJ-NP-Z]{3}-[2-9A-HJ-NP-Z]{3}-[2-9A-HJ-NP-Z]{3}$")

    fun extractInviteId(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // 1. Check if raw is already a direct 9-character code (e.g. 4X5-KXU-RS9)
        val upper = trimmed.uppercase()
        if (VALID_CODE_REGEX.matches(upper)) {
            return upper
        }

        // 2. Try parsing as Uri
        try {
            val uri = Uri.parse(trimmed) ?: return null
            val scheme = uri.scheme?.lowercase() ?: return null

            when (scheme) {
                "wardenchat" -> {
                    if (uri.host?.lowercase() == "add") {
                        val lastSegment = uri.lastPathSegment?.trim()?.uppercase()
                        if (lastSegment != null && VALID_CODE_REGEX.matches(lastSegment)) {
                            return lastSegment
                        }
                    }
                }
                "https" -> {
                    val host = uri.host?.lowercase()
                    val path = uri.path ?: ""
                    if (host == "darkstone-studio.github.io" && path.startsWith("/WardenChat/add")) {
                        val idParam = uri.getQueryParameter("id")?.trim()?.uppercase()
                        if (idParam != null && VALID_CODE_REGEX.matches(idParam)) {
                            return idParam
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Invalid URI format
        }

        return null
    }
}
