package me.exz.volumetricflask;

import me.exz.volumetricflask.common.block.BlockOInterface;
import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
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

import static me.exz.volumetricflask.VolumetricFlask.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class Items {
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK = new ItemVolumetricFlask();
    public static final BlockOInterface BLOCK_O_INTERFACE = new BlockOInterface();
    public static final ItemBlock ITEM_BLOCK_O_INTERFACE = new ItemBlock(BLOCK_O_INTERFACE);

    public Items(){
        BLOCK_O_INTERFACE.setTileEntity(TileOInterface.class);
    }
    @SubscribeEvent
    public static void onRegistry(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(VOLUMETRIC_FLASK);
        ITEM_BLOCK_O_INTERFACE.setRegistryName(ITEM_BLOCK_O_INTERFACE.getBlock().getRegistryName());
        registry.register(ITEM_BLOCK_O_INTERFACE);
    }

    @SubscribeEvent
    public static void onBlockRegistry(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        GameRegistry.registerTileEntity(TileOInterface.class, new ResourceLocation(MODID, "o_interface"));
        BLOCK_O_INTERFACE.setTileEntity(TileOInterface.class);
        registry.register(BLOCK_O_INTERFACE);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(VOLUMETRIC_FLASK, 0, new ModelResourceLocation(VOLUMETRIC_FLASK.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_BLOCK_O_INTERFACE,0,new ModelResourceLocation(ITEM_BLOCK_O_INTERFACE.getRegistryName(),"omnidirectional=true"));
    }
}
