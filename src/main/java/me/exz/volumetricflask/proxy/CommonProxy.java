package me.exz.volumetricflask.proxy;

import me.exz.volumetricflask.Items;
import me.exz.volumetricflask.common.tile.TileOInterface;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static me.exz.volumetricflask.Items.BLOCK_O_INTERFACE;
import static me.exz.volumetricflask.VolumetricFlask.MODID;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new Items());
    }
}
