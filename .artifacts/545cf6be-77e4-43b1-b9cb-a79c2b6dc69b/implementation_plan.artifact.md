# Message Loss Fix Implementation Plan

Fix message loss issues caused by premature message deletion in background workers and foreground listeners, and implement proper acknowledgment-based deletion and deduplication.

## User Review Required

> [!IMPORTANT]
> - **MailboxCheckWorker**: Remove message document deletion loop. Add check against `last_notified_ts` in SharedPreferences to prevent duplicate notifications every 15 minutes. Set notification ID `1001` with `.setOnlyAlertOnce(true)`.
> - **MainActivity (`onResume`)**: Call `NotificationManagerCompat.from(this).cancel(1001)` when the app comes to foreground.
> - **MailboxRepository (`listenToMailbox`)**: Change `onMessageReceived` callback to return `Boolean`. Implement a `LinkedHashSet` (max size 200) for processed document IDs to prevent duplicate processing. Validate message (`text` and `senderId` not blank), call `onMessageReceived`, and ONLY delete the document if it returns `true`.
> - **LinkUpViewModel (`startMailboxListener`)**: Return `true` in `onMessageReceived` when message is successfully added to state.
> - **Privacy Constraint**: Do NOT add new log lines that log message content.

## Proposed Changes

### Data Layer

#### [MODIFY] [MailboxCheckWorker.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/MailboxCheckWorker.kt)
- Remove deletion loop.
- Implement `last_notified_ts` SharedPreferences check.
- Use `.setOnlyAlertOnce(true)` on notification builder.

#### [MODIFY] [MailboxRepository.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/MailboxRepository.kt)
- Update `listenToMailbox` callback to return `Boolean`.
- Add `LinkedHashSet` for deduplication (max 200 IDs).
- Process message, call callback, and delete document ONLY on `true`.

### UI / ViewModel

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/MainActivity.kt)
- Cancel notification `1001` in `onResume`.

#### [MODIFY] [LinkUpViewModel.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/LinkUpViewModel.kt)
- Return `true` in `onMessageReceived` callback.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation.

### Manual Verification
- Deploy app, receive messages while app is in background / foreground, verify no message loss, verify notifications behave correctly.
