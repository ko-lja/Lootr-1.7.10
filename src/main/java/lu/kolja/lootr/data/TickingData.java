package lu.kolja.lootr.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

public class TickingData extends WorldSavedData {

    public static final String DECAY = "lootr_decay";
    public static final String REFRESH = "lootr_refresh";

    private final Map<UUID, Integer> ticks = new HashMap<>();

    public TickingData(String name) {
        super(name);
    }

    public int getValue(UUID id) {
        return ticks.getOrDefault(id, -1);
    }

    public boolean isDone(UUID id) {
        int value = getValue(id);
        return value == 0 || value == 1;
    }

    public void setValue(UUID id, int value) {
        ticks.put(id, value);
        markDirty();
    }

    public void remove(UUID id) {
        if (ticks.remove(id) != null) {
            markDirty();
        }
    }

    public void tick() {
        boolean changed = false;
        for (var entry : ticks.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ticks.clear();
        var list = compound.getTagList("result", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            var tag = list.getCompoundTagAt(i);
            ticks.put(UUID.fromString(tag.getString("id")), tag.getInteger("value"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        var list = new NBTTagList();
        for (var entry : ticks.entrySet()) {
            var tag = new NBTTagCompound();
            tag.setString(
                "id",
                entry.getKey()
                    .toString());
            tag.setInteger("value", entry.getValue());
            list.appendTag(tag);
        }
        compound.setTag("result", list);
    }
}
