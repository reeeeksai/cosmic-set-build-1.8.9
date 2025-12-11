package com.example.examplemod; // <-- same package

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ExampleMod.openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiDesiredEnchants());
        }
    }
}
