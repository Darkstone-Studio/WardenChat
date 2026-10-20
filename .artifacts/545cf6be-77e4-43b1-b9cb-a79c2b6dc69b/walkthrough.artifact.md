# QR Code Brightness Boost & Scanning Walkthrough

Successfully added automatic screen brightness boost and screen-on flag when the QR code dialog is open in Profile, restoring previous brightness upon dismissal.

## Changes

### UI & Window Attributes (`ProfileScreen.kt`)
- **[MODIFY] [ProfileScreen.kt](file:///C:/Users/Onur/AndroidStudioProjects/WardenChat/app/src/main/java/com/example/wardenchat/ui/ProfileScreen.kt)**: Added a `DisposableEffect` inside the QR code Dialog to:
  - Find the host `Activity` from context.
  - Save the old window brightness and set `screenBrightness = 1f` (maximum brightness).
  - Add `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` so the screen doesn't dim while displaying the QR code.
  - Automatically restore the previous brightness and clear `FLAG_KEEP_SCREEN_ON` on disposal (whether closed via button, back press, outside tap, or app pause).

## Verification Results

### Automated Tests
- Executed `./gradlew app:assembleDebug`: **Build finished successfully.**
