package me.exz.volumetricflask.common.items;

import me.exz.volumetricflask.TabVolumetricFlask;
import me.exz.volumetricflask.VolumetricFlask;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.List;

public class ItemVolumetricFlask extends Item {
    public ItemVolumetricFlask() {
        this.setUnlocalizedName(VolumetricFlask.MODID + ".volumetric_flask");
        this.setRegistryName("volumetric_flask");
        this.setMaxStackSize(64);
        this.setCreativeTab(TabVolumetricFlask.TAB_VOLUMETRIC_FLASK);
        this.setMaxDamage(65536);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
//        System.out.println("init caps");
        return new FluidCapabilityProvider(stack);
    }


    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidCapabilityProvider fluidCapabilityProvider = (FluidCapabilityProvider) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidCapabilityProvider != null) {
            int capacity;
            String fluidName;
            FluidStack fluidStack = fluidCapabilityProvider.getFluid();
            if (fluidStack != null) {
                fluidName = fluidStack.getLocalizedName();
            } else {
                fluidName = "Empty";
            }
            IFluidTankProperties[] fluidTankProperties = fluidCapabilityProvider.getTankProperties();
            capacity = fluidCapabilityProvider.getCapacity();
            return String.format("%dmB Volumetric Flask (%s)", capacity, fluidName);
        } else {
            return "Volumetric Flask";
        }
    }
}
