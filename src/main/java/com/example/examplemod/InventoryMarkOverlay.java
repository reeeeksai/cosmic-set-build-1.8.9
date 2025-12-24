package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class InventoryMarkOverlay {

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen gui = event.gui;
        // only draw marks when an inventory/container GUI is open
        if (!(gui instanceof GuiContainer)) return;
        // show full marks only when viewing the player's inventory GUI;
        // when in other containers (e.g., chest) we will hide marks for
        // armor/worn slots so they don't draw over the container view
        boolean isPlayerInventoryGui = gui instanceof GuiInventory;
        MarkedSlots marks = MarkedSlots.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        GuiContainer guiC = (GuiContainer) gui;
        int guiLeft = 0;
        int guiTop = 0;
        // reflectively read guiLeft / guiTop similar to other code when needed
        String[] leftNames = new String[] { "guiLeft", "field_147003_i", "field_146999_f" };
        String[] topNames = new String[] { "guiTop", "field_147009_r", "field_147000_g" };
        for (String n : leftNames) {
            try {
                java.lang.reflect.Field fLeft = GuiContainer.class.getDeclaredField(n);
                fLeft.setAccessible(true);
                guiLeft = fLeft.getInt(guiC);
                break;
            } catch (Exception ex) { }
        }
        for (String n : topNames) {
            try {
                java.lang.reflect.Field fTop = GuiContainer.class.getDeclaredField(n);
                fTop.setAccessible(true);
                guiTop = fTop.getInt(guiC);
                break;
            } catch (Exception ex) { }
        }

        for (int i = 0; i < 6; i++) {
            if (!marks.isMarked(i)) continue;
            if (!marks.hasLastRect(i)) continue;
            // when not in the player's inventory GUI, skip drawing marks for
            // worn/equipped armor pieces only (but still draw marks for
            // armor items that are in the player's main inventory)
            if (!isPlayerInventoryGui && i >= 0 && i <= 3) {
                int invIdxForMark = marks.getMarkedInvIndex(i);
                if (invIdxForMark < 0) continue; // mark refers to equipped/worn piece
            }
            int relX = marks.getLastX(i);
            int relY = marks.getLastY(i);
            int w = marks.getLastW(i);
            int h = marks.getLastH(i);
            // if this mark refers to a mainInventory index, try to find the
            // corresponding Slot in the current GUI (player inventory may
            // shift between GUIs like chest vs player inventory). Fall back
            // to stored relative coords if not found.
            int invIdxForMark = marks.getMarkedInvIndex(i);
            int drawX = guiLeft + relX; // default/fallback
            int drawY = guiTop + relY;  // default/fallback
            if (invIdxForMark >= 0) {
                boolean found = false;
                for (Object o : guiC.inventorySlots.inventorySlots) {
                    if (!(o instanceof Slot)) continue;
                    Slot s = (Slot) o;
                    try {
                        if (s.inventory == mc.thePlayer.inventory) {
                            ItemStack st = s.getStack();
                            ItemStack expected = mc.thePlayer.inventory.mainInventory[invIdxForMark];
                            if (expected != null && st == expected) {
                                drawX = guiLeft + s.xDisplayPosition;
                                drawY = guiTop + s.yDisplayPosition;
                                found = true;
                                break;
                            }
                        }
                    } catch (Exception ex) { /* ignore */ }
                }
                if (!found) { drawX = guiLeft + relX; drawY = guiTop + relY; }
            } else {
                drawX = guiLeft + relX;
                drawY = guiTop + relY;
            }
            Gui.drawRect(drawX - 2, drawY - 2, drawX + w + 2, drawY + h + 2, 0x8040FF40);
        }

        // Additionally highlight any books in the player's inventory that provide
        // enchants missing from each marked piece.
        // active desired enchants
        EnchantSet active = EnchantSetManager.getInstance().getActiveSet();
        if (active == null) return;

        // iterate marked slots and, for each, compute missing enchants then highlight books
        for (int i = 0; i < 6; i++) {
            if (!marks.isMarked(i)) continue;
            int invIdx = marks.getMarkedInvIndex(i);
            // when not in the player's inventory GUI, skip processing marks that
            // refer to equipped/worn armor pieces (invIdx < 0). If invIdx >= 0
            // the mark refers to a mainInventory slot and should still be processed.
            if (!isPlayerInventoryGui && i >= 0 && i <= 3 && invIdx < 0) continue;
            if (mc.thePlayer == null) continue;
            ItemStack piece = null;
            if (invIdx >= 0) {
                piece = mc.thePlayer.inventory.mainInventory[invIdx];
            } else if (i >= 0 && i <= 3) {
                // marked slot refers to a worn armor piece; armorInventory indices
                // are 0=boots,1=leggings,2=chestplate,3=helmet, while our mark
                // indices are 0=helmet..3=boots, so map via (3 - i)
                int armorIdx = 3 - i;
                try { piece = mc.thePlayer.inventory.armorInventory[armorIdx]; } catch (Exception e) { piece = null; }
            }
            if (piece == null) continue;

            // map index to SlotType name
            String slotName;
            switch (i) {
                case 0: slotName = SlotType.HELMET.name(); break;
                case 1: slotName = SlotType.CHESTPLATE.name(); break;
                case 2: slotName = SlotType.LEGGINGS.name(); break;
                case 3: slotName = SlotType.BOOTS.name(); break;
                case 4: slotName = SlotType.SWORD.name(); break;
                case 5: slotName = SlotType.AXE.name(); break;
                default: slotName = null; break;
            }
            if (slotName == null) continue;

            java.util.List<String> desired = active.desired.get(slotName);
            if (desired == null || desired.isEmpty()) continue;

            // extract enchant names present on the piece (from lore or display)
            Set<String> present = extractEnchantNames(piece);
            Set<String> missing = new HashSet<String>();
            for (String want : desired) {
                if (want == null) continue;
                String wantLc = want.toLowerCase();
                boolean found = false;
                for (String p : present) {
                    if (p.toLowerCase().contains(wantLc) || wantLc.contains(p.toLowerCase())) { found = true; break; }
                }
                if (!found) missing.add(wantLc);
            }
            if (missing.isEmpty()) continue;

            // scan visible container slots for books/items that provide any missing enchant
            for (int si = 0; si < guiC.inventorySlots.inventorySlots.size(); si++) {
                Object o = guiC.inventorySlots.inventorySlots.get(si);
                if (!(o instanceof Slot)) continue;
                Slot s = (Slot) o;
                // include any container shown in the GUI (player inventory and other containers)
                int sx = s.xDisplayPosition;
                int sy = s.yDisplayPosition;
                ItemStack st = s.getStack();
                if (st == null) continue;
                // only consider regular books (plain book, writable/written books)
                if (!(st.getItem() == Items.book || st.getItem() instanceof ItemWritableBook)) continue;
                if (stackProvidesAnyMissing(st, missing)) {
                    int status = bookProvidesMissingStatus(st, missing);
                    int color = (status == 2) ? 0x80FFA500 : 0x8040FF40; // orange for partial, green for missing
                    Gui.drawRect(guiLeft + sx - 2, guiTop + sy - 2, guiLeft + sx + 16 + 2, guiTop + sy + 16 + 2, color);
                }
            }
        }
    }

    // return 0 = none, 1 = provides missing (green), 2 = provides partial (orange)
    private static int bookProvidesMissingStatus(ItemStack stack, Set<String> missingLc) {
        if (stack == null || missingLc == null || missingLc.isEmpty()) return 0;
        // Only consider the book's display name (title) when deciding status.
        try {
            String name = stack.getDisplayName();
            if (name != null) {
                String line = name.replaceAll("§.", "").trim();
                int status = lineProvidesMissingStatus(line, missingLc);
                if (status > 0) return status;
            }
        } catch (Exception e) { }
        return 0;
    }

    private static int lineProvidesMissingStatus(String line, Set<String> missingLc) {
        if (line == null || line.isEmpty()) return 0;
        String cleaned = line.toLowerCase();
        for (String m : missingLc) {
            if (m == null) continue;
            int pos = cleaned.indexOf(m);
            if (pos < 0) continue;
            // look immediately after the matched enchant name for a level (roman or digits)
            int afterPos = pos + m.length();
            String after = cleaned.substring(afterPos);
            int level = extractLeadingLevel(after);
            EnchantDef def = findEnchantDefByName(m);
            if (def != null) {
                if (level > 0 && level < def.maxLevel) return 2; // partial
                return 1; // treat unknown or >= max as full provider
            } else {
                if (level > 0) return 2; // heuristic: has level -> partial/full
                return 1;
            }
        }
        // fallback: if no direct name match, try to extract any level and match by contains
        int level = extractLevelFromLine(cleaned);
        if (level > 0) return 1;
        return 0;
    }

    private static EnchantDef findEnchantDefByName(String nameLc) {
        if (nameLc == null) return null;
        String nl = nameLc.toLowerCase();
        for (EnchantDef d : EnchantRegistry.all()) {
            if (d == null || d.name == null) continue;
            if (d.name.toLowerCase().equals(nl) || d.name.toLowerCase().contains(nl) || nl.contains(d.name.toLowerCase())) return d;
        }
        return null;
    }

    private static int extractLevelFromLine(String lineLc) {
        if (lineLc == null) return 0;
        // find any roman numeral or digits anywhere (fallback)
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(i{1,3}|iv|v|vi{0,3}|ix|x)\\b", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(lineLc);
        if (m.find()) {
            String roman = m.group(1).toUpperCase();
            int v = romanToInt(roman);
            if (v > 0) return v;
        }
        m = java.util.regex.Pattern.compile("\\b(\\d{1,2})\\b").matcher(lineLc);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception e) { }
        }
        return 0;
    }

    // extract a level that appears at the start of the provided string (after enchant name)
    private static int extractLeadingLevel(String s) {
        if (s == null) return 0;
        // trim leading separators
        s = s.trim();
        if (s.length() == 0) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(?:[:\\-\\s]*)?(i{1,3}|iv|v|vi{0,3}|ix|x)\\b", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(s);
        if (m.find()) {
            String roman = m.group(1).toUpperCase();
            int v = romanToInt(roman);
            if (v > 0) return v;
        }
        m = java.util.regex.Pattern.compile("^(?:[:\\-\\s]*)?(\\d{1,2})\\b").matcher(s);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception e) { }
        }
        return 0;
    }

    private static int romanToInt(String r) {
        if (r == null) return 0;
        // avoid string-switch for Java 6 compatibility
        if (r.equals("I")) return 1;
        if (r.equals("II")) return 2;
        if (r.equals("III")) return 3;
        if (r.equals("IV")) return 4;
        if (r.equals("V")) return 5;
        if (r.equals("VI")) return 6;
        if (r.equals("VII")) return 7;
        if (r.equals("VIII")) return 8;
        if (r.equals("IX")) return 9;
        if (r.equals("X")) return 10;
        try { return Integer.parseInt(r); } catch (Exception e) { return 0; }
    }

    private static Set<String> extractEnchantNames(ItemStack stack) {
        Set<String> out = new HashSet<String>();
        if (stack == null) return out;
        try {
            if (stack.hasTagCompound()) {
                NBTTagCompound tag = stack.getTagCompound();
                if (tag.hasKey("display", 10)) {
                    NBTTagCompound disp = tag.getCompoundTag("display");
                    if (disp.hasKey("Lore", 9)) {
                        NBTTagList lore = disp.getTagList("Lore", 8);
                        for (int i = 0; i < lore.tagCount(); i++) {
                            String line = lore.getStringTagAt(i);
                            line = line.replaceAll("§.", "");
                            // strip roman numerals and level suffixes
                            line = line.replaceAll("\\bI{1,3}\\b", "");
                            out.add(line.trim());
                        }
                    }
                }
            }
        } catch (Exception e) { /* ignore parsing errors */ }
        return out;
    }

    private static boolean stackProvidesAnyMissing(ItemStack stack, Set<String> missingLc) {
        if (stack == null || missingLc == null || missingLc.isEmpty()) return false;
        // Only check the book's display name (title) for missing enchant keywords.
        try {
            String name = stack.getDisplayName();
            if (name != null) {
                String nl = name.replaceAll("§.", "").toLowerCase();
                for (String m : missingLc) if (nl.contains(m)) return true;
            }
        } catch (Exception e) { }
        return false;
    }
}
