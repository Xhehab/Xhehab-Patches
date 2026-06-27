package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // TrainWise — Workout & Fitness Coach
    val TRAINWISE_COMPATIBILITY = Compatibility(
        name = "TrainWise - Workout & Fitness Coach",
        packageName = "com.trainwiseapp.app",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x00C853,
        targets = listOf(AppTarget(version = "1.4.10"))
    )
}
