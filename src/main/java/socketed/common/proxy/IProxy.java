package socketed.common.proxy;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

public interface IProxy {

    void registerItemRenderer(Item item, int meta);

    boolean isPrivateSingleplayer(MinecraftServer server);

    void openEditor(Map<String, String> gemTypes, Map<String, String> gemCombinations);
}