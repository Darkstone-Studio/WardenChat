# Room Database Contact Persistence Implementation Plan

Persist the chat/contact list in Warden Chat using Room Database (`androidx.room`) so that added contacts and last message previews persist across app restarts.

## User Review Required

> [!IMPORTANT]
> - **Room Entity (`ContactEntity`)**: Table `contacts` with `peerId` (String, PrimaryKey), `addedAt` (Long), `lastMessage` (String?, nullable), and `lastMessageTime` (Long?, nullable).
> - **Room DAO (`ContactDao`)**: Methods to get all contacts as a Flow, insert/update a contact, and update last message/time.
> - **Room Database (`WardenDatabase`)**: Singleton database instance.
> - **Dependencies**: Add Room (`2.6.1`) and KSP plugin (`2.1.0-1.0.29`) to Gradle configuration.
> - **ViewModel Integration**: `LinkUpViewModel` will observe Room's contact list flow and update it when a chat is started or messages are sent/received. Message content itself is still fetched ephemerally via Firebase Firestore mailbox.

## Proposed Changes

### Gradle Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/gradle/libs.versions.toml)
- Add room version (`2.6.1`) and ksp version (`2.1.0-1.0.29`).
- Add room libraries (`room-runtime`, `room-ktx`, `room-compiler`) and ksp plugin.

#### [MODIFY] [build.gradle.kts (Root)](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/build.gradle.kts)
- Apply ksp plugin alias (apply false).

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/build.gradle.kts)
- Apply ksp plugin.
- Add room dependencies.

### Database & Entity Layer

#### [NEW] [ContactEntity.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/room/ContactEntity.kt)
- Room entity for `contacts` table.

#### [NEW] [ContactDao.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/room/ContactDao.kt)
- Data Access Object for contacts.

#### [NEW] [WardenDatabase.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/room/WardenDatabase.kt)
- Room database singleton.

### ViewModel & UI

#### [MODIFY] [LinkUpViewModel.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/LinkUpViewModel.kt)
- Integrate `ContactDao` to load saved contacts on startup.
- Update contact when starting a chat (`startChatWithPeer`) and when sending/receiving messages (`lastMessage`, `lastMessageTime`).

#### [MODIFY] [ChatListScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Update to display persisted contacts and their last message previews from Room.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify KSP and Room compilation.

### Manual Verification
- Deploy app, add contacts and send/receive messages, restart the app, and verify that the contact list and last message previews persist correctly.
