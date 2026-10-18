# Self-Destruct Settings Migration Implementation Plan

Move the "Mesajları Yok Et (5sn)" (Self-Destruct) switch from the chat detail screen to the Settings list in the Profile screen, persisting it across app restarts via DataStore.

## User Review Required

> [!IMPORTANT]
> - **DataStore Persistence**: Add `selfDestructEnabled` (`Boolean`, default `false`) key in `UserPreferencesRepository`.
> - **ViewModel (`LinkUpViewModel`)**: Replace in-memory `_selfDestructEnabled` StateFlow with a flow backed by `UserPreferencesRepository`, and provide `toggleSelfDestruct(enabled: Boolean)` to save it to DataStore.
> - **ChatDetailScreen**: Completely remove the top self-destruct Switch and its label. Message bubble fade-out logic remains intact based on the global `selfDestructEnabled` state.
> - **ProfileScreen**: Add a new "Mesajları Yok Et" settings item with a Material 3 `Switch` (active track color `AccentBlue`), subtitle `"Açıkken mesajlar 5 saniye sonra ekrandan silinir"`, and clickable row.

## Proposed Changes

### Data Layer (`UserPreferencesRepository.kt`)

#### [MODIFY] [UserPreferencesRepository.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/data/UserPreferencesRepository.kt")
- Add `SELF_DESTRUCT_KEY` (boolean preference).
- Add `selfDestructFlow: Flow<Boolean>`.
- Add `saveSelfDestruct(enabled: Boolean)`.

### ViewModel (`LinkUpViewModel.kt`)

#### [MODIFY] [LinkUpViewModel.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/LinkUpViewModel.kt")
- Update `selfDestructEnabled` StateFlow to be collected from `UserPreferencesRepository`.
- Update `toggleSelfDestruct(enabled)` to call `userPrefsRepo.saveSelfDestruct(enabled)`.

### UI Screens

#### [MODIFY] [ChatDetailScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ChatDetailScreen.kt")
- Remove the top self-destruct Switch Row.

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ProfileScreen.kt")
- Add "Mesajları Yok Et" settings item with a toggle switch in the Settings card.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation.

### Manual Verification
- Deploy app, toggle "Mesajları Yok Et" in Profile settings, restart app to verify persistence, send/receive messages in chat detail to verify self-destruct behavior.
