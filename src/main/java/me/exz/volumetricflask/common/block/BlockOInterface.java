package me.exz.volumetricflask.common.block;

import appeng.block.misc.BlockInterface;

import static me.exz.volumetricflask.TabVolumetricFlask.TAB_VOLUMETRIC_FLASK;
import static me.exz.volumetricflask.VolumetricFlask.MODID;

public class BlockOInterface extends BlockInterface {
    public BlockOInterface() {
        super();
        this.setUnlocalizedName(MODID + ".o_interface");
        this.setRegistryName("o_interface");
        this.setCreativeTab(TAB_VOLUMETRIC_FLASK);
    }

}
