package app.revanced.patches.youtube.utils.overridespeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object SpeedClassFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("L"),
    opcodes = listOf(Opcode.RETURN_OBJECT),
    strings = listOf("PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT")
)