package lu.kolja.lootr.data;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import lu.kolja.lootr.tile.LootrChestTileEntity;

public class SpecialChestInventory implements IInventory {

    private final ChestData data;
    private final UUID player;
    private final ItemStack[] contents;

    public SpecialChestInventory(ChestData data, UUID player, ItemStack[] contents) {
        this.data = data;
        this.player = player;
        this.contents = contents;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public boolean isAt(World world, int x, int y, int z) {
        return data.isAt(world.provider.dimensionId, x, y, z);
    }

    private LootrChestTileEntity getTile() {
        var world = DimensionManager.getWorld(data.getDimension());
        if (world == null || !world.blockExists(data.getX(), data.getY(), data.getZ())) {
            return null;
        }
        if (world.getTileEntity(data.getX(), data.getY(), data.getZ()) instanceof LootrChestTileEntity tile
            && tile.getTileId()
                .equals(data.getTileId())) {
            return tile;
        }
        return null;
    }

    @Override
    public int getSizeInventory() {
        return contents.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return contents[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        var stack = contents[slot];
        if (stack == null) {
            return null;
        }
        if (stack.stackSize <= count) {
            contents[slot] = null;
            markDirty();
            return stack;
        }
        var split = stack.splitStack(count);
        markDirty();
        return split;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        var stack = contents[slot];
        contents[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        contents[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.lootr.chest";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        data.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entity) {
        return entity.getUniqueID()
            .equals(player) && entity.worldObj.provider.dimensionId == data.getDimension()
            && entity.getDistanceSq(data.getX() + 0.5D, data.getY() + 0.5D, data.getZ() + 0.5D) <= 64.0D
            && getTile() != null;
    }

    @Override
    public void openInventory() {
        var tile = getTile();
        if (tile != null) {
            tile.openInventory();
        }
    }

    @Override
    public void closeInventory() {
        markDirty();
        var tile = getTile();
        if (tile != null) {
            tile.closeInventory();
            tile.addOpener(player);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }
}
