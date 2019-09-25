package me.exz.volumetricflask.common.helpers;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.ConfigManager;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;
import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
import me.exz.volumetricflask.utils.FluidAdaptor;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class DualityOInterface extends DualityInterface implements IAEFluidInventory, ITickable {

    private final AEFluidInventory tanks = new AEFluidInventory(this, 1, Fluid.BUCKET_VOLUME * 64);
    private static final String FLUID_NBT_KEY = "storage_fluid";

    public DualityOInterface(AENetworkProxy networkProxy, IInterfaceHost ih) {
        super(networkProxy, ih);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        AENetworkProxy gridProxy = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "gridProxy");
        List<ICraftingPatternDetails> craftingList = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "craftingList");
        IInterfaceHost iHost = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "iHost");

        if (this.hasItemsToSend() || !gridProxy.isActive() || !craftingList.contains(patternDetails)) {
            return false;
        }

        final TileEntity tile = iHost.getTileEntity();
        final World w = tile.getWorld();

        final EnumSet<EnumFacing> possibleDirections = iHost.getTargets();
        for (final EnumFacing s : possibleDirections) {
            final TileEntity te = w.getTileEntity(tile.getPos().offset(s));
            if (te instanceof IInterfaceHost) {
                try {
                    AENetworkProxy gridProxy2 = ReflectionHelper.getPrivateValue(DualityInterface.class, ((IInterfaceHost) te).getInterfaceDuality(), "gridProxy");
                    if (gridProxy2.getGrid() == gridProxy.getGrid()) {
                        continue;
                    }
                } catch (final GridAccessException e) {
                    continue;
                }
                continue;
            }

            if (te instanceof ICraftingMachine) {
                final ICraftingMachine cm = (ICraftingMachine) te;
                if (cm.acceptsPlans()) {
                    if (cm.pushPattern(patternDetails, table, s.getOpposite())) {
                        return true;
                    }
                    continue;
                }
                continue;
            }
            final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
            final FluidAdaptor fad = FluidAdaptor.getAdaptor(te, s.getOpposite());
            if (ad == null && fad == null) {
                //no inventory and no tank
                continue;
            }
            //is blocking
            if (this.isBlocking()) {
                if (ad != null) {
                    if (!ad.simulateRemove(1, ItemStack.EMPTY, null).isEmpty()) {
                        continue;
                    }
                }
                if (fad != null) {
                    if (!fad.isEmpty()) {
                        continue;
                    }
                }
            }

            //determine whether pattern has flask or other items
            boolean hasOther = false;
            boolean hasFlask = false;
            for (int x = 0; x < table.getSizeInventory(); x++) {
                final ItemStack is = table.getStackInSlot(x);
                if (!is.isEmpty()) {
                    if (is.getItem() instanceof ItemVolumetricFlask) {
                        hasFlask = true;
                    } else {
                        hasOther = true;
                    }
                }
            }

            if (hasOther) {
                if (ad == null || !this.acceptsItems(ad, table)) {
                    continue;
                }
            }
            if (hasFlask) {
                if (fad == null || !this.acceptsFluid(fad, table)) {
                    continue;
                }
            }

            for (int x = 0; x < table.getSizeInventory(); x++) {
                final ItemStack is = table.getStackInSlot(x);
                if (!is.isEmpty()) {
                    if (is.getItem() instanceof ItemVolumetricFlask && fad != null) {
                        final ItemStack added = fad.addFlask(is);
                        this.addToSendList(added);
                        ItemStack emptyVolumetricFlask = new ItemStack(is.getItem(), is.getCount() - added.getCount());
                        emptyVolumetricFlask.setTagCompound(new NBTTagCompound());
                        InventoryAdaptor iad = new AdaptorItemHandler(this.getInternalInventory());
                        iad.addItems(emptyVolumetricFlask);
                    } else if (ad != null) {
                        final ItemStack added = ad.addItems(is);
                        this.addToSendList(added);
                    }
                }
            }
            this.pushItemsOut(possibleDirections);
            return true;
        }

        return false;
    }


    private boolean hasItemsToSend() {
        List<ItemStack> waitingToSend = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "waitingToSend");
        return waitingToSend != null && !waitingToSend.isEmpty();
    }

    private void pushItemsOut(final EnumSet<EnumFacing> possibleDirections) {
        if (!this.hasItemsToSend()) {
            return;
        }

        IInterfaceHost iHost = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "iHost");
        List<ItemStack> waitingToSend = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "waitingToSend");

        final TileEntity tile = iHost.getTileEntity();
        final World w = tile.getWorld();

        final Iterator<ItemStack> i = waitingToSend.iterator();
        while (i.hasNext()) {
            ItemStack whatToSend = i.next();
            if (whatToSend.getItem() instanceof ItemVolumetricFlask) {
                for (final EnumFacing s : possibleDirections) {
                    final TileEntity te = w.getTileEntity(tile.getPos().offset(s));
                    if (te == null) {
                        continue;
                    }
                    final FluidAdaptor fad = FluidAdaptor.getAdaptor(te, s.getOpposite());
                    if (fad != null) {
                        final ItemStack result = fad.addFlask(whatToSend);
                        ItemStack emptyVolumetricFlask = new ItemStack(whatToSend.getItem(), whatToSend.getCount() - result.getCount());
                        emptyVolumetricFlask.setTagCompound(new NBTTagCompound());
                        InventoryAdaptor iad = new AdaptorItemHandler(this.getInternalInventory());
                        iad.addItems(emptyVolumetricFlask);
                        if (result.isEmpty()) {
                            whatToSend = ItemStack.EMPTY;
                        } else {
                            whatToSend.setCount(whatToSend.getCount() - (whatToSend.getCount() - result.getCount()));
                        }

                        if (whatToSend.isEmpty()) {
                            break;
                        }
                    }
                }
            } else {
                for (final EnumFacing s : possibleDirections) {
                    final TileEntity te = w.getTileEntity(tile.getPos().offset(s));
                    if (te == null) {
                        continue;
                    }
                    final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
                    if (ad != null) {
                        final ItemStack result = ad.addItems(whatToSend);

                        if (result.isEmpty()) {
                            whatToSend = ItemStack.EMPTY;
                        } else {
                            whatToSend.setCount(whatToSend.getCount() - (whatToSend.getCount() - result.getCount()));
                        }
                        if (whatToSend.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            if (whatToSend.isEmpty()) {
                i.remove();
            }
        }

        if (waitingToSend.isEmpty()) {
            ReflectionHelper.setPrivateValue(DualityInterface.class, this, null, "waitingToSend");
        }
    }

    private boolean isBlocking() {
        ConfigManager cm = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "cm");
        return cm.getSetting(Settings.BLOCK) == YesNo.YES;
    }

    private boolean acceptsItems(final InventoryAdaptor ad, final InventoryCrafting table) {
        for (int x = 0; x < table.getSizeInventory(); x++) {
            final ItemStack is = table.getStackInSlot(x);
            if (is.isEmpty()) {
                continue;
            }
            if (is.getItem() instanceof ItemVolumetricFlask) {
                continue;
            }

            if (!ad.simulateAdd(is.copy()).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean acceptsFluid(final FluidAdaptor fad, final InventoryCrafting table) {
        for (int x = 0; x < table.getSizeInventory(); x++) {
            final ItemStack is = table.getStackInSlot(x);
            if (is.isEmpty()) {
                continue;
            }
            if (!(is.getItem() instanceof ItemVolumetricFlask)) {
                continue;
            }

            if (!fad.simulateAdd(is.copy()).isEmpty()) {
                return false;
            }
            ItemStack emptyVolumetricFlask = new ItemStack(is.getItem(), is.getCount());
            emptyVolumetricFlask.setTagCompound(new NBTTagCompound());
            InventoryAdaptor iad = new AdaptorItemHandler(this.getInternalInventory());
            if (!iad.simulateAdd(emptyVolumetricFlask).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void addToSendList(final ItemStack is) {
        if (is.isEmpty()) {
            return;
        }
        AENetworkProxy gridProxy = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "gridProxy");
        List<ItemStack> waitingToSend = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "waitingToSend");
        if (waitingToSend == null) {
            waitingToSend = new ArrayList<>();
            ReflectionHelper.setPrivateValue(DualityInterface.class, this, waitingToSend, "waitingToSend");
//            waitingToSend = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "waitingToSend");
        }

        waitingToSend.add(is);

        try {
            gridProxy.getTick().wakeDevice(gridProxy.getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    private IFluidHandler getFluidHandler(final TileEntity te, final EnumFacing d) {
        if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d)) {
            return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d);
        }
        return null;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.tanks.writeToNBT(data, FLUID_NBT_KEY);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.tanks.readFromNBT(data, FLUID_NBT_KEY);
    }

    @Override
    public <T> T getCapability(Capability<T> capabilityClass, EnumFacing facing) {
        if (capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this.tanks;
        } else {
            return super.getCapability(capabilityClass, facing);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capabilityClass, EnumFacing facing) {
        return super.hasCapability(capabilityClass, facing) || capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {

    }

    @Override
    public void update() {
        IAEFluidStack aeFluidStack = this.tanks.getFluidInSlot(0);
        if (aeFluidStack != null && aeFluidStack.getStackSize() != 0) {
            try {
                AENetworkProxy gridProxy = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "gridProxy");
                IMEInventory<IAEFluidStack> dest = gridProxy.getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
                IActionSource src = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "interfaceRequestSource");
                IAEFluidStack left = dest.injectItems(aeFluidStack.copy(), Actionable.MODULATE, src);
                this.tanks.setFluidInSlot(0, left);
            } catch (GridAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
