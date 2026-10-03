package lu.kolja.lootr.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import lu.kolja.lootr.LootrConfig;
import lu.kolja.lootr.data.DataStorage;
import lu.kolja.lootr.data.TickingData;
import lu.kolja.lootr.tile.LootrChestTileEntity;

public final class ChestUtil {

    private ChestUtil() {}

    public static void handleOpen(World world, int x, int y, int z, EntityPlayerMP player) {
        if (player instanceof FakePlayer || !(world.getTileEntity(x, y, z) instanceof LootrChestTileEntity tile)) {
            return;
        }
        var id = tile.getTileId();
        int dimension = world.provider.dimensionId;

        var decay = DataStorage.ticking(TickingData.DECAY);
        if (decay.isDone(id)) {
            decay.remove(id);
            world.func_147480_a(x, y, z, true);
            player.addChatComponentMessage(alert("lootr.message.decayed", EnumChatFormatting.RED));
            return;
        }
        int decayValue = decay.getValue(id);
        if (decayValue > 0) {
            if (LootrConfig.shouldNotify(decayValue)) {
                player
                    .addChatComponentMessage(alert("lootr.message.decay_in", EnumChatFormatting.RED, decayValue / 20));
            }
        } else if (decayValue == -1 && LootrConfig.isDecaying(dimension)) {
            decay.setValue(id, LootrConfig.decayValue);
            player.addChatComponentMessage(
                alert("lootr.message.decay_start", EnumChatFormatting.RED, LootrConfig.decayValue / 20));
        }

        var refresh = DataStorage.ticking(TickingData.REFRESH);
        if (refresh.isDone(id)) {
            DataStorage.refresh(tile);
            refresh.remove(id);
            player.addChatComponentMessage(alert("lootr.message.refreshed", EnumChatFormatting.BLUE));
        }
        int refreshValue = refresh.getValue(id);
        if (refreshValue > 0) {
            if (LootrConfig.shouldNotify(refreshValue)) {
                player.addChatComponentMessage(
                    alert("lootr.message.refresh_in", EnumChatFormatting.BLUE, refreshValue / 20));
            }
        } else if (refreshValue == -1 && LootrConfig.isRefreshing(dimension)) {
            refresh.setValue(id, LootrConfig.refreshValue);
            player.addChatComponentMessage(
                alert("lootr.message.refresh_start", EnumChatFormatting.BLUE, LootrConfig.refreshValue / 20));
        }

        player.displayGUIChest(DataStorage.getInventory(world, tile, player.getUniqueID()));
    }

    public static void handleSneak(World world, int x, int y, int z, EntityPlayerMP player) {
        if (world.getTileEntity(x, y, z) instanceof LootrChestTileEntity tile) {
            tile.removeOpener(player.getUniqueID());
        }
    }

    public static IChatComponent message(String key, EnumChatFormatting color, Object... args) {
        return new ChatComponentTranslation(key, args).setChatStyle(new ChatStyle().setColor(color));
    }

    private static IChatComponent alert(String key, EnumChatFormatting color, Object... args) {
        return new ChatComponentTranslation(key, args).setChatStyle(
            new ChatStyle().setColor(color)
                .setBold(true));
    }
}
