package lu.kolja.lootr.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lu.kolja.lootr.CommonProxy;
import lu.kolja.lootr.Lootr;
import lu.kolja.lootr.LootrConfig;
import lu.kolja.lootr.block.LootrChestBlock;
import lu.kolja.lootr.tile.LootrChestTileEntity;
import lu.kolja.lootr.util.StackUtil;

public class ChestConverter {

    private static final int[][] NEIGHBORS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    private static boolean suppressItems;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (suppressItems && event.entity instanceof EntityItem) {
            event.setCanceled(true);
        }
    }

    public static void convertGenerated(ChestPos pos) {
        var category = LootCategories.take(pos);
        var world = DimensionManager.getWorld(pos.dimension());
        if (world == null || !world.getChunkProvider()
            .chunkExists(pos.x() >> 4, pos.z() >> 4)) {
            return;
        }
        var block = world.getBlock(pos.x(), pos.y(), pos.z());
        if (!LootrConfig.isConvertible(block)
            || !(world.getTileEntity(pos.x(), pos.y(), pos.z()) instanceof IInventory inventory)
            || inventory instanceof LootrChestTileEntity) {
            return;
        }
        var template = snapshot(inventory);
        if (StackUtil.isEmpty(template) && !LootrConfig.convertEmptyChests) {
            return;
        }
        var tile = convert(world, pos.x(), pos.y(), pos.z(), inventory, template, isTrapped(block));
        if (tile != null && category != null) {
            tile.setLootCategory(category);
        }
    }

    public static boolean isConvertibleChest(TileEntity tile) {
        return tile instanceof IInventory && !(tile instanceof LootrChestTileEntity)
            && LootrConfig.isConvertible(tile.getBlockType());
    }

    public static boolean isTrapped(Block block) {
        return block instanceof BlockChest chest && chest.field_149956_a == 1;
    }

    public static ItemStack[] snapshot(IInventory inventory) {
        var template = new ItemStack[StackUtil.CHEST_SIZE];
        int size = inventory.getSizeInventory();
        int next = 0;
        for (int i = 0; i < size; i++) {
            var stack = inventory.getStackInSlot(i);
            if (size <= StackUtil.CHEST_SIZE) {
                template[i] = ItemStack.copyItemStack(stack);
            } else if (stack != null) {
                if (next < StackUtil.CHEST_SIZE) {
                    template[next++] = stack.copy();
                } else {
                    Lootr.LOG.warn("Dropping {} from oversized inventory {} during conversion", stack, inventory);
                }
            }
        }
        return template;
    }

    public static LootrChestTileEntity convert(World world, int x, int y, int z, IInventory inventory,
        ItemStack[] template, boolean trapped) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            inventory.setInventorySlotContents(i, null);
        }
        int meta = LootrChestBlock.facing(world.getBlockMetadata(x, y, z))
            | (trapped ? LootrChestBlock.TRAPPED_BIT : 0);
        suppressItems = true;
        try {
            world.setBlock(x, y, z, CommonProxy.chest, meta, 2);
            if (!(world.getTileEntity(x, y, z) instanceof LootrChestTileEntity)) {
                world.removeTileEntity(x, y, z);
            }

            for (var side : NEIGHBORS) {
                if (world.blockExists(x + side[0], y, z + side[1])
                    && world.getTileEntity(x + side[0], y, z + side[1]) instanceof TileEntityChest neighbor) {
                    neighbor.updateContainingBlockInfo();
                }
            }
            if (world.getTileEntity(x, y, z) instanceof LootrChestTileEntity tile) {
                tile.setTemplate(template);
                tile.getTileId();
                world.markBlockForUpdate(x, y, z);
                return tile;
            }
            Lootr.LOG.error(
                "Replacement tile at {},{},{} in dim {} is not a Lootr chest",
                x,
                y,
                z,
                world.provider.dimensionId);
            return null;
        } finally {
            suppressItems = false;
        }
    }
}
