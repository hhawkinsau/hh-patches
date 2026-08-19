# HH Patches

Custom patches compatible with [Morphe](https://morphe.software).

These patches change the launcher name and turn off analytics collection flags for:

- **SafePix** (`com.nubestour.safepix`) 1.1.5
- **Punge** (`com.markatlarge.scrub`) 3.1.4

Supply the original APK yourself. This repository does not host or redistribute any app.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=hhawkinsau/hh-patches

Or manually add this repository URL as a patch source in Morphe:

`https://github.com/hhawkinsau/hh-patches`

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/hhawkinsau/hh-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;2 patches total
<details open>
<summary>📦 Punge&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 3.1.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Custom app name for Punge](#custom-app-name-for-punge) | Changes the Punge launcher name to the name specified in patch options. | • App name |

</details>

<details open>
<summary>📦 SafePix&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 1.1.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Custom app name for SafePix](#custom-app-name-for-safepix) | Changes the SafePix launcher name to the name specified in patch options. | • App name |

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
- The built patches `.mpp` file is found in `patches/build/libs/patches-*.mpp`
- Apply the bundle with [Morphe Manager](https://morphe.software) or [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop)

Each app-name patch has an **App name** option in Morphe Manager (defaults: `SafePix HH` and `Punge HH`).
The analytics patches are on by default. They have a **Remove INTERNET permission** option that stays off unless you want to block all network access.

### `INSTALL_FAILED_NO_MATCHING_ABIS` (Punge)

Morphe CLI can patch both apps. The failure happens at **install**, not while applying patches.

APKPure XAPKs for these versions currently ship **only `armeabi-v7a`** (32-bit) native libraries (`libflutter.so`, `libapp.so`, plus ONNX on SafePix and TFLite on Punge). There is **no `arm64-v8a` split**. A 64-bit-only tablet/phone rejects that package with:

`INSTALL_FAILED_NO_MATCHING_ABIS: Failed to extract native libraries, res=-113`

If patched SafePix installed and Punge did not, the Punge file you patched is 32-bit-only and the device cannot run it. Get an **arm64-v8a** Punge package instead:

1. Install Punge from Play Store on the tablet (Play serves the matching ABI).
2. Export the installed splits (SAI, App Manager, or similar) and patch **that** XAPK/APKS.
3. In Morphe Manager Expert settings, leave **Optimize for device architecture** off unless the input already contains `config.arm64_v8a`. On a v7a-only XAPK, that option strips all eight Punge `.so` files and leaves a Flutter app with no native libraries.

## License

HH Patches are licensed under the [GNU General Public License v3.0](LICENSE)
