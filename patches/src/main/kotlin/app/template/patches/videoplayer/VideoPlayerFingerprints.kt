package app.template.patches.videoplayer

import app.morphe.patcher.Fingerprint

val VideoPlayerPurchaseListContainsUnlockFingerprint = Fingerprint(
    definingClass = "Le82;",
    name = "M",
    parameters = listOf("Ljava/util/List;"),
    returnType = "Z"
)

val VideoPlayerSetAdFreeModeFingerprint = Fingerprint(
    definingClass = "Lj61;",
    name = "k",
    parameters = listOf("Z"),
    returnType = "V"
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
