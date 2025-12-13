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
    };

    private static final int ENCHANT_ID_BASE = 3000;
    private static final int SLOT_ID_BASE = 1000;
    private static final int DONE_ID = 2000;
    private static final int TAG_ID_BASE = 4000; // base for tag label/remove pairs
    // set management IDs
    private static final int SET_PREV_ID = 6000;
    private static final int SET_NEXT_ID = 6001;
    private static final int SET_LABEL_ID = 6002;
    private static final int SET_NEW_ID = 6003;
    private static final int SET_DELETE_ID = 6005;

    private final List<GuiEnchantSelectButton> enchantButtons = new ArrayList<GuiEnchantSelectButton>();
    private final List<GuiTagButton> tagButtons = new ArrayList<GuiTagButton>();
    private GuiTextField searchField;
    private int scrollOffset = 0;
    private int slotScrollOffset = 0;

    // per-slot assigned enchants (6 slots now: Helmet, Chestplate, Leggings, Boots, Sword, Axe)
    private final List<String>[] slotEnchants = new ArrayList[6];

    // persistence manager
    private EnchantSetManager setManager;
    private boolean creatingNew = false;

    private GuiButton helmetButton;
    private GuiButton chestButton;
    private GuiButton leggingsButton;
    private GuiButton bootsButton;
    private GuiButton swordButton;
    private GuiButton axeButton;

    private String selectedEnchant = null;

    @Override
    public void initGui() {

        this.buttonList.clear();
        enchantButtons.clear();
        tagButtons.clear();
        selectedEnchant = null;

        for (int i = 0; i < slotEnchants.length; i++) slotEnchants[i] = new ArrayList<String>();

        int centerX = this.width / 2;
        int leftX = centerX - 200;

        // Search Bar
        this.searchField = new GuiTextField(8000, this.fontRendererObj, leftX, 15, 120, 20);
        this.searchField.setFocused(true);
        this.searchField.setCanLoseFocus(false);

        // Label
        this.buttonList.add(new GuiButton(9000, leftX, 40, 120, 20, "All Enchants"));

        // Set manager and set selector buttons
        setManager = EnchantSetManager.getInstance();
        this.buttonList.add(new GuiButton(SET_PREV_ID, leftX + 130, 15, 18, 20, "<"));
        this.buttonList.add(new GuiButton(SET_LABEL_ID, leftX + 150, 15, 140, 20, ""));
        this.buttonList.add(new GuiButton(SET_NEXT_ID, leftX + 294, 15, 18, 20, ">"));
        // place New and Delete on the first row next to the set selector
        this.buttonList.add(new GuiButton(SET_NEW_ID, leftX + 320, 15, 60, 20, "New Set"));
        this.buttonList.add(new GuiButton(SET_DELETE_ID, leftX + 386, 15, 60, 20, "Delete"));

        // populate GUI from active set
        loadActiveSetToGui();

        updateFilteredButtons();

        // Initialize slot buttons (smaller so they fit)
        int smallW = 120;
        int smallH = 18;
        helmetButton  = new GuiButton(SLOT_ID_BASE + 0, 0, 0, smallW, smallH, "");
        chestButton   = new GuiButton(SLOT_ID_BASE + 1, 0, 0, smallW, smallH, "");
        leggingsButton= new GuiButton(SLOT_ID_BASE + 2, 0, 0, smallW, smallH, "");
        bootsButton   = new GuiButton(SLOT_ID_BASE + 3, 0, 0, smallW, smallH, "");
        swordButton   = new GuiButton(SLOT_ID_BASE + 4, 0, 0, smallW, smallH, "");
        axeButton     = new GuiButton(SLOT_ID_BASE + 5, 0, 0, smallW, smallH, "");

        this.buttonList.add(helmetButton);
        this.buttonList.add(chestButton);
        this.buttonList.add(leggingsButton);
        this.buttonList.add(bootsButton);
        this.buttonList.add(swordButton);
        this.buttonList.add(axeButton);

        // no per-GUI mark buttons here; marking happens in the inventory overlay

        this.buttonList.add(new GuiButton(
                DONE_ID,
                centerX - 40,
                this.height - 30,
                80,
                20,
                "Done"
        ));

        updateSlotButtonsText();
        updateTagButtons();
    }

    private void loadActiveSetToGui() {
        // ensure slotEnchants initialized
        for (int i = 0; i < slotEnchants.length; i++) slotEnchants[i] = new ArrayList<String>();
        if (setManager == null) setManager = EnchantSetManager.getInstance();
        EnchantSet active = setManager.getActiveSet();
        if (active == null) return;
        // populate from desired
        for (int i = 0; i < SlotType.values().length; i++) {
            SlotType st = SlotType.values()[i];
            List<String> list = active.desired.get(st.name());
            if (list != null) {
                slotEnchants[i].clear();
                for (int j = 0; j < list.size(); j++) slotEnchants[i].add(list.get(j));
            }
        }
        // update label on GUI
        GuiButton labelBtn = getButtonById(SET_LABEL_ID);
        if (labelBtn != null) labelBtn.displayString = active.name;
        // reset create UI state
        creatingNew = false;
        GuiButton newBtn = getButtonById(SET_NEW_ID);
        if (newBtn != null) newBtn.displayString = "New Set";
    }

    private void saveGuiToActiveSet() {
        if (setManager == null) setManager = EnchantSetManager.getInstance();
        EnchantSet active = setManager.getActiveSet();
        if (active == null) return;
        for (int i = 0; i < SlotType.values().length; i++) {
            SlotType st = SlotType.values()[i];
            List<String> target = active.desired.get(st.name());
            if (target == null) {
                target = new ArrayList<String>();
                active.desired.put(st.name(), target);
            }
            target.clear();
            for (int j = 0; j < slotEnchants[i].size(); j++) target.add(slotEnchants[i].get(j));
        }
        active.touch();
        setManager.save();
    }

    private GuiButton getButtonById(int id) {
        for (Object o : this.buttonList) {
            if (o instanceof GuiButton) {
                GuiButton b = (GuiButton) o;
                if (b.id == id) return b;
            }
        }
        return null;
    }

    private void updateSlotButtonsText() {
        helmetButton.displayString   = "Helmet";
        chestButton.displayString    = "Chestplate";
        leggingsButton.displayString = "Leggings";
        bootsButton.displayString    = "Boots";
        swordButton.displayString    = "Sword";
        axeButton.displayString      = "Axe";
    }

    private void updateFilteredButtons() {
        // remove old enchant buttons
        this.buttonList.removeAll(enchantButtons);
        enchantButtons.clear();

        int centerX = this.width / 2;
        int leftX = centerX - 200;
        int id = 0;
        String query = this.searchField != null ? this.searchField.getText().toLowerCase() : "";

        for (int i = 0; i < ALL_ENCHANTS.length; i++) {
            String ench = ALL_ENCHANTS[i];
            if (query.isEmpty() || ench.toLowerCase().contains(query)) {
                GuiEnchantSelectButton btn = new GuiEnchantSelectButton(ENCHANT_ID_BASE + id++, leftX, 0, 120, 20, ench, i);
                this.buttonList.add(btn);
                enchantButtons.add(btn);
            }
        }

        updateTagButtons();
    }

    /**
     * Rebuild tag (label + remove) buttons for every assigned enchant under each slot.
     * Tag positions are updated in drawScreen; we only keep them in buttonList for click handling.
     */
    private void updateTagButtons() {
        // remove old tag buttons from buttonList
        this.buttonList.removeAll(tagButtons);
        tagButtons.clear();

        // For each slot, for each assigned enchant, create two buttons (label and remove)
        for (int slot = 0; slot < slotEnchants.length; slot++) {
            List<String> assigned = slotEnchants[slot];
            for (String enchName : assigned) {
                int globalIndex = getEnchantGlobalIndex(enchName);
                if (globalIndex < 0) continue;
                int baseId = TAG_ID_BASE + (slot * 1000) + (globalIndex * 2);
                // placeholder positions; will be set in drawScreen()
                GuiTagButton labelBtn = new GuiTagButton(baseId + 0, 0, 0, 100, 16, enchName, slot, enchName, false);
                GuiTagButton removeBtn = new GuiTagButton(baseId + 1, 0, 0, 16, 16, "x", slot, enchName, true);
                this.buttonList.add(labelBtn);
                this.buttonList.add(removeBtn);
                tagButtons.add(labelBtn);
                tagButtons.add(removeBtn);
            }
        }
    }

    private int getEnchantGlobalIndex(String name) {
        for (int i = 0; i < ALL_ENCHANTS.length; i++) if (ALL_ENCHANTS[i].equals(name)) return i;
        return -1;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int centerX = this.width / 2;

            // scroll enchant list (left side)
            if (mouseX < centerX - 60) {
                if (dWheel > 0) scrollOffset -= 22;
                else scrollOffset += 22;

                int listLength = enchantButtons.size() * 22;
                int viewHeight = this.height - 75;
                if (scrollOffset < 0) scrollOffset = 0;
                if (listLength > viewHeight && scrollOffset > listLength - viewHeight) scrollOffset = listLength - viewHeight;
                if (listLength <= viewHeight) scrollOffset = 0;
            } else {
                // slot horizontal scroll (if ever needed)
                if (dWheel > 0) slotScrollOffset -= 20;
                else slotScrollOffset += 20;
                int totalSlotWidth = 6 * 160;
                int slotViewX = (this.width / 2) - 50;
                int slotViewW = this.width - slotViewX - 20;
                if (slotScrollOffset < 0) slotScrollOffset = 0;
                if (totalSlotWidth > slotViewW && slotScrollOffset > totalSlotWidth - slotViewW) slotScrollOffset = totalSlotWidth - slotViewW;
                if (totalSlotWidth <= slotViewW) slotScrollOffset = 0;
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
    protected void actionPerformed(GuiButton button) throws IOException {
        // set navigation
        if (button.id == SET_PREV_ID || button.id == SET_NEXT_ID) {
            // save current GUI first
            saveGuiToActiveSet();
            List<EnchantSet> all = setManager.getAllSets();
            if (all.size() == 0) return;
            String currentId = setManager.getActiveSet() != null ? setManager.getActiveSet().id : null;
            int idx = 0;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).id.equals(currentId)) { idx = i; break; }
            }
            if (button.id == SET_PREV_ID) idx = (idx - 1 + all.size()) % all.size();
            else idx = (idx + 1) % all.size();
            setManager.setActiveSet(all.get(idx).id);
            loadActiveSetToGui();
            updateTagButtons();
            return;
        }

        if (button.id == SET_NEW_ID) {
            // toggle create-new mode: first click enters create mode (type name in search box), second click creates
            GuiButton nb = getButtonById(SET_NEW_ID);
            if (!creatingNew) {
                creatingNew = true;
                this.searchField.setText("");
                this.searchField.setFocused(true);
                if (nb != null) nb.displayString = "Create";
                return;
            } else {
                String name = this.searchField.getText();
                if (name == null || name.trim().length() == 0) name = "New Set";
                EnchantSet s = setManager.createSet(name);
                setManager.setActiveSet(s.id);
                creatingNew = false;
                if (nb != null) nb.displayString = "New Set";
                loadActiveSetToGui();
                updateTagButtons();
                return;
            }
        }

        

        if (button.id == SET_DELETE_ID) {
            EnchantSet active = setManager.getActiveSet();
            if (active == null) return;
            boolean ok = setManager.deleteSet(active.id);
            if (ok) {
                loadActiveSetToGui();
                updateTagButtons();
            }
            return;
        }

        // enchant selected -> highlight enchant and slot buttons (no auto-add)
        if (button.id >= ENCHANT_ID_BASE && button.id < ENCHANT_ID_BASE + 10000) {
            for (GuiEnchantSelectButton b : enchantButtons) {
                if (b.id == button.id) {
                    // toggle selection: clicking the same enchant again unselects it
                    if (selectedEnchant != null && selectedEnchant.equals(b.enchantName)) {
                        selectedEnchant = null;
                    } else {
                        selectedEnchant = b.enchantName;
                    }
                    return;
                }
            }
        }

        // slot clicked: add selected enchant to that slot (as its own box under the slot)
        if (button.id >= SLOT_ID_BASE && button.id <= SLOT_ID_BASE + 5) {
            if (selectedEnchant != null) {
                int idx = button.id - SLOT_ID_BASE;
                List<String> list = slotEnchants[idx];
                if (!list.contains(selectedEnchant)) {
                    list.add(selectedEnchant);
                    updateTagButtons();
                    // after placing the enchant, unselect it
                    selectedEnchant = null;
                    // autosave
                    saveGuiToActiveSet();
                }
            }
            return;
        }

        // marking is handled in inventory via key+click; no GUI mark buttons here

        // tag label / remove buttons
        if (button.id >= TAG_ID_BASE) {
            // decode: TAG_ID_BASE + (slot*1000) + (globalEnchantIndex*2) + sub
            int offset = button.id - TAG_ID_BASE;
            int slot = offset / 1000;
            int rem = offset % 1000;
            int enchPair = rem / 2;
            int sub = rem % 2; // 0 = label, 1 = remove
            // find enchant name from global index
            int globalIndex = enchPair;
            if (slot >= 0 && slot < slotEnchants.length && globalIndex >= 0 && globalIndex < ALL_ENCHANTS.length) {
                String enchName = ALL_ENCHANTS[globalIndex];
                if (sub == 1) {
                    // remove
                    slotEnchants[slot].remove(enchName);
                    updateTagButtons();
                    saveGuiToActiveSet();
                } else {
                    // label clicked: toggle selectedEnchant to this enchant (click again to unselect)
                    if (selectedEnchant != null && selectedEnchant.equals(enchName)) {
                        selectedEnchant = null;
                    } else {
                        selectedEnchant = enchName;
                    }
                }
            }
            return;
        }

        if (button.id == DONE_ID) {
            // save and close
            saveGuiToActiveSet();
            this.mc.displayGuiScreen(null);
            return;
        }

        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Enchant list layout (vertical)
        int centerX = this.width / 2;
        int leftX = centerX - 200; // keep in sync with initGui's leftX
        int listStartY = 65;
        int bottomLimit = this.height - 10;
        int viewHeight = bottomLimit - listStartY;
        int totalListHeight = enchantButtons.size() * 22;

        for (int i = 0; i < enchantButtons.size(); i++) {
            GuiEnchantSelectButton btn = enchantButtons.get(i);
            int relY = (i * 22) - scrollOffset;
            btn.yPosition = listStartY + relY;
            btn.xPosition = leftX; // align with "All Enchants" label
            btn.visible = btn.yPosition >= listStartY && btn.yPosition + 20 <= bottomLimit;
        }

        this.searchField.drawTextBox();

        // Slot buttons arranged 2-per-row (3 rows). placed to the right of the "All Enchants" label (leftX + 120 + gap)
        int slotWidth = helmetButton.width; // use actual button width (smaller)
        int slotGapX = 8;
        int slotViewX = leftX + 120 + 12; // right of the "All Enchants" box (120 width) + gap
        int leftColX = slotViewX;
        int rightColX = slotViewX + slotWidth + slotGapX;
        int slotStartY = 40; // same Y as the "All Enchants" label (which is at y=40)
        int rowGapY = 34;

        GuiButton[] slots = {helmetButton, chestButton, leggingsButton, bootsButton, swordButton, axeButton};
        for (int i = 0; i < slots.length; i++) {
            GuiButton btn = slots[i];
            int col = i % 2; // 0 = left, 1 = right
            int row = i / 2;
            btn.xPosition = (col == 0) ? leftColX : rightColX;
            btn.yPosition = slotStartY + (row * rowGapY);
            btn.visible = true;
        }

        // marking UI removed from this screen; inventory overlay shows marks instead

        // Rebuild tag buttons positions now that slot positions are known
        updateTagButtons();

        // Position tag buttons under each slot: stacked vertically per-slot
        for (GuiTagButton tb : tagButtons) {
            int slot = tb.slotIndex;
            if (slot < 0 || slot >= slots.length) {
                tb.visible = false;
                continue;
            }
            GuiButton slotBtn = slots[slot];
            List<String> assigned = slotEnchants[slot];
            int posIndex = assigned.indexOf(tb.enchantName);
            if (posIndex < 0) {
                tb.visible = false;
                continue;
            }
            int baseX = slotBtn.xPosition;
            int baseY = slotBtn.yPosition + slotBtn.height + 6;
            int fontW = this.fontRendererObj.getStringWidth(tb.enchantName);
            // cap label width to slot button width so tag stays within screen
            int labelWidth = Math.min(slotBtn.width, Math.max(40, fontW + 8));
            int gap = 4;
            int y = baseY + (posIndex * (16 + 4));
            if (!tb.isRemove) {
                tb.xPosition = baseX;
                tb.yPosition = y;
                tb.width = labelWidth;
                tb.height = 16;
                tb.visible = true;
            } else {
                int removeX = baseX + labelWidth + gap;
                tb.xPosition = removeX;
                tb.yPosition = y;
                tb.width = 16;
                tb.height = 16;
                tb.visible = true;
            }
        }

        // Draw header and default components
        this.drawCenteredString(this.fontRendererObj, "Select Desired Enchants (click enchant, then click a piece)", this.width / 2, 5, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Highlight selected enchant button and highlight all slot buttons when an enchant is selected
        if (selectedEnchant != null) {
            // highlight enchant
            for (GuiEnchantSelectButton b : enchantButtons) {
                if (selectedEnchant.equals(b.enchantName) && b.visible) {
                    drawRect(b.xPosition - 2, b.yPosition - 2, b.xPosition + b.width + 2, b.yPosition + b.height + 2, 0x80FFD700);
                }
            }
            // highlight all slot buttons to indicate they can receive the enchant
            for (GuiButton s : slots) {
                drawRect(s.xPosition - 2, s.yPosition - 2, s.xPosition + s.width + 2, s.yPosition + s.height + 2, 0x40FFD700);
            }
        }

        // marking visuals moved to inventory overlay; no GUI highlights here

        // Keep an indicator next to each slot showing how many enchants it has
        for (int i = 0; i < slots.length; i++) {
            GuiButton s = slots[i];
            String info = String.valueOf(slotEnchants[i].size()); // "0" when none
            int textW = this.fontRendererObj.getStringWidth(info);
            // place the count inside the right edge of the slot button (padding 6)
            int infoX = s.xPosition + s.width - textW - 6;
            // ensure it does not go left of the button start + small padding
            if (infoX < s.xPosition + 4) infoX = s.xPosition + 4;
            int infoY = s.yPosition + (s.height / 2) - (this.fontRendererObj.FONT_HEIGHT / 2);
            this.fontRendererObj.drawString(info, infoX, infoY, 0xFFFFFF);
        }

        // Draw scrollbars for enchant list
        if (totalListHeight > viewHeight) {
            int scrollBarX = leftX + 124;
            int scrollBarWidth = 6;
            drawRect(scrollBarX, listStartY, scrollBarX + scrollBarWidth, listStartY + viewHeight, 0x80000000);

            int thumbHeight = (int) ((float) viewHeight * viewHeight / totalListHeight);
            if (thumbHeight < 32) thumbHeight = 32;
            if (thumbHeight > viewHeight) thumbHeight = viewHeight;

            int maxScroll = totalListHeight - viewHeight;
            int thumbY = listStartY + (maxScroll == 0 ? 0 : (int) ((float) scrollOffset / maxScroll * (viewHeight - thumbHeight)));

            drawRect(scrollBarX, thumbY, scrollBarX + scrollBarWidth, thumbY + thumbHeight, 0xFFC0C0C0);
        }
    }

    private static class GuiEnchantSelectButton extends GuiButton {
        public final String enchantName;
        public final int globalIndex;
        public GuiEnchantSelectButton(int id, int x, int y, int w, int h, String enchantName, int globalIndex) {
            super(id, x, y, w, h, enchantName);
            this.enchantName = enchantName;
            this.globalIndex = globalIndex;
        }
    }

    private static class GuiTagButton extends GuiButton {
        public final int slotIndex;
        public final String enchantName;
        public final boolean isRemove;

        public GuiTagButton(int id, int x, int y, int w, int h, String display, int slotIndex, String enchantName, boolean isRemove) {
            super(id, x, y, w, h, display);
            this.slotIndex = slotIndex;
            this.enchantName = enchantName;
            this.isRemove = isRemove;
        }
    }
}
