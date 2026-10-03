package lu.kolja.lootr.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import lu.kolja.lootr.block.LootrChestBlock;
import lu.kolja.lootr.data.SpecialChestInventory;
import lu.kolja.lootr.util.StackUtil;

public class LootrChestTileEntity extends TileEntityChest {

    private final Set<UUID> openers = new HashSet<>();
    private ItemStack[] template;
    private String lootCategory;
    private UUID tileId;
    private int ticksSinceSync;

    public UUID getTileId() {
        if (tileId == null) {
            tileId = UUID.randomUUID();
            markDirty();
        }
        return tileId;
    }

    public ItemStack[] getTemplate() {
        return template;
    }

    public void setTemplate(ItemStack[] template) {
        this.template = template;
        markDirty();
    }

    public String getLootCategory() {
        return lootCategory;
    }

    public void setLootCategory(String lootCategory) {
        this.lootCategory = lootCategory;
        markDirty();
    }

    public Set<UUID> getOpeners() {
        return openers;
    }

    public void addOpener(UUID player) {
        if (openers.add(player)) {
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public void removeOpener(UUID player) {
        if (openers.remove(player)) {
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public boolean isTrapped() {
        return (getBlockMetadata() & LootrChestBlock.TRAPPED_BIT) != 0;
    }

    @Override
    public void updateEntity() {
        ++ticksSinceSync;
        if (!worldObj.isRemote && numPlayersUsing != 0 && (ticksSinceSync + xCoord + yCoord + zCoord) % 200 == 0) {
            numPlayersUsing = countPlayersUsing(worldObj, xCoord, yCoord, zCoord);
        }

        prevLidAngle = lidAngle;
        if (numPlayersUsing > 0 && lidAngle == 0.0F) {
            playSound("random.chestopen");
        }

        if (numPlayersUsing == 0 && lidAngle > 0.0F || numPlayersUsing > 0 && lidAngle < 1.0F) {
            float previous = lidAngle;
            lidAngle += numPlayersUsing > 0 ? 0.1F : -0.1F;
            if (lidAngle > 1.0F) {
                lidAngle = 1.0F;
            }
            if (lidAngle < 0.5F && previous >= 0.5F) {
                playSound("random.chestclosed");
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    private void playSound(String sound) {
        worldObj.playSoundEffect(
            xCoord + 0.5D,
            yCoord + 0.5D,
            zCoord + 0.5D,
            sound,
            0.5F,
            worldObj.rand.nextFloat() * 0.1F + 0.9F);
    }

    public static int countPlayersUsing(World world, int x, int y, int z) {
        int count = 0;
        var box = AxisAlignedBB.getBoundingBox(x - 5.0D, y - 5.0D, z - 5.0D, x + 6.0D, y + 6.0D, z + 6.0D);
        for (var player : world.getEntitiesWithinAABB(EntityPlayer.class, box)) {
            if (player.openContainer instanceof ContainerChest container
                && container.getLowerChestInventory() instanceof SpecialChestInventory inventory
                && inventory.isAt(world, x, y, z)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void checkForAdjacentChests() {
        if (!adjacentChestChecked) {
            adjacentChestChecked = true;
            adjacentChestZNeg = null;
            adjacentChestXPos = null;
            adjacentChestXNeg = null;
            adjacentChestZPos = null;
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y,
        int z) {
        return oldBlock != newBlock;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tileId = compound.hasKey("LootrTileId", 8) ? UUID.fromString(compound.getString("LootrTileId")) : null;
        template = compound.hasKey("LootrTemplate", 9)
            ? StackUtil.read(compound.getTagList("LootrTemplate", 10), StackUtil.CHEST_SIZE)
            : null;
        lootCategory = compound.hasKey("LootrCategory", 8) ? compound.getString("LootrCategory") : null;
        readOpeners(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (tileId != null) {
            compound.setString("LootrTileId", tileId.toString());
        }
        if (template != null) {
            compound.setTag("LootrTemplate", StackUtil.write(template));
        }
        if (lootCategory != null) {
            compound.setString("LootrCategory", lootCategory);
        }
        writeOpeners(compound);
    }

    private void readOpeners(NBTTagCompound compound) {
        openers.clear();
        var list = compound.getTagList("LootrOpeners", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            openers.add(UUID.fromString(list.getStringTagAt(i)));
        }
    }

    private void writeOpeners(NBTTagCompound compound) {
        var list = new NBTTagList();
        for (var opener : openers) {
            list.appendTag(new NBTTagString(opener.toString()));
        }
        compound.setTag("LootrOpeners", list);
    }

    @Override
    public Packet getDescriptionPacket() {
        var compound = new NBTTagCompound();
        writeOpeners(compound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readOpeners(packet.func_148857_g());
    }
}
