# Firebase Anonymous Authentication & Identity Mapping Implementation Plan

Add Firebase Anonymous Authentication to Warden Chat and link each identity code to the authenticated user's UID via an `identity_map/{peerId}` Firestore collection, ensuring data security and ownership validation.

## User Review Required

> [!IMPORTANT]
> - **Firebase Auth Dependency**: Add `com.google.firebase:firebase-auth-ktx` to `app/build.gradle.kts`.
> - **Anonymous Auth Initialization**: On startup, if `FirebaseAuth.getInstance().currentUser == null`, call `signInAnonymously()` and suspend mailbox listener startup until complete.
> - **Identity Mapping Collection**: `identity_map/{peerId}` containing `{ ownerUid: String }`.
> - **Identity Lifecycle**: When a new ID is generated (startup or "Generate New ID"), write to `identity_map/{yeniId}` with the current user's UID, and delete the old ID's `identity_map` entry along with its mailbox.
> - **Repository & ViewModel Updates**: Update `MailboxRepository` and `LinkUpViewModel` to await anonymous sign-in and manage identity mapping documents.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/build.gradle.kts)
- Add `implementation("com.google.firebase:firebase-auth-ktx")`.

### Data Layer (`MailboxRepository.kt`)

#### [MODIFY] [MailboxRepository.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/MailboxRepository.kt)
- Add suspend function `ensureAnonymousAuth()` using `Tasks.await(FirebaseAuth.getInstance().signInAnonymously())` if currentUser is null.
- Add methods to write and delete `identity_map/{peerId}` documents (`saveIdentityMapping`, `clearIdentityMapping`).
- Update `listenToMailbox` and `clearMailbox` to integrate identity mapping.

### ViewModel (`LinkUpViewModel.kt`)

#### [MODIFY] [LinkUpViewModel.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/LinkUpViewModel.kt)
- Update `loadOrGenerateId()` and `createNewRandomIdAndSave()` to await anonymous authentication before starting the mailbox listener and registering the identity map.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation.

### Manual Verification
- Deploy app, verify anonymous sign-in succeeds, check Firestore for `identity_map/{peerId}` document creation with `ownerUid`, test "Generate New ID" updates the mapping document and clears the old one.
