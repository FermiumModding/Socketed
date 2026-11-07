package socketed.common.config;

import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemType;

import java.util.*;

public abstract class DefaultJsonConfig {
    
    private static final Map<String, GemType> defaultGemTypes = new HashMap<>();
    private static final Map<String, GemCombinationType> defaultGemCombinationTypes = new HashMap<>();

    public static Map<String, GemType> getDefaultGemTypes() {
        return defaultGemTypes;
    }
    
    public static Map<String, GemCombinationType> getDefaultGemCombinationTypes() {
        return defaultGemCombinationTypes;
    }
    
    public static void registerDefaultGemType(String name, GemType gemType) {
        defaultGemTypes.put(name, gemType);
    }
    
    public static void registerDefaultGemCombinationType(String name, GemCombinationType gemCombinationType) {
        defaultGemCombinationTypes.put(name, gemCombinationType);
    }
}