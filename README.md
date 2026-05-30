# Smart Mail Sender

Smart Mail Sender is a simple Android app to compose, save, and send email drafts.

## What I fixed
- Resolved an InflateException in `fragment_compose.xml` by ensuring `Chip` views had `layout_width`/`layout_height`.
- Removed a duplicate fragment app bar to avoid header overlap; the activity toolbar is used as the primary app bar.

## Build & Run
Requirements: JDK, Android SDK, and an emulator or device.

From the project root:
```bash
./gradlew clean assembleDebug
./gradlew installDebug
adb shell monkey -p com.example.smartmailsender -c android.intent.category.LAUNCHER 1
```

Or open the project in Android Studio and run the `app` configuration.

## Notes
- The `fragment_compose.xml` layout was updated. See `app/src/main/res/layout/fragment_compose.xml` for details.
- This repository was initialized and the changes pushed to `origin/main`.

## Contributing
Open an issue or send a PR — happy to accept improvements.

## License
MIT
