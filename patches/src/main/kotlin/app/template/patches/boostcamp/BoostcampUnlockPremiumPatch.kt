package app.template.patches.boostcamp

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BOOSTCAMP_COMPATIBILITY
import app.template.patches.shared.injectRevenueCatHybridCustomerInfo
import app.template.patches.shared.revenueCatHybridCustomerInfoMapFingerprint

/**
 * Boostcamp (React Native + RevenueCat hybridcommon).
 *
 * Entitlement ID is `premium_entitlement` (from RC promo product IDs
 * `rc_promo_premium_entitlement_{monthly|yearly|lifetime|...}`).
 */
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
                primaryEntitlement = "premium_entitlement",
                primaryProduct = "boostcamp.pro.annual.dec.2023-12-11",
                entitlementIds = listOf(
                    "pro",
                    "premium",
                    "plus",
                    "subscription",
                    "membership",
                    "coach_pro",
                    "AI_PRO",
                    "ai_pro",
                    "full_access",
                    "all_access",
                    "entitlement_monthly",
                    "entitlement_yearly",
                    "entitlement_lifetime",
                    "entitlement_six_month",
                    "entitlement_three_month"
                ),
                productIds = listOf(
                    "ANDOID_ANNUAL_PRO_59_99_2025",
                    "ANDROID_MONTHLY_PRO_14_99_2025",
                    "ANDROID_ANNUAL_PRO_NEW",
                    "ANDROID_MONTHLY_PRO_NEW",
                    "ANDROID_ANNUAL_PRO_REFERRAL",
                    "ANDROID_MONTHLY_PRO_REFERRAL",
                    "ANDROID_AFFILIATE_ANNUAL_PRO_59_99",
                    "ANDROID_AFFILIATE_MONTHLY_PRO_14_99",
                    "ANDROID_ANNUAL_PRODUCT_ID",
                    "ANDROID_MONTHLY_PRODUCT_ID",
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
                    "boostcamp_pro:pro-monthly-referral",
                    "boostcamp_pro_test_prices:pro-annual-59-99-14-days-free-trial",
                    "boostcamp_pro_test_prices:pro-annual-39-99",
                    "rc_promo_premium_entitlement_monthly",
                    "rc_promo_premium_entitlement_yearly",
                    "rc_promo_premium_entitlement_lifetime",
                    "rc_promo_premium_entitlement_six_month",
                    "rc_promo_premium_entitlement_three_month",
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
