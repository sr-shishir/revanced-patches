package app.revanced.patches.youtube.utils.fix.parameter.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object ProtobufParameterBuilderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL_RANGE, // target reference
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT
    ),
    strings = listOf("Unexpected empty videoId.", "Prefetch request are disabled.")
)