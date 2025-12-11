package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestGuiListener {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.gui;
        if (Minecraft.getMinecraft().thePlayer == null) return;

        // tell user which GUI class opened (helps debug custom/server GUIs)
        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("Opened GUI: " + (gui == null ? "null" : gui.getClass().getName()))
        );

        // only proceed if it's a GuiContainer (has inventorySlots / Slot list)
        if (!(gui instanceof GuiContainer)) return;
        GuiContainer containerGui = (GuiContainer) gui;

        // iterate all slots in the container and show their stacks (Empty when slot.getStack() == null)
        for (int i = 0; i < containerGui.inventorySlots.inventorySlots.size(); i++) {
            Slot slot = containerGui.inventorySlots.inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            String name = (stack != null) ? stack.getDisplayName() : "Empty";
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Slot " + i + ": " + name));
        }
    }
}