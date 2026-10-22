package app.template.patches.videoplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.VIDEO_PLAYER_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val AD_REMOVED_KEY = "adRemoved"
private const val AD_REMOVED_MAX_GAP = 3

private fun constTrue(register: Int) =
    if (register < 16) "const/4 v$register, 0x1" else "const/16 v$register, 0x1"

private fun Instruction.isAdRemovedString() =
    (opcode == Opcode.CONST_STRING || opcode == Opcode.CONST_STRING_JUMBO) &&
        ((this as? ReferenceInstruction)?.reference as? StringReference)?.string == AD_REMOVED_KEY

private fun Instruction.adRemovedReadCallOrNull() =
    if (opcode == Opcode.INVOKE_STATIC) {
        (this as? ReferenceInstruction)?.reference as? MethodReference
    } else {
        null
    }

private fun MutableMethod.forceAdRemovedReadsTrue() {
    val methodInstructions = instructions

    for (i in methodInstructions.indices) {
        if (!methodInstructions[i].isAdRemovedString()) continue

        for (j in i + 1..minOf(i + 1 + AD_REMOVED_MAX_GAP, methodInstructions.size - 2)) {
            val call = methodInstructions[j].adRemovedReadCallOrNull() ?: continue
            if (
                call.definingClass != "Lwb4;" ||
                call.name != "b" ||
                call.returnType != "Z" ||
                call.parameterTypes.map { it.toString() } != listOf("Ljava/lang/String;", "Z")
            ) {
                continue
            }

            val moveResult = methodInstructions[j + 1]
            if (moveResult.opcode == Opcode.MOVE_RESULT) {
                val register = (moveResult as OneRegisterInstruction).registerA
                replaceInstruction(j + 1, constTrue(register))
            }
            break
        }
    }
}

@Suppress("unused")
val videoPlayerUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Video Player Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(VIDEO_PLAYER_COMPATIBILITY)

    execute {
        fun Fingerprint.forceTrue() {
            match(classDefBy(definingClass!!))
                .method
                .addInstructions(
                    0,
                    """
                        const/4 v0, 0x1
                        return v0
                    """.trimIndent()
                )
        }

        VideoPlayerPurchaseListContainsUnlockFingerprint.forceTrue()

        VideoPlayerSetAdFreeModeFingerprint
            .match(classDefBy(VideoPlayerSetAdFreeModeFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                """
                    const/4 p0, 0x1
                """.trimIndent()
            )

        VideoPlayerBillingStateGetters.forEach { fingerprint ->
            fingerprint.forceTrue()
        }

        // Keep the persisted ad-removed flag true as well, so every app path
        // that reads the preference directly also sees Pro as active.
        VideoPlayerAdRemovedReadFingerprint.matchAll().forEach { match ->
            match.method.forceAdRemovedReadsTrue()
        }
    }
}
