package me.exz.volumetricflask.common.items;

import me.exz.volumetricflask.TabVolumetricFlask;
import me.exz.volumetricflask.VolumetricFlask;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import javax.annotation.Nullable;

public class ItemVolumetricFlask extends Item {
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_16 = new ItemVolumetricFlask(16);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_32 = new ItemVolumetricFlask(32);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_18 = new ItemVolumetricFlask(18);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_36 = new ItemVolumetricFlask(36);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_72 = new ItemVolumetricFlask(72);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_144 = new ItemVolumetricFlask(144);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_100 = new ItemVolumetricFlask(100);
    public static final ItemVolumetricFlask VOLUMETRIC_FLASK_1000 = new ItemVolumetricFlask(1000);

    public int capacity;

    public ItemVolumetricFlask(int capacity) {
        this.setUnlocalizedName(VolumetricFlask.MODID + ".volumetric_flask");
        this.setRegistryName("volumetric_flask_" + capacity);
        this.setMaxStackSize(64);
        this.setCreativeTab(TabVolumetricFlask.TAB_VOLUMETRIC_FLASK);
        this.capacity = capacity;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStackSimple(stack, capacity);
    }


    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidHandlerItemStackSimple fluidHandlerItemStackSimple = (FluidHandlerItemStackSimple) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItemStackSimple != null) {
            String fluidName;
            FluidStack fluidStack = fluidHandlerItemStackSimple.getFluid();
            if (fluidStack != null) {
                fluidName = fluidStack.getLocalizedName();
            } else {
                fluidName = "Empty";
            }
            IFluidTankProperties[] fluidTankProperties = fluidHandlerItemStackSimple.getTankProperties();
            return String.format("%dmB Volumetric Flask (%s)", capacity, fluidName);
        } else {
            return "Volumetric Flask";
        }
    }
}
