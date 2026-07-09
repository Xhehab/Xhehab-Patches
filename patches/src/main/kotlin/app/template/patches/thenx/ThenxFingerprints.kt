package app.template.patches.thenx

import app.morphe.patcher.Fingerprint

object ThenxCustomerInfoActiveSubscriptionsFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getActiveSubscriptions",
    parameters = emptyList(),
    returnType = "Ljava/util/Set;"
)

object ThenxCustomerInfoAllPurchasedProductIdsFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getAllPurchasedProductIds",
    parameters = emptyList(),
    returnType = "Ljava/util/Set;"
)

object ThenxCustomerInfoLatestExpirationDateFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getLatestExpirationDate",
    parameters = emptyList(),
    returnType = "Ljava/util/Date;"
)

object ThenxCustomerInfoExpirationDateForProductIdFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getExpirationDateForProductId",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "Ljava/util/Date;"
)

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

object ThenxEntitlementInfoIsActiveFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    parameters = emptyList(),
    returnType = "Z"
)

object ThenxEntitlementInfoExpirationDateFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "getExpirationDate",
    parameters = emptyList(),
    returnType = "Ljava/util/Date;"
)
