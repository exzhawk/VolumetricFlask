package me.exz.volumetricflask.common.block;

import me.exz.volumetricflask.common.tile.TileBuffer;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static me.exz.volumetricflask.TabVolumetricFlask.TAB_VOLUMETRIC_FLASK;
import static me.exz.volumetricflask.VolumetricFlask.MODID;


public class BlockBuffer extends Block {
    public BlockBuffer() {
        super(Material.ROCK);
        this.setUnlocalizedName(MODID+ ".buffer");
        this.setRegistryName("buffer");
        this.setCreativeTab(TAB_VOLUMETRIC_FLASK);
        this.setSoundType(SoundType.STONE);
        this.setHardness(2.5F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileBuffer();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

}
