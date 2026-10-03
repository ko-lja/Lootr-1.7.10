package lu.kolja.lootr;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;

public class LootrConfig {

    private static final String CONVERSION = "conversion";
    private static final String BEHAVIOR = "behavior";
    private static final String DECAY = "decay";
    private static final String CLIENT = "client";

    public static String[] convertibleBlocks;
    public static int[] dimensionWhitelist;
    public static int[] dimensionBlacklist;
    public static boolean convertEmptyChests;
    public static boolean rerollFromChestGenHooks;
    public static boolean shufflePerPlayer;

    public static boolean disableBreak;
    public static boolean enableFakePlayerBreak;
    public static boolean blastResistant;
    public static boolean blastImmune;
    public static boolean zeroComparator;

    public static int decayValue;
    public static int refreshValue;
    public static boolean decayAll;
    public static boolean refreshAll;
    public static int[] decayDimensions;
    public static int[] refreshDimensions;
    public static boolean disableNotifications;
    public static int notificationDelay;

    public static boolean vanillaTextures;
    public static int unopenedTint;
    public static int openedTint;

    private static Set<Block> convertible;

    public static void load(File file) {
        var config = new Configuration(file);

        convertibleBlocks = config.getStringList(
            "convertibleBlocks",
            CONVERSION,
            new String[] { "minecraft:chest", "minecraft:trapped_chest" },
            "registry names of blocks whose freshly world-generated, non-empty inventories become Lootr chests (the tile entity must be an IInventory)");
        dimensionWhitelist = config
            .get(CONVERSION, "dimensionWhitelist", new int[0], "if not empty, only convert chests in these dimensions")
            .getIntList();
        dimensionBlacklist = config
            .get(CONVERSION, "dimensionBlacklist", new int[0], "never convert chests in these dimensions")
            .getIntList();
        convertEmptyChests = config.getBoolean(
            "convertEmptyChests",
            CONVERSION,
            false,
            "also convert freshly generated chests that are still empty after generation (for mods that fill chests later; players will see an empty chest)");

        rerollFromChestGenHooks = config.getBoolean(
            "rerollFromChestGenHooks",
            CONVERSION,
            false,
            "chests that were filled from a ChestGenHooks category (vanilla structures and most mods) roll fresh loot from that category for every player instead of copying the generated contents");
        shufflePerPlayer = config.getBoolean(
            "shufflePerPlayer",
            CONVERSION,
            false,
            "shuffle the slots of the copied contents for every player (cosmetic variety)");

        disableBreak = config.getBoolean(
            "disableBreak",
            BEHAVIOR,
            false,
            "prevent breaking Lootr chests, except while sneaking in creative mode");
        enableFakePlayerBreak = config.getBoolean(
            "enableFakePlayerBreak",
            BEHAVIOR,
            false,
            "let fake players break Lootr chests without sneaking, overriding disableBreak for them");
        blastResistant = config
            .getBoolean("blastResistant", BEHAVIOR, false, "Lootr chests resist creeper and TNT explosions");
        blastImmune = config
            .getBoolean("blastImmune", BEHAVIOR, false, "Lootr chests cannot be destroyed by any explosion");
        zeroComparator = config
            .getBoolean("zeroComparator", BEHAVIOR, false, "comparators read 0 from Lootr chests instead of 1");

        decayValue = config.getInt(
            "decayValue",
            DECAY,
            5 * 60 * 20,
            0,
            Integer.MAX_VALUE,
            "ticks a decaying chest lasts after it is first opened");
        refreshValue = config.getInt(
            "refreshValue",
            DECAY,
            20 * 60 * 20,
            0,
            Integer.MAX_VALUE,
            "ticks after the first opening before a refreshing chest gets fresh contents for everyone");
        decayAll = config
            .getBoolean("decayAll", DECAY, false, "every Lootr chest decays after it is opened for the first time");
        refreshAll = config.getBoolean(
            "refreshAll",
            DECAY,
            false,
            "every Lootr chest refreshes after it is opened for the first time");
        decayDimensions = config.get(DECAY, "decayDimensions", new int[0], "dimensions whose Lootr chests decay")
            .getIntList();
        refreshDimensions = config.get(DECAY, "refreshDimensions", new int[0], "dimensions whose Lootr chests refresh")
            .getIntList();
        disableNotifications = config.getBoolean(
            "disableNotifications",
            DECAY,
            false,
            "do not tell players how long a chest has left before it decays or refreshes");
        notificationDelay = config.getInt(
            "notificationDelay",
            DECAY,
            30 * 20,
            -1,
            Integer.MAX_VALUE,
            "only notify when fewer than this many ticks remain (-1 means always notify)");

        vanillaTextures = config.getBoolean(
            "vanillaTextures",
            CLIENT,
            false,
            "render Lootr chests like vanilla chests (no tint, no opened state)");
        unopenedTint = parseColor(
            config.getString("unopenedTint", CLIENT, "E0B040", "RGB hex tint of a Lootr chest you have not opened yet"),
            0xE0B040);
        openedTint = parseColor(
            config.getString("openedTint", CLIENT, "8C8C8C", "RGB hex tint of a Lootr chest you have already opened"),
            0x8C8C8C);

        convertible = null;
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static boolean isConvertible(Block block) {
        if (convertible == null) {
            var blocks = new HashSet<Block>();
            for (var name : convertibleBlocks) {
                var resolved = Block.getBlockFromName(name.trim());
                if (resolved == null || resolved == Blocks.air) {
                    Lootr.LOG.warn("Unknown block '{}' in lootr convertibleBlocks", name);
                } else {
                    blocks.add(resolved);
                }
            }
            convertible = blocks;
        }
        return convertible.contains(block);
    }

    public static boolean isDimensionAllowed(int dimension) {
        return (dimensionWhitelist.length == 0 || contains(dimensionWhitelist, dimension))
            && !contains(dimensionBlacklist, dimension);
    }

    public static boolean isDecaying(int dimension) {
        return decayAll || contains(decayDimensions, dimension);
    }

    public static boolean isRefreshing(int dimension) {
        return refreshAll || contains(refreshDimensions, dimension);
    }

    public static boolean shouldNotify(int remaining) {
        return !disableNotifications && (notificationDelay == -1 || remaining <= notificationDelay);
    }

    private static boolean contains(int[] values, int value) {
        for (int v : values) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }

    private static int parseColor(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim(), 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            Lootr.LOG.warn("Invalid color '{}' in lootr config, using {}", value, Integer.toHexString(fallback));
            return fallback;
        }
    }
}
