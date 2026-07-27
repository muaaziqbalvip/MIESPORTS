# MI ESPORT — Setup Guide

Poora setup step-by-step. Sab kuch is order mein karein.

## 0. Gradle Wrapper — Ab Automatic Hai

Is zip mein `gradlew` script aur `gradle-wrapper.jar` shamil nahi hain (ye generated files hain), **lekin GitHub Actions workflow khud inhe generate kar leta hai** build ke waqt — aapko kuch manually karne ki zaroorat nahi.

Agar aap **local machine par** (Android Studio) build karna chahte hain, to Android Studio khud gradlew detect karke wrapper generate kar dega jab aap project open karenge. Ya terminal mein:
```
gradle wrapper --gradle-version 8.9
```

## 1. Firebase Project Banayein

1. https://console.firebase.google.com par jayein → **Add Project** → naam "MI ESPORT" rakhein.
2. Project ke andar **Android app add karein**:
   - Package name: `com.miesport.app`
   - SHA-1 (ye humesha fixed rahegi, keystore is zip mein `app/release.keystore` mein maujood hai):
     ```
     EF:C3:28:3A:D7:18:91:E0:CB:62:11:C5:9D:AF:4D:F6:AE:11:E8:8C
     ```
   - SHA-256 (agar Firebase maange):
     ```
     53:8C:72:7F:95:AA:58:05:AE:DE:9B:56:86:0E:34:6A:9A:F9:06:CC:78:E1:27:12:A5:0A:8B:86:42:96:B5:76
     ```
   - `google-services.json` download karein → `app/google-services.json` mein daalein (local build ke liye)
3. Enable karein:
   - **Authentication** → Sign-in method → Google + Email/Password enable karein
   - **Firestore Database** → Production mode mein create karein
   - **Realtime Database** → Create karein (Firestore ke barabar region choose karein)
   - **Cloud Messaging** → automatically enabled hota hai
   - **App Check** → Play Integrity provider enroll karein (release ke liye), Debug provider dev ke liye
4. **Web app bhi add karein** (Admin Panel ke liye) → config values copy karke `admin-panel/js/firebase-config.js` mein daalein.
5. Firestore mein manually ek document banayein: `admins/{apka-uid}` = `{ role: "superadmin" }` — ye aapko admin panel access dega. UID Firebase Authentication tab se milega jab aap pehli dafa sign up karenge.

## 1.5 Keystore Details (PERMANENT — is zip mein already banaya hua hai)

Keystore file: `app/release.keystore` (isko kabhi delete/replace na karein warna SHA-1 badal jayegi aur Google Sign-In tootjayegi)

```
storeFile     = release.keystore
storePassword = MiEsport@2026
keyAlias      = mi_esport_key
keyPassword   = MiEsport@2026
```

Ye values already `keystore.properties` file mein hain (local build ke liye) aur GitHub Secrets mein bhi daalni hain (neeche section 4 dekhein).

**Zaroori:** `app/release.keystore` file ko kahin safe backup rakhein (Google Drive, USB). Agar ye kho gayi to future updates purani app ke upar install nahi ho sakengi (Play Store isko reject karega) — aapko naya app entry banani padegi.

## 2. Deploy Security Rules

Firebase CLI install karein (`npm install -g firebase-tools`), phir:
```
firebase login
firebase init firestore database
firebase deploy --only firestore:rules,database
```
Ye `firestore.rules` aur `database.rules.json` files (already project root mein maujood hain) deploy kar dega.

## 3. Android App — SHA-1 Fix

Aapne jo SHA-1 diya: `E3:B0:75:C4:B6:E6:0A:CB:82:1E:15:CE:0B:9E:74:28:FD:65:61:66`

Ye Firebase Console mein Android app settings ke andar **dono jagah add karein**:
- Debug SHA-1 (development ke liye, local machine se `./gradlew signingReport` se milta hai)
- Release SHA-1 (ye aapka diya hua hai — apni release keystore se generate hua)

Firebase Console → Project Settings → Your apps → (Android app) → Add fingerprint → paste karein.

**Google Sign-In ke liye Web Client ID bhi zaroori hai:**
`app/src/main/java/com/miesport/app/MainActivity.kt` mein ye line update karein:
```kotlin
private val webClientId = "REPLACE_WITH_FIREBASE_WEB_CLIENT_ID"
```
Ye value Firebase Console → Project Settings → General → "Web SDK configuration" ya `google-services.json` ke andar `"client_type": 3` wali entry mein milegi.

## 4. GitHub Actions Secrets

Repo Settings → Secrets and variables → Actions → ye 5 secrets add karein:

| Secret Name | Value |
|---|---|
| `GOOGLE_SERVICES_JSON` | Firebase se download ki `google-services.json` ka poora content, base64 encoded (`base64 -w0 google-services.json`) |
| `KEYSTORE_BASE64` | Is zip mein `keystore-base64.txt` file ka poora content (already encode kiya hua hai — bas copy-paste karein) |
| `KEYSTORE_PASSWORD` | `MiEsport@2026` |
| `KEY_ALIAS` | `mi_esport_key` |
| `KEY_PASSWORD` | `MiEsport@2026` |

`keystore-base64.txt` is zip ke root mein hai — usko kholein, poora text select karein, `KEYSTORE_BASE64` secret mein paste kar dein.

Push karte hi `main` branch par, GitHub Actions automatically APK build karega (isi keystore se sign hoga, SHA-1 hamesha `EF:C3:28:3A:D7:18:91:E0:CB:62:11:C5:9D:AF:4D:F6:AE:11:E8:8C` rahegi) aur GitHub Releases mein publish kar dega.

## 5. Admin Panel Deploy (Vercel)

```
cd admin-panel
vercel --prod
```
Ya Vercel dashboard se `admin-panel` folder ko import kar dein. `js/firebase-config.js` mein apne Firebase web config values daalna na bhoolein.

## 6. Project Structure

```
mi-esport/
├── app/                     ← Android app (Kotlin + Jetpack Compose)
│   └── src/main/java/com/miesport/app/
│       ├── ui/screens/      ← Login, Home, Tournament, Wallet, Live, Profile, etc.
│       ├── ui/theme/        ← Premium dark theme, colors, glassmorphism
│       ├── ui/components/   ← Reusable GlassCard, BottomNav, PulsingDot
│       ├── data/model/      ← Firestore/RTDB data models
│       ├── data/firebase/   ← Auth, Firestore, Realtime repositories
│       └── navigation/      ← NavHost + routes
├── admin-panel/             ← Web admin panel (vanilla JS + Firebase SDK)
├── .github/workflows/       ← CI/CD build + release
├── firestore.rules          ← Security rules
└── database.rules.json      ← RTDB security rules
```

## 7. Abhi Kya Kaam Karta Hai

- Login (Google + Email/Password + Forgot Password)
- Home (hero banner, live tournaments, featured events)
- Tournament list + filter (Solo/Duo/Squad) + detail + registration
- Room ID/Password reveal via Realtime Database (admin publish karta hai)
- Wallet (deposit/withdraw request + transaction history)
- Live (YouTube embed jab admin live status set kare)
- Leaderboard (monthly/season toggle)
- Teams (create/join)
- Rewards (daily/referral/lucky draw UI — backend logic Cloud Functions mein add karna hoga)
- Notifications list
- Profile + Sign Out
- Admin Panel: tournaments CRUD, registrations verify, wallet approve/reject, user ban/unban, room ID publish, live status update, notification queue

## 8. Aage Kya Karna Hai (Cloud Functions — abhi included nahi)

- FCM push notifications ka actual delivery (abhi sirf Firestore mein record likha jata hai — ek Cloud Function chahiye jo `broadcast_notifications` aur `notifications/{uid}/items` ko trigger karke Admin SDK se FCM bhejay)
- Referral reward automation
- Daily login streak tracking
- Leaderboard auto-computation (wins/earnings se points calculate karke `leaderboard/{period}/entries` update karna)
- Screenshot upload to Firebase Storage (abhi UI placeholder hai, image picker + upload logic add karni hai)
