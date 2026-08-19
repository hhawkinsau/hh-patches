package app.hh.patches.safepix

import app.hh.patches.shared.Constants.COMPATIBILITY_SAFEPIX
import app.hh.patches.shared.replaceLauncherIcons
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val customLauncherIconPatch = resourcePatch(
    name = "Custom launcher icon for SafePix",
    description = "Replaces the SafePix launcher icon with the HH badge so the " +
        "patched install is distinct from the Play Store app.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SAFEPIX)

    execute {
        replaceLauncherIcons("safepix")
    }
}
