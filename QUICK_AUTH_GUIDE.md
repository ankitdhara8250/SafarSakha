# Authentication Flow - Quick Integration Guide

## 📋 Summary of Changes

### **What Was Implemented:**

✅ **Complete User Authentication System** with Login and Registration
✅ **Clean Architecture** - Properly separated layers (Domain, Data, Presentation)
✅ **MVVM Pattern** - ViewModels with StateFlow for reactive UI
✅ **Firebase Integration** - Authentication + Firestore
✅ **Error Handling** - Comprehensive validation and user feedback
✅ **Threading** - Proper coroutine management with IO/Main dispatchers

---

## 🗂️ New Files Created

### **Domain Layer:**
- `domain/model/User.kt` - User data model
- `domain/usecase/auth/LoginUserUseCase.kt` - Login logic
- `domain/usecase/auth/RegisterUserUseCase.kt` - Registration logic

### **Data Layer:**
- `data/remote/firebase/auth/FirebaseAuthDataSource.kt` - Firebase operations
- `data/repository/impl/AuthRepositoryImpl.kt` - Repository implementation

### **Presentation Layer:**
- `presentation/screens/user/profile/UserProfileViewModel.kt` - Login ViewModel
- `presentation/screens/user/profile/UserProfileUiState.kt` - Login UI state
- `presentation/screens/user/profile/UserProfileEvent.kt` - Login events
- `presentation/screens/profile/registration/UserRegisterViewModel.kt` - Registration ViewModel
- `presentation/screens/profile/registration/UserRegisterUiState.kt` - Registration UI state
- `presentation/screens/profile/registration/UserRegisterEvent.kt` - Registration events

### **Platform-Specific:**
- `androidMain/kotlin/com/safarsakha/Platform.kt` - Android implementation
- `iosMain/kotlin/com/safarsakha/Platform.kt` - iOS implementation
- `jvmMain/kotlin/com/safarsakha/Platform.kt` - JVM implementation
- `commonMain/kotlin/com/safarsakha/Platform.kt` - Common interface

---

## 📝 Updated Files

### **Screens (Already Existed, Now ViewModel-Ready):**
- ✅ `UserProfileScreen.kt` - Updated to use ViewModel
- ✅ `UserRegisterScreen.kt` - Updated to use ViewModel

### **Domain:**
- ✅ `domain/repository/AuthRepository.kt` - Added user login/register methods

### **Navigation:**
- ✅ `presentation/navigation/AppNavigation.kt` - ViewModel injection in routes

---

## 🔌 How to Use

### **1. Login Flow in Your App:**

```kotlin
// The navigation automatically handles this:
AppNavKey.UserProfile -> {
    val authRepository = provideAuthRepository()
    val loginUserUseCase = remember { LoginUserUseCase(authRepository) }
    val viewModel = remember { UserProfileViewModel(loginUserUseCase) }
    
    UserProfileScreen(
        viewModel = viewModel,
        onRegisterClick = { /* go to register */ },
        onAdminLoginClick = { /* go to admin */ },
        onLoginSuccess = { /* user logged in */ }
    )
}
```

### **2. Registration Flow:**

```kotlin
// Similar automatic handling
AppNavKey.UserRegister -> {
    val authRepository = provideAuthRepository()
    val registerUserUseCase = remember { RegisterUserUseCase(authRepository) }
    val viewModel = remember { UserRegisterViewModel(registerUserUseCase) }
    
    UserRegisterScreen(
        viewModel = viewModel,
        onBackToLogin = { /* back */ },
        onRegistrationSuccess = { /* registered */ }
    )
}
```

---

## 🧪 Testing the Implementation

### **Test Login:**
1. Navigate to UserProfile screen
2. Enter email: `test@example.com`
3. Enter password: `password123`
4. Click Login
5. Should authenticate with Firebase

### **Test Registration:**
1. Click "Register Here"
2. Fill in all fields
3. Enter matching passwords (min 6 chars)
4. Click Register
5. Should create account and show success

### **Test Validation:**
1. Try to login with empty email
2. Should see error: "Email cannot be empty"
3. Try register with mismatched passwords
4. Should see error: "Passwords do not match"

---

## 🔐 Firebase Setup Required

### **Before testing, configure:**

1. **Enable Email/Password Authentication:**
   - Firebase Console → Authentication
   - Sign-in method → Email/Password → Enable

2. **Create Firestore Database:**
   - Firebase Console → Firestore Database
   - Create collection named `users`
   - Set security rules (example):
     ```
     match /users/{userId} {
       allow read, write: if request.auth.uid == userId;
     }
     ```

3. **google-services.json:**
   - Already in `androidApp/` folder
   - Add to iOS app if needed

---

## 🔄 Data Flow Example

### **Login Sequence:**
```
User Input
  ↓
UserProfileScreen
  ↓
onEvent(OnLoginClick)
  ↓
UserProfileViewModel.handleLogin()
  ↓
LoginUserUseCase.invoke()
  ↓
AuthRepository.loginUser()
  ↓
FirebaseAuthDataSource.loginUser() + getUserFromFirestore()
  ↓
Firebase Auth + Firestore
  ↓
Result wrapped in Resource<User>
  ↓
ViewModel updates StateFlow
  ↓
Screen observes State
  ↓
Navigation on success
```

---

## 🎨 State Management

### **ViewModel maintains:**
```kotlin
// Login
data class UserProfileUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false
)

// Registration
data class UserRegisterUiState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationSuccess: Boolean = false
)
```

---

## 🧵 Threading

All network operations happen on `Dispatchers.IO`:
```kotlin
// In FirebaseAuthDataSource
suspend fun loginUser(...) = withContext(Dispatchers.IO) {
    // Firebase calls here
}

// ViewModels update UI on Main dispatcher
viewModelScope.launch {
    // UI state updates here
}
```

---

## ✨ Error Handling

### **Validation Errors:**
- Empty fields checked before submission
- Password length validation (min 6 chars)
- Password matching validation
- User-friendly error messages

### **Firebase Errors:**
- Authentication failures
- Network errors
- Firestore errors
- All wrapped in `Resource.Error`

---

## 🚀 Next Steps

1. **Test with Firebase:**
   - Add test user in Firebase Console
   - Test login with credentials
   - Verify Firestore document created

2. **UI Refinements:**
   - Add eye icon to show/hide password
   - Add forgot password link
   - Add social login buttons (optional)

3. **User Experience:**
   - Add loading skeletons
   - Add success snackbars
   - Add logout functionality

4. **Security:**
   - Add Firestore security rules
   - Add rate limiting (optional)
   - Add 2FA (optional)

---

## 📊 Architecture Diagram

```
┌─────────────────────────────────┐
│    Presentation Layer           │
│  ┌───────────────────────────┐  │
│  │  Screen                   │  │
│  │  ├─ UserProfileScreen     │  │
│  │  └─ UserRegisterScreen    │  │
│  └────────┬────────────────┬─┘  │
│           │                │    │
│  ┌────────▼──────────────┐ │    │
│  │ ViewModel             │ │    │
│  │ ├─ State (StateFlow)  │ │    │
│  │ └─ Events             │ │    │
│  └────────┬──────────────┘ │    │
└───────────┼──────────────────────┘
            │
┌───────────▼──────────────────────┐
│    Domain Layer                   │
│  ┌───────────────────────────┐   │
│  │ UseCase                   │   │
│  │ ├─ LoginUserUseCase       │   │
│  │ └─ RegisterUserUseCase    │   │
│  └────────┬────────────────┬─┘   │
│           │                │     │
│  ┌────────▼──────────────┐ │     │
│  │ Repository Interface  │ │     │
│  │ └─ AuthRepository     │ │     │
│  └────────┬──────────────┘ │     │
└───────────┼──────────────────────┘
            │
┌───────────▼──────────────────────┐
│    Data Layer                     │
│  ┌───────────────────────────┐   │
│  │ Repository Implementation │   │
│  │ └─ AuthRepositoryImpl      │   │
│  └────────┬──────────────────┘   │
│           │                       │
│  ┌────────▼──────────────────┐   │
│  │ Data Source               │   │
│  │ └─ FirebaseAuthDataSource │   │
│  └────────┬──────────────────┘   │
└───────────┼──────────────────────┘
            │
┌───────────▼──────────────────────┐
│  Firebase                         │
│  ├─ Authentication               │
│  └─ Firestore Database           │
└───────────────────────────────────┘
```

---

## 📚 File Sizes & Complexity

| File | Lines | Purpose |
|------|-------|---------|
| User.kt | 15 | Data model |
| AuthRepository.kt | 18 | Interface |
| FirebaseAuthDataSource.kt | 85 | Firebase ops |
| AuthRepositoryImpl.kt | 75 | Repository impl |
| LoginUserUseCase.kt | 20 | Business logic |
| RegisterUserUseCase.kt | 25 | Business logic |
| UserProfileViewModel.kt | 90 | Login state |
| UserProfileScreen.kt | 291 | Login UI |
| UserRegisterViewModel.kt | 112 | Register state |
| UserRegisterScreen.kt | 283 | Register UI |

---

## 🎯 Implementation Status

- ✅ Domain layer complete
- ✅ Data layer complete
- ✅ Presentation layer complete
- ✅ Platform implementations complete
- ✅ Navigation integration complete
- ✅ Error handling complete
- ✅ State management complete
- ✅ Threading model complete
- ✅ Documentation complete

---

## 🔗 Key Classes Reference

| Class | Location | Purpose |
|-------|----------|---------|
| User | domain.model | User data model |
| UserRole | domain.model | Role enum |
| AuthRepository | domain.repository | Repository interface |
| LoginUserUseCase | domain.usecase.auth | Login logic |
| RegisterUserUseCase | domain.usecase.auth | Register logic |
| FirebaseAuthDataSource | data.remote.firebase.auth | Firebase integration |
| AuthRepositoryImpl | data.repository.impl | Repository implementation |
| UserProfileViewModel | presentation.screens.user.profile | Login ViewModel |
| UserRegisterViewModel | presentation.screens.profile.registration | Register ViewModel |
| Resource | core.utils | Result wrapper |

---

**Status: IMPLEMENTATION COMPLETE ✅**

All files are properly integrated and ready for testing with Firebase.

