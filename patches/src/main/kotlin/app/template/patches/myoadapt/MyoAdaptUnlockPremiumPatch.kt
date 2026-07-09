package app.template.patches.myoadapt

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MYOADAPT_COMPATIBILITY
import app.template.patches.shared.injectRevenueCatHybridCustomerInfo
import app.template.patches.shared.revenueCatHybridCustomerInfoMapFingerprint

@Suppress("unused")
val myoAdaptUnlockPremiumPatch = bytecodePatch(
    name = "Unlock MyoAdapt Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(MYOADAPT_COMPATIBILITY)

    execute {
        val fingerprint = revenueCatHybridCustomerInfoMapFingerprint()
        fingerprint
            .match(classDefBy(fingerprint.definingClass!!))
            .method
            .injectRevenueCatHybridCustomerInfo(
                appName = "MyoAdapt",
                primaryEntitlement = "pro",
                primaryProduct = "1_subscription",
                entitlementIds = listOf(
                    "premium",
                    "subscription",
                    "membership",
                    "full_access",
                    "all_access",
                    "Core.Common.Models.Subscription",
                    "Core.Common.Models.Pro",
                    "availableVISAVE_PRO"
                ),
                productIds = listOf("premium", "subscription")
            )
    }
}
