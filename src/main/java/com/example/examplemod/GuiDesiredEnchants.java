package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiDesiredEnchants extends GuiScreen {

    private static final String[] ALL_ENCHANTS = new String[]{
            "Lifesteal",
            "Overload",
            "Gears",
            "Tank",
            "Rage",
            "Combo",
            "Execute",
            "Sniper",
            "Idk",
            "Idk",
            "Idk",
            "Idk",
            "Idk",
            "Idk",
    };


    private final List<GuiEnchantSelectButton> enchantButtons = new ArrayList<GuiEnchantSelectButton>();
    private GuiTextField searchField;
    private int scrollOffset = 0;
    private int slotScrollOffset = 0;

    private GuiButton helmetButton;
    private GuiButton chestButton;
    private GuiButton bootsButton;
    private GuiButton swordButton;
    private GuiButton axeButton;

    private String selectedEnchant = null;

    @Override
    public void initGui() {

        this.buttonList.clear();
        enchantButtons.clear();
        selectedEnchant = null;

        int centerX = this.width / 2;
        int leftX = centerX - 200;

        // Search Bar
        this.searchField = new GuiTextField(8000, this.fontRendererObj, leftX, 15, 120, 20);
        this.searchField.setFocused(true);
        this.searchField.setCanLoseFocus(false);

        // Move "All Enchants" label button down slightly
        this.buttonList.add(new GuiButton(9000, leftX, 40, 120, 20, "All Enchants"));

        updateFilteredButtons();

        // Initialize slot buttons
        // Positions will be updated in drawScreen
        helmetButton = new GuiButton(1000, 0, 0, 150, 20, "");
        chestButton = new GuiButton(1001, 0, 0, 150, 20, "");
        bootsButton = new GuiButton(1002, 0, 0, 150, 20, "");
        swordButton = new GuiButton(1003, 0, 0, 150, 20, "");
        axeButton = new GuiButton(1004, 0, 0, 150, 20, "");

        this.buttonList.add(helmetButton);
        this.buttonList.add(chestButton);
        this.buttonList.add(bootsButton);
        this.buttonList.add(swordButton);
        this.buttonList.add(axeButton);

        this.buttonList.add(new GuiButton(
                2000,
                centerX - 40,
                this.height - 30,
                80,
                20,
                "Done"
        ));

        updateSlotButtonsText();
    }

    private void updateSlotButtonsText() {
        helmetButton.displayString = "Helmet: ";
        chestButton.displayString = "Chestplate: ";
        bootsButton.displayString = "Boots: ";
        swordButton.displayString = "Sword: ";
        axeButton.displayString = "Axe: ";
    }

    private void updateFilteredButtons() {
        // Remove old buttons to avoid duplicates
        this.buttonList.removeAll(enchantButtons);
        enchantButtons.clear();

        int centerX = this.width / 2;
        int leftX = centerX - 200;
        int id = 0;
        String query = searchField.getText().toLowerCase();

        for (String ench : ALL_ENCHANTS) {
            if (query.isEmpty() || ench.toLowerCase().contains(query)) {
                // Y position is set in drawScreen
                GuiEnchantSelectButton btn = new GuiEnchantSelectButton(id++, leftX, 0, 120, 20, ench);
                this.buttonList.add(btn);
                enchantButtons.add(btn);
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int centerX = this.width / 2;

            if (mouseX < centerX - 60) {
                if (dWheel > 0) {
                    scrollOffset -= 22;
                } else {
                    scrollOffset += 22;
                }

                int listLength = enchantButtons.size() * 22;
                int viewHeight = this.height - 75; // Area between header and bottom

                if (scrollOffset < 0) scrollOffset = 0;
                if (listLength > viewHeight && scrollOffset > listLength - viewHeight) {
                    scrollOffset = listLength - viewHeight;
                } else if (listLength <= viewHeight) {
                    scrollOffset = 0;
                }
            } else {
                if (dWheel > 0) {
                    slotScrollOffset -= 20;
                } else {
                    slotScrollOffset += 20;
                }

                int totalSlotWidth = 5 * 160; // 5 buttons * (150 width + 10 padding)
                int slotViewW = this.width - (centerX - 50) - 20;

                if (slotScrollOffset < 0) slotScrollOffset = 0;
                if (totalSlotWidth > slotViewW && slotScrollOffset > totalSlotWidth - slotViewW) {
                    slotScrollOffset = totalSlotWidth - slotViewW;
                } else if (totalSlotWidth <= slotViewW) {
                    slotScrollOffset = 0;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            scrollOffset = 0;
            updateFilteredButtons();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Update Scrollable List
        int listStartY = 65;
        int bottomLimit = this.height - 10;
        int viewHeight = bottomLimit - listStartY;
        int totalListHeight = enchantButtons.size() * 22;

        for (int i = 0; i < enchantButtons.size(); i++) {
            GuiEnchantSelectButton btn = enchantButtons.get(i);
            int relY = (i * 22) - scrollOffset;
            btn.yPosition = listStartY + relY;
            btn.visible = btn.yPosition >= listStartY && btn.yPosition + 20 <= bottomLimit;
        }

        this.searchField.drawTextBox();

        // Update Slot Button Positions (Horizontal Scroll)
        int centerX = this.width / 2;
        int slotViewX = centerX - 50;
        int slotViewY = 40;
        int slotViewW = this.width - slotViewX - 20;

        GuiButton[] slots = {helmetButton, chestButton, bootsButton, swordButton, axeButton};
        for (int i = 0; i < slots.length; i++) {
            GuiButton btn = slots[i];
            int relX = (i * 160) - slotScrollOffset;
            btn.xPosition = slotViewX + relX;
            btn.yPosition = slotViewY;
            // Only show if visible within the horizontal viewport
            btn.visible = btn.xPosition + btn.width > slotViewX && btn.xPosition < slotViewX + slotViewW;
        }

        // Draw rest of the GUI
        this.drawCenteredString(this.fontRendererObj, "Select Desired Enchants", this.width / 2, 5, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw Horizontal Scrollbar for Slots
        int totalSlotWidth = slots.length * 160;
        if (totalSlotWidth > slotViewW) {
            int scrollBarX = slotViewX;
            int scrollBarY = slotViewY + 25; // Below buttons
            int scrollBarHeight = 6;

            // Track
            drawRect(scrollBarX, scrollBarY, scrollBarX + slotViewW, scrollBarY + scrollBarHeight, 0x80000000);

            // Thumb
            int thumbWidth = (int) ((float) slotViewW * slotViewW / totalSlotWidth);
            if (thumbWidth < 32) thumbWidth = 32;
            if (thumbWidth > slotViewW) thumbWidth = slotViewW;

            int maxScroll = totalSlotWidth - slotViewW;
            int thumbX = scrollBarX + (int) ((float) slotScrollOffset / maxScroll * (slotViewW - thumbWidth));

            drawRect(thumbX, scrollBarY, thumbX + thumbWidth, scrollBarY + scrollBarHeight, 0xFFC0C0C0);
        }

        // Draw Scrollbar
        if (totalListHeight > viewHeight) {
            int leftX = centerX - 200;
            int scrollBarX = leftX + 124;
            int scrollBarWidth = 6;

            // Track
            drawRect(scrollBarX, listStartY, scrollBarX + scrollBarWidth, listStartY + viewHeight, 0x80000000);

            // Thumb
            int thumbHeight = (int) ((float) viewHeight * viewHeight / totalListHeight);
            if (thumbHeight < 32) thumbHeight = 32;
            if (thumbHeight > viewHeight) thumbHeight = viewHeight;

            int maxScroll = totalListHeight - viewHeight;
            int thumbY = listStartY + (int) ((float) scrollOffset / maxScroll * (viewHeight - thumbHeight));

            drawRect(scrollBarX, thumbY, scrollBarX + scrollBarWidth, thumbY + thumbHeight, 0xFFC0C0C0);
        }
    }

    private static class GuiEnchantSelectButton extends GuiButton {
        public final String enchantName;

        public GuiEnchantSelectButton(int id, int x, int y, int w, int h, String enchantName) {
            super(id, x, y, w, h, enchantName);
            this.enchantName = enchantName;
        }
    }
}
