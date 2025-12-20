package com.example.examplemod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable enchant list panel: search field, vertical list of enchant buttons, scrollbar.
 * Instantiate in any GUI and call its lifecycle methods.
 */
public class GuiEnchantPanel {

    private static final int PANEL_ENCHANT_ID_BASE = 7000;

    private final GuiScreen parent;
    private GuiTextField searchField;
    private final List<GuiButton> enchantButtons = new ArrayList<GuiButton>();
    private int scrollOffset = 0;
    private final List<String> renderLabels = new ArrayList<String>();
    private final List<String> rawNames = new ArrayList<String>();
    private boolean filteringEnabled = true;

    private int leftX = 0;
    private int listStartY = 0;
    private int viewHeight = 0;
    private int buttonWidth = 120;
    private int buttonHeight = 20;

    private List<EnchantDef> allEnchantDefs = new ArrayList<EnchantDef>();
    private List<String> displayNames = new ArrayList<String>();
    private net.minecraft.client.gui.FontRenderer fontRenderer;
    private List<GuiButton> parentButtonList;

    public GuiEnchantPanel(GuiScreen parent) {
        this.parent = parent;
    }

    /**
     * Initialize panel. Call from the GUI's initGui with appropriate layout values.
     */
    public void init(int leftX, int listStartY, int viewHeight, List<EnchantDef> allDefs, List<String> displayNames, net.minecraft.client.gui.FontRenderer fontRenderer, List<GuiButton> parentButtonList) {
        this.leftX = leftX;
        this.listStartY = listStartY;
        this.viewHeight = viewHeight;
        this.allEnchantDefs = allDefs != null ? allDefs : new ArrayList<EnchantDef>();
        this.displayNames = displayNames != null ? displayNames : new ArrayList<String>();

        // store renderer and parent button list
        this.fontRenderer = fontRenderer;
        this.parentButtonList = parentButtonList;

        // create search field (unique id space within panel)
        this.searchField = new GuiTextField(PANEL_ENCHANT_ID_BASE - 1, this.fontRenderer, leftX, listStartY - 26, buttonWidth, 20);
        this.searchField.setFocused(false);
        this.searchField.setCanLoseFocus(true);

        updateFilteredButtons();
    }

    public void setText(String text) {
        if (this.searchField != null) this.searchField.setText(text);
    }

    public String getText() {
        return this.searchField != null ? this.searchField.getText() : "";
    }

    public void setFocused(boolean focused) {
        if (this.searchField != null) this.searchField.setFocused(focused);
    }

    public void updateFilteredButtons() {
        // remove old buttons from parent.buttonList if we previously added them
        try {
            if (this.parentButtonList != null) this.parentButtonList.removeAll(enchantButtons);
        } catch (Exception e) { }
        enchantButtons.clear();
        renderLabels.clear();
        rawNames.clear();

        String query = searchField != null ? searchField.getText().toLowerCase() : "";
        int id = 0;
        for (int i = 0; i < allEnchantDefs.size(); i++) {
            EnchantDef def = allEnchantDefs.get(i);
            String name = def != null ? def.name : "";
            String label = (displayNames.size() > i && displayNames.get(i) != null) ? displayNames.get(i) : name;
            if (query.isEmpty() || name.toLowerCase().contains(query)) {
                GuiButton b = new GuiButton(PANEL_ENCHANT_ID_BASE + id++, leftX, 0, buttonWidth, buttonHeight, label);
                enchantButtons.add(b);
                renderLabels.add(label);
                rawNames.add(name);
                if (this.parentButtonList != null) this.parentButtonList.add(b);
            }
        }
        scrollOffset = 0;
    }

    public void handleMouseInput() throws IOException {
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            if (dWheel > 0) scrollOffset -= buttonHeight + 2;
            else scrollOffset += buttonHeight + 2;
            int total = enchantButtons.size() * (buttonHeight + 2);
            if (scrollOffset < 0) scrollOffset = 0;
            if (total <= viewHeight) scrollOffset = 0;
            else if (scrollOffset > total - viewHeight) scrollOffset = total - viewHeight;
        }
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (this.searchField != null && this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            if (filteringEnabled) updateFilteredButtons();
            return true;
        }
        return false;
    }

    public void setFilteringEnabled(boolean enabled) {
        this.filteringEnabled = enabled;
        if (enabled) {
            updateFilteredButtons();
        } else {
            // hide any existing enchant buttons while filtering is disabled
            for (GuiButton b : enchantButtons) {
                if (b != null) b.visible = false;
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.searchField != null) this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Returns enchant name if an enchant button was pressed, otherwise null.
     */
    public String onButtonPressed(GuiButton button) {
        if (button == null) return null;
        int id = button.id - PANEL_ENCHANT_ID_BASE;
        if (id >= 0 && id < enchantButtons.size()) {
            return rawNames.size() > id ? rawNames.get(id) : enchantButtons.get(id).displayString;
        }
        return null;
    }

    public void drawPanel(int mouseX, int mouseY, float partialTicks) {
        if (searchField != null) searchField.drawTextBox();
        if (!filteringEnabled) {
            // when filtering is disabled we don't show the enchant list
            for (GuiButton b : enchantButtons) if (b != null) b.visible = false;
            return;
        }

        int total = enchantButtons.size() * (buttonHeight + 2);
        for (int i = 0; i < enchantButtons.size(); i++) {
            GuiButton b = enchantButtons.get(i);
            int y = listStartY + (i * (buttonHeight + 2)) - scrollOffset;
            b.xPosition = leftX;
            b.yPosition = y;
            b.width = buttonWidth;
            b.height = buttonHeight;
            // Ensure buttons never appear above listStartY (prevents overlap with the search field)
            b.visible = (y >= listStartY) && (y < listStartY + viewHeight);
            // hide default button text so parent GUI can draw scaled labels after buttons are rendered
            if (b.visible) {
                b.displayString = "";
            }
        }

        // scrollbar
        if (total > viewHeight) {
            int barX = leftX + buttonWidth + 4;
            int barW = 6;
            int thumb = Math.max(32, (viewHeight * viewHeight) / total);
            int maxScroll = total - viewHeight;
            int thumbY = listStartY + (maxScroll == 0 ? 0 : (scrollOffset * (viewHeight - thumb) / maxScroll));
            // track
            drawRect(barX, listStartY, barX + barW, listStartY + viewHeight, 0x80000000);
            // thumb
            drawRect(barX, thumbY, barX + barW, thumbY + thumb, 0xFFC0C0C0);
        }
    }

    /**
     * Draw the stored labels at a reduced scale. Call after parent `super.drawScreen` so labels appear on top.
     */
    public void drawScaledLabels(int mouseX, int mouseY, float partialTicks) {
        if (fontRenderer == null) return;
        if (!filteringEnabled) return;
        float s = 0.745f;
        float inv = 1.0f / s;
        GL11.glPushMatrix();
        GL11.glScalef(s, s, 1f);
        for (int i = 0; i < enchantButtons.size(); i++) {
            GuiButton b = enchantButtons.get(i);
            if (!b.visible) continue;
            String text = renderLabels.size() > i ? renderLabels.get(i) : b.displayString;
            if (text == null) text = "";
            int tx = (int)((b.xPosition + 4) * inv);
            int ty = (int)((b.yPosition + (b.height / 2) - (fontRenderer.FONT_HEIGHT / 2)) * inv);
            fontRenderer.drawStringWithShadow(text, tx, ty, 0xFFFFFF);
        }
        GL11.glPopMatrix();
    }

    // helper drawRect that forwards to parent
    private void drawRect(int left, int top, int right, int bottom, int color) {
        parent.drawRect(left, top, right, bottom, color);
    }
}
