package app.revanced.patches.youtube.player.watermark.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.player.watermark.fingerprints.HideWatermarkFingerprint
import app.revanced.patches.youtube.player.watermark.fingerprints.HideWatermarkParentFingerprint
import app.revanced.patches.youtube.utils.annotations.YouTubeCompatibility
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.PLAYER
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@Name("Hide channel watermark")
@Description("Hides creator's watermarks on videos.")
@DependsOn([SettingsPatch::class])
@YouTubeCompatibility
@Version("0.0.1")
class HideChannelWatermarkBytecodePatch : BytecodePatch(
    listOf(HideWatermarkParentFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {

        HideWatermarkParentFingerprint.result?.let { parentResult ->
            HideWatermarkFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.endIndex
                    val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                    removeInstruction(insertIndex)
                    addInstructions(
                        insertIndex, """
                            invoke-static {}, $PLAYER->hideChannelWatermark()Z
                            move-result v$register
                            """
                    )
                }
            } ?: return HideWatermarkFingerprint.toErrorResult()
        } ?: return HideWatermarkParentFingerprint.toErrorResult()

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: HIDE_CHANNEL_WATERMARK"
            )
        )

        SettingsPatch.updatePatchStatus("hide-channel-watermark")

        return PatchResultSuccess()
    }
}
