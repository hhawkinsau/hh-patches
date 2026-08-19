package app.hh.patches.safepix

import app.hh.patches.shared.Constants.COMPATIBILITY_SAFEPIX
import app.hh.patches.shared.disableAnalytics
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val disableAnalyticsPatch = resourcePatch(
    name = "Disable analytics for SafePix",
    description = "Disables Firebase Analytics and advertising-ID collection.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SAFEPIX)

    val removeInternetOption = booleanOption(
        key = "removeInternet",
        default = false,
        title = "Remove INTERNET permission",
        description = "Also strip INTERNET and related network permissions. " +
            "This enforces the offline claim, but breaks the Network Images URL scanner " +
            "and any Play Billing callbacks.",
    )

    execute {
        disableAnalytics(removeInternet = removeInternetOption.value == true)
    }
}
