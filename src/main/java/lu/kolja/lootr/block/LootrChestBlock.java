package lu.kolja.lootr.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import lu.kolja.lootr.LootrConfig;
import lu.kolja.lootr.data.DataStorage;
import lu.kolja.lootr.tile.LootrChestTileEntity;
import lu.kolja.lootr.util.ChestUtil;

public class LootrChestBlock extends BlockChest {

    public static final int CHEST_TYPE = 76;
    public static final int TRAPPED_BIT = 8;
    private static final int[] FACING_BY_YAW = { 2, 5, 3, 4 };

    public LootrChestBlock() {
        super(CHEST_TYPE);
        setHardness(2.5F);
        setStepSound(soundTypeWood);
        setBlockName("lootr_chest");
    }

    public static int facing(int meta) {
        int facing = meta & 7;
        return facing >= 2 && facing <= 5 ? facing : 3;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new LootrChestTileEntity();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        if (player instanceof EntityPlayerMP playerMP) {
            if (player.isSneaking()) {
                ChestUtil.handleSneak(world, x, y, z, playerMP);
            } else if (!isBlocked(world, x, y, z)) {
                ChestUtil.handleOpen(world, x, y, z, playerMP);
            }
        }
        return true;
    }

    private static boolean isBlocked(World world, int x, int y, int z) {
        if (world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) {
            return true;
        }
        var box = AxisAlignedBB.getBoundingBox(x, y + 1, z, x + 1, y + 2, z + 1);
        for (var ocelot : world.getEntitiesWithinAABB(EntityOcelot.class, box)) {
            if (ocelot.isSitting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {}

    @Override
    public void func_149954_e(World world, int x, int y, int z) {}

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int yaw = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, FACING_BY_YAW[yaw], 3);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return world.getBlock(x, y, z)
            .isReplaceable(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    @Override
    public IInventory func_149951_m(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return side != -1 && (world.getBlockMetadata(x, y, z) & TRAPPED_BIT) != 0;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        if ((world.getBlockMetadata(x, y, z) & TRAPPED_BIT) != 0
            && world.getTileEntity(x, y, z) instanceof LootrChestTileEntity tile) {
            return MathHelper.clamp_int(tile.numPlayersUsing, 0, 15);
        }
        return 0;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return LootrConfig.zeroComparator ? 0 : 1;
    }

    @Override
    public float getExplosionResistance(Entity exploder, World world, int x, int y, int z, double explosionX,
        double explosionY, double explosionZ) {
        if (LootrConfig.blastImmune) {
            return Float.MAX_VALUE;
        }
        if (LootrConfig.blastResistant) {
            return 16.0F;
        }
        return super.getExplosionResistance(exploder, world, x, y, z, explosionX, explosionY, explosionZ);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof LootrChestTileEntity tile) {
            var data = DataStorage.find(tile.getTileId());
            if (data != null && !data.getInventories()
                .isEmpty()) {
                data.getInventories()
                    .clear();
                data.markDirty();
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock((meta & TRAPPED_BIT) != 0 ? Blocks.trapped_chest : Blocks.chest);
    }
}
