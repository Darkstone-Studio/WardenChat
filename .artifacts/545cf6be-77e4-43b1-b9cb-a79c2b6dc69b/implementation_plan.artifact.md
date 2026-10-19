# QR Code Scanning Support Implementation Plan

Add QR code scanning support for invite links in Warden Chat using Google Play Services Code Scanner (no camera permission required).

## User Review Required

> [!IMPORTANT]
> - **Dependency**: Add `implementation("com.google.android.gms:play-services-code-scanner:16.1.0")` to `app/build.gradle.kts`.
> - **Manifest**: Add `<meta-data android:name="com.google.mlkit.vision.DEPENDENCIES" android:value="barcode_ui"/>` inside `<application>` in `AndroidManifest.xml`.
> - **Shared Helper (`InviteUtils.kt`)**: Implement `extractInviteId(raw: String): String?` to handle raw IDs, `wardenchat://add/<id>`, and `https://darkstone-studio.github.io/WardenChat/add.html?id=<id>`, validating with the code regex.
> - **MainActivity**: Refactor `handleIntent` to use `extractInviteId`.
> - **ChatListScreen**: Add "QR ile Ekle" button (`44.dp`, outlined, `AccentBlue`) below the "Başlat" button in the "Yeni kişi ekle" dialog. Trigger Google Play Services Code Scanner on click, validate scanned raw value with `extractInviteId`, and set `pendingInviteId` on success.

## Proposed Changes

### Build & Manifest

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/build.gradle.kts)
- Add `implementation("com.google.android.gms:play-services-code-scanner:16.1.0")`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/AndroidManifest.xml)
- Add ML Kit barcode_ui dependency metadata.

### Utility Layer

#### [NEW] [InviteUtils.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/util/InviteUtils.kt)
- Implement `extractInviteId(raw: String): String?`.

### UI & MainActivity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/MainActivity.kt)
- Use `extractInviteId` in `handleIntent`.

#### [MODIFY] [ChatListScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Add "QR ile Ekle" button to the "Yeni kişi ekle" dialog and implement GmsBarcodeScanning integration.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation.

### Manual Verification
- Deploy app, open "Yeni kişi ekle" dialog, tap "QR ile Ekle", scan QR code, verify confirmation dialog appears.
