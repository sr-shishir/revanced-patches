package app.revanced.patches.youtube.utils.playercontrols.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction

object UserScrubbingFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    opcodes = listOf(Opcode.OR_INT_LIT8),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation!!.instructions.any {
            ((it as? NarrowLiteralInstruction)?.narrowLiteral == 64)
        }
    }
)