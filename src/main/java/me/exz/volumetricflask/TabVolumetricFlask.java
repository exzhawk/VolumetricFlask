package me.exz.volumetricflask;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import static me.exz.volumetricflask.common.items.ItemVolumetricFlask.VOLUMETRIC_FLASK_144;

public class TabVolumetricFlask extends CreativeTabs {
    public static final TabVolumetricFlask TAB_VOLUMETRIC_FLASK = new TabVolumetricFlask();

    private TabVolumetricFlask() {
        super("volumetricflask");
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(VOLUMETRIC_FLASK_144);
    }
}
