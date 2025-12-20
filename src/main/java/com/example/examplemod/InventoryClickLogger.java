package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.settings.KeyBinding;

public class InventoryClickLogger {
    private static KeyBinding key;
    private boolean prevKeyDown = false;

    public static void init() {
        if (key == null) {
            key = new KeyBinding("Log inventory item", Keyboard.KEY_K, "Cosmic Set Builder");
            ClientRegistry.registerKeyBinding(key);
            InventoryClickLogger inst = new InventoryClickLogger();
            MinecraftForge.EVENT_BUS.register(inst);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().thePlayer == null) return;

        // Verify existing marks: if the tracked item has moved from the recorded inventory slot, clear the mark.
        // Minecraft mc = Minecraft.getMinecraft();
        // MarkedSlots marks = MarkedSlots.getInstance();
        // for (int i = 0; i < 6; i++) {
        //     if (!marks.isMarked(i)) continue;
        //     int invIdx = marks.getMarkedInvIndex(i);
        //     String sig = marks.getMarkedSignature(i);
        //     if (invIdx >= 0) {
        //         ItemStack cur = mc.thePlayer.inventory.mainInventory[invIdx];
        //         String curSig = signatureOf(cur);
        //         if (sig == null || (curSig == null) || !sig.equals(curSig)) {
        //             // item moved or changed — clear mark
        //             marks.setMarkedAt(i, false, 0,0,0,0, -1, null);
        //         }
        //     }
        // }

        boolean down = Keyboard.isKeyDown(key.getKeyCode());
        if (down && !prevKeyDown) {
            processClick();
        }
        prevKeyDown = down;
    }

    private void processClick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiContainer)) {
            // suppressed chat: not in inventory
            return;
        }

        GuiContainer gui = (GuiContainer) mc.currentScreen;
        // convert mouse coordinates to GUI (scaled) coordinates using ScaledResolution
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        int mouseX = Mouse.getX() / scale;
        int mouseY = sr.getScaledHeight() - Mouse.getY() / scale - 1;
        int guiLeft = 0;
        int guiTop = 0;
        try {
            java.lang.reflect.Field fLeft = GuiContainer.class.getDeclaredField("guiLeft");
            java.lang.reflect.Field fTop = GuiContainer.class.getDeclaredField("guiTop");
            fLeft.setAccessible(true);
            fTop.setAccessible(true);
            guiLeft = fLeft.getInt(gui);
            guiTop = fTop.getInt(gui);
        } catch (Exception e) {
            // fallback to 0
        }
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        // debug: show computed absolute and relative mouse coords + gui offsets
        // suppressed debug chat

        // debug: list all slot positions (index:x,y,item)
        for (int i = 0; i < gui.inventorySlots.inventorySlots.size(); i++) {
            Object o = gui.inventorySlots.inventorySlots.get(i);
            if (!(o instanceof Slot)) continue;
            Slot s = (Slot) o;
            ItemStack st = s.getStack();
            String nm = st == null ? "<empty>" : st.getDisplayName();
            // suppressed debug chat for slot positions
        }

        for (int i = 0; i < gui.inventorySlots.inventorySlots.size(); i++) {
            Object o = gui.inventorySlots.inventorySlots.get(i);
            if (!(o instanceof Slot)) continue;
            Slot slot = (Slot) o;
            int sx = slot.xDisplayPosition;
            int sy = slot.yDisplayPosition;
            if (relX >= sx && relX < sx + 16 && relY >= sy && relY < sy + 16) {
                // Only allow marking when clicking a slot that belongs to the player's inventory
                if (slot.inventory != mc.thePlayer.inventory) {
                    // not a player-invento (ery slot.g. crafting output, container chest), ignore
                    return;
                }
                ItemStack stack = slot.getStack();
                // suppressed debug chat for clicked slot
                // determine which logical slot index to toggle (0..5)
                int markIndex = -1;
                if (stack != null) {
                    Item it = stack.getItem();
                    if (it instanceof ItemArmor) {
                        ItemArmor armor = (ItemArmor) it;
                        int type = armor.armorType; // 0=helmet? (MCP mapping may differ)
                        // In MCP, armorType: 0=helmet,1=plate,2=legs,3=boots
                        if (type == 0) markIndex = 0; // helmet
                        else if (type == 1) markIndex = 1; // chest
                        else if (type == 2) markIndex = 2; // legs
                        else if (type == 3) markIndex = 3; // boots
                    } else {
                        // if player is holding this item or it's a sword/axe, map to held indexes
                        ItemStack held = mc.thePlayer.getCurrentEquippedItem();
                        if (held != null && held == stack) {
                            if (it instanceof ItemSword) markIndex = 4;
                            else if (it instanceof ItemAxe) markIndex = 5;
                            else markIndex = 4; // default to sword/held
                        } else {
                            // try to detect by item class name for tools
                            if (it instanceof ItemSword) markIndex = 4;
                            else if (it instanceof ItemAxe) markIndex = 5;
                        }
                    }
                }

                // toggle mark if we mapped to a valid index
                if (markIndex >= 0 && markIndex < 6) {
                    MarkedSlots marks = MarkedSlots.getInstance();
                    marks.toggle(markIndex);
                    boolean now = marks.isMarked(markIndex);
                    // store slot rectangle when marked so overlay can render
                    if (now) {
                        // determine which player inventory index this corresponds to (object identity)
                        int invIndex = -1;
                        for (int k = 0; k < mc.thePlayer.inventory.mainInventory.length; k++) {
                            if (mc.thePlayer.inventory.mainInventory[k] == stack) { invIndex = k; break; }
                        }
                        String sig = signatureOf(stack);
                        marks.setMarkedAt(markIndex, true, guiLeft + sx, guiTop + sy, 16, 16, invIndex, sig);
                    } else {
                        marks.setMarkedAt(markIndex, false, 0,0,0,0, -1, null);
                    }
                    String lbl = "";
                    switch (markIndex) {
                        case 0: lbl = "Helmet"; break;
                        case 1: lbl = "Chestplate"; break;
                        case 2: lbl = "Leggings"; break;
                        case 3: lbl = "Boots"; break;
                        case 4: lbl = "Sword(held)"; break;
                        case 5: lbl = "Axe(held)"; break;
                    }
                    // suppressed mark/unmark chat
                    // log item info immediately
                    logItemStack(stack);
                    return;
                }

                // fallback: just log
                logItemStack(stack);
                return;
            }
        }

        // suppressed chat: no item slot clicked
    }

    private void logItemStack(ItemStack stack) {
        if (stack == null) {
            System.out.println("Clicked empty slot.");
            return;
        }
        System.out.println("======== Clicked Item: " + stack.getDisplayName() + " ========");

        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
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

    private static String signatureOf(ItemStack stack) {
        if (stack == null) return null;
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(stack.getItem().getUnlocalizedName());
        } catch (Exception e) {
            sb.append(stack.getDisplayName());
        }
        sb.append(":").append(stack.getItemDamage());
        if (stack.hasTagCompound()) {
            try { sb.append("|").append(stack.getTagCompound().toString()); } catch (Exception e) { /* ignore */ }
        } else {
            sb.append("|").append(stack.getDisplayName());
        }
        return sb.toString();
    }
}
