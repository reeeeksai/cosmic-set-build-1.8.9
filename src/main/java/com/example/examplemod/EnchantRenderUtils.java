package com.example.examplemod;

/**
 * Small utility helpers for rendering enchant-related UI strings.
 */
public final class EnchantRenderUtils {

    private EnchantRenderUtils() {}

    public static String rarityColorCode(EnchantDef def) {
        if (def == null || def.rarity == null) return "";
        switch (def.rarity) {
            case COMMON: return "\u00A7l"; // bold default (white)
            case UNIQUE: return "\u00A7a\u00A7l"; // bright green + bold
            case ELITE: return "\u00A7b\u00A7l"; // aqua + bold
            case ULTIMATE: return "\u00A7e\u00A7l"; // yellow + bold
            case LEGENDARY: return "\u00A76\u00A7l"; // gold + bold
            case MASTERY: return "\u00A74\u00A7l"; // dark red + bold
            case SOUL: return  "\u00A7c\u00A7l"; // red + bold
            case HEROIC: return "\u00A7d\u00A7l"; // light purple + bold
            default: return "";
        }
    }
}
