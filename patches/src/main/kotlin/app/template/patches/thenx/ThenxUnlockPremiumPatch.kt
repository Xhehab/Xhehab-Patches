package app.template.patches.thenx

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.pairip.disablePairIpLicenseCheckPatch
import app.template.patches.shared.Constants.THENX_COMPATIBILITY
import app.template.patches.shared.replaceImplementation
import app.template.patches.shared.returnEarlyBoolean

private const val THENX_ENTITLEMENT_ID = "premium"
private const val THENX_PRODUCT_ID = "thenx_premium"
private const val FUTURE_EXPIRATION_MILLIS = "0x000001e163b3d800"

@Suppress("unused")
val thenxUnlockPremiumPatch = bytecodePatch(
    name = "Unlock THENX Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(THENX_COMPATIBILITY)
    dependsOn(disablePairIpLicenseCheckPatch)

    execute {
        listOf(
            ThenxCustomerInfoActiveSubscriptionsFingerprint,
            ThenxCustomerInfoAllPurchasedProductIdsFingerprint
        ).forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!)).let { match ->
                match.classDef.replaceImplementation(
                    match.method,
                    registerCount = 2,
                    smali = """
                    const-string v0, "$THENX_PRODUCT_ID"
                    invoke-static {v0}, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
                    move-result-object v0
                    return-object v0
                    """
                )
            }
        }

        listOf(
            ThenxEntitlementInfosActiveFingerprint,
            ThenxEntitlementInfosAllFingerprint
        ).forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!)).let { match ->
                match.classDef.replaceImplementation(
                    match.method,
                    registerCount = 3,
                    smali = """
                    const-string v0, "$THENX_ENTITLEMENT_ID"
                    const/4 v1, 0x0
                    invoke-static {v0, v1}, Ljava/util/Collections;->singletonMap(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;
                    move-result-object v0
                    return-object v0
                    """
                )
            }
        }

        listOf(
            ThenxCustomerInfoLatestExpirationDateFingerprint,
            ThenxCustomerInfoExpirationDateForProductIdFingerprint,
            ThenxEntitlementInfoExpirationDateFingerprint
        ).forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!)).let { match ->
                match.classDef.replaceImplementation(
                    match.method,
                    registerCount = 5,
                    smali = """
                    new-instance v0, Ljava/util/Date;
                    const-wide v1, $FUTURE_EXPIRATION_MILLIS
                    invoke-direct {v0, v1, v2}, Ljava/util/Date;-><init>(J)V
                    return-object v0
                    """
                )
            }
        }

        ThenxEntitlementInfoIsActiveFingerprint
            .match(classDefBy(ThenxEntitlementInfoIsActiveFingerprint.definingClass!!))
            .method
            .returnEarlyBoolean(true)
    }

}
