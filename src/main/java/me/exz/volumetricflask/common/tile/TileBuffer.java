package me.exz.volumetricflask.common.tile;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.ItemSlot;
import me.exz.volumetricflask.utils.FluidAdaptor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TileBuffer extends TileEntity implements ITickable, IAEAppEngInventory, IAEFluidInventory {
    private static final String ITEM_NBT_KEY = "storage_item";
    private static final String FLUID_NBT_KEY = "storage_fluid";
    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 9);
    private AEFluidInventory tanks = new AEFluidInventory(this, 9, Fluid.BUCKET_VOLUME * 64);

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.readFromNBT(compound, ITEM_NBT_KEY);
        tanks.readFromNBT(compound, FLUID_NBT_KEY);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        inventory.writeToNBT(compound, ITEM_NBT_KEY);
        tanks.writeToNBT(compound, FLUID_NBT_KEY);
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {

        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tanks;
        }
        return null;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }
        if (world.getTotalWorldTime() % 20 != 0) {
            return;
        }
        for (EnumFacing facing : EnumSet.allOf(EnumFacing.class)) {
            final TileEntity te = world.getTileEntity(getPos().offset(facing));
            if (te == null) {
                continue;
            }
            if (te instanceof TileInterface) {
                continue;
            }

            final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, facing.getOpposite());
            final IFluidHandler fh = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
            if (ad != null) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    final ItemStack is = inventory.getStackInSlot(i);
                    ItemStack left = ad.addItems(is);
                    inventory.setStackInSlot(i, left);
                }
            }
            if (fh != null) {
                for (int i = 0; i < tanks.getSlots(); i++) {
                    IAEFluidStack aeFluidStack = tanks.getFluidInSlot(i);
                    if (aeFluidStack == null) {
                        continue;
                    }
                    final FluidStack fs = aeFluidStack.getFluidStack();
                    int filled = fh.fill(fs, true);
                    tanks.drain(i, filled, true);
                }
            }
        }
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {

    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {

    }
}
