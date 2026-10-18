# Invite Link Support Implementation Plan

Add invite link support to Warden Chat via deep links (`wardenchat://add/...` and `https://darkstone-studio.github.io/WardenChat/add.html?id=...`), handle pending invite IDs in ViewModel, show confirmation dialogs in UI, and share invite links from Profile.

## User Review Required

> [!IMPORTANT]
> - **AndroidManifest.xml**: Set `launchMode="singleTop"` on `MainActivity` and add two intent filters for `wardenchat://add/...` and `https://darkstone-studio.github.io/WardenChat/add.html?id=...` (without `autoVerify`).
> - **MainActivity**: Extract invite ID in `onCreate` and `onNewIntent`, validate format, and pass to ViewModel's `pendingInviteId`.
> - **ViewModel (`LinkUpViewModel`)**: Manage `pendingInviteId: StateFlow<String?>`, handle adding/checking contact upon dialog confirmation (prevent adding own ID, open existing chat if present, or add new contact and open).
> - **UI (`ChatListScreen`)**: Observe `pendingInviteId` when onboarding is complete (`hasUsername == true`), show `AlertDialog` ("Yeni kişi", "[KİMLİK] kimlik koduyla sohbet başlatmak ister misin?"), and handle actions.
> - **Profile Screen ("Davet Linkini Paylaş")**: Add share link item to Settings list opening Android share sheet (`Intent.ACTION_SEND`, `text/plain`).
> - **Constants (`AppConstants.kt`)**: Define `INVITE_BASE_URL = "https://darkstone-studio.github.io/WardenChat/add.html?id="`.

## Proposed Changes

### Manifest & Constants

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/AndroidManifest.xml)
- Set `launchMode="singleTop"` on `MainActivity` and add VIEW intent filters.

#### [MODIFY] [AppConstants.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/AppConstants.kt)
- Add `INVITE_BASE_URL`.

### MainActivity & ViewModel

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/MainActivity.kt)
- Implement `onNewIntent`, extract invite ID from intent data in `onCreate` and `onNewIntent`, and update ViewModel's `pendingInviteId`.

#### [MODIFY] [LinkUpViewModel.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/LinkUpViewModel.kt)
- Add `pendingInviteId: StateFlow<String?>`, `setPendingInviteId(id: String?)`, and `clearPendingInviteId()`.

### UI Screens

#### [MODIFY] [ChatListScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Observe `pendingInviteId` and `hasUsername`. Show invite confirmation `AlertDialog` when ready. Handle own ID warning, existing chat, or new contact creation.

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ProfileScreen.kt)
- Add "Davet Linkini Paylaş" settings item with Android share sheet action.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation.

### Manual Verification
- Deploy app, test sharing invite link from Profile, open link via deep link (adb or browser), verify confirmation dialog appears, verify own ID check and contact adding.
