package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiDesiredEnchants extends GuiScreen {

        // dynamic list of enchant definitions from EnchantRegistry
        private java.util.List<EnchantDef> allEnchantDefs = new java.util.ArrayList<EnchantDef>();

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

    private GuiEnchantPanel enchantPanel;
    private final List<GuiTagButton> tagButtons = new ArrayList<GuiTagButton>();
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
        tagButtons.clear();
        selectedEnchant = null;

        for (int i = 0; i < slotEnchants.length; i++) slotEnchants[i] = new ArrayList<String>();

        int centerX = this.width / 2;
        int leftX = centerX - 200;

        // create reusable enchant panel (search + list + scrollbar)
        enchantPanel = new GuiEnchantPanel(this);
        int listStartY = 40; // align with set selector row
        int viewHeight = Math.max(40, this.height - 10 - listStartY);

        // Set manager and set selector buttons — compute to ensure Delete fits on screen
        setManager = EnchantSetManager.getInstance();
        int setPrevX = leftX + 130;
        int setLabelX = setPrevX + 20;
        int setLabelW = 120; // initial desired width for the label
        int setNextW = 18;
        int setNewW = 60;
        int setDeleteW = 60;

        // compute available space and shrink label if needed so Delete doesn't overflow
        int spacing1 = 4; // between label and next
        int spacing2 = 8; // between next and new
        int spacing3 = 8; // between new and delete
        int totalNeeded = 18 + (setLabelW) + spacing1 + setNextW + spacing2 + setNewW + spacing3 + setDeleteW;
        int startX = setPrevX;
        int maxRight = this.width - 10;
        if (startX + totalNeeded > maxRight) {
            int overflow = (startX + totalNeeded) - maxRight;
            setLabelW = Math.max(40, setLabelW - overflow);
            totalNeeded = 18 + (setLabelW) + spacing1 + setNextW + spacing2 + setNewW + spacing3 + setDeleteW;
        }

        int setNextX = setLabelX + setLabelW + spacing1;
        int setNewX = setNextX + setNextW + spacing2;
        int setDeleteX = setNewX + setNewW + spacing3;

        this.buttonList.add(new GuiButton(SET_PREV_ID, setPrevX, 15, 18, 20, "<"));
        this.buttonList.add(new GuiButton(SET_LABEL_ID, setLabelX, 15, setLabelW, 20, ""));
        this.buttonList.add(new GuiButton(SET_NEXT_ID, setNextX, 15, setNextW, 20, ">"));
        // place New and Delete on the first row next to the set selector
        this.buttonList.add(new GuiButton(SET_NEW_ID, setNewX, 15, setNewW, 20, "New Set"));
        this.buttonList.add(new GuiButton(SET_DELETE_ID, setDeleteX, 15, setDeleteW, 20, "Delete"));

        // populate GUI from active set
        loadActiveSetToGui();

        // load all enchant defs from the registry and sort by name
        allEnchantDefs.clear();
        for (EnchantDef d : EnchantRegistry.all()) allEnchantDefs.add(d);
        java.util.Collections.sort(allEnchantDefs, new java.util.Comparator<EnchantDef>() {
            @Override public int compare(EnchantDef a, EnchantDef b) { return a.name.compareToIgnoreCase(b.name); }
        });

        // build colored display names and initialize panel now that we have enchant defs
        List<String> displayNames = new ArrayList<String>();
        for (EnchantDef d : allEnchantDefs) displayNames.add(d != null ? EnchantRenderUtils.rarityColorCode(d) + d.name : "");
        if (enchantPanel != null) enchantPanel.init(leftX, listStartY, viewHeight, allEnchantDefs, displayNames, this.fontRendererObj, this.buttonList);

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
                // sort loaded enchants by rarity priority so display/remove order is consistent
                java.util.Collections.sort(slotEnchants[i], new java.util.Comparator<String>() {
                    @Override public int compare(String a, String b) {
                        int ai = getEnchantGlobalIndex(a);
                        int bi = getEnchantGlobalIndex(b);
                        EnchantDef ad = ai >= 0 ? allEnchantDefs.get(ai) : null;
                        EnchantDef bd = bi >= 0 ? allEnchantDefs.get(bi) : null;
                        int pa = ad != null ? rarityPriority(ad.rarity) : Integer.MAX_VALUE;
                        int pb = bd != null ? rarityPriority(bd.rarity) : Integer.MAX_VALUE;
                        if (pa != pb) return Integer.compare(pa, pb);
                        if (ad != null && bd != null) return ad.name.compareToIgnoreCase(bd.name);
                        return a.compareToIgnoreCase(b);
                    }
                });
            }
        }
        // update label on GUI (truncate to fit the button width)
        GuiButton labelBtn = getButtonById(SET_LABEL_ID);
        if (labelBtn != null) {
            int avail = Math.max(4, labelBtn.width - 8);
            String disp = this.fontRendererObj.trimStringToWidth(active.name, avail);
            labelBtn.displayString = disp;
        }
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
        // delegate to reusable panel
        if (enchantPanel != null) {
            enchantPanel.updateFilteredButtons();
        }
    }

    /**
     * Rebuild tag (label + remove) buttons for every assigned enchant under each slot.
     * Tag positions are updated in drawScreen; we only keep them in buttonList for click handling.
     */
    public void updateTagButtons() {
        // Remove any existing tag buttons and keep list empty.
        this.buttonList.removeAll(tagButtons);
        tagButtons.clear();
    }

    private int getEnchantGlobalIndex(String name) {
        for (int i = 0; i < allEnchantDefs.size(); i++) if (allEnchantDefs.get(i).name.equals(name)) return i;
        return -1;
    }

    private int rarityPriority(Rarity r) {
        if (r == null) return Integer.MAX_VALUE;
        switch (r) {
            case MASTERY: return 0;
            case HEROIC:  return 1;
            case SOUL:    return 2;
            case LEGENDARY: return 3;
            case ULTIMATE: return 4;
            case ELITE:   return 5;
            case UNIQUE:  return 6;
            case COMMON:  return 7;
            default: return 8;
        }
    }

    // Accessors for other GUIs
    public List<EnchantDef> getAllEnchantDefs() {
        return allEnchantDefs;
    }

    public String getColoredName(EnchantDef def) {
        return EnchantRenderUtils.rarityColorCode(def) + (def != null ? def.name : "");
    }

    public List<String> getSlotEnchants(int idx) {
        return slotEnchants[idx];
    }

    public boolean addEnchantToSlot(int idx, String enchName) {
        if (enchName == null) return false;
        List<String> lst = slotEnchants[idx];
        // detect build-from conflicts: replace base enchants if the new enchant is an upgrade
        int newGi = getEnchantGlobalIndex(enchName);
        EnchantDef newDef = newGi >= 0 ? allEnchantDefs.get(newGi) : null;
        List<String> warnings = new ArrayList<String>();
        List<String> replaced = new ArrayList<String>();
        if (newDef != null) {
            String newId = newDef.id;
            String newBuilds = newDef.buildsFromId;
            // If new builds from an existing enchant on the piece, remove that existing enchant (auto-upgrade)
            if (newBuilds != null) {
                // iterate over a copy to avoid concurrent modification
                for (String existingName : new ArrayList<String>(lst)) {
                    int exGi = getEnchantGlobalIndex(existingName);
                    EnchantDef exDef = exGi >= 0 ? allEnchantDefs.get(exGi) : null;
                    if (exDef == null) continue;
                    if (exDef.id.equals(newBuilds)) {
                        lst.remove(existingName);
                        replaced.add(exDef.name);
                    }
                }
            }
            // If existing enchants build from the new one (i.e. an upgraded enchant is present),
            // remove that upgraded enchant as well (allow replacement both ways).
            for (String existingName : new ArrayList<String>(lst)) {
                int exGi = getEnchantGlobalIndex(existingName);
                EnchantDef exDef = exGi >= 0 ? allEnchantDefs.get(exGi) : null;
                if (exDef == null) continue;
                if (exDef.buildsFromId != null && exDef.buildsFromId.equals(newId)) {
                    lst.remove(existingName);
                    replaced.add(exDef.name);
                }
            }
        }

        if (!lst.contains(enchName)) {
            // enforce maximum enchants per slot (16). Note: replacements above may have freed slots.
            if (lst.size() >= 16) {
                return false;
            }
            // insert new enchant into list according to rarity priority (so display is ordered)
            int insertAt = lst.size();
            if (newDef != null) {
                int newP = rarityPriority(newDef.rarity);
                for (int i = 0; i < lst.size(); i++) {
                    int exGi = getEnchantGlobalIndex(lst.get(i));
                    EnchantDef exDef = exGi >= 0 ? allEnchantDefs.get(exGi) : null;
                    int exP = exDef != null ? rarityPriority(exDef.rarity) : Integer.MAX_VALUE;
                    if (exP > newP) { insertAt = i; break; }
                    if (exP == newP && exDef != null && newDef != null) {
                        if (exDef.name.compareToIgnoreCase(newDef.name) > 0) { insertAt = i; break; }
                    }
                }
            }
            lst.add(insertAt, enchName);
            updateTagButtons();
            saveGuiToActiveSet();
            // show replacements and warnings to player if any
            // previously reported replacements/warnings to chat; now suppressed
            return true;
        }
        return false;
    }

    public void removeEnchantFromSlot(int idx, String enchName) {
        if (enchName == null) return;
        List<String> lst = slotEnchants[idx];
        if (lst.remove(enchName)) {
            updateTagButtons();
            saveGuiToActiveSet();
        }
    }

    public boolean enchantAppliesToName(String name, SlotType st) {
        int gi = getEnchantGlobalIndex(name);
        if (gi < 0) return false;
        return enchantAppliesTo(allEnchantDefs.get(gi), st);
    }

    private boolean enchantAppliesTo(EnchantDef def, SlotType st) {
        if (def == null || def.appliesTo == null) return false;
        switch (st) {
            case HELMET:
                return def.appliesTo.contains(SlotGroup.HELMET) || def.appliesTo.contains(SlotGroup.ARMOR);
            case CHESTPLATE:
                return def.appliesTo.contains(SlotGroup.CHESTPLATE) || def.appliesTo.contains(SlotGroup.ARMOR);
            case LEGGINGS:
                return def.appliesTo.contains(SlotGroup.LEGGINGS) || def.appliesTo.contains(SlotGroup.ARMOR);
            case BOOTS:
                return def.appliesTo.contains(SlotGroup.BOOTS) || def.appliesTo.contains(SlotGroup.ARMOR);
            case SWORD:
                return def.appliesTo.contains(SlotGroup.SWORD) || def.appliesTo.contains(SlotGroup.WEAPON);
            case AXE:
                return def.appliesTo.contains(SlotGroup.AXE) || def.appliesTo.contains(SlotGroup.WEAPON);
            default:
                return false;
        }
    }

    // Rarity color codes moved to EnchantRenderUtils

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (enchantPanel != null) enchantPanel.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (enchantPanel != null && enchantPanel.textboxKeyTyped(typedChar, keyCode)) {
            // handled by panel
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (enchantPanel != null) enchantPanel.mouseClicked(mouseX, mouseY, mouseButton);
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
                if (enchantPanel != null) { enchantPanel.setText("New Set"); enchantPanel.setFocused(true); enchantPanel.setFilteringEnabled(false); }
                if (nb != null) nb.displayString = "Create";
                return;
            } else {
                String name = enchantPanel != null ? enchantPanel.getText() : null;
                if (name == null || name.trim().length() == 0) name = "New Set";
                EnchantSet s = setManager.createSet(name);
                setManager.setActiveSet(s.id);
                creatingNew = false;
                if (enchantPanel != null) enchantPanel.setText("");
                if (enchantPanel != null) enchantPanel.setFilteringEnabled(true);
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

        // enchant selected via panel -> highlight enchant and slot buttons
        if (enchantPanel != null) {
            String clicked = enchantPanel.onButtonPressed(button);
            if (clicked != null) {
                if (selectedEnchant != null && selectedEnchant.equals(clicked)) selectedEnchant = null;
                else selectedEnchant = clicked;
                return;
            }
        }

        // slot clicked: if an enchant is selected, add it to the clicked slot; otherwise open per-piece GUI
        if (button.id >= SLOT_ID_BASE && button.id <= SLOT_ID_BASE + 5) {
            int idx = button.id - SLOT_ID_BASE;
            if (selectedEnchant != null) {
                // only add if the selected enchant applies to this slot type
                SlotType st = SlotType.values()[idx];
                if (enchantAppliesToName(selectedEnchant, st)) {
                    boolean added = addEnchantToSlot(idx, selectedEnchant);
                    if (added) selectedEnchant = null;
                    updateTagButtons();
                } else {
                    // do nothing if enchant doesn't apply
                }
            } else {
                this.mc.displayGuiScreen(new GuiPieceEnchants(this, idx));
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
            if (slot >= 0 && slot < slotEnchants.length && globalIndex >= 0 && globalIndex < allEnchantDefs.size()) {
                String enchName = allEnchantDefs.get(globalIndex).name;
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

        // Enchant panel (search + list + scrollbar)
        int centerX = this.width / 2;
        int leftX = centerX - 200; // keep in sync with initGui's leftX
        int listStartY = 40;
        int bottomLimit = this.height - 10;
        int viewHeight = bottomLimit - listStartY;
        if (enchantPanel != null) enchantPanel.drawPanel(mouseX, mouseY, partialTicks);

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

        // Per-piece enchant lists are not shown on this screen anymore.
        // tagButtons are kept empty so no labels are rendered here.

        // Draw header and default components
        this.drawCenteredString(this.fontRendererObj, "Select Desired Enchants (click enchant, then click a piece)", this.width / 2, 5, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // draw scaled enchant labels from panel on top of default button rendering
        if (enchantPanel != null) enchantPanel.drawScaledLabels(mouseX, mouseY, partialTicks);

        // Highlight selected enchant button and highlight all slot buttons when an enchant is selected
        if (selectedEnchant != null) {
            // highlight only slot buttons that can receive the selected enchant
            for (int i = 0; i < slots.length; i++) {
                GuiButton s = slots[i];
                SlotType st = SlotType.values()[i];
                if (enchantAppliesToName(selectedEnchant, st) && !slotEnchants[i].contains(selectedEnchant)) {
                    drawRect(s.xPosition - 2, s.yPosition - 2, s.xPosition + s.width + 2, s.yPosition + s.height + 2, 0x40FFD700);
                }
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

        // enchant panel handles its own scrollbar drawing

        // enchant list rendering handled by GuiEnchantPanel
    }

    private static class GuiEnchantSelectButton extends GuiButton {
        public final String enchantName;
        public final int globalIndex;
        public GuiEnchantSelectButton(int id, int x, int y, int w, int h, String display, String enchantName, int globalIndex) {
            super(id, x, y, w, h, display);
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
