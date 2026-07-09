package app.template.patches.thenx

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.pairip.disablePairIpLicenseCheckPatch
import app.template.patches.shared.Constants.THENX_COMPATIBILITY
import app.template.patches.shared.replaceImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

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
