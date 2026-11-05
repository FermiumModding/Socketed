package socketed.client.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.model.ModelLoader;
import socketed.client.gui.editor.GuiSocketedEditor;
import socketed.common.proxy.IProxy;

import java.util.Map;

public class ClientProxy implements IProxy {

    @Override
    public void registerItemRenderer(Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Override
    public boolean isPrivateSingleplayer(MinecraftServer server) {
        return server instanceof IntegratedServer && !((IntegratedServer)server).getPublic();
    }

    @Override
    public void openEditor(Map<String, String> gemTypes, Map<String, String> gemCombinations) {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiSocketedEditor(gemTypes, gemCombinations)));
    }
}