package app.hh.patches.safepix

import app.hh.patches.shared.Constants.COMPATIBILITY_SAFEPIX
import app.hh.patches.shared.setAppName
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption

private const val DEFAULT_APP_NAME = "SafePix HH"

@Suppress("unused")
val customAppNamePatch = resourcePatch(
    name = "Custom app name for SafePix",
    description = "Changes the SafePix launcher name to the name specified in patch options.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SAFEPIX)

    val appNameOption = stringOption(
        key = "appName",
        default = DEFAULT_APP_NAME,
        values = mapOf(
            "Default" to DEFAULT_APP_NAME,
            "SafePix" to "SafePix",
        ),
        title = "App name",
        description = "The name shown under the app icon.",
        required = true,
    )

    execute {
        setAppName(appNameOption.value!!)
    }
}
