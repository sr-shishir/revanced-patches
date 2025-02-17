package app.revanced.patches.music.utils.microg.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.utils.annotations.MusicCompatibility
import app.revanced.patches.music.utils.fix.clientspoof.patch.ClientSpoofMusicPatch
import app.revanced.patches.music.utils.microg.bytecode.fingerprints.CastContextFetchFingerprint
import app.revanced.patches.music.utils.microg.bytecode.fingerprints.CastDynamiteModuleFingerprint
import app.revanced.patches.music.utils.microg.bytecode.fingerprints.CastDynamiteModuleV2Fingerprint
import app.revanced.patches.music.utils.microg.bytecode.fingerprints.GooglePlayUtilityFingerprint
import app.revanced.patches.music.utils.microg.bytecode.fingerprints.PrimeFingerprint
import app.revanced.patches.music.utils.microg.bytecode.fingerprints.ServiceCheckFingerprint
import app.revanced.patches.music.utils.microg.resource.patch.MicroGResourcePatch
import app.revanced.patches.music.utils.microg.shared.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.utils.microg.shared.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.shared.patch.packagename.PackageNamePatch
import app.revanced.util.microg.MicroGBytecodeHelper

@Patch
@DependsOn(
    [
        ClientSpoofMusicPatch::class,
        MicroGResourcePatch::class,
        PackageNamePatch::class
    ]
)
@Name("Microg support")
@Description("Allows ReVanced Music to run without root and under a different package name with MicroG.")
@MusicCompatibility
@Version("0.0.2")
class MicroGPatch : BytecodePatch(
    listOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
        PrimeFingerprint,
    )
) {
    // NOTE: the previous patch also replaced the following strings, but it seems like they are not needed:
    // - "com.google.android.gms.chimera.GmsIntentOperationService",
    // - "com.google.android.gms.phenotype.internal.IPhenotypeCallbacks",
    // - "com.google.android.gms.phenotype.internal.IPhenotypeService",
    // - "com.google.android.gms.phenotype.PACKAGE_NAME",
    // - "com.google.android.gms.phenotype.UPDATE",
    // - "com.google.android.gms.phenotype",
    override fun execute(context: BytecodeContext): PatchResult {
        val packageNameYouTube = PackageNamePatch.YouTubePackageName!!
        val packageNameMusic = PackageNamePatch.MusicPackageName!!

        // apply common microG patch
        MicroGBytecodeHelper.patchBytecode(
            context,
            arrayOf(
                MicroGBytecodeHelper.packageNameTransform(
                    YOUTUBE_PACKAGE_NAME,
                    packageNameYouTube
                )
            ),
            MicroGBytecodeHelper.PrimeMethodTransformationData(
                PrimeFingerprint,
                MUSIC_PACKAGE_NAME,
                packageNameMusic
            ),
            listOf(
                ServiceCheckFingerprint,
                GooglePlayUtilityFingerprint,
                CastDynamiteModuleFingerprint,
                CastDynamiteModuleV2Fingerprint,
                CastContextFetchFingerprint
            )
        )

        return PatchResultSuccess()
    }
}
