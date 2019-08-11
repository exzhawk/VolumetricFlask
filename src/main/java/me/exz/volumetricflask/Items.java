package me.exz.volumetricflask;

import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.List;

import static me.exz.volumetricflask.VolumetricFlask.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class Items {
    private static final List<ItemVolumetricFlask> VOLUMETRIC_FLASKS = Arrays.asList(
            ItemVolumetricFlask.VOLUMETRIC_FLASK_16,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_32,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_18,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_36,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_72,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_144,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_100,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_1000);

    public Items() {

    }

    @SubscribeEvent
    public static void onRegistry(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (ItemVolumetricFlask volumetricFlask : VOLUMETRIC_FLASKS) {
            registry.register(volumetricFlask);
        }
    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (ItemVolumetricFlask volumetricFlask : VOLUMETRIC_FLASKS) {
            ModelLoader.setCustomModelResourceLocation(volumetricFlask, 0, new ModelResourceLocation(MODID + ":volumetric_flask", "inventory"));
        }
    }
}
