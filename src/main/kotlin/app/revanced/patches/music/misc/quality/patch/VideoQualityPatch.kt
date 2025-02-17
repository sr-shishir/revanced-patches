package app.revanced.patches.music.misc.quality.patch

import app.revanced.extensions.findMutableMethodOf
import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.misc.quality.fingerprints.MusicVideoQualitySettingsFingerprint
import app.revanced.patches.music.misc.quality.fingerprints.MusicVideoQualitySettingsParentFingerprint
import app.revanced.patches.music.misc.quality.fingerprints.UserQualityChangeFingerprint
import app.revanced.patches.music.utils.annotations.MusicCompatibility
import app.revanced.patches.music.utils.resourceid.patch.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.resource.patch.SettingsPatch
import app.revanced.patches.music.utils.videoid.patch.VideoIdPatch
import app.revanced.util.enum.CategoryType
import app.revanced.util.integrations.Constants.MUSIC_MISC_PATH
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.Reference

@Patch
@Name("Remember video quality")
@Description("Save the video quality value whenever you change the video quality.")
@DependsOn(
    [
        SettingsPatch::class,
        SharedResourceIdPatch::class,
        VideoIdPatch::class
    ]
)
@MusicCompatibility
@Version("0.0.1")
class VideoQualityPatch : BytecodePatch(
    listOf(
        MusicVideoQualitySettingsParentFingerprint,
        UserQualityChangeFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {

        UserQualityChangeFingerprint.result?.let {
            it.mutableMethod.apply {
                val endIndex = it.scanResult.patternScanResult!!.endIndex
                val qualityChangedClass =
                    context.findClass(
                        (getInstruction<BuilderInstruction21c>(endIndex))
                            .reference.toString()
                    )!!
                        .mutableClass

                for (method in qualityChangedClass.methods) {
                    qualityChangedClass.findMutableMethodOf(method).apply {
                        if (this.name == "onItemClick") {
                            for ((index, instruction) in implementation!!.instructions.withIndex()) {
                                if (instruction.opcode != Opcode.INVOKE_INTERFACE) continue

                                qualityReference =
                                    getInstruction<ReferenceInstruction>(index - 1).reference
                                qIndexMethodName =
                                    ((getInstruction<Instruction35c>(index).reference) as MethodReference).name

                                addInstruction(
                                    0,
                                    "invoke-static {p3}, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->userChangedQuality(I)V"
                                )
                                break
                            }
                        }
                    }
                }
            }
        } ?: return UserQualityChangeFingerprint.toErrorResult()

        MusicVideoQualitySettingsParentFingerprint.result?.let { parentResult ->
            MusicVideoQualitySettingsFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.mutableMethod?.addInstructions(
                0, """
                    const-string v0, "$qIndexMethodName"
                    sput-object v0, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->qIndexMethod:Ljava/lang/String;
                    iget-object v0, p0, $qualityReference
                    invoke-static {p1, p2, v0}, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->setVideoQuality([Ljava/lang/Object;ILjava/lang/Object;)I
                    move-result p2
                    """
            ) ?: return MusicVideoQualitySettingsFingerprint.toErrorResult()
        } ?: return MusicVideoQualitySettingsParentFingerprint.toErrorResult()

        VideoIdPatch.injectCall("$INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->newVideoStarted(Ljava/lang/String;)V")
        SettingsPatch.addMusicPreference(
            CategoryType.MISC,
            "revanced_enable_save_video_quality",
            "true"
        )

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR =
            "$MUSIC_MISC_PATH/VideoQualityPatch;"

        private lateinit var qIndexMethodName: String
        private lateinit var qualityReference: Reference
    }
}
