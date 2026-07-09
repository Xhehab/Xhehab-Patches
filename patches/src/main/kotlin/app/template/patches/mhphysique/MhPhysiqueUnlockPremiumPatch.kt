package app.template.patches.mhphysique

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.pairip.disablePairIpLicenseCheckPatch
import app.template.patches.shared.Constants.MH_PHYSIQUE_COMPATIBILITY
import app.template.patches.shared.replaceTextOnce

private const val MH_MAIN_JS_PATH = "assets/public/main.3386af3d1c878129.js"

private val mhPhysiqueUnlockResourcePatch = rawResourcePatch {
    execute {
        val mainJs = get(MH_MAIN_JS_PATH)
        if (!mainJs.exists()) {
            throw PatchException("$MH_MAIN_JS_PATH not found in the APK.")
        }

        mainJs.writeBytes(
            mainJs.readBytes().replaceTextOnce(
                path = MH_MAIN_JS_PATH,
                search = "isFullAppUnlocked(){return this.isWelcomePhaseDone()&&this.auth.getAuthRole()>=ns.lP.Paid}",
                replacement = "isFullAppUnlocked(){return!0}"
            )
        )
    }
}

@Suppress("unused")
val mhPhysiqueUnlockPremiumPatch = bytecodePatch(
    name = "Unlock MH Physique Premium",
    description = "Unlock all Paid features",
    default = true
) {
    compatibleWith(MH_PHYSIQUE_COMPATIBILITY)
    dependsOn(mhPhysiqueUnlockResourcePatch, disablePairIpLicenseCheckPatch)
}
