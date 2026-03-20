package socketed.common.init;


import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import socketed.Socketed;
import socketed.common.block.BlockSocketing;

public class ModBlocks {

    @GameRegistry.ObjectHolder(Socketed.MODID + ":socket_bench")
    public static final BlockSocketing SOCKETING_BENCH = new BlockSocketing("socket_bench");

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        registry.register(SOCKETING_BENCH);
    }
}
