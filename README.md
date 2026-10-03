<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

## Supabase connection

The Android client includes a Supabase REST boundary configured for project
`vrdtilzypkizvqganotu`. Keep the project URL, publishable key, and JWKS URL in
the ignored `.env` file; `.env.example` contains the public project settings.
Supabase Row Level Security must protect every table.

Never add `SUPABASE_SECRET_KEY` or a service-role key to the Android project,
Git, or a released APK. Server-only operations belong in the Hostinger backend
or a Supabase Edge Function. Hostinger remains the source of truth for the
existing auth, appointments, referrals, and messages while the Realtime and
Storage migration is completed behind `app/src/main/java/com/example/data/supabase/`.

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/9d413859-ce46-4aca-9617-302c82885fe2

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.
