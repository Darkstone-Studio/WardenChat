# Username Transmission & Room Migration Implementation Plan

Transmit the user's username (`displayName`) along with messages in Firestore, sanitize incoming names, persist `displayName` in Room with a 1->2 database migration, and display the display name across chat previews, chat details (with ID visible), and background notifications.

## User Review Required

> [!IMPORTANT]
> - **Firestore Message Payload**: Add `senderName` (`String`) to message maps in `MailboxRepository.sendMessage`, sourced from DataStore `displayName`.
> - **Incoming Name Sanitization**: Trim, max 20 chars, and strip control characters (`\n`, `\t`, etc.). Ignore if empty.
> - **Room Database Migration (v1 -> v2)**: Add `displayName` (`String?`) to `ContactEntity`. Implement explicit migration `MIGRATION_1_2` (`ALTER TABLE contacts ADD COLUMN displayName TEXT`) without destructive migration.
> - **Display Logic**:
>   - *Chat list row*: Show `displayName` as primary title if present, with ID code in small gray text below/beside it. Avatar initial uses name's first letter (or ID if name missing).
>   - *Chat detail TopAppBar*: Show peer's `displayName` prominently, with their ID code in small gray text underneath (ensuring ID is always visible for impersonation protection).
>   - *Background Notification (`MailboxCheckWorker`)*: Notification text format `"Yeni mesaj: [senderName ?: senderId]"`, sanitizing name identically.

## Proposed Changes

### Data & Room Layer

#### [MODIFY] [ContactEntity.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/room/ContactEntity.kt)
- Add `displayName: String? = null` field.

#### [MODIFY] [ContactDao.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/room/ContactDao.kt)
- Add query `UPDATE contacts SET displayName = :name WHERE peerId = :peerId AND (:name IS NOT NULL AND :name != '')`.

#### [MODIFY] [WardenDatabase.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/room/WardenDatabase.kt)
- Increment version to `2`.
- Define `MIGRATION_1_2` and add it via `addMigrations(MIGRATION_1_2)`.

### Repository & ViewModel

#### [MODIFY] [MailboxRepository.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/MailboxRepository.kt)
- Update `sendMessage` to accept and include `senderName`.

#### [MODIFY] [LinkUpViewModel.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/LinkUpViewModel.kt)
- Pass current `displayName` into `sendMessage`.
- Sanitize incoming `senderName` in `startMailboxListener` and update Room contact `displayName`.

### UI Screens & Worker

#### [MODIFY] [ChatListScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatListScreen.kt)
- Display `contact.displayName` (or peerId if null) as primary title with peerId subtitle. Avatar initial uses name or peerId.

#### [MODIFY] [ChatDetailScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatDetailScreen.kt)
- Update TopAppBar to show peer's `displayName` (or peerId) and peerId in subtitle.

#### [MODIFY] [MailboxCheckWorker.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/MailboxCheckWorker.kt)
- Extract and sanitize `senderName` from Firestore messages for notification text.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation and database migration schema.

### Manual Verification
- Deploy app, test sending/receiving messages between devices/instances, verify `displayName` appears in chat list, chat detail TopAppBar, and background notifications.
