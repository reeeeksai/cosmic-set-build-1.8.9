package com.example.examplemod; // <-- your package

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DesiredEnchants {

    public enum SlotType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        SWORD,
        AXE
    }

    // Map: SlotType -> set of enchant names the user wants
    private static final Map<SlotType, Set<String>> desired = new HashMap<SlotType, Set<String>>();

    static {
        for (SlotType slot : SlotType.values()) {
            desired.put(slot, new HashSet<String>());
        }
    }

    public static boolean isDesired(SlotType slot, String enchantName) {
        if (enchantName == null) return false;
        return desired.get(slot).contains(enchantName);
    }

    public static void toggle(SlotType slot, String enchantName) {
        if (enchantName == null) return;
        Set<String> set = desired.get(slot);
        if (set.contains(enchantName)) {
            set.remove(enchantName);
        } else {
            set.add(enchantName);
        }
    }

    public static Set<String> getDesiredFor(SlotType slot) {
        return desired.get(slot);
    }

    // Helper: returns "Lifesteal, Overload" or "(none)" for a slot
    public static String getDesiredAsString(SlotType slot) {
        Set<String> set = desired.get(slot);
        if (set == null || set.isEmpty()) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : set) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(s);
            first = false;
        }
        return sb.toString();
    }
}
