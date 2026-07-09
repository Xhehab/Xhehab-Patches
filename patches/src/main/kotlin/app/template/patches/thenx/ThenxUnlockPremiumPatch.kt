package app.template.patches.thenx

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.pairip.disablePairIpLicenseCheckPatch
import app.template.patches.shared.Constants.THENX_COMPATIBILITY
import app.template.patches.shared.replaceImplementation

private const val THENX_ENTITLEMENT_ID = "premium"

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
    }

}
