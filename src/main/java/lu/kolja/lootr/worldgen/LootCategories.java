package lu.kolja.lootr.worldgen;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandomChestContent;

public final class LootCategories {

    private static final int REMEMBERED = 8;
    private static final ThreadLocal<ArrayDeque<Object[]>> RECENT = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<ChestPos, String> PENDING = new HashMap<>();

    private LootCategories() {}

    public static void remember(WeightedRandomChestContent[] items, String category) {
        var recent = RECENT.get();
        recent.addFirst(new Object[] { items, category });
        if (recent.size() > REMEMBERED) {
            recent.removeLast();
        }
    }

    public static void onFill(WeightedRandomChestContent[] items, IInventory inventory) {
        if (!(inventory instanceof TileEntity tile) || tile.getWorldObj() == null
            || tile.getWorldObj().isRemote
            || !WorldgenTracker.isPopulating()) {
            return;
        }
        for (var entry : RECENT.get()) {
            if (entry[0] == items) {
                synchronized (PENDING) {
                    PENDING.put(
                        new ChestPos(tile.getWorldObj().provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord),
                        (String) entry[1]);
                }
                return;
            }
        }
    }

    public static String take(ChestPos pos) {
        synchronized (PENDING) {
            return PENDING.remove(pos);
        }
    }

    public static void clear() {
        synchronized (PENDING) {
            PENDING.clear();
        }
    }
}
