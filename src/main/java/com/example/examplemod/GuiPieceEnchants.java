package com.example.examplemod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI for editing a single piece's enchants.
 * Left: compatible enchants (searchable) via GuiEnchantPanel.
 * Right: current enchants on this piece with 'x' buttons to remove.
 */
public class GuiPieceEnchants extends GuiScreen {

    private final GuiDesiredEnchants parentGui;
    private final int slotIndex;
    private GuiEnchantPanel panel;
    private List<GuiButton> removeButtons = new ArrayList<GuiButton>();
    private static final int REMOVE_BASE = 9100;
    private static final int BACK_ID = 9110;

    public GuiPieceEnchants(GuiDesiredEnchants parent, int slotIndex) {
        this.parentGui = parent;
        this.slotIndex = slotIndex;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int leftX = centerX - 200;
        int listStartY = 40;
        int viewHeight = Math.max(40, this.height - 10 - listStartY);

        // build compatible list
        List<EnchantDef> compat = new ArrayList<EnchantDef>();
        List<String> disp = new ArrayList<String>();
        SlotType st = SlotType.values()[slotIndex];
        for (EnchantDef d : parentGui.getAllEnchantDefs()) {
            if (parentGui.enchantAppliesToName(d.name, st)) {
                compat.add(d);
                disp.add(parentGui.getColoredName(d));
            }
        }

        panel = new GuiEnchantPanel(this);
        panel.init(leftX, listStartY, viewHeight, compat, disp, this.fontRendererObj, this.buttonList);

        // Back button
        this.buttonList.add(new GuiButton(BACK_ID, centerX - 40, this.height - 30, 80, 20, "Back"));

        updateRemoveButtons();
    }

    private void updateRemoveButtons() {
        // remove previous
        this.buttonList.removeAll(removeButtons);
        removeButtons.clear();

        // right column
        int centerX = this.width / 2;
        int rightX = centerX + 40;
        int y = 50;
        List<String> assigned = parentGui.getSlotEnchants(slotIndex);
        for (int i = 0; i < assigned.size(); i++) {
            int id = REMOVE_BASE + i;
            GuiButton b = new GuiButton(id, rightX + 110, y, 16, 16, "x");
            this.buttonList.add(b);
            removeButtons.add(b);
            y += 20;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (panel != null) panel.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (panel != null && panel.textboxKeyTyped(typedChar, keyCode)) {
            // handled in panel
        } else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (panel != null) panel.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        // panel button (left) -> add enchant to slot
        String clicked = panel != null ? panel.onButtonPressed(button) : null;
        if (clicked != null) {
            // add to parent slot
            parentGui.addEnchantToSlot(slotIndex, clicked);
            updateRemoveButtons();
            return;
        }

        if (button.id >= REMOVE_BASE && button.id < REMOVE_BASE + 1000) {
            int idx = button.id - REMOVE_BASE;
            List<String> assigned = parentGui.getSlotEnchants(slotIndex);
            if (idx >= 0 && idx < assigned.size()) {
                String name = assigned.get(idx);
                parentGui.removeEnchantFromSlot(slotIndex, name);
                updateRemoveButtons();
            }
            return;
        }

        if (button.id == BACK_ID) {
            this.mc.displayGuiScreen(parentGui);
            return;
        }

        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (panel != null) panel.drawPanel(mouseX, mouseY, partialTicks);

        // draw right-side assigned enchant labels
        int centerX = this.width / 2;
        int rightX = centerX + 40;
        int y = 50;
        List<String> assigned = parentGui.getSlotEnchants(slotIndex);
        for (int i = 0; i < assigned.size(); i++) {
            String name = assigned.get(i);
            // colored display if known
            EnchantDef def = null;
            for (EnchantDef d : parentGui.getAllEnchantDefs()) if (d.name.equals(name)) { def = d; break; }
            String text = def != null ? parentGui.getColoredName(def) : name;
            this.fontRendererObj.drawStringWithShadow(text, rightX + 6, y + 2, 0xFFFFFF);
            y += 20;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (panel != null) panel.drawScaledLabels(mouseX, mouseY, partialTicks);
    }
}
