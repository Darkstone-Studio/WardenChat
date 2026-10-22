# Move "Add New Person" Action to a Bottom-Right Floating Action Button

This plan relocates the "Yeni kişi Ekle" (Add New Person) action from the top dashed box in `ChatListScreen` to a circular Floating Action Button (FAB) at the bottom-right corner of the screen, as requested.

## User Review Required

> [!NOTE]
> - The top dashed box button for adding a new contact in `ChatListScreen` will be removed.
> - A circular `FloatingActionButton` with `AccentBlue` background and `Icons.Default.Add` will be added at the bottom right (`floatingActionButton` slot of `Scaffold`).
> - The empty state message text will be updated from referencing "yukarıdan" (above) to referencing the bottom-right "+" button.

## Open Questions

- None.

## Proposed Changes

### UI Components

#### [MODIFY] [ChatListScreen.kt](file:///C:/Users/Onur/Desktop/WardenChat_DATA/WardenChat-Version/Warden%20Chat%20v1.13.1/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Remove the dashed border extension function if no longer used, or keep it if needed elsewhere (it's only used for the add button).
- Remove the Pinned Top Action Area (Dashed Button) `Box` and accompanying `Spacer`s from the main column.
- Update empty state text guidance from "Yukarıdan yeni kişi ekleyebilirsin" to "Sağ alttaki + butonuna basarak yeni kişi ekleyebilirsin".
- Add `floatingActionButton` parameter to `Scaffold`:
  ```kotlin
  floatingActionButton = {
      FloatingActionButton(
          onClick = { showAddDialog = true },
          containerColor = AccentBlue,
          contentColor = TextDark,
          shape = CircleShape
      ) {
          Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Yeni Kişi Ekle",
              modifier = Modifier.size(24.dp)
          )
      }
  }
  ```

## Verification Plan

### Automated Tests
- Build the project using Gradle (`app:assembleDebug`) to ensure no compilation errors.

### Manual Verification
- Review the `ChatListScreen` Compose preview or run the app on a device/emulator to verify that:
  1. The top dashed add button is removed.
  2. A circular `+` button is visible at the bottom right of the screen.
  3. Clicking the FAB successfully opens the "YENİ KİŞİ EKLE" dialog.
  4. The empty state text correctly guides the user to the bottom button.
