package lu.kolja.lootr;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import lu.kolja.lootr.client.LootrChestItemRenderer;
import lu.kolja.lootr.client.LootrChestRenderer;
import lu.kolja.lootr.tile.LootrChestTileEntity;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.bindTileEntitySpecialRenderer(LootrChestTileEntity.class, new LootrChestRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(chest), new LootrChestItemRenderer());
    }
}
