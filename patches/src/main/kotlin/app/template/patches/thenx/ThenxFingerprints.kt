package app.template.patches.thenx

import app.morphe.patcher.Fingerprint

object ThenxEntitlementInfosActiveFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
    name = "getActive",
    parameters = emptyList(),
    returnType = "Ljava/util/Map;"
)

object ThenxEntitlementInfosAllFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
    name = "getAll",
    parameters = emptyList(),
    returnType = "Ljava/util/Map;"
)

object ThenxCustomerInfoActiveCheckFingerprint : Fingerprint(
    definingClass = "Lve/c\$o\$a;",
    name = "invokeSuspend",
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;"
)
