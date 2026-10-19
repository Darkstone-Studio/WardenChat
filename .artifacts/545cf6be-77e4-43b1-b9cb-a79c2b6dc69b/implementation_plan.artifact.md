# QR Code Invite Sharing Implementation Plan

Add QR code generation and display feature for invite links in Warden Chat using ZXing.

## User Review Required

> [!IMPORTANT]
> - **Dependency**: Add `implementation("com.google.zxing:core:3.5.3")` to `app/build.gradle.kts`.
> - **QR Utility (`QrCode.kt`)**: Helper `generateQrBitmap(content: String, size: Int = 768): ImageBitmap` using `QRCodeWriter` with hints (`ERROR_CORRECTION = M`, `MARGIN = 1`, `CHARACTER_SET = "UTF-8"`). Black modules (`0xFF000000`), white background (`0xFFFFFFFF`).
> - **Profile Screen Layout**: Next to the 44dp "Davet Linkini Paylaş" button, add a square 44dp icon button (`Icons.Default.QrCode2`) with an `AccentBlue` 50% border. The share button takes remaining width (`weight(1f)`), separated by 8dp.
> - **QR Code Dialog**: Tapping the QR button opens a modal `Dialog`: `DarkSurface` background, 16dp rounded corners, `DarkBorder`, containing a centered pure white card (16dp rounded corners, 16dp padding) with the QR bitmap (`240dp`), username, ID code, subtext `"Bu kodu taratan kişi seni ekleyebilir"`, and a `"Kapat"` button.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/build.gradle.kts)
- Add `implementation("com.google.zxing:core:3.5.3")`.

### Utility Layer

#### [NEW] [QrCode.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/util/QrCode.kt)
- Implement `generateQrBitmap` using ZXing.

### UI Screens

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ProfileScreen.kt)
- Update share invite row to contain both the Share button (`weight(1f)`) and the QR code icon button (`44.dp` square).
- Implement QR code `Dialog` when QR button is clicked.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation and ZXing integration.

### Manual Verification
- Deploy app, navigate to Profile, tap QR code button, verify QR code dialog opens with pure white card, QR code, username, ID, and closes on Kapat.
