package app.template.patches.thenx

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.pairip.disablePairIpLicenseCheckPatch
import app.template.patches.shared.Constants.THENX_COMPATIBILITY
import app.template.patches.shared.replaceImplementation
import app.template.patches.shared.returnEarlyBoolean
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * THENX (native Compose + RevenueCat + backend UserApiModel.isPro).
 *
 * Premium is gated mainly by UserApiModel.isPro from the API. RC entitlement
 * maps are spoofed as a second layer for client-side Purchases checks.
 *
 * Note: EntitlementInfo date getters / VERIFIED verification previously crashed
 * the RC verifier — keep map values null and force isActive/isPro instead.
 */
private const val THENX_ENTITLEMENT_ID = "premium"
private const val THENX_PRODUCT_ID = "thenx_premium"

@Suppress("unused")
val thenxUnlockPremiumPatch = bytecodePatch(
    name = "Unlock THENX Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(THENX_COMPATIBILITY)
    dependsOn(disablePairIpLicenseCheckPatch)

    execute {
        // 1) Backend pro flag — primary gate for paywall / membership UI
        ThenxUserApiModelIsProFingerprint
            .match(classDefBy(ThenxUserApiModelIsProFingerprint.definingClass!!))
            .method
            .apply {
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object v0
                    return-object v0
                    """
                )
            }

        // 2) Content-level hasAccess flags
        listOf(
            ThenxExerciseHasAccessFingerprint,
            ThenxWorkoutHasAccessFingerprint
        ).forEach { fingerprint ->
            fingerprint
                .match(classDefBy(fingerprint.definingClass!!))
                .method
                .apply {
                    addInstructions(
                        0,
                        """
                        const/4 v0, 0x1
                        invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                        move-result-object v0
                        return-object v0
                        """
                    )
                }
        }

        // 3) RC EntitlementInfo.isActive always true (safe; no verifier side-effects)
        ThenxEntitlementInfoIsActiveFingerprint
            .match(classDefBy(ThenxEntitlementInfoIsActiveFingerprint.definingClass!!))
            .method
            .returnEarlyBoolean(true)

        // 4) Non-empty activeSubscriptions / purchased product ids
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

        // 5) EntitlementInfos active/all non-empty (null values — avoids verifier crash)
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

        // 6) Force empty-check on active map to false in RC customer-info coroutine
        ThenxCustomerInfoActiveCheckFingerprint
            .match(classDefBy(ThenxCustomerInfoActiveCheckFingerprint.definingClass!!))
            .method
            .apply {
                val isEmptyIndex = instructions.indexOfFirst { instruction ->
                    instruction is ReferenceInstruction &&
                        (instruction.reference as? MethodReference)?.let { reference ->
                            reference.definingClass == "Ljava/util/Map;" &&
                                reference.name == "isEmpty" &&
                                reference.returnType == "Z"
                        } == true
                }
                if (isEmptyIndex < 0) error("THENX: active entitlement empty check not found.")

                val resultIndex = isEmptyIndex + 1
                val resultRegister = (instructions[resultIndex] as? OneRegisterInstruction)?.registerA
                    ?: error("THENX: active entitlement empty-check result register not found.")

                replaceInstruction(resultIndex, "const/16 v$resultRegister, 0x0")
            }
    }
}
