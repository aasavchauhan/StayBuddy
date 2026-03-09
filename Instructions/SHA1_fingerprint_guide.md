# SHA-1 Fingerprint Guide

## Your Debug SHA-1

```
E7:4D:03:9C:60:07:CD:54:33:22:A3:41:C5:E2:D0:ED:05:A7:DF:63
```

## How to Get it Again

Run this command in your terminal:

```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android
```

## Where to Register

1. Go to [Firebase Console](https://console.firebase.google.com/project/stay-buddy-9d294/settings/general)
2. Project Settings → Your Apps → Android app
3. Click "Add fingerprint"
4. Paste the SHA-1 value
5. Click Save

## Why It's Needed

- **Google Sign-In** requires SHA-1 to verify your app
- **Phone Authentication (OTP)** also uses it for verification
- You need BOTH debug and release fingerprints (release when you build APK/AAB for store)

## Getting Release SHA-1

When you create a release keystore later:

```bash
keytool -list -v -keystore your-release-key.jks -alias your-alias -storepass your-password
```
