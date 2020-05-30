package me.exz.volumetricflask;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import static me.exz.volumetricflask.Items.VOLUMETRIC_FLASK;

public class TabVolumetricFlask extends CreativeTabs {
    public static final TabVolumetricFlask TAB_VOLUMETRIC_FLASK = new TabVolumetricFlask();

    private TabVolumetricFlask() {
        super("volumetricflask");
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(VOLUMETRIC_FLASK); // TODO: 2020/5/30 get a real one with capacity
    }
}
