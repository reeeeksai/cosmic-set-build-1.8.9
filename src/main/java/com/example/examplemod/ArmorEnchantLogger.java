package com.example.examplemod; // <-- change to your package

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemAxe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Map;

public class ArmorEnchantLogger {

    private int tickCounter = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < 20) return; // ~1 second
        tickCounter = 0;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        EntityPlayerSP player = mc.thePlayer;

        // For each marked type (0..3 = armor pieces, 4 = sword, 5 = axe), log any matching items
        // found in the player's worn slots and main inventory (and held item for weapons).
        for (int markIndex = 0; markIndex < 6; markIndex++) {
            if (!MarkedSlots.getInstance().isMarked(markIndex)) continue;

            java.util.List<ItemStack> found = new java.util.ArrayList<ItemStack>();

            if (markIndex >= 0 && markIndex <= 3) {
                // armor mapping: wearSlot = 3 - markIndex (player.getCurrentArmor: 0=boots..3=helmet)
                int wearSlot = 3 - markIndex;
                ItemStack worn = player.getCurrentArmor(wearSlot);
                if (worn != null && worn.getItem() instanceof ItemArmor) {
                    ItemArmor ia = (ItemArmor) worn.getItem();
                    if (ia.armorType == markIndex) found.add(worn);
                }
                // scan main inventory for armor pieces of this type
                for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                    ItemStack s = player.inventory.mainInventory[i];
                    if (s == null) continue;
                    if (s.getItem() instanceof ItemArmor) {
                        ItemArmor ia = (ItemArmor) s.getItem();
                        if (ia.armorType == markIndex) found.add(s);
                    }
                }
            } else if (markIndex == 4) {
                // swords: check held item and inventory
                ItemStack held = player.getCurrentEquippedItem();
                if (held != null && held.getItem() instanceof ItemSword) found.add(held);
                for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                    ItemStack s = player.inventory.mainInventory[i];
                    if (s == null) continue;
                    if (s.getItem() instanceof ItemSword) found.add(s);
                }
            } else if (markIndex == 5) {
                // axes
                ItemStack held = player.getCurrentEquippedItem();
                if (held != null && held.getItem() instanceof ItemAxe) found.add(held);
                for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                    ItemStack s = player.inventory.mainInventory[i];
                    if (s == null) continue;
                    if (s.getItem() instanceof ItemAxe) found.add(s);
                }
            }

            if (found.size() == 0) continue;

            String typeName = (markIndex == 4) ? "Sword" : (markIndex == 5) ? "Axe" : "Armor(" + markIndex + ")";
            System.out.println("======== Marked Type " + typeName + " (found " + found.size() + ") ========");
            for (ItemStack armor : found) {
                System.out.println("Item: " + armor.getDisplayName());
                if (armor.hasTagCompound()) {
                    NBTTagCompound tag = armor.getTagCompound();
                    if (tag.hasKey("display", 10)) {
                        NBTTagCompound display = tag.getCompoundTag("display");
                        if (display.hasKey("Lore", 9)) {
                            System.out.println("Custom Enchants (Lore):");
                            NBTTagList lore = display.getTagList("Lore", 8);
                            for (int i = 0; i < lore.tagCount(); i++) {
                                String line = lore.getStringTagAt(i);
                                line = line.replaceAll("§.", "");
                                System.out.println("  • " + line);
                            }
                        } else {
                            System.out.println("No lore found.");
                        }
                    } else {
                        System.out.println("No display tag found.");
                    }
                    System.out.println("NBT (raw): " + tag.toString());
                } else {
                    System.out.println("No NBT data on this item.");
                }
            }
        }
    }
}
