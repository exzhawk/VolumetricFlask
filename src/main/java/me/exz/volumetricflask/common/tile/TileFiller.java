package me.exz.volumetricflask.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkTile;
import me.exz.volumetricflask.common.items.ItemVolumetricFlask;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static me.exz.volumetricflask.Items.VOLUMETRIC_FLASKS;

public class TileFiller extends AENetworkTile implements IGridHost, IGridBlock, ICraftingProvider, IMEMonitorHandlerReceiver<IAEFluidStack>, ITickable {


    private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orElse(null);
    private final IFluidStorageChannel fluidStorageChannel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    private final IItemStorageChannel itemStorageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    private List<Fluid> fluids = new ArrayList<Fluid>();
    private IGridNode node = null;
    private boolean isFirstGetGridNode = true;
    ItemStack returnStack = null;

    protected IGrid grid;
    protected int usedChannels;

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        IStorageGrid storage = getStorageGrid();
        if (storage == null) {
            return;
        }
        final IMEMonitor<IAEFluidStack> fluidStorage = storage.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
        for (IAEFluidStack aeFluidStack : fluidStorage.getStorageList()) {
            Fluid fluid = aeFluidStack.getFluid();
            if (fluid == null) {
                continue;
            }
            for (ItemVolumetricFlask flask : VOLUMETRIC_FLASKS) {
                ItemStack empty = new ItemStack(flask, 1);
                empty.setTagCompound(new NBTTagCompound());
                ItemStack filled = new ItemStack(flask, 1);
                IFluidHandler fluidHandler = filled.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                int capacity = flask.capacity;
                fluidHandler.fill(new FluidStack(fluid, capacity), true);
                ItemStack pattern = getPattern(empty, filled);
                ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
                ICraftingPatternDetails details = patter.getPatternForItem(pattern, world);
                if (details == null) {
                    continue;
                }
                craftingTracker.addCraftingOption(this, details);
            }
        }

    }


    private ItemStack getPattern(ItemStack emptyContainer, ItemStack filledContainer) {
        NBTTagList in = new NBTTagList();
        NBTTagList out = new NBTTagList();
        in.appendTag(emptyContainer.writeToNBT(new NBTTagCompound()));
        out.appendTag(filledContainer.writeToNBT(new NBTTagCompound()));
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setTag("in", in);
        itemTag.setTag("out", out);
        itemTag.setBoolean("crafting", false);
        ItemStack pattern = new ItemStack(this.encodedPattern);
        pattern.setTagCompound(itemTag);
        return pattern;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (this.returnStack != null && !this.returnStack.isEmpty()) {
            return false;
        }
        IStorageGrid storage = getStorageGrid();
        if (storage == null) {
            return false;
        }
        ItemStack filled = patternDetails.getCondensedOutputs()[0].getDefinition();

        IFluidHandler fluidHandler = filled.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack fluidStack = fluidHandler.getTankProperties()[0].getContents();
        final IMEMonitor<IAEFluidStack> fluidStorage = storage.getInventory(fluidStorageChannel);
        IAEFluidStack aeFluidStack = fluidStorageChannel.createStack(fluidStack);
        IAEFluidStack extracted = fluidStorage.extractItems(aeFluidStack.copy(), Actionable.SIMULATE, new MachineSource(this));
        if (extracted == null || extracted.getStackSize() != aeFluidStack.getStackSize()) {
            return false;
        }
        fluidStorage.extractItems(aeFluidStack.copy(), Actionable.MODULATE, new MachineSource(this));
        this.returnStack = filled;


        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    public void postUpdateEvent() {
        if (getGridNode(AEPartLocation.INTERNAL) != null && getGridNode(AEPartLocation.INTERNAL).getGrid() != null) {
            getGridNode(AEPartLocation.INTERNAL).getGrid().postEvent(new MENetworkCraftingPatternChange(this, getGridNode(AEPartLocation.INTERNAL)));
        }
    }

    @MENetworkEventSubscribe
    public void powerUpdate(MENetworkPowerStatusChange event) {
        IStorageGrid storage = getStorageGrid();
        if (storage != null) {
            postChange(storage.getInventory(fluidStorageChannel), null, null);
        }
    }

    @MENetworkEventSubscribe
    public void cellUpdate(MENetworkCellArrayUpdate event) {
        IStorageGrid storage = getStorageGrid();
        if (storage != null) {
            postChange(storage.getInventory(fluidStorageChannel), null, null);
        }
    }

    @Override
    public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, IActionSource actionSource) {
        List<Fluid> oldFluids = new ArrayList<Fluid>(this.fluids);
        boolean mustUpdate = false;
        this.fluids.clear();
        for (IAEFluidStack fluid : ((IMEMonitor<IAEFluidStack>) monitor)
                .getStorageList()) {
            if (!oldFluids.contains(fluid.getFluid())) {
                mustUpdate = true;
            } else {
                oldFluids.remove(fluid.getFluid());
            }
            this.fluids.add(fluid.getFluid());
        }
        if (!(oldFluids.isEmpty() && !mustUpdate)) {
            postUpdateEvent();
        }
    }

    @Override
    public void onListUpdate() {

    }

    public void registerListener() {
        IStorageGrid storage = getStorageGrid();
        if (storage == null) {
            return;
        }
        IMEMonitor<IAEFluidStack> fluidInventory = storage.getInventory(fluidStorageChannel);
        postChange(fluidInventory, null, null);
        fluidInventory.addListener(this, null);
    }

    public void removeListener() {
        IStorageGrid storage = getStorageGrid();
        if (storage == null) {
            return;
        }
        IMEMonitor<IAEFluidStack> fluidInventory = storage.getInventory(fluidStorageChannel);
        fluidInventory.removeListener(this);
    }

    @Override
    public double getIdlePowerUsage() {
        return 0;
    }

    @Nonnull
    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Nonnull
    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(@Nonnull GridNotification notification) {

    }

    @Override
    public void setNetworkStatus(IGrid grid, int channelsInUse) {
        if (this.grid != null && this.grid != grid) {
            this.updateGrid(this.grid, grid);
            this.grid = grid;
            this.usedChannels = channelsInUse;
            if (this.grid.getCache(IStorageGrid.class) != null) {
                IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
                this.postChange(storageGrid.getInventory(fluidStorageChannel), null, null);
            }
        } else {
            this.grid = grid;
            this.usedChannels = usedChannels;
        }
        IStorageGrid storage = getStorageGrid();
        postChange(storage.getInventory(fluidStorageChannel), null, null);
    }

    public void updateGrid(IGrid oldGrid, IGrid newGrid) {
        if (oldGrid != null) {
            IStorageGrid storage = oldGrid.getCache(IStorageGrid.class);
            if (storage != null) {
                storage.getInventory(fluidStorageChannel).removeListener(this);
            }
        }
        if (newGrid != null) {
            IStorageGrid storage = newGrid.getCache(IStorageGrid.class);
            if (storage != null) {
                storage.getInventory(fluidStorageChannel).addListener(this, null);
            }
        }
    }

    @Nullable
    private IStorageGrid getStorageGrid() {
        this.node = getGridNode(AEPartLocation.INTERNAL);
        if (this.node == null) {
            return null;
        }
        IGrid grid = this.node.getGrid();
        if (grid == null) {
            return null;
        }
        return grid.getCache(IStorageGrid.class);
    }

    @Nonnull
    @Override
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Nonnull
    @Override
    public IGridHost getMachine() {
        return this;
    }

    @Override
    public IGridNode getActionableNode() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return null;
        }
        if (this.node == null) {
            this.node = AEApi.instance().grid().createGridNode(this);
        }
        return this.node;
    }


    @Override
    public IGridNode getGridNode(AEPartLocation location) {
        if (FMLCommonHandler.instance().getSide().isClient() && (world == null || world.isRemote)) {
            return null;
        }
        if (this.isFirstGetGridNode) {
            this.isFirstGetGridNode = false;
            getActionableNode().updateState();
            IStorageGrid storage = getStorageGrid();
            storage.getInventory(fluidStorageChannel).addListener(this, null);
        }
        return this.node;
    }

    @Nonnull
    @Override
    public ItemStack getMachineRepresentation() {
        DimensionalCoord loc = getLocation();
        IBlockState blockState = loc.getWorld().getBlockState(loc.getPos());
        Block block = blockState.getBlock();
        return new ItemStack(block, 1, block.getMetaFromState(blockState));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.returnStack != null && !this.returnStack.isEmpty()) {
            compound.setTag("return", this.returnStack.writeToNBT(new NBTTagCompound()));
        } else {
            compound.setBoolean("isReturnEmpty", true);
        }
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node != null) {
            NBTTagCompound nodeTag = new NBTTagCompound();
            node.saveToNBT("node0", nodeTag);
            compound.setTag("nodes", nodeTag);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("return")) {
            this.returnStack = new ItemStack(compound.getCompoundTag("return"));
        } else if (compound.hasKey("isReturnEmpty") && compound.getBoolean("isReturnEmpty")) {
            this.returnStack = null;
        }
        if (hasWorld()) {
            IGridNode node = getGridNode(AEPartLocation.INTERNAL);
            if (compound.hasKey("nodes") && node != null) {
                node.loadFromNBT("node0", compound.getCompoundTag("nodes"));
                node.updateState();
            }
        }
    }

    @Override
    public void update() {
        if (!hasWorld()) {
            return;
        }
        IStorageGrid storage = getStorageGrid();
        if (storage == null) {
            return;
        }
        if (this.returnStack != null && !this.returnStack.isEmpty()) {
            IAEItemStack toInject = itemStorageChannel.createStack(this.returnStack);
            if (storage.getInventory(itemStorageChannel).canAccept(toInject.copy()) &&
                    storage.getInventory(itemStorageChannel).injectItems(toInject.copy(), Actionable.SIMULATE, new MachineSource(this)) == null) {
                storage.getInventory(itemStorageChannel).injectItems(toInject, Actionable.MODULATE, new MachineSource(this));
                this.returnStack = null;
            }
        }

        if (world.getTotalWorldTime() % 100 == 0) {
            postChange(storage.getInventory(fluidStorageChannel), null, null);
        }
    }
}
