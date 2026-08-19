package app.hh.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_SAFEPIX = Compatibility(
        name = "SafePix",
        packageName = "com.nubestour.safepix",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x00ACC1,
        targets = listOf(
            AppTarget(
                version = "1.1.5",
            ),
            AppTarget(
                version = null,
                isExperimental = true,
            ),
        ),
    )

    val COMPATIBILITY_PUNGE = Compatibility(
        name = "Punge",
        packageName = "com.markatlarge.scrub",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6C5CE7,
        targets = listOf(
            AppTarget(
                version = "3.1.4",
            ),
            AppTarget(
                version = null,
                isExperimental = true,
            ),
        ),
    )
}
