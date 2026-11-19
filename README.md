# Mi

A simple, offline anime library for Android. Save the shows you care about with a poster and a watch link, sort them into lists, and open them with one tap.

## Features

- **Poster grid** of your saved anime
- **Three tabs:** Watching, Completed, Plan to Watch
- **One-tap watch:** tap a poster to open its link (Bilibili, Crunchyroll, or any website)
- **Long-press a poster** to move it to another tab or delete it
- **Works offline:** your list is stored on your phone (Room database)
- **Smart links:** a link without `https://` is fixed automatically
- **Fallback poster:** if a poster URL is blank or broken, the title is shown instead

## Download the app

1. Open the **Actions** tab of this repository.
2. Click the latest successful run (green check).
3. Under **Artifacts**, download **app-debug** and unzip it.
4. Send `app-debug.apk` to your Android phone and open it to install. If Android asks, allow installing from your browser or file manager.

Every build is signed with the same key (`app/debug.keystore`), so updating over an older install keeps your saved list.

## How to use

1. Tap the **+** button.
2. Enter a title, an optional poster image URL, and the watch link.
3. Pick a category and tap **Save**.
4. Tap a poster to watch. Long-press it to move or delete.

**Tip:** for a poster, open an image in your browser, copy the image address (it usually ends in `.jpg` or `.png`), and paste it in.

## Built with

- Kotlin
- Jetpack Compose (Material 3)
- Room (offline database)
- Coil (image loading)
- GitHub Actions (automatic APK builds)

## Project structure

```
app/src/main/java/com/example/animelibrary/
  MainActivity.kt      screens and UI
  AnimeViewModel.kt    app logic and link cleanup
  Data.kt              database, categories, and data model
.github/workflows/build.yml   builds the APK on every push
```

## Building

There's nothing to install. Push to GitHub and the workflow builds the APK automatically. You can also run it manually from **Actions → Build APK → Run workflow**.

Requires Android 8.0 (API 26) or newer.
