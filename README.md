# HH Patches

Custom patches compatible with [Morphe](https://morphe.software).

- **SafePix** (`com.nubestour.safepix`) 1.1.5
- **Punge** (`com.markatlarge.scrub`) 3.1.4

Supply the original APK yourself. This repository does not host or redistribute any app.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=hhawkinsau/hh-patches

Or add this repository URL as a patch source in Morphe:

`https://github.com/hhawkinsau/hh-patches`

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.2.1](https://github.com/hhawkinsau/hh-patches/releases/tag/v1.2.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;5 patches total
<details open>
<summary>📦 Punge&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 3.1.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Custom app name for Punge](#custom-app-name-for-punge) | Changes the Punge launcher name to the name specified in patch options. | • App name |
| [Disable analytics for Punge](#disable-analytics-for-punge) | Disables Firebase Analytics, Crashlytics, and advertising-ID collection. | • Remove INTERNET permission |

</details>

<details open>
<summary>📦 SafePix&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 1.1.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Custom app name for SafePix](#custom-app-name-for-safepix) | Changes the SafePix launcher name to the name specified in patch options. | • App name |
| [Disable Play Store redirect](#disable-play-store-redirect) | Stops SafePix from opening the Play Store on launch when the app was sideloaded or patched instead of installed from Play. |  |
| [Disable analytics for SafePix](#disable-analytics-for-safepix) | Disables Firebase Analytics and advertising-ID collection. | • Remove INTERNET permission |

</details>

<!-- PATCHES_END -->

### Building locally

Gradle needs a GitHub token with `read:packages` to resolve Morphe packages. Put it in `~/.gradle/gradle.properties`:

```properties
gpr.user = YOUR_GITHUB_USERNAME
gpr.key = YOUR_GITHUB_PAT
```

For GitHub Actions, add the same PAT as a `GPR_KEY` repository secret.

- Run `./gradlew buildAndroid`
- The built patches `.mpp` file is in `patches/build/libs/patches-*.mpp`
- Apply the bundle with [Morphe Manager](https://morphe.software) or [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop)

### `INSTALL_FAILED_NO_MATCHING_ABIS`

APKPure's Punge 3.1.4 package is 32-bit (`armeabi-v7a`) only. A 64-bit-only device will refuse to install it. SafePix 1.1.5 from APKPure includes `arm64-v8a`.

For Punge, install from Play Store (or export that install) so the package includes `arm64-v8a`, then patch that.

Leave **Optimize for device architecture** off unless the input already has `config.arm64_v8a`.

## License

HH Patches are licensed under the [GNU General Public License v3.0](LICENSE)
