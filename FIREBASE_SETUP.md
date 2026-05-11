# Firebase Setup

1. Go to https://console.firebase.google.com/
2. Create project "StandBy-Us"
3. Register Android app with package `com.standbyus.app`
4. Download `google-services.json` → place in `StandByUs/app/` directory
5. Enable **Anonymous Authentication** (Authentication → Sign-in method → Anonymous → Enable)
6. Create **Firestore Database** (Firestore → Create database → Start in test mode)
7. Set Firestore security rules after creation:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

8. (Optional) Enable Cloud Messaging for FCM
