package lu.kolja.lootr.worldgen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import lu.kolja.lootr.LootrConfig;
import lu.kolja.lootr.tile.LootrChestTileEntity;

public class WorldgenTracker {

    private static final ThreadLocal<int[]> POPULATE_DEPTH = ThreadLocal.withInitial(() -> new int[1]);
    private static final Set<ChestPos> QUEUE = new LinkedHashSet<>();

    public static void beginPopulate() {
        POPULATE_DEPTH.get()[0]++;
    }

    public static void endPopulate() {
        POPULATE_DEPTH.get()[0]--;
    }

    public static boolean isPopulating() {
        return POPULATE_DEPTH.get()[0] > 0;
    }

    public static void onTileEntitySet(World world, int x, int y, int z, TileEntity tile) {
        if (!isPopulating() || world.isRemote
            || !(tile instanceof IInventory)
            || tile instanceof LootrChestTileEntity) {
            return;
        }
        int dimension = world.provider.dimensionId;
        if (!LootrConfig.isDimensionAllowed(dimension) || !LootrConfig.isConvertible(world.getBlock(x, y, z))) {
            return;
        }
        synchronized (QUEUE) {
            QUEUE.add(new ChestPos(dimension, x, y, z));
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        List<ChestPos> pending;
        synchronized (QUEUE) {
            pending = new ArrayList<>(QUEUE);
            QUEUE.clear();
        }
        for (var pos : pending) {
            ChestConverter.convertGenerated(pos);
        }
        LootCategories.clear();
    }
}
