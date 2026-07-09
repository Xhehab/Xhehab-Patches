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
                    "ANDOID_ANNUAL_PRO_59_99_2025",
                    "ANDROID_MONTHLY_PRO_14_99_2025",
                    "boostcamp.pro.monthly.dec.2023-12-24",
                    "boostcamp.pro.annual.59.99.14.days.free",
                    "boostcamp.pro.annual.referralCode",
                    "boostcamp.pro.monthly.14.99.14.days.freeTrial",
                    "boostcamp.pro.monthly.referral",
                    "boostcamp_premium_annual_subscription",
                    "boostcamp_premium_annual_subscription:annual-sub-plan-1",
                    "boostcamp_premium_monthly_subscription",
                    "boostcamp_premium_monthly_subscription:monthly-sub-plan-1",
                    "boostcamp_pro:pro-annual-referral_channel",
                    "ANDROID_MONTHLY_PRO_REFERRAL",
                    "ANDROID_AFFILIATE_ANNUAL_PRO_59_99",
                    "ANDROID_AFFILIATE_MONTHLY_PRO_14_99",
                    "ANDROID_ANNUAL_PRODUCT_ID",
                    "ANDROID_MONTHLY_PRODUCT_ID",
                    "IOS_AFFILIATE_ANNUAL_PRO_59_99",
                    "IOS_AFFILIATE_MONTHLY_PRO_14_99",
                    "IOS_ANNUAL_PRODUCT_ID",
                    "IOS_MONTHLY_PRODUCT_ID",
                    "IOS_ANNUAL_PRO_59_99_2025",
                    "IOS_ANNUAL_PRO_NEW",
                    "IOS_MONTHLY_PRO_14_99_2025",
                    "IOS_MONTHLY_PRO_NEW",
                    "IOS_MONTHLY_PRO",
                    "IOS_ANNUAL_PRO_REFERRAL"
                )
            )
    }
}
