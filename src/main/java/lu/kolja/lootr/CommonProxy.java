package lu.kolja.lootr;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import lu.kolja.lootr.block.LootrChestBlock;
import lu.kolja.lootr.command.LootrCommand;
import lu.kolja.lootr.data.DataStorage;
import lu.kolja.lootr.tile.LootrChestTileEntity;
import lu.kolja.lootr.worldgen.ChestConverter;
import lu.kolja.lootr.worldgen.WorldgenTracker;

public class CommonProxy {

    public static LootrChestBlock chest;

    public void preInit(FMLPreInitializationEvent event) {
        LootrConfig.load(event.getSuggestedConfigurationFile());
        chest = new LootrChestBlock();
        GameRegistry.registerBlock(chest, "lootr_chest");
        GameRegistry.registerTileEntity(LootrChestTileEntity.class, "lootr:chest");
    }

    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(new WorldgenTracker());
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(new ChestConverter());
        MinecraftForge.EVENT_BUS.register(new BreakHandler());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DataStorage.tickTimers();
        }
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new LootrCommand());
    }
}
