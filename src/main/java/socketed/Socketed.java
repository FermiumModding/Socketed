package socketed;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.common.command.AddSocketCommand;
import socketed.common.config.JsonConfig;
import socketed.common.container.GuiHandlerSocketing;
import socketed.common.init.ModBlocks;
import socketed.common.init.ModItems;
import socketed.common.init.ModRecipes;
import socketed.common.loot.LootFunctionAddSocketsRandomly;
import socketed.common.loot.LootFunctionSetSockets;
import socketed.common.proxy.IProxy;

@Mod(modid = Socketed.MODID, name = Socketed.MODNAME, version = Socketed.VERSION)
public class Socketed {

    public static final String MODID = "socketed";
    public static final String MODNAME = "Socketed";
    public static final String VERSION = "1.0.0";
    public static Logger LOGGER = LogManager.getLogger();

    @Mod.Instance(MODID)
    public static Socketed instance;

    @SidedProxy(serverSide = "socketed.common.proxy.ServerProxy", clientSide = "socketed.client.proxy.ClientProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        JsonConfig.preInit(event.getModConfigurationDirectory());

        CapabilitySocketableHandler.registerCapability();
        CapabilityEffectsCacheHandler.registerCapability();

        LootFunctionManager.registerFunction(new LootFunctionSetSockets.Serializer());
        LootFunctionManager.registerFunction(new LootFunctionAddSocketsRandomly.Serializer());

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandlerSocketing());
    }
    
    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        JsonConfig.init();
    }
    
    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        JsonConfig.postInit();
    }
    
    @Mod.EventHandler
    public static void serverInit(FMLServerStartingEvent event) {
        event.registerServerCommand(new AddSocketCommand());
    }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            ModItems.registerItems(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            ModBlocks.registerBlocks(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
            ModRecipes.registerRecipes(event.getRegistry());
        }

        @SubscribeEvent
        public static void registerItemModels(ModelRegistryEvent event) {
            ModItems.registerModels();
        }
    }
}