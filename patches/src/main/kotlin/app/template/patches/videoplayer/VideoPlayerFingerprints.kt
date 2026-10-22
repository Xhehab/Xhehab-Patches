package app.template.patches.videoplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

val VideoPlayerPurchaseListContainsUnlockFingerprint = Fingerprint(
    definingClass = "Le82;",
    name = "M",
    parameters = listOf("Ljava/util/List;"),
    returnType = "Z"
)

/**
 * Matches every method that reads the "adRemoved" SharedPreferences flag,
 * e.g. e82.K() and e82.W(). The read helper is wb4.b(String, Z)Z.
 */
val VideoPlayerAdRemovedReadFingerprint = Fingerprint(
    filters = listOf(
        string("adRemoved"),
        methodCall(
            definingClass = "Lwb4;",
            name = "b",
            parameters = listOf("Ljava/lang/String;", "Z"),
            returnType = "Z",
            opcode = Opcode.INVOKE_STATIC,
            location = InstructionLocation.MatchAfterWithin(3)
        ),
        opcode(Opcode.MOVE_RESULT, InstructionLocation.MatchAfterImmediately())
    )
)

val VideoPlayerSetAdFreeModeFingerprint = Fingerprint(
    definingClass = "Lj61;",
    name = "k",
    parameters = listOf("Z"),
    returnType = "V"
)

/**
 * No-arg method in PlayerActivity that maintains the rate-dialog counters.
 * Matches on both counter preference keys so it stays independent of the
 * obfuscated method name.
 */
val VideoPlayerRateCountFingerprint = Fingerprint(
    definingClass = "Lcom/inshot/xplayer/activities/PlayerActivity;",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("showRateCount", "showRateWatchTimeCount")
)

val VideoPlayerBillingStateGetters = listOf(
    Fingerprint(
        definingClass = "Le82\$b;",
        name = "a",
        parameters = listOf("Le82\$b;"),
        returnType = "Z"
    ),
    Fingerprint(
        definingClass = "Le82\$b;",
        name = "c",
        parameters = listOf("Le82\$b;"),
        returnType = "Z"
    ),
    Fingerprint(
        definingClass = "Le82\$b;",
        name = "e",
        parameters = emptyList(),
        returnType = "Z"
    ),
    Fingerprint(
        definingClass = "Le82\$b;",
        name = "f",
        parameters = emptyList(),
        returnType = "Z"
    )
)
