package me.exz.volumetricflask;

import appeng.api.AEApi;
import me.exz.volumetricflask.common.block.BlockBuffer;
import me.exz.volumetricflask.common.block.BlockFiller;
import me.exz.volumetricflask.common.block.BlockOInterface;
import me.exz.volumetricflask.common.items.ItemPartOInterface;
import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
import me.exz.volumetricflask.common.tile.TileBuffer;
import me.exz.volumetricflask.common.tile.TileFiller;
import me.exz.volumetricflask.common.tile.TileOInterface;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.List;

import static me.exz.volumetricflask.VolumetricFlask.MODID;
import static me.exz.volumetricflask.common.parts.PartOInterface.*;

@Mod.EventBusSubscriber(modid = MODID)
public class Items {
    public static final List<ItemVolumetricFlask> VOLUMETRIC_FLASKS = Arrays.asList(
            ItemVolumetricFlask.VOLUMETRIC_FLASK_16,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_32,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_18,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_36,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_72,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_144,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_100,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_1000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_50,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_250,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_2000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_4000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_8000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_16000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_32000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_64000,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_33,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_133,
            ItemVolumetricFlask.VOLUMETRIC_FLASK_300
    );
    public static final BlockOInterface BLOCK_O_INTERFACE = new BlockOInterface();
    public static final ItemBlock ITEM_BLOCK_O_INTERFACE = new ItemBlock(BLOCK_O_INTERFACE);
    public static final BlockBuffer BLOCK_BUFFER = new BlockBuffer();
    public static final ItemBlock ITEM_BLOCK_BUFFER = new ItemBlock(BLOCK_BUFFER);
    public static final BlockFiller BLOCK_FILLER = new BlockFiller();
    public static final ItemBlock ITEM_BLOCK_FILLER = new ItemBlock(BLOCK_FILLER);

    public static final ItemPartOInterface ITEM_PART_O_INTERFACE = new ItemPartOInterface();

    public Items() {
        BLOCK_O_INTERFACE.setTileEntity(TileOInterface.class);
    }

    @SubscribeEvent
    public static void onRegistry(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (ItemVolumetricFlask volumetricFlask : VOLUMETRIC_FLASKS) {
            registry.register(volumetricFlask);
        }
        registry.register(ITEM_PART_O_INTERFACE);
        ITEM_BLOCK_O_INTERFACE.setRegistryName(ITEM_BLOCK_O_INTERFACE.getBlock().getRegistryName());
        registry.register(ITEM_BLOCK_O_INTERFACE);
        ITEM_BLOCK_BUFFER.setRegistryName(ITEM_BLOCK_BUFFER.getBlock().getRegistryName());
        registry.register(ITEM_BLOCK_BUFFER);
        ITEM_BLOCK_FILLER.setRegistryName(ITEM_BLOCK_FILLER.getBlock().getRegistryName());
        registry.register(ITEM_BLOCK_FILLER);
    }

    @SubscribeEvent
    public static void onBlockRegistry(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        GameRegistry.registerTileEntity(TileOInterface.class, new ResourceLocation(MODID, "o_interface"));
        GameRegistry.registerTileEntity(TileBuffer.class, new ResourceLocation(MODID, "buffer"));
        GameRegistry.registerTileEntity(TileFiller.class, new ResourceLocation(MODID, "filler"));
        BLOCK_O_INTERFACE.setTileEntity(TileOInterface.class);
        registry.register(BLOCK_O_INTERFACE);
        registry.register(BLOCK_BUFFER);
        registry.register(BLOCK_FILLER);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (ItemVolumetricFlask volumetricFlask : VOLUMETRIC_FLASKS) {
            ModelLoader.setCustomModelResourceLocation(volumetricFlask, 0, new ModelResourceLocation(MODID + ":volumetric_flask", "inventory"));
        }
        ModelLoader.setCustomModelResourceLocation(ITEM_BLOCK_O_INTERFACE, 0, new ModelResourceLocation(ITEM_BLOCK_O_INTERFACE.getRegistryName(), "facing=down,omnidirectional=true"));
        ModelLoader.setCustomModelResourceLocation(ITEM_BLOCK_BUFFER, 0, new ModelResourceLocation(ITEM_BLOCK_BUFFER.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_BLOCK_FILLER, 0, new ModelResourceLocation(ITEM_BLOCK_FILLER.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_PART_O_INTERFACE, 0, new ModelResourceLocation(ITEM_PART_O_INTERFACE.getRegistryName().toString()));
        AEApi.instance().registries().partModels().registerModels(MODEL_BASE, MODEL_ON, MODEL_OFF, MODEL_HAS_CHANNEL);
    }
}
