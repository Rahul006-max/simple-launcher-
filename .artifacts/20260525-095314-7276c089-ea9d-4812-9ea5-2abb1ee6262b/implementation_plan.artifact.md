# Implementation Plan - Minimal Text Launcher

This plan outlines the steps to transform the existing `simplelauncher` app into a minimal, text-based Android launcher as per the provided JSON specification.

## User Review Required

> [!IMPORTANT]
> - **Compose Setup**: The project currently doesn't have Jetpack Compose configured. I will be adding the necessary dependencies and build configurations.
> - **Default Launcher**: To make this app a functional launcher, the user will need to manually set it as the default home app in Android settings after deployment.
> - **App Visibility**: I'll add `<queries>` to the manifest to ensure all installed apps can be listed on Android 11+.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///C:/Users/rahul/AndroidStudioProjects/simplelauncher/gradle/libs.versions.toml)
- Add versions for Compose and Material3.
- Add library definitions for `androidx-compose-bom`, `androidx-ui`, `androidx-ui-graphics`, `androidx-ui-tooling-preview`, `androidx-material3`, and `androidx-activity-compose`.

#### [app/build.gradle.kts](file:///C:/Users/rahul/AndroidStudioProjects/simplelauncher/app/build.gradle.kts)
- Enable Compose build feature.
- Set `kotlinOptions` and `composeOptions` if necessary (though modern AGP handles much of this).
- Add Compose dependencies using the BOM.

---

### Manifest and Resources

#### [AndroidManifest.xml](file:///C:/Users/rahul/AndroidStudioProjects/simplelauncher/app/src/main/AndroidManifest.xml)
- Set `MainActivity` `launchMode="singleTask"`.
- Add `CATEGORY_HOME` and `CATEGORY_DEFAULT` to the intent-filter.
- Add `<queries>` tag to allow querying all launchable apps.

#### [DELETE] [activity_main.xml](file:///C:/Users/rahul/AndroidStudioProjects/simplelauncher/app/src/main/res/layout/activity_main.xml)
- This file will no longer be needed as we are switching to Compose.

---

### UI and Logic

#### [MainActivity.kt](file:///C:/Users/rahul/AndroidStudioProjects/simplelauncher/app/src/main/java/com/example/simplelauncher/MainActivity.kt)
- **Data Model**: Implement `AppInfo` data class.
- **Data Layer**: Implement `getInstalledApps` function using `PackageManager`.
- **UI Components**:
    - `TypographicClock`: Massive HH:mm and date.
    - `BatteryStatus`: BroadcastReceiver for battery percentage.
    - `FavoriteApps`: Column with hardcoded packages (Notes, Calendar, etc.).
    - `TodoMinimal`: Mutable state list with text field for adding tasks.
    - `AppDrawer`: `BottomSheetScaffold` containing a `LazyColumn` of all apps.
    - `QuickActions`: Dialer and WhatsApp shortcuts.
- **Styling**: strictly Black/White/Gray monochrome theme.

## Verification Plan

### Automated Tests
- I will attempt to run `gradlew assembleDebug` to ensure the project builds with the new Compose dependencies.

### Manual Verification
- **Deployment**: Deploy the app to the device using `deploy` tool.
- **UI Check**: Verify the monochrome aesthetic, the clock, battery status, and favorites.
- **Functionality Check**:
    - Swipe up to open the drawer.
    - Click an app in the drawer to launch it.
    - Add/Delete items in the to-do list.
    - Click quick action icons.
- **Logs**: Monitor `logcat` for any issues with package querying or app launching.
