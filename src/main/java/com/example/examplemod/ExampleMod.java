package com.example.examplemod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;


import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION)
public class ExampleMod
{
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";
    public static KeyBinding openGuiKey;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {

        // Create keybinding: key G, category "Example Mod"
        openGuiKey = new KeyBinding("Open Enchant GUI", Keyboard.KEY_G, "Example Mod");
        ClientRegistry.registerKeyBinding(openGuiKey);

        // Register key handler
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());

		// some example code
        MinecraftForge.EVENT_BUS.register(new ArmorEnchantLogger());
        // initialize inventory click logger (registers its keybinding and handler)
        com.example.examplemod.InventoryClickLogger.init();

        // register client-only GUI listener and overlay
        MinecraftForge.EVENT_BUS.register(new com.example.examplemod.InventoryMarkOverlay());
    }
}
