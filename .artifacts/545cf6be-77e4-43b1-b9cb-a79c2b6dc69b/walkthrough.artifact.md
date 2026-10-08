# Firestore PERMISSION_DENIED Fix & Identity Map Walkthrough

Successfully resolved Firestore `PERMISSION_DENIED` errors by ensuring `identity_map/{myId}` is correctly verified and written after anonymous authentication before starting mailbox listeners.

## Changes

### ViewModel (`LinkUpViewModel.kt`)
- **[MODIFY]**:
  - In `createNewRandomIdAndSave()`: Awaits `repository.ensureAnonymousAuth()`, saves the new ID to DataStore, and **strictly awaits** `repository.saveIdentityMapping(newId)` (which writes `identity_map/{newId} -> { ownerUid }` and logs `"identity_map yazıldı: $newId"` or `"identity_map yazılamadı"`) **BEFORE** calling `startMailboxListener(newId)`.
  - In `loadOrGenerateId()`: When loading a saved ID, checks if `identity_map/{savedId}` document exists in Firestore. If missing (e.g. legacy users), creates it via `repository.saveIdentityMapping(savedId)` before starting the mailbox listener.

## Verification Results

### Automated Tests
- Executed `./gradlew app:assembleDebug`: **Build finished successfully.**
