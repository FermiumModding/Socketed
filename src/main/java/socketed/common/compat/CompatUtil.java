package socketed.common.compat;

import net.minecraftforge.fml.common.Loader;

public class CompatUtil {
    public static final LoadedContainer firstaid = new LoadedContainer("firstaid");

    public static class LoadedContainer {
        private final String key;
        private Boolean isLoaded = null;

        private LoadedContainer(String key) {
            this.key = key;
        }

        public boolean isLoaded() {
            if (this.isLoaded == null) isLoaded = Loader.isModLoaded(key);
            return isLoaded;
        }
    }
}
