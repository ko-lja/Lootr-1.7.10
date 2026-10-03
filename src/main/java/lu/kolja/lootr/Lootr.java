package lu.kolja.lootr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Lootr.MODID, version = Tags.VERSION, name = "Lootr", acceptedMinecraftVersions = "[1.7.10]")
public class Lootr {

    public static final String MODID = "lootr";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "lu.kolja.lootr.ClientProxy", serverSide = "lu.kolja.lootr.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
