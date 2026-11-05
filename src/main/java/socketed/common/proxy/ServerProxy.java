package socketed.common.proxy;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

public class ServerProxy implements IProxy {

    @Override
    public void registerItemRenderer(Item item, int meta) { }

    @Override
    public boolean isPrivateSingleplayer(MinecraftServer server) {
        return false;
    }

    @Override
    public void openEditor(Map<String, String> gemTypes, Map<String, String> gemCombinations) { }
}