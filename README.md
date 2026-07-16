# Exile

A free, open-source Android app that automatically exits YouTube Shorts the
moment they open. no subscription, no ads, no account, no data collection.

Built because every existing "block Shorts" app on the Play Store gated the
real fix behind a paywall. This one is free forever and the full source is
right here.

## Download

**[Download the APK](https://exileweb.pages.dev)**

Android will warn about installing outside the Play Store — this is normal
for any app distributed independently, not a sign of a problem. Tap through
to allow it, then install.

You can also build it yourself (see below).

## What it does

Uses Android's built-in Accessibility Service API to watch the YouTube app.
When it detects the Shorts player is open (`reel_recycler`,
`reel_player_page_container`, etc. — YouTube's own internal view names), it
immediately triggers a "back" action to close it. That's it. No modification
to the YouTube app itself, no root required.

## Privacy

- No internet permission — the app cannot make network requests, period.
- No data is logged, stored, or transmitted anywhere.
- Only reads on-screen structure (view IDs / button labels) while YouTube is
  the active app in the foreground — never other apps, never your other
  activity.
- 100% of the source is in this repo. Nothing is obfuscated or hidden.

## Installing

1. [Download the APK](https://exileweb.pages.dev) and install it — Android
   will warn about installing outside the Play Store, which is normal for
   independently distributed apps. Tap through to allow it.

   **Or build it yourself:**
   1. Install [Android Studio](https://developer.android.com/studio) (free).
   2. Clone this repo.
   3. Open the project folder in Android Studio and let it sync.
   4. Connect an Android phone via USB with **USB debugging** enabled
      (Settings → About phone → tap Build number 7x → Developer options →
      enable USB debugging).
   5. Click **Run**, select your device. It installs directly.

2. Open **Exile** → tap **"Open Accessibility Settings."**
3. Find **Exile** in the list → turn it on.
4. Open YouTube, tap a Short — it should immediately back out.

## Contributing

YouTube periodically changes the internal view IDs it uses for the Shorts
player, which can break detection after an app update. If Shorts stop being
blocked:

1. Open `app/src/main/java/com/shortsblocker/app/ShortsBlockerService.kt`.
2. Set `DEBUG_DUMP_IDS = true`, rebuild, and reinstall.
3. Enable the accessibility service, open YouTube Shorts, and watch Logcat
   filtered by `tag:ShortsBlockerDebug` — it dumps every view ID/label
   currently on screen.
4. Find the new Shorts-related identifiers and add them to the
   `SHORTS_ID_MARKERS` list.
5. Set `DEBUG_DUMP_IDS` back to `false`.
6. Open a pull request with the updated markers — this helps everyone using
   the app, not just you.

Issues and PRs are welcome. This is a small, single-purpose tool, so please
keep changes focused (no telemetry, no ads, no new permissions without a
strong reason — that's the whole point of this project).

## Limitations

- Android only. iOS doesn't allow this kind of accessibility-based control
  over another app's UI, so there's no equivalent build for iPhone.
- Only affects the YouTube app specifically (`com.google.android.youtube`),
  not YouTube in a mobile browser.
- Android requires a persistent notification/indicator while any
  accessibility service is active — this is an OS-level requirement, not
  something this app can suppress.
- Some phone manufacturers (Vivo/iQOO/Xiaomi/etc.) aggressively kill
  background services to save battery. If blocking stops working after a
  while, check your phone's battery/background-activity settings for this
  app and allow it to run unrestricted.

## License

MIT — do whatever you want with it, including forking it, selling support
for it, or building your own version. Attribution appreciated but not
required.
