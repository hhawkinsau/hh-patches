package app.hh.patches.punge

import app.hh.patches.shared.Constants.COMPATIBILITY_PUNGE
import app.hh.patches.shared.disableAnalytics
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val disableAnalyticsPatch = resourcePatch(
    name = "Disable analytics for Punge",
    description = "Disables Firebase Analytics, Crashlytics, and advertising-ID collection.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_PUNGE)

    val removeInternetOption = booleanOption(
        key = "removeInternet",
        default = false,
        title = "Remove INTERNET permission",
        description = "Also strip INTERNET and related network permissions. " +
            "This blocks ads, analytics, and Crashlytics, but also breaks Play Billing / IAP.",
    )

    execute {
        disableAnalytics(removeInternet = removeInternetOption.value == true)
    }
}
