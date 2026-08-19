package app.hh.patches.punge

import app.hh.patches.shared.Constants.COMPATIBILITY_PUNGE
import app.hh.patches.shared.replaceLauncherIcons
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val customLauncherIconPatch = resourcePatch(
    name = "Custom launcher icon for Punge",
    description = "Replaces the Punge launcher icon with the HH badge so the " +
        "patched install is distinct from the Play Store app.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_PUNGE)

    execute {
        replaceLauncherIcons("punge")
    }
}
