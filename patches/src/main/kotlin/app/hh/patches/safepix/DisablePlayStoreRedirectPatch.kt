package app.hh.patches.safepix

import app.hh.patches.shared.Constants.COMPATIBILITY_SAFEPIX
import app.hh.patches.shared.spoofPlayStoreInstaller
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Google Play "Automatic Integrity Protection" (PairIP) is what bounces a
 * sideloaded / re-signed SafePix install straight into the Play Store.
 *
 * On launch it either:
 * - calls [LicenseClientV3.onActivityCreate], which opens the store listing, or
 * - runs the older [LicenseClient] LVL flow, which does the same on failure.
 *
 * This patch short-circuits those client-side checks and also spoofs the
 * installer package so any leftover "were we installed from Play?" tests pass.
 */
@Suppress("unused")
val disablePlayStoreRedirectPatch = bytecodePatch(
    name = "Disable Play Store redirect",
    description = "Stops SafePix from opening the Play Store on launch when the " +
        "app was sideloaded or patched instead of installed from Play.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SAFEPIX)

    execute {
        var patched = 0

        // V3: this is the method that actually starts the Play Store intent.
        if (LicenseClientV3OnCreateFingerprint.returnVoidEarly()) patched++

        // Older LVL client: never contact Play, never show the failure dialog.
        if (ConnectToLicensingServiceFingerprint.returnVoidEarly()) patched++
        if (RetryOrThrowFingerprint.returnVoidEarly()) patched++
        if (InitializeLicenseCheckFingerprint.returnVoidEarly()) patched++
        if (ValidateLicenseResponseFingerprint.returnVoidEarly()) patched++
        if (LicenseResponseHelperFingerprint.returnVoidEarly()) patched++

        ProcessLicenseResponseFingerprint.methodOrNull?.let { method ->
            val responseRegister = if (AccessFlags.STATIC.isSet(method.accessFlags)) "p0" else "p1"
            method.addInstruction(0, "const/4 $responseRegister, 0x0")
            patched++
        }

        // Signature / tamper prompt that can also kill the process.
        if (SignatureCheckFingerprint.returnVoidEarly()) patched++

        OpenPlayStoreFingerprint.matchAllOrNull()?.forEach { match ->
            match.method.returnVoidEarly()
            patched++
        }

        val spoofedInstaller = spoofPlayStoreInstaller()
        if (spoofedInstaller) patched++

        if (patched == 0) {
            throw PatchException(
                "Could not find SafePix's Play Store redirect (PairIP license check " +
                    "or installer-source check). The app may have changed; no changes applied.",
            )
        }
    }
}

context(_: BytecodePatchContext)
private fun Fingerprint.returnVoidEarly(): Boolean {
    val method = methodOrNull ?: return false
    method.returnVoidEarly()
    return true
}

private fun MutableMethod.returnVoidEarly() {
    check(returnType.startsWith("V")) {
        "Cannot return-void from $this (return type $returnType)"
    }
    addInstruction(0, "return-void")
}

private object LicenseClientV3OnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck3/LicenseClientV3;",
    name = "onActivityCreate",
    returnType = "V",
)

private object ConnectToLicensingServiceFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "connectToLicensingService",
    returnType = "V",
)

private object RetryOrThrowFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "retryOrThrow",
    returnType = "V",
)

private object InitializeLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
)

private object ProcessLicenseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
)

private object ValidateLicenseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/ResponseValidator;",
    name = "validateResponse",
    returnType = "V",
)

private object LicenseResponseHelperFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
)

private object SignatureCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/SignatureCheck;",
    name = "verifyIntegrity",
    returnType = "V",
)

private object OpenPlayStoreFingerprint : Fingerprint(
    name = "openPlayStore",
    returnType = "V",
    custom = { _, classDef -> classDef.type.startsWith("Lcom/pairip/") },
)
