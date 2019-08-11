package me.exz.volumetricflask;

import me.exz.volumetricflask.proxy.CommonProxy;
import me.exz.volumetricflask.server.Command;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = VolumetricFlask.MODID, name = VolumetricFlask.NAME, version = VolumetricFlask.VERSION, dependencies = "required-after:appliedenergistics2")
public class VolumetricFlask {
    public static final String MODID = "volumetricflask";
    public static final String NAME = "Volumetric Flask";
    public static final String VERSION = "@VERSION@";
    @Mod.Instance
    public static VolumetricFlask instance;

    @SidedProxy(clientSide = "me.exz.volumetricflask.proxy.ClientProxy", serverSide = "me.exz.volumetricflask.proxy.CommonProxy")
    private static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new Command());
    }
}
