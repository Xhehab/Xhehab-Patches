package app.template.patches.trainwise

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// CustomerInfo map mapper - converts CustomerInfo to a Map sent to React Native
val CustomerInfoMapFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/hybridcommon/mappers/CustomerInfoMapperKt;",
    name = "map",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    returnType = "Ljava/util/Map;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL)
)

val CommonGetCustomerInfoFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/hybridcommon/CommonKt;",
    name = "getCustomerInfo",
    parameters = listOf("Lcom/revenuecat/purchases/hybridcommon/OnResult;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

val CommonLogInFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/hybridcommon/CommonKt;",
    name = "logIn",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/revenuecat/purchases/hybridcommon/OnResult;"
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)
