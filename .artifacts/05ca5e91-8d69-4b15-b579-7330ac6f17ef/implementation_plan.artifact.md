# Animated Bottom Sheet for Adding New Contact with QR Icon Inside Input Bar

This plan replaces the static add dialog in `ChatListScreen` with an animated Material 3 `ModalBottomSheet` that opens smoothly from the bottom when tapping the `+` Floating Action Button. Inside this sheet, a task-like long input bar contains the placeholder "Yeni kişi ekle" with a QR code scanner icon positioned on the right side.

## User Review Required

> [!NOTE]
> - Tapping the bottom-right `+` FAB will now open an animated bottom sheet (`ModalBottomSheet`) instead of a centered dialog.
> - The add container will feature a long input bar (task style) with placeholder `"Yeni kişi ekle"`.
> - The right side of the input bar will house the QR code scanner icon (`Icons.Outlined.QrCodeScanner`) for quick QR scanning.
> - A "Başlat" button will be present to initiate chat with the entered code.

## Open Questions

- None.

## Proposed Changes

### UI Components

#### [MODIFY] [ChatListScreen.kt](file:///C:/Users/Onur/Desktop/WardenChat_DATA/WardenChat-Version/Warden%20Chat%20v1.13.1/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Replace the `Dialog` implementation for adding contacts with `ModalBottomSheet` (`@OptIn(ExperimentalMaterial3Api::class)`).
- Structure the sheet content:
  - Title: "YENİ KİŞİ EKLE"
  - Subtitle: "Bağlanmak istediğin kişinin 9 haneli kimlik kodunu gir:"
  - Long input bar (`OutlinedTextField`) with placeholder `"Yeni kişi ekle"`, keyboard options, and a `trailingIcon` containing the QR code scanner icon (`Icons.Outlined.QrCodeScanner`).
  - "Başlat" action button.

## Verification Plan

### Automated Tests
- Build project using Gradle (`app:assembleDebug`) to verify compilation and Material3 API usage.

### Manual Verification
- Test tapping the bottom-right `+` button to verify the bottom sheet opens with an animation.
- Verify the long input bar displays `"Yeni kişi ekle"` and has the QR code icon on the right.
- Verify tapping the QR icon launches the QR scanner.
- Verify entering a code and tapping "Başlat" starts the chat.
