package app.hh.patches.shared

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext

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

internal val AD_ID_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
    "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
)

private val INTERNET_PERMISSIONS = setOf(
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
)

internal val FIREBASE_MEASUREMENT_COMPONENTS = setOf(
    "com.google.android.gms.measurement.AppMeasurementService",
    "com.google.android.gms.measurement.AppMeasurementJobService",
    "com.google.android.gms.measurement.AppMeasurementReceiver",
    "com.google.firebase.sessions.SessionLifecycleService",
)

private val REMOTE_CONFIG_META = mapOf(
    "firebase_remote_config_fetch_disallow" to "true",
)

/**
 * Turns off common Firebase / Google Analytics collection flags, advertising-ID
 * permissions. Optionally also strips INTERNET so the app cannot phone home at
 * all.
 *
 * Photo/video scan payloads are not touched. This only blocks the telemetry and
 * ad-ID channels that both store listings contradict with "no tracking" claims.
 */
internal fun ResourcePatchContext.disableAnalytics(removeInternet: Boolean) {
    document("AndroidManifest.xml").use { document ->
        val application = document.applicationElement()
            ?: throw PatchException("AndroidManifest.xml is missing an <application> element.")

        TELEMETRY_META.forEach { (name, value) ->
            application.setMetaData(name, value)
        }

        // Keep DataTransport, Firebase Sessions, and Firebase IID components
        // registered. Other bundled SDKs initialize through these components;
        // disabling them can leave Flutter waiting indefinitely at its splash.
        val permissions = AD_ID_PERMISSIONS.toMutableSet()
        if (removeInternet) {
            permissions += INTERNET_PERMISSIONS
        }
        document.removePermissions(permissions)
    }
}

/**
 * Disables Play Measurement senders and marks Remote Config fetch as disallowed.
 * Does not disable [FirebaseInitProvider] so Firebase.initializeApp() still works.
 */
internal fun ResourcePatchContext.disableRemoteConfig() {
    document("AndroidManifest.xml").use { document ->
        val application = document.applicationElement()
            ?: throw PatchException("AndroidManifest.xml is missing an <application> element.")

        REMOTE_CONFIG_META.forEach { (name, value) ->
            application.setMetaData(name, value)
        }

        FIREBASE_MEASUREMENT_COMPONENTS.forEach { componentName ->
            application.disableComponent(componentName)
        }

        // Drop Remote Config component registrars so the SDK is not discovered.
        application.childElements("service")
            .filter { it.androidName() == "com.google.firebase.components.ComponentDiscoveryService" }
            .forEach { service ->
                service.childElements("meta-data")
                    .filter { it.androidName().contains("remoteconfig", ignoreCase = true) }
                    .forEach { it.parentNode?.removeChild(it) }
            }
    }
}
