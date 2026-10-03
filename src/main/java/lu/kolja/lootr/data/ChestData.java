package lu.kolja.lootr.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

import lu.kolja.lootr.util.StackUtil;

public class ChestData extends WorldSavedData {

    public static final String PREFIX = "lootr_";

    private final Map<UUID, SpecialChestInventory> inventories = new HashMap<>();
    private UUID tileId;
    private int dimension;
    private int x;
    private int y;
    private int z;

    public ChestData(String name) {
        super(name);
    }

    public static String name(UUID tileId) {
        return PREFIX + tileId;
    }

    public UUID getTileId() {
        return tileId;
    }

    public Map<UUID, SpecialChestInventory> getInventories() {
        return inventories;
    }

    public void setLocation(UUID tileId, int dimension, int x, int y, int z) {
        if (!tileId.equals(this.tileId) || dimension != this.dimension || x != this.x || y != this.y || z != this.z) {
            this.tileId = tileId;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            markDirty();
        }
    }

    public boolean isAt(int dimension, int x, int y, int z) {
        return this.dimension == dimension && this.x == x && this.y == y && this.z == z;
    }

    public int getDimension() {
        return dimension;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        tileId = compound.hasKey("tileId", 8) ? UUID.fromString(compound.getString("tileId")) : null;
        dimension = compound.getInteger("dimension");
        x = compound.getInteger("x");
        y = compound.getInteger("y");
        z = compound.getInteger("z");
        inventories.clear();
        var list = compound.getTagList("inventories", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            var tag = list.getCompoundTagAt(i);
            var player = UUID.fromString(tag.getString("player"));
            var contents = StackUtil.read(tag.getTagList("items", 10), StackUtil.CHEST_SIZE);
            inventories.put(player, new SpecialChestInventory(this, player, contents));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if (tileId != null) {
            compound.setString("tileId", tileId.toString());
        }
        compound.setInteger("dimension", dimension);
        compound.setInteger("x", x);
        compound.setInteger("y", y);
        compound.setInteger("z", z);
        var list = new NBTTagList();
        for (var entry : inventories.entrySet()) {
            var tag = new NBTTagCompound();
            tag.setString(
                "player",
                entry.getKey()
                    .toString());
            tag.setTag(
                "items",
                StackUtil.write(
                    entry.getValue()
                        .getContents()));
            list.appendTag(tag);
        }
        compound.setTag("inventories", list);
    }
}
