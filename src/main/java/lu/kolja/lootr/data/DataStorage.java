package lu.kolja.lootr.data;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DimensionManager;

import lu.kolja.lootr.LootrConfig;
import lu.kolja.lootr.tile.LootrChestTileEntity;
import lu.kolja.lootr.util.StackUtil;

public final class DataStorage {

    private DataStorage() {}

    public static MapStorage storage() {
        return DimensionManager.getWorld(0).mapStorage;
    }

    private static <T extends WorldSavedData> T getOrCreate(Class<T> type, String name, Function<String, T> factory) {
        var storage = storage();
        var data = type.cast(storage.loadData(type, name));
        if (data == null) {
            data = factory.apply(name);
            storage.setData(name, data);
        }
        return data;
    }

    public static ChestData find(UUID tileId) {
        return (ChestData) storage().loadData(ChestData.class, ChestData.name(tileId));
    }

    public static ChestData get(UUID tileId) {
        return getOrCreate(ChestData.class, ChestData.name(tileId), ChestData::new);
    }

    public static TickingData ticking(String name) {
        return getOrCreate(TickingData.class, name, TickingData::new);
    }

    public static void tickTimers() {
        if (DimensionManager.getWorld(0) != null) {
            ticking(TickingData.DECAY).tick();
            ticking(TickingData.REFRESH).tick();
        }
    }

    public static void refresh(LootrChestTileEntity tile) {
        var data = find(tile.getTileId());
        if (data != null) {
            data.getInventories()
                .clear();
            data.markDirty();
        }
        for (var opener : tile.getOpeners()
            .toArray(new UUID[0])) {
            tile.removeOpener(opener);
        }
    }

    private static void fill(LootrChestTileEntity tile, SpecialChestInventory inventory) {
        var random = new Random();
        var category = tile.getLootCategory();
        if (LootrConfig.rerollFromChestGenHooks && category != null) {
            var items = ChestGenHooks.getItems(category, random);
            if (items.length > 0) {
                WeightedRandomChestContent
                    .generateChestContents(random, items, inventory, ChestGenHooks.getCount(category, random));
                return;
            }
        }
        var template = tile.getTemplate();
        if (template == null) {
            return;
        }
        var contents = StackUtil.copy(template);
        if (LootrConfig.shufflePerPlayer) {
            Collections.shuffle(Arrays.asList(contents), random);
        }
        System.arraycopy(contents, 0, inventory.getContents(), 0, contents.length);
    }

    public static int clearPlayer(UUID player) {
        var storage = storage();
        storage.saveAllData();
        var dir = new File(
            DimensionManager.getWorld(0)
                .getSaveHandler()
                .getWorldDirectory(),
            "data");
        var files = dir.listFiles((d, name) -> name.startsWith(ChestData.PREFIX) && name.endsWith(".dat"));
        int cleared = 0;
        if (files != null) {
            for (var file : files) {
                var name = file.getName();
                var data = (ChestData) storage.loadData(ChestData.class, name.substring(0, name.length() - 4));
                if (data != null && data.getInventories()
                    .remove(player) != null) {
                    data.markDirty();
                    cleared++;
                }
            }
        }
        return cleared;
    }

    public static SpecialChestInventory getInventory(World world, LootrChestTileEntity tile, UUID player) {
        var data = get(tile.getTileId());
        data.setLocation(tile.getTileId(), world.provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord);
        var inventory = data.getInventories()
            .get(player);
        if (inventory == null) {
            inventory = new SpecialChestInventory(data, player, new ItemStack[StackUtil.CHEST_SIZE]);
            fill(tile, inventory);
            data.getInventories()
                .put(player, inventory);
            data.markDirty();
        }
        return inventory;
    }
}
