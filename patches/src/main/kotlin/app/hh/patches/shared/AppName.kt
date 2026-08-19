package app.hh.patches.shared

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import org.w3c.dom.Element

private val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private val APP_NAME_STRINGS = setOf("app_name", "appName", "application_name")

/**
 * Changes the launcher / application label in the decoded manifest and
 * any matching app-name string resources.
 */
internal fun ResourcePatchContext.setAppName(appName: String) {
    val name = appName.trim()
    if (name.isEmpty()) {
        throw PatchException("App name cannot be empty.")
    }

    document("AndroidManifest.xml").use { document ->
        val application = document.getElementsByTagName("application").item(0) as? Element
            ?: throw PatchException("AndroidManifest.xml is missing an <application> element.")

        application.setLabel(name)

        for (tag in listOf("activity", "activity-alias")) {
            val nodes = document.getElementsByTagName(tag)
            for (i in 0 until nodes.length) {
                val element = nodes.item(i) as Element
                val current = element.getLabel()
                if (current.isNotEmpty() && (current == "@string/app_name" || current == "@string/appName")) {
                    element.setLabel(name)
                }
            }
        }
    }

    val resDirectory = get("res")
    resDirectory.listFiles()
        ?.filter { it.isDirectory && it.name.startsWith("values") }
        ?.forEach { valuesDir ->
            val stringsXml = valuesDir.resolve("strings.xml")
            if (!stringsXml.isFile) return@forEach

            document("res/${valuesDir.name}/strings.xml").use { document ->
                val nodes = document.getElementsByTagName("string")
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    if (node.getAttribute("name") in APP_NAME_STRINGS) {
                        node.textContent = name
                    }
                }
            }
        }
}

private fun Element.getLabel(): String {
    val namespaced = getAttributeNS(ANDROID_NS, "label")
    return namespaced.ifEmpty { getAttribute("android:label") }
}

private fun Element.setLabel(value: String) {
    if (hasAttributeNS(ANDROID_NS, "label")) {
        setAttributeNS(ANDROID_NS, "android:label", value)
    } else {
        setAttribute("android:label", value)
    }
}
