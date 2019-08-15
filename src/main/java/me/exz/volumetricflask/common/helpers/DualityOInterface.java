package me.exz.volumetricflask.common.helpers;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.ConfigManager;
import appeng.util.InventoryAdaptor;
import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class DualityOInterface extends DualityInterface {
//    private List<ItemStack> waitingToSend = null;

//    private final AENetworkProxy gridProxy;
//    private List<ICraftingPatternDetails> craftingList = null;
//    private final IInterfaceHost iHost;
//    private final ConfigManager cm = new ConfigManager(this);


    public DualityOInterface(AENetworkProxy networkProxy, IInterfaceHost ih) {
        super(networkProxy, ih);
//        this.gridProxy = networkProxy;
//        this.iHost = ih;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
//        Method method = ReflectionHelper.findMethod(DualityInterface.class, "hasItemsToSend", null);
//        try {
//            method.invoke(this);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
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
            final IFluidHandler fluidHandler = this.getFluidHandler(te, s.getOpposite());
            if (ad != null || fluidHandler != null) {
                if (this.isBlocking()) {
                    if (!ad.simulateRemove(1, ItemStack.EMPTY, null).isEmpty()) {
                        continue;
                    }
                }

                if (this.acceptsItems(ad, table)) {
                    for (int x = 0; x < table.getSizeInventory(); x++) {
                        final ItemStack is = table.getStackInSlot(x);
                        if (!is.isEmpty()) {
                            final ItemStack added = ad.addItems(is);
                            this.addToSendList(added);
                        }
                    }
                    this.pushItemsOut(possibleDirections);
                    return true;
                }
            }
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

            if (whatToSend.isEmpty()) {
                i.remove();
            }
        }

        if (waitingToSend.isEmpty()) {
            waitingToSend = null;
        }
    }

    private boolean isBlocking() {
        ConfigManager cm = ReflectionHelper.getPrivateValue(DualityInterface.class, this, "cm");
        return cm.getSetting(Settings.BLOCK) == YesNo.YES;
    }

    private boolean acceptsItems(final InventoryAdaptor ad, final InventoryCrafting table) {
        for (int x = 0; x < table.getSizeInventory(); x++) {
            final ItemStack is = table.getStackInSlot(x);
            if (is.isEmpty() || is.getItem() instanceof ItemVolumetricFlask) {
                continue;
            }

            if (!ad.simulateAdd(is.copy()).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean acceptsFluid(final IFluidHandler fluidHandler, final InventoryCrafting table) {
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
}
