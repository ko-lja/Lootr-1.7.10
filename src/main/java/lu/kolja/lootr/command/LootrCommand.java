package lu.kolja.lootr.command;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import lu.kolja.lootr.data.DataStorage;
import lu.kolja.lootr.tile.LootrChestTileEntity;
import lu.kolja.lootr.worldgen.ChestConverter;

public class LootrCommand extends CommandBase {

    private static final String USAGE = "lootr.commands.usage";
    private static final String[] SUBCOMMANDS = { "custom", "clear", "openers", "reset", "info" };

    @Override
    public String getCommandName() {
        return "lootr";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        if (args.length == 2 && args[0].equals("clear")) {
            return getListOfStringsMatchingLastWord(
                args,
                MinecraftServer.getServer()
                    .getAllUsernames());
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return args.length >= 1 && args[0].equals("clear") && index == 1;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(USAGE);
        }
        switch (args[0]) {
            case "custom" -> custom(sender);
            case "clear" -> clear(sender, args);
            case "openers" -> openers(sender, lootrChest(sender, args));
            case "reset" -> reset(sender, lootrChest(sender, args));
            case "info" -> info(sender, lootrChest(sender, args));
            default -> throw new WrongUsageException(USAGE);
        }
    }

    private static void custom(ICommandSender sender) {
        var player = getCommandSenderAsPlayer(sender);
        var world = player.worldObj;
        var target = findTarget(player, ChestConverter::isConvertibleChest);
        if (target == null) {
            throw new CommandException("lootr.commands.custom.none");
        }
        int x = target[0], y = target[1], z = target[2];
        var inventory = (IInventory) world.getTileEntity(x, y, z);
        var template = ChestConverter.snapshot(inventory);
        var tile = ChestConverter
            .convert(world, x, y, z, inventory, template, ChestConverter.isTrapped(world.getBlock(x, y, z)));
        if (tile == null) {
            throw new CommandException("lootr.commands.custom.failed");
        }
        sender.addChatMessage(new ChatComponentTranslation("lootr.commands.custom.done", x, y, z, countStacks(tile)));
    }

    private static void clear(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException(USAGE);
        }
        var player = resolvePlayer(args[1]);
        if (player == null) {
            throw new CommandException("lootr.commands.clear.unknown", args[1]);
        }
        int cleared = DataStorage.clearPlayer(player);
        sender.addChatMessage(new ChatComponentTranslation("lootr.commands.clear.done", cleared, args[1]));
    }

    private static void openers(ICommandSender sender, LootrChestTileEntity tile) {
        var openers = tile.getOpeners();
        sender.addChatMessage(
            new ChatComponentTranslation(
                "lootr.commands.openers.header",
                tile.xCoord,
                tile.yCoord,
                tile.zCoord,
                openers.size()));
        var cache = MinecraftServer.getServer()
            .func_152358_ax();
        for (var opener : openers) {
            var profile = cache.func_152652_a(opener);
            sender.addChatMessage(
                new ChatComponentText(opener + " (" + (profile == null ? "?" : profile.getName()) + ")"));
        }
    }

    private static void reset(ICommandSender sender, LootrChestTileEntity tile) {
        var data = DataStorage.find(tile.getTileId());
        int removed = data == null ? 0
            : data.getInventories()
                .size();
        DataStorage.refresh(tile);
        sender.addChatMessage(
            new ChatComponentTranslation("lootr.commands.reset.done", tile.xCoord, tile.yCoord, tile.zCoord, removed));
    }

    private static void info(ICommandSender sender, LootrChestTileEntity tile) {
        var data = DataStorage.find(tile.getTileId());
        sender.addChatMessage(
            new ChatComponentTranslation(
                "lootr.commands.info",
                tile.xCoord,
                tile.yCoord,
                tile.zCoord,
                tile.getTileId()
                    .toString(),
                countStacks(tile),
                data == null ? 0
                    : data.getInventories()
                        .size(),
                tile.getOpeners()
                    .size(),
                tile.isTrapped() ? "yes" : "no",
                tile.getLootCategory() == null ? "-" : tile.getLootCategory()));
    }

    private static LootrChestTileEntity lootrChest(ICommandSender sender, String[] args) {
        var world = sender.getEntityWorld();
        int[] target;
        if (args.length >= 4) {
            var coords = sender.getPlayerCoordinates();
            target = new int[] { MathHelper.floor_double(func_110666_a(sender, coords.posX, args[1])),
                MathHelper.floor_double(func_110666_a(sender, coords.posY, args[2])),
                MathHelper.floor_double(func_110666_a(sender, coords.posZ, args[3])) };
        } else {
            target = findTarget(getCommandSenderAsPlayer(sender), tile -> tile instanceof LootrChestTileEntity);
        }
        if (target != null
            && world.getTileEntity(target[0], target[1], target[2]) instanceof LootrChestTileEntity tile) {
            return tile;
        }
        throw new CommandException("lootr.commands.none");
    }

    private static int[] findTarget(EntityPlayerMP player, Predicate<TileEntity> matches) {
        var world = player.worldObj;
        var eye = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        var look = player.getLookVec();
        var hit = world.rayTraceBlocks(eye, eye.addVector(look.xCoord * 5.0D, look.yCoord * 5.0D, look.zCoord * 5.0D));
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
            && matches.test(world.getTileEntity(hit.blockX, hit.blockY, hit.blockZ))) {
            return new int[] { hit.blockX, hit.blockY, hit.blockZ };
        }
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);
        for (int dy = 0; dy >= -1; dy--) {
            if (matches.test(world.getTileEntity(x, y + dy, z))) {
                return new int[] { x, y + dy, z };
            }
        }
        return null;
    }

    private static UUID resolvePlayer(String name) {
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            var profile = MinecraftServer.getServer()
                .func_152358_ax()
                .func_152655_a(name);
            return profile == null ? null : profile.getId();
        }
    }

    private static int countStacks(LootrChestTileEntity tile) {
        int stacks = 0;
        if (tile.getTemplate() != null) {
            for (var stack : tile.getTemplate()) {
                if (stack != null) {
                    stacks++;
                }
            }
        }
        return stacks;
    }
}
