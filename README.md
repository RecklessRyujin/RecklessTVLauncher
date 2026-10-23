# Reckless TV Launcher

A lightweight Java/XML Android TV launcher for Fire OS and Android TV.

## Features
- Dark premium layout with green accent
- Clock/date header and organized app grid
- Four-column app grid with consistent tile spacing
- Green outline focus state with dark interior
- No focus scaling/elevation animation, reducing focus clipping at screen edges
- App discovery via LEANBACK_LAUNCHER and LAUNCHER categories
- Quick actions for Settings, Refresh apps, and Search
- No network permission and no external dependencies

## Build
Open this repository in GitHub and run the **Build Reckless TV Launcher** workflow under Actions. The workflow uploads `RecklessTV-debug-apk`.

## Install
Install `app-debug.apk` using atvTools. Package name remains `dev.reckless.tvlauncher`, so it updates the existing installation.

## Notes
Fire OS may not honor third-party HOME intent filters for the physical Home button. Keep the stock launcher installed until the custom launcher has been tested.
