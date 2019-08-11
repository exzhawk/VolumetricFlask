package me.exz.volumetricflask.common.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidCapabilityProviderTank implements IFluidTank, IFluidHandlerItem, ICapabilityProvider {
    @Nullable
    protected FluidStack fluid;

    @Nonnull
    protected ItemStack container;
    protected int capacity;

    public FluidCapabilityProviderTank(@Nonnull ItemStack container) {
        this.container = container;
        this.capacity = 144;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY ? (T) this : null;
    }

    @Nullable
    public FluidStack getFluid() {
        return fluid;
    }
    public void setFluid(@Nullable FluidStack fluid)
    {
        this.fluid = fluid;
    }
    @Override
    public int getFluidAmount() {
        if (fluid == null) {
            return 0;
        }
        return fluid.amount;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{new FluidTankProperties(fluid,capacity)};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (container.getCount() != 1 || resource == null || resource.amount <= 0 )
        {
            return 0;
        }

        FluidStack contained = getFluid();
        if (contained == null)
        {
            int fillAmount = Math.min(capacity, resource.amount);
            if (fillAmount == capacity) {
                if (doFill) {
                    FluidStack filled = resource.copy();
                    filled.amount = fillAmount;
                    setFluid(filled);
                }

                return fillAmount;
            }
        }

        return 0;    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (container.getCount() != 1 || resource == null || resource.amount <= 0 || !resource.isFluidEqual(getFluid()))
        {
            return null;
        }
        return drain(resource.amount, doDrain);    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (container.getCount() != 1 || maxDrain <= 0)
        {
            return null;
        }

        FluidStack contained = getFluid();
        if (contained == null || contained.amount <= 0 )
        {
            return null;
        }

        final int drainAmount = Math.min(contained.amount, maxDrain);
        if (drainAmount == capacity) {
            FluidStack drained = contained.copy();
            return drained;
        }

        return null;    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return container;
    }
    public FluidCapabilityProviderTank readFromNBT(NBTTagCompound nbt)
    {
        if (!nbt.hasKey("Empty"))
        {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
            setFluid(fluid);
        }
        else
        {
            setFluid(null);
        }
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (fluid != null)
        {
            fluid.writeToNBT(nbt);
        }
        else
        {
            nbt.setString("Empty", "");
        }
        return nbt;
    }
}
