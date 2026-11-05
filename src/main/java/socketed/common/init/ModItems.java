package socketed.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;
import socketed.Socketed;
import socketed.common.config.ForgeConfig;
import socketed.common.item.ItemSocketGeneric;
import socketed.common.item.ItemSocketTier;


import java.util.ArrayList;
import java.util.List;

public class ModItems {
    

    public static Item ITEM_SOCKET_BENCH;
    public static final List<ItemSocketGeneric> socketItems = new ArrayList<>();

    static {
        if(ForgeConfig.ADD_SOCKETS.registerSocketItems) {
            socketItems.add(new ItemSocketGeneric("socket_generic"));
            for(int i = 0; i <= ForgeConfig.COMMON.maxSocketTier; i++) {
                socketItems.add(new ItemSocketTier("socket_tier_" + i, i));
            }
        }
    }
    
    public static void registerItems(IForgeRegistry<Item> registry) {

        for(ItemSocketGeneric socket : socketItems) {
            registry.register(socket);
        }
        ITEM_SOCKET_BENCH = new ItemBlock(ModBlocks.SOCKETING_BENCH).setRegistryName(Socketed.MODID, "socket_bench").setTranslationKey("socket_bench");
        registry.register(ITEM_SOCKET_BENCH);
    }

    public static void registerModels() {

        for(ItemSocketGeneric socket : socketItems) {
            socket.registerModel();
        }
        ModBlocks.SOCKETING_BENCH.initModel();
    }
}