package app.hh.patches.shared

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import org.w3c.dom.Element

private val MIPMAP_DENSITIES = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
private val ICON_FILES = listOf(
    "ic_launcher.png",
    "ic_launcher_foreground.png",
    "ic_launcher_background.png",
)

private object IconAssets

/**
 * Overwrites launcher mipmaps with the bundled HH icons and points adaptive
 * icon XML at the new foreground.
 */
internal fun ResourcePatchContext.replaceLauncherIcons(assetFolder: String) {
    val classLoader = IconAssets::class.java.classLoader
        ?: throw PatchException("Could not load launcher icon resources.")
    val res = get("res")
    var copied = 0

    MIPMAP_DENSITIES.forEach { density ->
        val sourceDir = "icons/$assetFolder/mipmap-$density"
        val targetDirs = listOf(
            res.resolve("mipmap-$density"),
            res.resolve("mipmap-$density-v4"),
        ).filter { it.isDirectory }

        ICON_FILES.forEach { fileName ->
            val bytes = classLoader.getResourceAsStream("$sourceDir/$fileName")?.use { it.readBytes() }
                ?: return@forEach

            if (targetDirs.isEmpty()) {
                val created = res.resolve("mipmap-$density").also { it.mkdirs() }
                created.resolve(fileName).writeBytes(bytes)
                copied++
            } else {
                targetDirs.forEach { dir ->
                    dir.resolve(fileName).writeBytes(bytes)
                    copied++
                }
            }
        }

        // Density splits sometimes keep the adaptive foreground under drawable.
        val splitForeground = res.resolve("drawable-$density-v4")
        if (splitForeground.isDirectory) {
            classLoader.getResourceAsStream("$sourceDir/ic_launcher_foreground.png")?.use { stream ->
                splitForeground.resolve("ic_launcher_foreground.png").outputStream().use { out ->
                    stream.copyTo(out)
                }
                copied++
            }
        }
    }

    if (copied == 0) {
        throw PatchException("No launcher icon files were replaced for $assetFolder.")
    }

    res.listFiles()
        ?.filter { it.isDirectory && it.name.startsWith("mipmap-anydpi") }
        ?.forEach { anyDpi ->
            val adaptive = anyDpi.resolve("ic_launcher.xml")
            if (adaptive.isFile) {
                document("res/${anyDpi.name}/ic_launcher.xml").use { document ->
                    val icon = document.documentElement ?: return@use
                    val foreground = icon.getElementsByTagName("foreground")
                    val current = if (foreground.length > 0) {
                        (foreground.item(0) as Element).androidDrawable()
                    } else {
                        ""
                    }
                    if (current.isEmpty() || current == "@null") {
                        // Punge ships a null adaptive foreground; the legacy
                        // mipmap is the file that actually shows on the launcher.
                        ensureAdaptiveLayer(icon, "foreground", "@mipmap/ic_launcher")
                    }
                    if (icon.getElementsByTagName("background").length == 0) {
                        ensureAdaptiveLayer(icon, "background", "@mipmap/ic_launcher_background")
                    }
                }
            }
        }
}

private fun Element.androidDrawable(): String {
    val namespaced = getAttributeNS(ANDROID_NS, "drawable")
    return namespaced.ifEmpty { getAttribute("android:drawable") }
}

private fun ensureAdaptiveLayer(icon: Element, tag: String, drawable: String) {
    val nodes = icon.getElementsByTagName(tag)
    val layer = if (nodes.length > 0) {
        nodes.item(0) as Element
    } else {
        icon.ownerDocument.createElement(tag).also { icon.appendChild(it) }
    }
    layer.setAndroidAttr("drawable", drawable)
}
