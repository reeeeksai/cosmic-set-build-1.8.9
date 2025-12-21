package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
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
        MarkedSlots marks = MarkedSlots.getInstance();
        for (int i = 0; i < 6; i++) {
            if (!marks.isMarked(i)) continue;
            if (!marks.hasLastRect(i)) continue;
            int x = marks.getLastX(i);
            int y = marks.getLastY(i);
            int w = marks.getLastW(i);
            int h = marks.getLastH(i);
            // draw translucent green rectangle
            Gui.drawRect(x - 2, y - 2, x + w + 2, y + h + 2, 0x8040FF40);
        }

        // Additionally highlight any books in the player's inventory that provide
        // enchants missing from each marked piece.
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

        // active desired enchants
        EnchantSet active = EnchantSetManager.getInstance().getActiveSet();
        if (active == null) return;

        // iterate marked slots and, for each, compute missing enchants then highlight books
        for (int i = 0; i < 6; i++) {
            if (!marks.isMarked(i)) continue;
            int invIdx = marks.getMarkedInvIndex(i);
            if (invIdx < 0 || mc.thePlayer == null) continue;
            ItemStack piece = mc.thePlayer.inventory.mainInventory[invIdx];
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

            // scan visible inventory slots for books/items that provide any missing enchant
            for (int si = 0; si < guiC.inventorySlots.inventorySlots.size(); si++) {
                Object o = guiC.inventorySlots.inventorySlots.get(si);
                if (!(o instanceof Slot)) continue;
                Slot s = (Slot) o;
                if (s.inventory != mc.thePlayer.inventory) continue; // only player inventory
                int sx = s.xDisplayPosition;
                int sy = s.yDisplayPosition;
                ItemStack st = s.getStack();
                if (st == null) continue;
                // only consider regular books (plain book, writable/written books)
                if (!(st.getItem() == Items.book || st.getItem() instanceof ItemWritableBook)) continue;
                if (stackProvidesAnyMissing(st, missing)) {
                    Gui.drawRect(guiLeft + sx - 2, guiTop + sy - 2, guiLeft + sx + 16 + 2, guiTop + sy + 16 + 2, 0x8040FF40);
                }
            }
        }
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
        // check lore/display
        try {
            if (stack.hasTagCompound()) {
                NBTTagCompound tag = stack.getTagCompound();
                if (tag.hasKey("display", 10)) {
                    NBTTagCompound disp = tag.getCompoundTag("display");
                    if (disp.hasKey("Lore", 9)) {
                        NBTTagList lore = disp.getTagList("Lore", 8);
                        for (int i = 0; i < lore.tagCount(); i++) {
                            String line = lore.getStringTagAt(i).replaceAll("§.", "").toLowerCase();
                            for (String m : missingLc) if (line.contains(m)) return true;
                        }
                    }
                }
            }
        } catch (Exception e) { }
        // fallback: check displayName
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
