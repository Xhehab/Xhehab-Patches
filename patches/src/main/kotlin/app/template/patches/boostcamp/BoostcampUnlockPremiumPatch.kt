package app.template.patches.boostcamp

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BOOSTCAMP_COMPATIBILITY
import app.template.patches.shared.injectRevenueCatHybridCustomerInfo
import app.template.patches.shared.revenueCatHybridCustomerInfoMapFingerprint

@Suppress("unused")
val boostcampUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Boostcamp Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(BOOSTCAMP_COMPATIBILITY)

    execute {
        val fingerprint = revenueCatHybridCustomerInfoMapFingerprint()
        fingerprint
            .match(classDefBy(fingerprint.definingClass!!))
            .method
            .injectRevenueCatHybridCustomerInfo(
                appName = "Boostcamp",
                primaryEntitlement = "pro",
                primaryProduct = "boostcamp.pro.annual.dec.2023-12-11",
                entitlementIds = listOf(
                    "premium",
                    "plus",
                    "subscription",
                    "membership",
                    "coach_pro",
                    "AI_PRO",
                    "ai_pro",
                    "full_access",
                    "all_access"
                ),
                productIds = listOf(
                    "boostcamp.pro.monthly.dec.2023-12-24",
                    "boostcamp_premium_annual_subscription",
                    "boostcamp_premium_monthly_subscription",
                    "ANDROID_MONTHLY_PRO_REFERRAL",
                    "ANDROID_AFFILIATE_ANNUAL_PRO_59_99",
                    "ANDROID_AFFILIATE_MONTHLY_PRO_14_99",
                    "ANDROID_ANNUAL_PRODUCT_ID",
                    "ANDROID_MONTHLY_PRODUCT_ID",
                    "IOS_MONTHLY_PRO_14_99_2025",
                    "IOS_MONTHLY_PRO",
                    "IOS_ANNUAL_PRO_REFERRAL"
                )
            )
    }
}
