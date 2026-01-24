# Android 10 (API 29) Testing Checklist

## Pre-Testing Code Review ✅

### Fixed Issues:
1. ✅ **CollapsibleContainer LaunchedEffect** - Fixed potential recomposition issue by extracting saved state value
2. ✅ **Missing R import** - Added missing `import com.playercombatassistant.pca.R` in MainActivity
3. ✅ **Null Safety** - All code paths use safe null handling (?.let, ?:, etc.)

### Verified Compatibility:
1. ✅ **Theme.kt** - Dynamic colors properly guarded with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`
2. ✅ **ModalBottomSheet** - Compatible with API 29 (Material 3 1.0.0+)
3. ✅ **Navigation** - Uses standard Navigation Compose (API 21+)
4. ✅ **DataStore** - Compatible with API 21+
5. ✅ **ViewModels** - Standard AndroidViewModel (API 14+)

## Manual Testing Checklist

### Screen Navigation
- [ ] App launches without crashes
- [ ] Combat screen displays correctly
- [ ] Navigation to History screen works
- [ ] Navigation to Settings screen works
- [ ] Back navigation works from History
- [ ] Back navigation works from Settings
- [ ] Top app bar buttons (History, Settings) work correctly

### Collapsible Containers
- [ ] Combat Status and Controls card is always visible (not collapsible)
- [ ] Improvised Weapons section collapses/expands correctly
- [ ] Effects section collapses/expands correctly
- [ ] Modifier Summary section collapses/expands correctly (when available)
- [ ] Spell Slots section collapses/expands correctly
- [ ] Modifier Builder section collapses/expands correctly
- [ ] Condition Presets section collapses/expands correctly
- [ ] Collapsed state persists when navigating between screens
- [ ] Expanded state persists when navigating between screens
- [ ] State resets on app restart (session-only persistence)

### Add Effect Sheet
- [ ] "Add Effect" button opens the bottom sheet
- [ ] Sheet displays correctly with all three modes (Generic, Condition, Modifier)
- [ ] Mode switching works correctly
- [ ] Condition picker displays and selects correctly
- [ ] Modifier picker displays and selects correctly
- [ ] Form validation works (name required, duration valid)
- [ ] Color picker displays and selects correctly
- [ ] "Add Effect" button enables/disables based on validation
- [ ] Sheet dismisses correctly on "Add Effect"
- [ ] Sheet dismisses correctly on back press
- [ ] Sheet dismisses correctly on outside tap

### Combat Functionality
- [ ] Start Combat button works
- [ ] Next Round button works
- [ ] End Combat button works
- [ ] Round counter displays correctly
- [ ] Combat state label displays correctly
- [ ] Improvised weapon rolling works
- [ ] Location selection works
- [ ] Random location rolling works
- [ ] Effects list displays correctly
- [ ] Generic effects display correctly
- [ ] Effect timers count down correctly

### Layout Responsiveness
- [ ] Phone layout displays correctly (compact width)
- [ ] Tablet layout displays correctly (medium/expanded width)
- [ ] Scroll behavior works on phone layout
- [ ] Scroll behavior works on tablet layout (both columns)
- [ ] No overlapping content
- [ ] All sections are accessible via scrolling

### Error Handling
- [ ] No crashes when opening any screen
- [ ] No crashes when collapsing/expanding containers
- [ ] No crashes when using Add Effect sheet
- [ ] No crashes when navigating
- [ ] No API errors in logcat related to unsupported features
- [ ] No null pointer exceptions

### Data Persistence
- [ ] Settings persist across app restarts
- [ ] Pinned modifiers persist across app restarts
- [ ] Combat history persists across app restarts
- [ ] Collapsible states do NOT persist across app restarts (session-only)

## Known Android 10 Compatibility Notes

### ✅ Safe to Use:
- All Material 3 components (API 21+)
- ModalBottomSheet (API 21+)
- Navigation Compose (API 21+)
- DataStore Preferences (API 21+)
- ViewModels (API 14+)
- Compose animations (API 21+)

### ⚠️ Version-Guarded:
- Dynamic Colors (only enabled on API 31+)

### ❌ Not Used (would require higher API):
- None - all features are compatible with API 29

## Testing Instructions

1. **Setup Android 10 Emulator:**
   - Create AVD with API 29 (Android 10)
   - Ensure sufficient RAM and storage

2. **Build and Install:**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Run Tests:**
   - Go through each item in the checklist above
   - Note any crashes, errors, or unexpected behavior
   - Check logcat for any API-related errors

4. **Report Issues:**
   - Document any crashes with stack traces
   - Note any UI glitches or layout issues
   - Report any API compatibility errors

## Expected Results

All tests should pass without crashes or API errors. The app should function identically on Android 10 as on newer versions, except for dynamic colors which are intentionally disabled on Android 10.
