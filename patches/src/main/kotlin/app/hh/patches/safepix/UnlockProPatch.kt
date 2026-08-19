package app.hh.patches.safepix

import app.hh.patches.shared.Constants.COMPATIBILITY_SAFEPIX
import app.hh.patches.shared.returnActiveRevenueCatEntitlements
import app.hh.patches.shared.returnPurchasedProduct
import app.hh.patches.shared.returnTrueEarly
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch

private const val PRODUCT_ID = "safepix_pro"
private val ENTITLEMENT_IDS = listOf(
    "premium",
    "pro",
    "safepix_pro",
    "SafePix Pro",
)

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock SafePix Pro",
    description = "Unlocks SafePix Pro by making RevenueCat report an active premium entitlement.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SAFEPIX)

    execute {
        val active = EntitlementInfosGetActiveFingerprint.methodOrNull
        val all = EntitlementInfosGetAllFingerprint.methodOrNull
        val isActive = EntitlementInfoIsActiveFingerprint.methodOrNull
        val activeSubscriptions = ActiveSubscriptionsFingerprint.methodOrNull
        val purchasedProducts = PurchasedProductsFingerprint.methodOrNull

        if (
            active == null ||
            all == null ||
            isActive == null ||
            activeSubscriptions == null ||
            purchasedProducts == null
        ) {
            throw PatchException(
                "Could not find SafePix's RevenueCat entitlement checks. " +
                    "The app may have changed; no changes applied.",
            )
        }

        active.returnActiveRevenueCatEntitlements(ENTITLEMENT_IDS, PRODUCT_ID)
        all.returnActiveRevenueCatEntitlements(ENTITLEMENT_IDS, PRODUCT_ID)
        isActive.returnTrueEarly()
        activeSubscriptions.returnPurchasedProduct(PRODUCT_ID)
        purchasedProducts.returnPurchasedProduct(PRODUCT_ID)
    }
}

private object EntitlementInfosGetActiveFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
    name = "getActive",
    parameters = listOf(),
    returnType = "Ljava/util/Map;",
)

private object EntitlementInfosGetAllFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
    name = "getAll",
    parameters = listOf(),
    returnType = "Ljava/util/Map;",
)

private object EntitlementInfoIsActiveFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    parameters = listOf(),
    returnType = "Z",
)

private object ActiveSubscriptionsFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getActiveSubscriptions",
    parameters = listOf(),
    returnType = "Ljava/util/Set;",
)

private object PurchasedProductsFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getAllPurchasedProductIds",
    parameters = listOf(),
    returnType = "Ljava/util/Set;",
)
