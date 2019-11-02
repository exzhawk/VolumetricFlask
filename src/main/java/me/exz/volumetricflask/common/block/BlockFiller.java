package me.exz.volumetricflask.common.block;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.util.Platform;
import com.google.common.collect.Lists;
import me.exz.volumetricflask.common.tile.TileFiller;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static me.exz.volumetricflask.TabVolumetricFlask.TAB_VOLUMETRIC_FLASK;
import static me.exz.volumetricflask.VolumetricFlask.MODID;

public class BlockFiller extends Block {
    public BlockFiller() {
        super(Material.ROCK);
        this.setUnlocalizedName(MODID + ".filler");
        this.setRegistryName("filler");
        this.setCreativeTab(TAB_VOLUMETRIC_FLASK);
        this.setSoundType(SoundType.STONE);
        this.setHardness(2.5F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFiller();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isRemote) {
            return;
        }
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null) {
            return;
        }
        IGridHost gridHost = (IGridHost) tile;
        IGridNode node = gridHost.getGridNode(AEPartLocation.INTERNAL);
        if (node != null && placer instanceof EntityPlayer) {
            int playerID = AEApi.instance().registries().players().getID((EntityPlayer) placer);
            node.setPlayerID(playerID);
            node.updateState();
        }

        if (tile instanceof TileFiller) {
            ((TileFiller) tile).registerListener();
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem;
        if (player != null && !player.getHeldItem(hand).isEmpty()) {
            heldItem = player.getHeldItem(hand);
            if (Platform.isWrench(player, heldItem, pos) && player.isSneaking()) {
                final IBlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();
                final ItemStack[] itemDropCandidates = Platform.getBlockDrops(world, pos);
                if (block.removedByPlayer(blockState, world, pos, player, false)) {
                    final List<ItemStack> itemsToDrop = Lists.newArrayList(itemDropCandidates);
                    Platform.spawnDrops(world, pos, itemsToDrop);
                    world.setBlockToAir(pos);
                }

                return false;

            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) {
            super.breakBlock(worldIn, pos, state);
            return;
        }
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null) {
            return;
        }
        IGridHost gridHost = (IGridHost) tile;
        IGridNode node = gridHost.getGridNode(AEPartLocation.INTERNAL);
        if (node != null) {
            node.destroy();
        }
        if (tile instanceof TileFiller) {
            ((TileFiller) tile).removeListener();
        }
        super.breakBlock(worldIn, pos, state);

    }
}
