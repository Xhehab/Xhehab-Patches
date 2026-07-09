package app.template.patches.myoadapt

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MYOADAPT_COMPATIBILITY
import app.template.patches.shared.injectRevenueCatHybridCustomerInfo
import app.template.patches.shared.replaceImplementation
import app.template.patches.shared.returnEarlyBoolean
import app.template.patches.shared.revenueCatHybridCustomerInfoMapFingerprint

/**
 * MyoAdapt (Expo/RN + RevenueCat hybridcommon).
 *
 * Layers:
 * 1) Hybrid CustomerInfo map spoof (JS unlock)
 * 2) Native EntitlementInfos active/all non-empty
 * 3) EntitlementInfosMapper full spoof
 * 4) EntitlementInfo.isActive always true
 *
 * Primary entitlement is unknown from the minified Hermes bundle; inject a
 * broad set of common ids so JS `entitlements.active[id]` checks succeed.
 */
private const val PRIMARY_ENTITLEMENT = "pro"
private const val PRIMARY_PRODUCT = "myoadapt_pro_yearly"
private const val PURCHASE_DATE = "2026-07-09T00:00:00.000Z"
private const val EXPIRATION_DATE = "2035-07-09T00:00:00.000Z"
private const val PURCHASE_DATE_MILLIS = "0x19f442c9400L"
private const val EXPIRATION_DATE_MILLIS = "0x1e163b3d800L"

private val MYOADAPT_ENTITLEMENT_IDS = listOf(
    PRIMARY_ENTITLEMENT,
    "premium",
    "subscription",
    "membership",
    "full_access",
    "all_access",
    "plus",
    "solo",
    "duo",
    "myoadapt_pro",
    "myoadapt_premium",
    "MyoAdapt Pro",
    "Core.Common.Models.Subscription",
    "Core.Common.Models.Pro",
    "availableVISAVE_PRO",
    "entitlement_monthly",
    "entitlement_yearly",
    "entitlement_lifetime",
    "rc_promo_pro_monthly",
    "rc_promo_pro_yearly",
    "rc_promo_pro_lifetime",
    "rc_promo_premium_monthly",
    "rc_promo_premium_yearly",
    "rc_promo_premium_lifetime"
)

private val MYOADAPT_PRODUCT_IDS = listOf(
    PRIMARY_PRODUCT,
    "myoadapt_pro_monthly",
    "myoadapt_pro_yearly",
    "myoadapt_solo_monthly",
    "myoadapt_solo_yearly",
    "myoadapt_duo_monthly",
    "myoadapt_duo_yearly",
    "1_subscription",
    "premium",
    "subscription",
    "pro",
    "solo",
    "duo",
    "rc_monthly",
    "rc_annual",
    "rc_lifetime",
    "rc_promo_pro_monthly",
    "rc_promo_pro_yearly",
    "rc_promo_pro_lifetime"
)

@Suppress("unused")
val myoAdaptUnlockPremiumPatch = bytecodePatch(
    name = "Unlock MyoAdapt Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(MYOADAPT_COMPATIBILITY)

    execute {
        // 1) Hybrid CustomerInfo map → JS sees active entitlements
        val customerInfoMap = revenueCatHybridCustomerInfoMapFingerprint()
        customerInfoMap
            .match(classDefBy(customerInfoMap.definingClass!!))
            .method
            .injectRevenueCatHybridCustomerInfo(
                appName = "MyoAdapt",
                primaryEntitlement = PRIMARY_ENTITLEMENT,
                primaryProduct = PRIMARY_PRODUCT,
                entitlementIds = MYOADAPT_ENTITLEMENT_IDS.filter { it != PRIMARY_ENTITLEMENT },
                productIds = MYOADAPT_PRODUCT_IDS.filter { it != PRIMARY_PRODUCT }
            )

        // 2) Native getActive/getAll non-empty
        val entitlementMapSmali = buildNativeEntitlementMapSmali()
        listOf(
            MyoAdaptEntitlementInfosActiveFingerprint,
            MyoAdaptEntitlementInfosAllFingerprint
        ).forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!)).let { match ->
                match.classDef.replaceImplementation(
                    match.method,
                    registerCount = 4,
                    smali = entitlementMapSmali
                )
            }
        }

        // 3) Hybrid EntitlementInfosMapper full spoof
        MyoAdaptEntitlementInfosMapperFingerprint
            .match(classDefBy(MyoAdaptEntitlementInfosMapperFingerprint.definingClass!!))
            .let { match ->
                match.classDef.replaceImplementation(
                    match.method,
                    registerCount = 10,
                    smali = buildEntitlementInfosMapperSmali()
                )
            }

        // 4) isActive always true for any real EntitlementInfo instances
        MyoAdaptEntitlementInfoIsActiveFingerprint
            .match(classDefBy(MyoAdaptEntitlementInfoIsActiveFingerprint.definingClass!!))
            .method
            .returnEarlyBoolean(true)
    }
}

private fun buildNativeEntitlementMapSmali(): String {
    val puts = MYOADAPT_ENTITLEMENT_IDS.distinct().joinToString("\n") { id ->
        """
        const-string v2, "$id"
        invoke-interface {v0, v2, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
        """.trimIndent()
    }
    return """
        new-instance v0, Ljava/util/HashMap;
        invoke-direct {v0}, Ljava/util/HashMap;-><init>()V
        const/4 v1, 0x0
        $puts
        return-object v0
    """.trimIndent()
}

private fun buildEntitlementInfosMapperSmali(): String {
    val entitlementPuts = MYOADAPT_ENTITLEMENT_IDS.distinct().joinToString("\n") { id ->
        """
        const-string v2, "$id"
        invoke-interface {v6, v2, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
        invoke-interface {v7, v2, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
        """.trimIndent()
    }
    return """
        new-instance v1, Ljava/util/HashMap;
        invoke-direct {v1}, Ljava/util/HashMap;-><init>()V

        const-string v2, "identifier"
        const-string v3, "$PRIMARY_ENTITLEMENT"
        invoke-interface {v1, v2, v3}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "isActive"
        const/4 v4, 0x1
        invoke-static {v4}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
        move-result-object v4
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "willRenew"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "periodType"
        const-string v4, "NORMAL"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "latestPurchaseDate"
        const-string v4, "$PURCHASE_DATE"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "latestPurchaseDateMillis"
        const-wide v8, $PURCHASE_DATE_MILLIS
        invoke-static {v8, v9}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
        move-result-object v8
        invoke-interface {v1, v2, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "originalPurchaseDate"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "originalPurchaseDateMillis"
        invoke-interface {v1, v2, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "expirationDate"
        const-string v4, "$EXPIRATION_DATE"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "expirationDateMillis"
        const-wide v8, $EXPIRATION_DATE_MILLIS
        invoke-static {v8, v9}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
        move-result-object v8
        invoke-interface {v1, v2, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "store"
        const-string v4, "PLAY_STORE"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "productIdentifier"
        const-string v4, "$PRIMARY_PRODUCT"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "productPlanIdentifier"
        const/4 v4, 0x0
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "isSandbox"
        const/4 v4, 0x0
        invoke-static {v4}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
        move-result-object v4
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "unsubscribeDetectedAt"
        const/4 v4, 0x0
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "unsubscribeDetectedAtMillis"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "billingIssueDetectedAt"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "billingIssueDetectedAtMillis"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "ownershipType"
        const-string v4, "PURCHASED"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v2, "verification"
        const-string v4, "VERIFIED"
        invoke-interface {v1, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        new-instance v5, Ljava/util/HashMap;
        invoke-direct {v5}, Ljava/util/HashMap;-><init>()V

        new-instance v6, Ljava/util/HashMap;
        invoke-direct {v6}, Ljava/util/HashMap;-><init>()V

        new-instance v7, Ljava/util/HashMap;
        invoke-direct {v7}, Ljava/util/HashMap;-><init>()V

        $entitlementPuts

        const-string v1, "active"
        invoke-interface {v5, v1, v6}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v1, "all"
        invoke-interface {v5, v1, v7}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        const-string v1, "verification"
        const-string v2, "VERIFIED"
        invoke-interface {v5, v1, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

        return-object v5
    """.trimIndent()
}
