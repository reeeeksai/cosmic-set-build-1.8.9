package com.example.examplemod;

/**
 * Small helper for Roman numeral formatting.
 * Supports values 1..10 as requested; values outside that range
 * are returned as decimal strings.
 */
public final class RomanNumerals {
    private RomanNumerals() {}

    public static String toRoman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(n);
        }
    }
}
