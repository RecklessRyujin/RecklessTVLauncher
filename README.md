# Reckless TV Launcher

A lightweight Java/XML launcher for Fire OS and Android TV. Package name remains `dev.reckless.tvlauncher`; target SDK remains 28 for older Fire OS behavior.

## Included features
- Existing dark launcher UI, clock/date, app grid, quick actions, and appearance preferences.
- Manage Apps: hide and restore launchable apps within Reckless TV. This does not uninstall, disable, or alter app data.
- TV Health: read-only storage, RAM, and Android/device details. Fire OS may limit reported values.
- ADB Setup & Recovery: setup guidance, Home-button limitations, and conservative recovery commands.
- Appearance reset now clears only appearance keys; it preserves hidden-app preferences and unrelated settings.

## Important Fire TV limitations
- Fire OS may ignore third-party HOME intent filters for the physical Home button. This build does not implement an accessibility overlay or claim to override the Home button.
- No Amazon/system package debloating is performed. Package dependencies vary by model and firmware; no unverified package list is safe to ship as a one-click debloater.
- Keep Amazon Home installed and retain a known recovery route. Only re-enable packages you intentionally disabled.

## Build
Run the **Build Reckless TV Launcher** workflow in GitHub Actions, or run `gradle assembleDebug` with Android SDK installed. The workflow uploads `app-debug.apk`.

## Install
Install the APK using atvTools. The package name is unchanged, so it can update an existing installation.
