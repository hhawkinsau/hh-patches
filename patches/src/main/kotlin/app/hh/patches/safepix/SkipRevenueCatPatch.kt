package app.hh.patches.safepix

import app.hh.patches.shared.Constants.COMPATIBILITY_SAFEPIX
import app.hh.patches.shared.succeedLastCallbackAndReturn
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val skipRevenueCatPatch = bytecodePatch(
    name = "Skip RevenueCat startup for SafePix",
    description = "Skips RevenueCat configure on launch so local detection works offline. " +
        "Do not combine with Unlock SafePix Pro, which needs CustomerInfo.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SAFEPIX)

    val allowRestoreOption = booleanOption(
        key = "allowRestore",
        default = false,
        title = "Allow restore purchases",
        description = "Leave RevenueCat configure in place so Restore Purchases still works. " +
            "Off by default so launch does not wait on the network.",
    )

    execute {
        if (allowRestoreOption.value == true) {
            return@execute
        }

        val patched = SetupPurchasesFingerprint.succeedLastCallbackAndReturn()
        if (!patched) {
            throw PatchException(
                "Could not find RevenueCat setupPurchases. " +
                    "The app may have changed; no changes applied.",
            )
        }
    }
}

private object SetupPurchasesFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases_flutter/PurchasesFlutterPlugin;",
    name = "setupPurchases",
)
