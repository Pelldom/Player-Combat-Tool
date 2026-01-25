package com.playercombatassistant.pca.spells

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Represents a spellcasting source (e.g., Wizard, Cleric, Sorcerer).
 * Each source has independent spell slots per level.
 */
@Serializable
data class SpellcastingSource(
    /**
     * Unique identifier for this source.
     */
    val id: String,

    /**
     * Display name of the spellcasting source.
     */
    val name: String,

    /**
     * Color used to identify this source in the UI.
     * Stored as ARGB integer for serialization.
     */
    @Serializable(with = ColorSerializer::class)
    val color: Color,

    /**
     * Spell slots by level.
     * Key = spell level (0-9)
     * Value = list of slot states (true = available, false = used)
     */
    val slotsByLevel: Map<Int, List<Boolean>> = emptyMap(),
)

/**
 * Serializer for Color to/from ARGB integer.
 */
object ColorSerializer : kotlinx.serialization.KSerializer<Color> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        "Color",
        kotlinx.serialization.descriptors.PrimitiveKind.LONG,
    )

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Color) {
        // Convert Color to ARGB integer
        val argb = (value.alpha * 255).toInt() shl 24 or
                ((value.red * 255).toInt() shl 16) or
                ((value.green * 255).toInt() shl 8) or
                (value.blue * 255).toInt()
        encoder.encodeLong(argb.toLong())
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Color {
        val argb = decoder.decodeLong().toInt()
        val alpha = ((argb shr 24) and 0xFF) / 255f
        val red = ((argb shr 16) and 0xFF) / 255f
        val green = ((argb shr 8) and 0xFF) / 255f
        val blue = (argb and 0xFF) / 255f
        return Color(red, green, blue, alpha)
    }
}
