package com.playercombatassistant.pca.improvised.imports

import com.playercombatassistant.pca.improvised.Handedness

/**
 * Safe, defensive parser for improvised weapon entry strings (e.g., entries in `sasha_tables.json`).
 *
 * Goals:
 * - Never throw.
 * - Extract best-effort display fields (no rule interpretation/enforcement).
 * - If a field cannot be determined, leave it empty / null.
 * - If parsing is ambiguous, preserve the full original string in [notes] for display/debugging.
 *
 * Non-goals:
 * - Full normalization or transformation into final improvised domain models.
 * - Parsing of bonus effects, ranges, AC, DCs, or "Special" mechanics.
 */
object SashaEntryParser {

    data class ParsedEntry(
        val name: String,
        val damage: String,
        val damageType: String,
        val handedness: Handedness?,
        val notes: String,
        val ambiguous: Boolean,
    )

    /**
     * Parse a single entry string.
     *
     * @param raw The original entry string from JSON.
     */
    fun parse(raw: String): ParsedEntry {
        // Ensure this never throws.
        return runCatching { parseInternal(raw) }.getOrElse {
            ParsedEntry(
                name = "",
                damage = "",
                damageType = "",
                handedness = null,
                notes = raw,
                ambiguous = true,
            )
        }
    }

    private fun parseInternal(raw: String): ParsedEntry {
        val input = raw.trim()
        if (input.isEmpty()) {
            return ParsedEntry("", "", "", null, "", ambiguous = false)
        }

        val (namePart, remainder, nameAmbiguous) = splitName(input)

        // Dice match: only accept dice in form NdM (e.g. 1d6). We do not parse modifiers or multiple dice.
        val diceMatches = DICE_REGEX.findAll(remainder).toList()
        val damage = diceMatches.firstOrNull()?.value.orEmpty()
        val diceAmbiguous = diceMatches.size > 1

        val damageType = extractDamageType(remainder, damage)

        val handedness = extractHandedness(remainder)

        val notesFromPeriod = extractNotesAfterFirstPeriod(remainder)

        // Consider it ambiguous if name delimiter wasn't found, or multiple dice appear.
        val ambiguous = nameAmbiguous || diceAmbiguous

        val notes = when {
            ambiguous -> input // preserve the full original string if ambiguous
            notesFromPeriod.isNotEmpty() -> notesFromPeriod
            else -> "" // unknown / none
        }

        return ParsedEntry(
            name = namePart,
            damage = damage,
            damageType = damageType,
            handedness = handedness,
            notes = notes,
            ambiguous = ambiguous,
        )
    }

    private data class NameSplit(
        val name: String,
        val remainder: String,
        val ambiguous: Boolean,
    )

    /**
     * Attempts to split "Name – rest" (en dash) or "Name - rest" (hyphen).
     *
     * If not found, returns empty name and treats parsing as ambiguous.
     */
    private fun splitName(input: String): NameSplit {
        val enDash = input.indexOf(" – ")
        if (enDash >= 0) {
            val name = input.substring(0, enDash).trim()
            val remainder = input.substring(enDash + 3).trim()
            return NameSplit(name, remainder, ambiguous = false)
        }

        val hyphen = input.indexOf(" - ")
        if (hyphen >= 0) {
            val name = input.substring(0, hyphen).trim()
            val remainder = input.substring(hyphen + 3).trim()
            return NameSplit(name, remainder, ambiguous = false)
        }

        return NameSplit(name = "", remainder = input, ambiguous = true)
    }

    /**
     * Extracts damage type text immediately following the dice.
     *
     * Example:
     * - "1d4 bludgeoning (light)." -> "bludgeoning"
     * - "1d4 bludgeoning +1 fire (light)." -> "bludgeoning +1 fire"
     *
     * If no dice was found, returns "".
     */
    private fun extractDamageType(remainder: String, dice: String): String {
        if (dice.isEmpty()) return ""
        val start = remainder.indexOf(dice)
        if (start < 0) return ""

        var after = remainder.substring(start + dice.length).trimStart()
        if (after.isEmpty()) return ""

        // Stop at first "(" or "." to avoid swallowing notes/parentheticals.
        val stopIdx = listOf(after.indexOf('('), after.indexOf('.'))
            .filter { it >= 0 }
            .minOrNull() ?: after.length

        after = after.substring(0, stopIdx).trim()
        // Strip trailing punctuation.
        after = after.trimEnd(',', ';', ':')
        return after
    }

    /**
     * Extracts handedness from the first parenthetical group, based on known tokens:
     * - "light" or "1H" -> ONE_HANDED
     * - "2H" -> TWO_HANDED
     * - "versatile" -> VERSATILE
     */
    private fun extractHandedness(remainder: String): Handedness? {
        val m = PAREN_REGEX.find(remainder) ?: return null
        val content = m.groupValues.getOrNull(1)?.lowercase().orEmpty()

        return when {
            content.contains("versatile") -> Handedness.VERSATILE
            content.contains("2h") -> Handedness.TWO_HANDED
            content.contains("1h") -> Handedness.ONE_HANDED
            content.contains("light") -> Handedness.ONE_HANDED
            else -> null
        }
    }

    /**
     * Notes are everything after the first '.' character.
     * Returns "" if there is no '.' or nothing after it.
     */
    private fun extractNotesAfterFirstPeriod(remainder: String): String {
        val idx = remainder.indexOf('.')
        if (idx < 0) return ""
        val after = remainder.substring(idx + 1).trim()
        return after
    }

    private val DICE_REGEX = Regex("\\b\\d+d\\d+\\b")
    private val PAREN_REGEX = Regex("\\(([^)]*)\\)")
}

