# Testing & Verification Guide

## ✅ Pre-Deployment Checklist

### Step 1: Sync and Build

```bash
# Open terminal in project root
cd D:\Kotlin\SafarSakha

# Sync gradle
./gradlew sync

# Clean build
./gradlew clean

# Build all variants
./gradlew build
```

**Expected Output:**
```
✅ BUILD SUCCESSFUL in XXs
```

---

### Step 2: Verify Dependencies

Check that FileKit libraries are downloaded:

```bash
./gradlew dependencies | grep filekit
```

**Expected Output:**
```
io.github.vinceglb:filekit-core:0.8.5
io.github.vinceglb:filekit-compose:0.8.5
```

---

### Step 3: Verify No Compilation Errors

```bash
./gradlew compileKotlin
```

**Expected Output:**
```
✅ BUILD SUCCESSFUL
```

---

## 🧪 Manual Testing Scenarios

### Test 1: Android Device/Emulator

#### Setup:
1. Connect Android device or open emulator
2. Run the app:
   ```bash
   ./gradlew assembleDebug
   adb install build/outputs/apk/debug/app-debug.apk
   ```

#### Test Steps:
1. Launch app
2. Navigate to Edit Tour Package screen
3. Click "Change Image" button
4. **Expected**: Android Gallery app opens
5. Select any image file
6. **Expected**: Image preview displays with "NEW" badge
7. Verify snackbar shows: "Image selected successfully"
8. Edit other fields if desired
9. Click "Update Package"
10. **Expected**: Loading indicator shows
11. **Expected**: Image uploads to Firebase Storage
12. **Expected**: Navigation returns to package list
13. **Expected**: Package displays new image

#### Success Criteria:
- ✅ Gallery opens without errors
- ✅ Image can be selected
- ✅ Preview shows selected image
- ✅ "NEW" badge visible
- ✅ Upload completes
- ✅ New image in package list

---

### Test 2: iOS Device/Simulator

#### Setup:
1. Open iOS simulator or connect iOS device
2. Build and run iOS app:
   ```bash
   ./gradlew iosX64Debug
   ```

#### Test Steps:
1. Launch app
2. Navigate to Edit Tour Package screen
3. Click "Change Image" button
4. **Expected**: iOS Photos app opens
5. Select any image from Photos
6. **Expected**: Image preview displays with "NEW" badge
7. Verify snackbar shows: "Image selected successfully"
8. Edit other fields if desired
9. Click "Update Package"
10. **Expected**: Loading indicator shows
11. **Expected**: Image uploads to Firebase Storage
12. **Expected**: Navigation returns to package list
13. **Expected**: Package displays new image

#### Success Criteria:
- ✅ Photos app opens without errors
- ✅ Image can be selected
- ✅ Preview shows selected image
- ✅ "NEW" badge visible
- ✅ Upload completes
- ✅ New image in package list

---

### Test 3: Desktop (JVM)

#### Setup:
```bash
./gradlew run
```

#### Test Steps:
1. Launch desktop app
2. Navigate to Edit Tour Package screen
3. Click "Change Image" button
4. **Expected**: Native file picker opens
5. Browse to any image file and select it
6. **Expected**: Image preview displays with "NEW" badge
7. Verify snackbar shows: "Image selected successfully"
8. Edit other fields if desired
9. Click "Update Package"
10. **Expected**: Loading indicator shows
11. **Expected**: Image uploads to Firebase Storage
12. **Expected**: Navigation returns to package list
13. **Expected**: Package displays new image

#### Success Criteria:
- ✅ File picker opens without errors
- ✅ Image can be selected
- ✅ Preview shows selected image
- ✅ "NEW" badge visible
- ✅ Upload completes
- ✅ New image in package list

---

## 🔍 Error Handling Tests

### Test 4: Invalid File Type

#### Steps:
1. Click "Change Image"
2. Select a non-image file (PDF, TXT, etc.)
3. **Expected**: Snackbar shows "Please select a valid image file"
4. **Expected**: No preview updates
5. **Expected**: No error crash

---

### Test 5: Large File

#### Steps:
1. Click "Change Image"
2. Select a large image file (10+ MB)
3. **Expected**: File loads successfully
4. **Expected**: Preview displays
5. Click "Update Package"
6. **Expected**: Upload progresses
7. **Expected**: Upload completes (may take longer)
8. **Expected**: No memory issues

---

### Test 6: Cancel Selection

#### Steps:
1. Click "Change Image"
2. Cancel the file picker (close without selecting)
3. **Expected**: Nothing happens
4. **Expected**: No errors
5. **Expected**: Screen remains unchanged

---

### Test 7: Multiple Image Changes

#### Steps:
1. Click "Change Image"
2. Select Image A
3. **Expected**: Image A preview shows
4. Click "Change Image" again
5. Select Image B
6. **Expected**: Image B preview shows
7. Click "Update Package"
8. **Expected**: Image B uploads and saves
9. Verify in package list that Image B is displayed

---

### Test 8: Network Failure

#### Steps:
1. Click "Change Image"
2. Select an image
3. Disconnect internet
4. Click "Update Package"
5. **Expected**: Snackbar shows network error
6. **Expected**: No crash
7. **Expected**: User can retry when online

---

## 📊 Firebase Integration Tests

### Test 9: Verify Firebase Upload

#### Steps:
1. Complete a successful image change (Test 1, 2, or 3)
2. Check Firebase Console:
   - Go to Storage bucket
   - Look for `/tour_packages_images/` folder
   - **Expected**: New file like `tour_1717418400000.jpg`
   - **Expected**: File has correct image content
   - **Expected**: File size matches uploaded file

---

### Test 10: Verify Firestore Update

#### Steps:
1. Complete a successful image change
2. Check Firebase Console:
   - Go to Firestore Database
   - Find the edited tour package document
   - **Expected**: `imageUrl` field contains Firebase Storage URL
   - **Expected**: URL format: `https://firebasestorage.googleapis.com/...`
   - **Expected**: URL is accessible in browser

---

### Test 11: Verify Image Display

#### Steps:
1. Complete successful image change
2. Return to package list
3. **Expected**: Package card shows new image
4. Click on package to open detail view
5. **Expected**: Image displays correctly
6. Return to edit screen
7. **Expected**: Old image replaced with new one

---

## 🎨 UI/UX Tests

### Test 12: Badge Display

#### Steps:
1. Open edit screen with existing image
2. Click "Change Image"
3. Select new image
4. **Expected**: Preview shows image
5. **Expected**: Green "NEW" badge visible in top-right corner
6. **Expected**: Badge is readable and not cut off

---

### Test 13: Snackbar Messages

#### Steps:
1. Test various scenarios and verify snackbar messages:
   - ✅ "Image selected successfully" - when image selected
   - ✅ "Please select a valid image file" - when invalid file
   - ✅ "Error loading image: {error}" - when file read fails
   - ✅ "Image upload failed" - when Firebase upload fails

---

### Test 14: Loading States

#### Steps:
1. Select image
2. Click "Update Package"
3. While uploading:
   - **Expected**: Button shows loading spinner
   - **Expected**: Button is disabled
   - **Expected**: Loading indicator in center (if still loading)
4. After upload:
   - **Expected**: Button shows text again
   - **Expected**: Button is enabled
   - **Expected**: Navigation occurs

---

## 🚀 Regression Tests

### Test 15: Existing Features Still Work

#### Steps:
1. Verify other edit fields still work:
   - ✅ Can edit Title
   - ✅ Can edit Location
   - ✅ Can edit Duration
   - ✅ Can edit Price
   - ✅ Can edit Description
   - ✅ Can edit Included Services
2. Verify validation still works:
   - ✅ Required fields show errors
   - ✅ Invalid values rejected
3. Verify update without image change works:
   - ✅ Edit title only
   - ✅ Click Update
   - ✅ Successfully saves (existing image preserved)

---

## 📱 Cross-Platform Compatibility

### Supported Formats Test

| Format | Android | iOS | Desktop | Status |
|--------|---------|-----|---------|--------|
| JPG | ✅ | ✅ | ✅ | Supported |
| JPEG | ✅ | ✅ | ✅ | Supported |
| PNG | ✅ | ✅ | ✅ | Supported |
| GIF | ✅ | ✅ | ✅ | Supported |
| WebP | ✅ | ✅ | ✅ | Supported |
| BMP | ❌ | ❌ | ❌ | Not Supported |
| TIFF | ❌ | ❌ | ❌ | Not Supported |

---

## ✅ Final Verification Checklist

Before declaring complete, verify:

- [ ] Gradle syncs without errors
- [ ] Project builds successfully
- [ ] No compilation warnings
- [ ] All tests pass on Android
- [ ] All tests pass on iOS
- [ ] All tests pass on Desktop
- [ ] Firebase upload works
- [ ] Firestore updates correctly
- [ ] Images display in package list
- [ ] Navigation works properly
- [ ] Error messages appear correctly
- [ ] Loading states display correctly
- [ ] Invalid files rejected
- [ ] Large files handled correctly
- [ ] Multiple image changes work
- [ ] Existing features not broken
- [ ] Badge displays correctly
- [ ] Snackbars show correct messages

---

## 🎯 Success Criteria

Project passes if:
✅ All manual tests pass on all platforms
✅ Firebase integration verified
✅ No compilation errors
✅ No runtime errors
✅ User can successfully change images
✅ Images appear in package list
✅ No regression in existing features

---

## 📝 Bug Report Template

If issues found:

```
Title: [Bug Title]
Platform: Android / iOS / Desktop
Steps to Reproduce:
1. ...
2. ...
3. ...

Expected:
...

Actual:
...

Error Message:
...

Logs:
...
```

---

## 🎉 Completion

Once all tests pass:
1. ✅ Mark as verified
2. ✅ Ready for production
3. ✅ Deploy to app stores
4. ✅ Announce feature to users

**Image picker implementation is complete and tested!** 🚀

