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
            key = new KeyBinding("Mark Inventory Item", Keyboard.KEY_M, "Cosmic Set Builder");
            ClientRegistry.registerKeyBinding(key);
            InventoryClickLogger inst = new InventoryClickLogger();
            MinecraftForge.EVENT_BUS.register(inst);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().thePlayer == null) return;

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
        // try several possible field names to be robust across obfuscated/production environments
        String[] leftNames = new String[] { "guiLeft", "field_147003_i", "field_146999_f" };
        String[] topNames = new String[] { "guiTop", "field_147009_r", "field_147000_g" };
        for (String n : leftNames) {
            try {
                java.lang.reflect.Field fLeft = GuiContainer.class.getDeclaredField(n);
                fLeft.setAccessible(true);
                guiLeft = fLeft.getInt(gui);
                break;
            } catch (Exception ex) {
                // try next
            }
        }
        for (String n : topNames) {
            try {
                java.lang.reflect.Field fTop = GuiContainer.class.getDeclaredField(n);
                fTop.setAccessible(true);
                guiTop = fTop.getInt(gui);
                break;
            } catch (Exception ex) {
                // try next
            }
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
                ItemStack stack = slot.getStack();
                // Determine the player's main-inventory index for the clicked stack.
                // Try identity match first (fast), then fall back to a signature
                // comparison so logically-equal stacks across container instances
                // are still recognized. If we cannot map to the player's inventory
                // and the slot isn't part of the player's inventory, ignore the click.
                int preInvIndex = -1;
                try {
                    if (stack != null) {
                        for (int k = 0; k < mc.thePlayer.inventory.mainInventory.length; k++) {
                            if (mc.thePlayer.inventory.mainInventory[k] == stack) { preInvIndex = k; break; }
                        }
                        if (preInvIndex == -1) {
                            String sig = signatureOf(stack);
                            if (sig != null) {
                                for (int k = 0; k < mc.thePlayer.inventory.mainInventory.length; k++) {
                                    ItemStack s2 = mc.thePlayer.inventory.mainInventory[k];
                                    if (s2 == null) continue;
                                    String s2sig = signatureOf(s2);
                                    if (sig.equals(s2sig)) { preInvIndex = k; break; }
                                }
                            }
                        }
                    }
                } catch (Exception e) { preInvIndex = -1; }
                if (slot.inventory != mc.thePlayer.inventory && preInvIndex == -1) {
                    return;
                }
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
                    boolean currentlyMarked = marks.isMarked(markIndex);
                    String prevSig = marks.getMarkedSignature(markIndex);
                    // use the precomputed inventory index (or -1 for worn/armor)
                    int invIndex = preInvIndex;
                    String sig = signatureOf(stack);
                    if (currentlyMarked) {
                        // if another item of the same logical slot is clicked, switch the mark
                        // to the newly clicked item instead of unmarking
                        boolean sameItem = (sig != null && sig.equals(prevSig));
                        if (!sameItem) {
                            // move the existing mark to the newly clicked slot
                            int storedIndex = (invIndex >= 0) ? invIndex : -1;
                            marks.setMarkedAt(markIndex, true, sx, sy, 16, 16, storedIndex, sig);
                        } else {
                            marks.setMarkedAt(markIndex, false, 0,0,0,0, -1, null);
                        }
                    } else {
                        // create a new mark at the clicked slot
                        int storedIndex = (invIndex >= 0) ? invIndex : -1;
                        marks.setMarkedAt(markIndex, true, sx, sy, 16, 16, storedIndex, sig);
                    }
                    boolean now = marks.isMarked(markIndex);
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
                    return;
                }
                return;
            }
        }

        // suppressed chat: no item slot clicked
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
