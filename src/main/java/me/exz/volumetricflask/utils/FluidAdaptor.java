package me.exz.volumetricflask.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class FluidAdaptor {
    private final IFluidHandler fluidHandler;

    public static FluidAdaptor getAdaptor(final TileEntity te, final EnumFacing d) {
        if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d)) {
            IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d);
            if (fluidHandler != null) {
                return new FluidAdaptor(fluidHandler);
            }
        }
        return null;
    }

    private FluidAdaptor(IFluidHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    public boolean isEmpty() {
        IFluidTankProperties[] fluidTankProperties = fluidHandler.getTankProperties();
        for (IFluidTankProperties fluidTankProperty : fluidTankProperties) {
            if (fluidTankProperty.getContents() != null && fluidTankProperty.getContents().amount != 0) {
                return false;
            }
        }
        return true;
    }

    public ItemStack addFlask(final ItemStack toBeAdded) {
        return this.addFlask(toBeAdded, false);
    }

    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        return this.addFlask(toBeSimulated, true);
    }

    private ItemStack addFlask(final ItemStack itemsToAdd, final boolean simulate) {
        if (itemsToAdd.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack left = itemsToAdd.copy();
        FluidHandlerItemStackSimple fluidHandlerItemStackSimple = (FluidHandlerItemStackSimple) itemsToAdd.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItemStackSimple == null) {
            return left;
        }
        FluidStack fluid = fluidHandlerItemStackSimple.getFluid();
        if (fluid == null || fluid.amount == 0) {
            return left;
        }
        //determine how many flask can be inserted
        int filledCount = 0;
        for (int i = itemsToAdd.getCount(); i > 0; i--) {
            int tryFilledAmount = fluid.amount * i;
            if (fluidHandler.fill(new FluidStack(fluid, tryFilledAmount), false) == tryFilledAmount) {
                filledCount = i;
                break;
            }
        }
        if (!simulate) {
            fluidHandler.fill(new FluidStack(fluid, fluid.amount * filledCount), true);
        }
        left.setCount(itemsToAdd.getCount() - filledCount);
        return left;
    }

}
