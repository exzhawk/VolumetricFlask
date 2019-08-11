package me.exz.volumetricflask.common.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import javax.annotation.Nonnull;

public class FluidCapabilityProvider extends FluidHandlerItemStackSimple {
    public int capacity = 144;
    public static final String CAPACITY_NBT_KEY = "Capacity";

    public FluidCapabilityProvider(@Nonnull ItemStack container) {
        super(container, 144);
        setCapacity(144);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (!container.hasTagCompound()) {
            container.setTagCompound(new NBTTagCompound());
        }
        container.getTagCompound().setInteger(CAPACITY_NBT_KEY, capacity);
    }

    public int getCapacity() {
        if (!container.hasTagCompound()) {
            this.setCapacity(144);
        }
        return container.getTagCompound().getInteger(CAPACITY_NBT_KEY);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        this.capacity = getCapacity();
        if (container.getCount() != 1 || resource == null || resource.amount <= 0 || !canFillFluidType(resource)) {
            return 0;
        }

        FluidStack contained = getFluid();
        if (contained == null) {
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

        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        this.capacity = getCapacity();
        if (container.getCount() != 1 || maxDrain <= 0) {
            return null;
        }

        FluidStack contained = getFluid();
        if (contained == null || contained.amount <= 0 || !canDrainFluidType(contained)) {
            return null;
        }

        final int drainAmount = Math.min(contained.amount, maxDrain);
        if (drainAmount == capacity) {
            FluidStack drained = contained.copy();

            if (doDrain) {
                setContainerToEmpty();
            }

            return drained;
        }

        return null;
    }
}
