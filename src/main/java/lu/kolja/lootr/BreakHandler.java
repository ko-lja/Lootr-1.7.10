package lu.kolja.lootr;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lu.kolja.lootr.util.ChestUtil;

public class BreakHandler {

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        var player = event.getPlayer();
        if (event.world.isRemote || event.block != CommonProxy.chest || player == null) {
            return;
        }
        if (player instanceof FakePlayer && LootrConfig.enableFakePlayerBreak) {
            return;
        }
        if (LootrConfig.disableBreak) {
            if (!player.capabilities.isCreativeMode) {
                event.setCanceled(true);
                player
                    .addChatComponentMessage(ChestUtil.message("lootr.message.cannot_break", EnumChatFormatting.AQUA));
            } else if (!player.isSneaking()) {
                event.setCanceled(true);
                player.addChatComponentMessage(
                    ChestUtil.message("lootr.message.cannot_break_sneak", EnumChatFormatting.AQUA));
            }
        } else if (!player.isSneaking()) {
            event.setCanceled(true);
            player.addChatComponentMessage(ChestUtil.message("lootr.message.should_sneak", EnumChatFormatting.AQUA));
            var emphasis = new ChatComponentTranslation("lootr.message.should_sneak3").setChatStyle(
                new ChatStyle().setColor(EnumChatFormatting.AQUA)
                    .setBold(true));
            player.addChatComponentMessage(
                ChestUtil.message("lootr.message.should_sneak2", EnumChatFormatting.AQUA, emphasis));
        }
    }
}
