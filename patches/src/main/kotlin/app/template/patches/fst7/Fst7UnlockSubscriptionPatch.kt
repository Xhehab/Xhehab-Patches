package app.template.patches.fst7

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FST7_COMPATIBILITY

@Suppress("unused")
val fst7UnlockSubscriptionPatch = bytecodePatch(
    name = "Unlock FST-7 Subscription",
    description = "Unlock FST-7 subscription state for local testing.",
    default = true
) {
    compatibleWith(FST7_COMPATIBILITY)

    execute {
        Fst7HybridHasActiveSubscriptionsFingerprint
            .match(classDefBy(Fst7HybridHasActiveSubscriptionsFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object v0
                    """.trimIndent()
                )
            }

        Fst7HybridGetActiveSubscriptionsFingerprint
            .match(classDefBy(Fst7HybridGetActiveSubscriptionsFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    new-array v0, v0, [Lcom/margelo/nitro/iap/NitroActiveSubscription;

                    new-instance v1, Lcom/margelo/nitro/iap/NitroActiveSubscription;
                    const-string v2, "plankk.fst7.subscription"
                    const/4 v3, 0x1
                    const-string v4, "xhehab-fst7-test"
                    const-string v5, "xhehab-fst7-token"
                    const-wide/16 v6, 0x0
                    const/4 v8, 0x0
                    const/4 v9, 0x0
                    sget-object v10, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    const/4 v11, 0x0
                    const/4 v12, 0x0
                    sget-object v13, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    const-string v14, "monthly"
                    const-string v15, "monthly"
                    const-string v16, "xhehab-fst7-token"
                    invoke-direct/range {v1 .. v16}, Lcom/margelo/nitro/iap/NitroActiveSubscription;-><init>(Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;DLjava/lang/Double;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/Double;Lcom/margelo/nitro/iap/NitroRenewalInfoIOS;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

                    const/4 v2, 0x0
                    aput-object v1, v0, v2
                    return-object v0
                    """.trimIndent()
                )
            }
    }
}
