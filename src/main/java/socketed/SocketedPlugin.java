package socketed;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class SocketedPlugin implements IFMLLoadingPlugin {

    public SocketedPlugin() {
        MixinBootstrap.init();
        FermiumRegistryAPI.enqueueMixin(false, "mixins.socketed.vanilla.json");
        FermiumRegistryAPI.enqueueMixin(true, "mixins.socketed.firstaid.json", () -> FermiumRegistryAPI.isModPresent("firstaid"));
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[0];
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}