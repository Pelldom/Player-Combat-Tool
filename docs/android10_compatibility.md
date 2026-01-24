# Android 10 (API 29) Compatibility

## Overview
This document outlines the compatibility measures taken to ensure the app runs correctly on Android 10 (API 29) and above.

## Configuration

### Minimum SDK
- **minSdk**: 29 (Android 10)
- **targetSdk**: 35 (Android 15)
- **compileSdk**: 35

### Rationale
- Android 10 (API 29) is set as the minimum supported version
- This ensures access to modern Android features while maintaining broad device compatibility
- All Material 3 components used are compatible with API 29

## Material 3 Components Compatibility

### ✅ Compatible Components (API 29+)
All Material 3 components used in this app are compatible with API 29:

1. **ModalBottomSheet** - Available since Material 3 1.0.0 (API 21+)
2. **SingleChoiceSegmentedButtonRow** - Available since Material 3 1.1.0 (API 21+)
3. **SegmentedButton** - Available since Material 3 1.1.0 (API 21+)
4. **DropdownMenu** - Available since Material 3 1.0.0 (API 21+)
5. **Scaffold** - Available since Material 3 1.0.0 (API 21+)
6. **TopAppBar** - Available since Material 3 1.0.0 (API 21+)
7. **Card** - Available since Material 3 1.0.0 (API 21+)
8. **Button** - Available since Material 3 1.0.0 (API 21+)
9. **TextField** - Available since Material 3 1.0.0 (API 21+)
10. **Checkbox** - Available since Material 3 1.0.0 (API 21+)

### API Level Guards

#### Dynamic Colors (API 31+)
Dynamic color theming is only enabled on Android 12 (API 31) and above:

```kotlin
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        // Dynamic colors (API 31+)
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
```

**Location**: `app/src/main/java/com/playercombatassistant/pca/ui/theme/Theme.kt`

## AndroidX Libraries Compatibility

All AndroidX libraries used are compatible with API 29:

- **androidx.compose:compose-bom** (2024.10.00) - Supports API 21+
- **androidx.activity:activity-compose** (1.9.3) - Supports API 21+
- **androidx.navigation:navigation-compose** (2.8.3) - Supports API 21+
- **androidx.datastore:datastore-preferences** (1.1.1) - Supports API 21+
- **androidx.lifecycle** (2.8.7) - Supports API 14+

## No API-Specific Issues

### Verified Components
- ✅ No WindowInsets usage (not needed for basic UI)
- ✅ No Edge-to-Edge requirements
- ✅ No Gesture Navigation dependencies
- ✅ No Scoped Storage issues (using DataStore, not file system)
- ✅ No Permission model changes affecting this app

### Compose Compatibility
- All Compose components work on API 29
- Material 3 components are backward compatible
- No experimental APIs that require higher API levels

## Testing Checklist

### Basic Screens (API 29)
- [x] Combat screen renders correctly
- [x] Effects list displays properly
- [x] Add Effect sheet opens and functions
- [x] Condition picker works
- [x] Modifier picker works
- [x] Pinned modifiers widget displays
- [x] Navigation works (History, Settings)
- [x] Dark/Light theme switching works

### Material 3 Components (API 29)
- [x] ModalBottomSheet opens and closes
- [x] SegmentedButtonRow displays and responds to clicks
- [x] DropdownMenu opens and selects items
- [x] Cards render with proper styling
- [x] Buttons respond to clicks
- [x] TextFields accept input

### Data Persistence (API 29)
- [x] DataStore Preferences saves and loads
- [x] Pinned modifiers persist across app restarts
- [x] Settings persist correctly

## Known Limitations

### None
All features work correctly on Android 10 (API 29) and above. No fallbacks or workarounds are required.

## Build Verification

To verify compatibility:
1. Set up Android 10 (API 29) emulator
2. Build and install the app
3. Test all screens and interactions
4. Verify no crashes or UI glitches

## Future Considerations

If adding features that require higher API levels:
1. Use `@RequiresApi` annotation
2. Add version checks: `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.XXX)`
3. Provide fallback implementations for older versions
4. Document API level requirements
