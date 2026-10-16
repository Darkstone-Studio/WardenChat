# Task Tracking

- [x] Update `ContactEntity.kt` with `displayName` column
- [x] Update `ContactDao.kt` with `updateDisplayName` query
- [x] Update `WardenDatabase.kt` with version 2 and `MIGRATION_1_2`
- [x] Update `MailboxRepository.kt` `sendMessage` to include `senderName`
- [x] Update `LinkUpViewModel.kt` to transmit `displayName` and sanitize incoming `senderName`
- [x] Update `ChatListScreen.kt` to display `displayName` and peerId subtitle
- [x] Update `ChatDetailScreen.kt` TopAppBar to display `displayName` and peerId
- [x] Update `MailboxCheckWorker.kt` to read sanitized `senderName` for notifications
- [x] Build and verify project build successfully (`app:assembleDebug`)
