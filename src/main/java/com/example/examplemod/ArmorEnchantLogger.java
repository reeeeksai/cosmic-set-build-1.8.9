package com.example.examplemod; // <-- change to your package

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
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

        // Loop armor slots
        for (int slot = 0; slot < 4; slot++) {
            ItemStack armor = player.getCurrentArmor(slot);
            if (armor == null) continue;

            System.out.println("======== Armor Slot " + slot + ": " + armor.getDisplayName() + " ========");

            //
            // 2. Print LORE-based enchants (Custom server enchants)
            //
            if (armor.hasTagCompound()) {
                NBTTagCompound tag = armor.getTagCompound();

                if (tag.hasKey("display", 10)) {
                    NBTTagCompound display = tag.getCompoundTag("display");

                    if (display.hasKey("Lore", 9)) {
                        System.out.println("Custom Enchants (Lore):");

                        NBTTagList lore = display.getTagList("Lore", 8);
                        for (int i = 0; i < lore.tagCount(); i++) {
                            String line = lore.getStringTagAt(i);

                            // Optional: remove § color codes
                            line = line.replaceAll("§.", "");

                            System.out.println("  • " + line);
                        }
                    } else {
                        System.out.println("No lore found.");
                    }
                } else {
                    System.out.println("No display tag found.");
                }

                //
                // 3. Print ALL NBT tags (in case enchants are stored elsewhere)
                //
                System.out.println("NBT (raw): " + tag.toString());
            } else {
                System.out.println("No NBT data on this item.");
            }
        }
    }
}
