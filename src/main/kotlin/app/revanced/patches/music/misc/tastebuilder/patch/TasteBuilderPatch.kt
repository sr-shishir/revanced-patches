package app.revanced.patches.music.misc.tastebuilder.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.misc.tastebuilder.fingerprints.TasteBuilderConstructorFingerprint
import app.revanced.patches.music.utils.annotations.MusicCompatibility
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@Name("Hide taste builder")
@Description("Hides the \"Tell us which artists you like\" card from homepage.")
@MusicCompatibility
@Version("0.0.1")
class TasteBuilderPatch : BytecodePatch(
    listOf(TasteBuilderConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        TasteBuilderConstructorFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex - 8
                val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        const/16 v1, 0x8
                        invoke-virtual {v$register, v1}, Landroid/view/View;->setVisibility(I)V
                        """
                )
            }
        } ?: return TasteBuilderConstructorFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
