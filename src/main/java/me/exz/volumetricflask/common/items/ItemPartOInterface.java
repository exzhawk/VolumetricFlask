package me.exz.volumetricflask.common.items;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import me.exz.volumetricflask.TabVolumetricFlask;
import me.exz.volumetricflask.VolumetricFlask;
import me.exz.volumetricflask.common.parts.PartOInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemPartOInterface extends Item implements IPartItem {

    public ItemPartOInterface() {
        this.setUnlocalizedName(VolumetricFlask.MODID + ".part_o_interface");
        this.setRegistryName("part_o_interface");
        this.setMaxStackSize(64);
        this.setCreativeTab(TabVolumetricFlask.TAB_VOLUMETRIC_FLASK);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        return new PartOInterface(is);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }

}
