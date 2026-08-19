package app.hh.patches.punge

import app.hh.patches.shared.Constants.COMPATIBILITY_PUNGE
import app.hh.patches.shared.setAppName
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption

private const val DEFAULT_APP_NAME = "Punge HH"

@Suppress("unused")
val customAppNamePatch = resourcePatch(
    name = "Custom app name for Punge",
    description = "Changes the Punge launcher name to the name specified in patch options. " +
        "Use an arm64-v8a XAPK on 64-bit devices; APKPure's 3.1.4 bundle is armeabi-v7a only " +
        "and installs as INSTALL_FAILED_NO_MATCHING_ABIS.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_PUNGE)

    val appNameOption = stringOption(
        key = "appName",
        default = DEFAULT_APP_NAME,
        values = mapOf(
            "Default" to DEFAULT_APP_NAME,
            "Punge" to "Punge",
        ),
        title = "App name",
        description = "The name shown under the app icon.",
        required = true,
    )

    execute {
        setAppName(appNameOption.value!!)
    }
}
