# Firebase authentication setup

The Android app uses Firebase Authentication email/password accounts. Its Android application ID is `com.aistudio.yawarhamdard.care`.

## Connect the Firebase project

1. In Firebase Console, create or select the Yawar Hamdard project, then register an Android app with the application ID above.
2. Download that app's `google-services.json` and place it at `app/google-services.json`.
3. In **Authentication → Sign-in method**, enable **Email/Password**. Keep email verification enabled; the app only enters the portal after verification.
4. Set the password policy to require at least 8 characters and enable email-enumeration protection in Firebase Authentication.
5. Review the verification and password-reset email templates and set the authorized support contact.

`google-services.json` contains public project identifiers, not an admin credential. Never put a Firebase Admin SDK service-account key, private signing key, or user password in this repository.

## Staff role assignment

New accounts have patient access by default. The app never accepts a role chosen in the UI. Doctor and coordinator screens require signed Firebase ID-token custom claims assigned by a trusted administrator using the Firebase Admin SDK in a protected server environment. Never assign claims from Android client code or expose an Admin SDK credential in the app.

Use the claim values `doctor` and `admin` for the `role` claim. After claims are assigned, the user must sign out and back in (or force-refresh the ID token) for the role to take effect. Protect every server data endpoint with token verification and role checks; hiding a screen in the app is not authorization.

## App Check and release

Firebase Authentication handles identity, email verification, and password resets. App Check is a separate control for attesting that requests come from a genuine app. This project currently has debug/reCAPTCHA App Check libraries but no production provider initialization. Before enforcing App Check, configure a release provider appropriate for the distribution channel, register the app and signing certificate in Firebase, then monitor requests before enabling enforcement.

## Current data limitation

The app's care records are still Room-backed sample/local data. Firebase sign-in does not turn those records into secure shared cloud records. Do not use the current sample data store for real patient or insurance records; a server-backed data model, per-user access rules, and operational privacy controls are still required for that.
