package app.hh.patches.shared

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

private val TELEMETRY_META = mapOf(
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_analytics_collection_enabled" to "false",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_enabled" to "false",
    "firebase_performance_collection_deactivated" to "true",
    "firebase_data_collection_default_enabled" to "false",
    "firebase_messaging_auto_init_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_automatic_screen_reporting_enabled" to "false",
    "google_analytics_deferred_deep_link_enabled" to "false",
)

private val AD_ID_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
)

private val INTERNET_PERMISSIONS = setOf(
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
)

private val FIREBASE_TRANSPORT_COMPONENTS = setOf(
    "com.google.android.datatransport.runtime.backends.TransportBackendDiscovery",
    "com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService",
    "com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver",
    "com.google.firebase.sessions.SessionLifecycleService",
    "com.google.firebase.iid.FirebaseInstanceIdReceiver",
)

/**
 * Turns off common Firebase / Google Analytics collection flags, advertising-ID
 * permissions, and DataTransport senders. Optionally also strips INTERNET so the
 * app cannot phone home at all.
 *
 * Photo/video scan payloads are not touched. This only blocks the telemetry and
 * ad-ID channels that both store listings contradict with "no tracking" claims.
 */
internal fun ResourcePatchContext.disableAnalytics(removeInternet: Boolean) {
    document("AndroidManifest.xml").use { document ->
        val application = document.getElementsByTagName("application").item(0) as? Element
            ?: throw PatchException("AndroidManifest.xml is missing an <application> element.")

        TELEMETRY_META.forEach { (name, value) ->
            application.setMetaData(name, value)
        }

        FIREBASE_TRANSPORT_COMPONENTS.forEach { componentName ->
            application.disableComponent(componentName)
        }

        val permissions = AD_ID_PERMISSIONS.toMutableSet()
        if (removeInternet) {
            permissions += INTERNET_PERMISSIONS
        }
        document.removePermissions(permissions)
    }
}

private fun Element.setMetaData(name: String, value: String) {
    val existing = childElements("meta-data").firstOrNull { it.androidName() == name }
    val node = existing ?: ownerDocument.createElement("meta-data").also { appendChild(it) }
    node.setAndroidAttr("name", name)
    node.setAndroidAttr("value", value)
}

private fun Element.disableComponent(componentName: String) {
    for (tag in listOf("activity", "provider", "service", "receiver")) {
        childElements(tag)
            .filter { it.androidName() == componentName }
            .forEach { component ->
                component.setAndroidAttr("enabled", "false")
                component.setAndroidAttr("exported", "false")
            }
    }
}

private fun Document.removePermissions(names: Set<String>) {
    for (tag in listOf("uses-permission", "uses-permission-sdk-23")) {
        val nodes = getElementsByTagName(tag)
        for (i in nodes.length - 1 downTo 0) {
            val element = nodes.item(i) as Element
            if (element.androidName() in names) {
                element.parentNode?.removeChild(element)
            }
        }
    }
}

private fun Element.childElements(tag: String): List<Element> {
    val nodes = childNodes
    return buildList {
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node is Element && node.nodeName == tag) add(node)
        }
    }
}

private fun Element.androidName(): String {
    val namespaced = getAttributeNS(ANDROID_NS, "name")
    return namespaced.ifEmpty { getAttribute("android:name") }
}

private fun Element.setAndroidAttr(name: String, value: String) {
    if (hasAttributeNS(ANDROID_NS, name)) {
        setAttributeNS(ANDROID_NS, "android:$name", value)
    } else {
        setAttribute("android:$name", value)
    }
}
