package me.exz.volumetricflask.common.items;

import me.exz.volumetricflask.TabVolumetricFlask;
import me.exz.volumetricflask.VolumetricFlask;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemVolumetricFlask extends Item {
    public static final int[] CAPACITIES = new int[]{100,200};

    public ItemVolumetricFlask() {
        this.setUnlocalizedName(VolumetricFlask.MODID + ".volumetric_flask");
        this.setRegistryName("volumetric_flask");
        this.setMaxStackSize(64);
        this.setCreativeTab(TabVolumetricFlask.TAB_VOLUMETRIC_FLASK);
        setHasSubtypes(true);
        setMaxDamage(0);
        setNoRepair();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
//        for (int capacity: CAPACITIES){
//            final ItemStack stack = new ItemStack(this,1,capacity);
//            stack.setTagCompound(new NBTTagCompound());
//            items.add(stack);
//        }
        items.addAll(getAllVolumetricFlasks());
    }

    public List<ItemStack> getAllVolumetricFlasks(){
        // TODO: 2020/5/30 invoke once and save all flasks for later usage
        List<ItemStack> allVolumetricFlasks=new ArrayList<>();
        for (int capacity : CAPACITIES) {
            final ItemStack stack = new ItemStack(this,1,capacity);
            stack.setTagCompound(new NBTTagCompound());
            allVolumetricFlasks.add(stack);
        }
        return allVolumetricFlasks;
    }


    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStackSimple(stack, stack.getMetadata());// TODO: 2020/5/30 get meta as capacity from itemstack
    }


    @SuppressWarnings("NullableProblems")
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
            return String.format("%dmB Volumetric Flask (%s)",stack.getMetadata(), fluidName);// TODO: 2020/5/30 get meta ass capacity from itemstack
        } else {
            return "Volumetric Flask";
        }
    }
}
