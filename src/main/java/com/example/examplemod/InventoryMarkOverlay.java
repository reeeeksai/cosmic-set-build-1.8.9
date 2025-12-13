package com.example.examplemod;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InventoryMarkOverlay {

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen gui = event.gui;
        MarkedSlots marks = MarkedSlots.getInstance();
        for (int i = 0; i < 6; i++) {
            if (!marks.isMarked(i)) continue;
            if (!marks.hasLastRect(i)) continue;
            int x = marks.getLastX(i);
            int y = marks.getLastY(i);
            int w = marks.getLastW(i);
            int h = marks.getLastH(i);
            // draw translucent green rectangle
            Gui.drawRect(x - 2, y - 2, x + w + 2, y + h + 2, 0x8040FF40);
        }
    }
}
