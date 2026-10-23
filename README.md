# Reckless TV Launcher

A small, offline-first Android TV home-screen replacement written in Java. Designed for Android 9 / Fire OS 7 and tested by building against compile SDK 35 with target SDK 28.

## Features
- Lightweight Java implementation; no third-party libraries or network permission.
- Dark app grid that discovers apps with Android TV and standard launcher intents.
- Remote-friendly focus and selection.
- Clock, app count, refresh button and settings shortcut.
- Declares itself as a HOME activity, but does not disable or uninstall Amazon's launcher.

## Build on GitHub Actions (phone-friendly)
1. Create a new GitHub repository.
2. Upload the contents of this folder to the repository root (the `.github` folder must be included).
3. Open **Actions** and run **Build Reckless TV Launcher**, or push to the `main` branch.
4. Open the successful workflow run, scroll to **Artifacts**, and download `RecklessTV-debug-apk`.
5. Extract the ZIP to get `app-debug.apk`.

## Install using atvTools
Install `app-debug.apk` with atvTools' APK installation option if available. Alternatively, if the APK is on a device path accessible to ADB, use:
```sh
adb install -r app-debug.apk
```
The `adb install` command is run from an ADB client environment, not inside the TV's remote shell unless your app specifically provides a local ADB client command.

## Important Fire TV behavior
- Installing this app does not automatically make it the default launcher.
- Fire OS may continue opening Amazon Home when the Home button is pressed.
- Keep Amazon's launcher enabled until this launcher is installed and tested. Do not disable `com.amazon.tv.launcher` as part of initial setup.
- This project is not an ad blocker. Removing the Amazon home screen can reduce exposure to its home-screen promotions, but ads within streaming services or system screens are separate.

## Compatibility
- Minimum SDK 23
- Target SDK 28
- Java 8 language level
- No AndroidX or external runtime dependencies
