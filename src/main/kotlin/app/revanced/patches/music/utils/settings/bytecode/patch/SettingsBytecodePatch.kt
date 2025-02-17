package app.revanced.patches.music.utils.settings.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.music.utils.integrations.patch.IntegrationsPatch
import app.revanced.patches.music.utils.settings.bytecode.fingerprints.PreferenceFingerprint
import app.revanced.patches.music.utils.settings.bytecode.fingerprints.SettingsHeadersFragmentFingerprint
import app.revanced.util.integrations.Constants.MUSIC_INTEGRATIONS_PATH
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@DependsOn([IntegrationsPatch::class])
class SettingsBytecodePatch : BytecodePatch(
    listOf(
        PreferenceFingerprint,
        SettingsHeadersFragmentFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {

        SettingsHeadersFragmentFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static {v$targetRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->setActivity(Ljava/lang/Object;)V"
                )
            }
        } ?: return SettingsHeadersFragmentFingerprint.toErrorResult()

        PreferenceFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val keyRegister = getInstruction<FiveRegisterInstruction>(targetIndex).registerD
                val valueRegister = getInstruction<FiveRegisterInstruction>(targetIndex).registerE

                addInstruction(
                    targetIndex,
                    "invoke-static {v$keyRegister, v$valueRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onPreferenceChanged(Ljava/lang/String;Z)V"
                )
            }
        } ?: return PreferenceFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "$MUSIC_INTEGRATIONS_PATH/settingsmenu/SharedPreferenceChangeListener;"
    }
}
