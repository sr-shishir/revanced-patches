package app.revanced.patches.youtube.utils.returnyoutubedislike.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object TextComponentContextFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT, // conversion context
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN
    )
)