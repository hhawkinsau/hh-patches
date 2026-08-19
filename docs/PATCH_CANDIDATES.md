# Top 5 patch candidates

Analyzed APKPure XAPKs for the versions this repo already targets. The binaries are not in git (this repo does not host or redistribute the apps).

| App | Package | Version | XAPK SHA-256 | ABI in this XAPK |
| --- | --- | --- | --- | --- |
| SafePix | `com.nubestour.safepix` | 1.1.5 (15) | `ba276b6e55d7e2ddc421106112585e62228a7f308d49e88a8361e3515ba9d5a7` | `arm64-v8a` |
| Punge | `com.markatlarge.scrub` | 3.1.4 (82) | `a4eba0e5d45118111ec65566dcecd7954796596d39103ad2338f1e50758a77b7` | `armeabi-v7a` only |

Both are Flutter apps wrapped by Google Play Automatic Integrity Protection (PairIP). `application` is `com.pairip.application.Application`. Existing patches already cover custom names, Firebase/ad-ID analytics flags, and SafePix’s Play Store bounce.

## Already shipped (do not redo)

- Custom app name (both)
- Disable analytics (both) — manifest flags, ad-ID permissions, DataTransport senders
- Disable Play Store redirect (SafePix only)

## Top 5

Ranked by user impact, fit with the privacy / sideload mission, and how much can be done with Morphe resource/bytecode patches (DEX) rather than Dart AOT (`libapp.so`).

### 1. Disable Play Store redirect for Punge

**Why first:** Sideloaded / re-signed Punge will hit the same PairIP license wall SafePix already had. The Punge base APK contains `com.pairip.licensecheck.LicenseActivity`, `LicenseClient`, `SignatureCheck`, and `com.android.vending.CHECK_LICENSE`. The Application class is PairIP’s, not Flutter’s.

**How:** Reuse `DisablePlayStoreRedirectPatch` + `spoofPlayStoreInstaller()` against `COMPATIBILITY_PUNGE`. Fingerprints already match the PairIP class names.

**Risk:** Low. Same approach as SafePix v1.1.0.

### 2. Hide ads for Punge

**Why:** Play lists Punge as “Contains ads”. The APK has a real AdMob app id (`ca-app-pub-2625767131604446~4692116258`), `AdActivity`, `MobileAdsInitProvider`, `AdService`, banner + interstitial Dart (`package:expunge/services/admob_service.dart`, `banner_ad_widgets/expunge_banner_ad.dart`), and two unit ids. Interstitials are skipped for some users (`Skipping interstitial: only r…` in `libapp.so`).

**How:**
- Resource: drop `com.google.android.gms.ads.APPLICATION_ID`, disable ad activities/providers/services, strip AdServices permissions (partially overlaps the analytics patch).
- Bytecode: no-op `MobileAds.initialize` / Flutter `plugins.flutter.io/google_mobile_ads` so the Dart UI does not sit on a failed load forever.

**Risk:** Medium. Resource-only may leave empty banner slots; a method-channel stub is cleaner. This hides ads. It does not unlock paid bulk-manage / unblur (see out of scope).

### 3. Disable Firebase Remote Config for Punge

**Why:** The current analytics patch turns off collection flags and DataTransport. Punge still ships a live Remote Config stack:

- `package:expunge/services/remote_config_service.dart`
- Flutter Firebase Remote Config registrars in the manifest
- `AppMeasurementService` / `AppMeasurementReceiver` still enabled
- Analytics events such as `paywall_shown`, `paywall_shown_blurred_item`, `punge_photo_upgrade_to_video`

Remote Config is how Play apps change ad frequency, paywall copy, and feature flags after install. That is a bigger privacy hole than the flags already patched, and it can fight local patches.

**How:** Extend `disableAnalytics()` for Punge: disable `FirebaseInitProvider`, Remote Config / Measurement components, and `firebase_remote_config` method-channel calls. Keep Crashlytics off (already flagged).

**Risk:** Low–medium. If a scan path waits on fetch-and-activate, default to the in-binary defaults (fail closed / local).

### 4. Custom launcher icons (SafePix + Punge)

**Why:** Manager and the device still show the stock icons. Names already have an “HH” option. Icon is the other half of “this is the patched install next to the Play one”.

**How:** Resource patch with a bundled mipmap, or a patch option that tints / badges the existing launcher. `apkFileType` stays `APKS`.

**Risk:** Low. Need density splits (`config.xxhdpi` / `config.mdpi`) so the icon is not only in `base`.

### 5. SafePix: skip RevenueCat startup so local mode is actually offline

**Why:** Listing and in-app copy say 100% offline / no tracking. The APK still:

- Starts RevenueCat (`purchases_flutter`, products “SafePix Pro Monthly/Annual”)
- Shows `Checking subscription status...` and `Failed to check subscription status:`
- Uses `INTERNET` + Play Billing for that check
- Gates **batch folder detection** and “full confidence & category distribution” behind Pro (`subscription.featureBatch`)

The existing `removeInternet` option already cuts the network, but then the UI still tries to check a subscription and can stall or error. A dedicated patch should skip RevenueCat init / treat “no network” as “not subscribed”, so gallery + single-image detect keep working.

**How:** Bytecode no-op of `Purchases.configure` / Flutter `purchases_flutter` setup, or a resource flag plus a small Dart-channel stub. Do **not** spoof entitlements (see below).

**Risk:** Medium. Paying users must still be able to restore if they want Pro; make restore an explicit option, default skip-check-only.

## Out of scope (present in the binaries, not recommended)

These are paid gates. Implementing them would be an IAP unlock, not a privacy/sideload patch.

| App | Gate in the binary |
| --- | --- |
| SafePix | Batch folder scan + full probability UI behind “SafePix Pro” (RevenueCat monthly/annual) |
| Punge | “Purchase a plan to select all and manage your images.” / “Purchase a plan to move or delete images.” / `paywall_shown_blurred_item` / video upgrade event `punge_photo_upgrade_to_video` |

Free Punge still scans the library; the paywall is bulk actions and unblurring leftover videos.

## Extra findings (not in the top 5)

- **ABI:** This SafePix 1.1.5 XAPK includes `config.arm64_v8a.apk`. Punge 3.1.4 is still `armeabi-v7a` only. The README’s “APKPure is 32-bit only” note is outdated for SafePix.
- **SafePix CAMERA** is optional in the manifest (`android.hardware.camera` not required) but the permission is still requested for “take photo”. Easy extra resource option.
- **Punge `READ_MEDIA_AUDIO`** is unused for a photo/video scanner; easy permission trim.
- **Both** query Play Billing and Amazon IAP (`com.amazon.device.iap.ResponseReceiver`).
- Flutter business logic lives in `libapp.so`. Ads / PairIP / Firebase / Billing are mostly DEX and are the realistic Morphe patches. Changing Pro flags in Dart needs a native/`libapp.so` approach that this tree does not do today.

## Suggested implementation order

1. Punge PairIP (copy SafePix patch, extend `compatibleWith`)
2. Punge ads (resource disable + ads channel stub)
3. Punge Remote Config / remaining Measurement
4. Custom icons
5. SafePix RevenueCat skip-check (no entitlement spoof)
