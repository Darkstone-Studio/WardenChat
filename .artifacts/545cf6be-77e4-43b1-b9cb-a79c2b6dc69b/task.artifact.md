# Task Tracking

- [x] Update `MailboxCheckWorker.kt` (remove deletion, add `last_notified_ts` check, set `setOnlyAlertOnce(true)`)
- [x] Update `MainActivity.kt` (`onResume` cancel notification 1001)
- [x] Update `MailboxRepository.kt` (`listenToMailbox` return Boolean, add LinkedHashSet deduplication, conditional deletion)
- [x] Update `LinkUpViewModel.kt` (return `true` in `onMessageReceived`)
- [x] Build and verify project build successfully (`app:assembleDebug`)
