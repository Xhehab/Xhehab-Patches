package app.template.patches.videoplayer

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.VIDEO_PLAYER_COMPATIBILITY

@Suppress("unused")
val videoPlayerDisableRateDialogPatch = bytecodePatch(
    name = "Disable Rate Dialog",
    description = "Disables the rate dialog counters so it never appears.",
    default = true
) {
    compatibleWith(VIDEO_PLAYER_COMPATIBILITY)

    execute {
        VideoPlayerRateCountFingerprint
            .match(classDefBy(VideoPlayerRateCountFingerprint.definingClass!!))
            .method
            .addInstruction(0, "return-void")
    }
}
