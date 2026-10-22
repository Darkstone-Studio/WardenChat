# Walkthrough - Move "Add New Person" Action to a Bottom-Right Floating Action Button

Relocated the "Yeni kişi Ekle" action from the top dashed button in `ChatListScreen` to a circular Floating Action Button (FAB) at the bottom-right corner of the screen.

## Changes

### UI & Navigation

#### [ChatListScreen.kt](file:///C:/Users/Onur/Desktop/WardenChat_DATA/WardenChat-Version/Warden%20Chat%20v1.13.1/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Removed the pinned top action area (dashed box button for adding a new contact).
- Added a circular `FloatingActionButton` to the `Scaffold` at the bottom right corner with `AccentBlue` background, `TextDark` icon tint, and `Icons.Default.Add`.
- Updated the empty state message text from `"Henüz sohbetin yok. Yukarıdan yeni kişi ekleyebilirsin."` to `"Henüz sohbetin yok. Sağ alttaki + butonuna basarak yeni kişi ekleyebilirsin."`.

```diff
-            // Pinned Top Action Area (Dashed Button)
-            Box( ... )
-
             if (contacts.isEmpty()) {
                 Box( ... ) {
                     Text(
-                        text = "Henüz sohbetin yok. Yukarıdan yeni kişi ekleyebilirsin.",
+                        text = "Henüz sohbetin yok. Sağ alttaki + butonuna basarak yeni kişi ekleyebilirsin.",
                         ...
                     )
                 }
             }
+    ) {
+        floatingActionButton = {
+            FloatingActionButton(
+                onClick = { showAddDialog = true },
+                containerColor = AccentBlue,
+                contentColor = TextDark,
+                shape = CircleShape
+            ) {
+                Icon(
+                    imageVector = Icons.Default.Add,
+                    contentDescription = "Yeni Kişi Ekle",
+                    modifier = Modifier.size(24.dp)
+                )
+            }
+        }
+    }
```

## Verification Results

### Automated Tests
- Executed `gradle_build("app:assembleDebug")` successfully with exit code 0 (`Build finished successfully.`).

### Manual Verification
- Verified that the top dashed add button is removed, the circular `+` button is pinned to the bottom-right corner, clicking it opens the "YENİ KİŞİ EKLE" dialog, and empty state instructions point to the bottom button.
