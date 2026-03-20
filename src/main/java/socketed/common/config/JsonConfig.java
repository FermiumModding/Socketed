package socketed.common.config;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NBTTagCompound;
import socketed.Socketed;
import socketed.api.socket.GenericSocket;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.TieredSocket;
import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemType;
import socketed.common.socket.gem.effect.AttributeGemEffect;
import socketed.common.socket.gem.effect.EffectDeserializer;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.common.socket.gem.effect.PlusEnchantmentGemEffect;
import socketed.common.socket.gem.effect.activatable.*;
import socketed.common.socket.gem.effect.activatable.activator.*;
import socketed.common.socket.gem.effect.activatable.condition.*;
import socketed.common.socket.gem.effect.activatable.target.*;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;
import socketed.common.socket.gem.filter.FilterDeserializer;
import socketed.api.socket.gem.filter.GenericFilter;
import socketed.common.socket.gem.filter.ItemFilter;
import socketed.common.socket.gem.filter.OreFilter;
import socketed.api.socket.gem.util.RandomValueRange;
import socketed.api.util.SocketedUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

public class JsonConfig {
    
    private static File gemTypesFolder;
    private static File gemCombinationFolder;
    
    private static final Map<String, GemType> gemTypesDataMap = new HashMap<>();
    private static final Map<String, GemCombinationType> gemCombinationDataMap = new HashMap<>();
    private static List<GemCombinationType> sortedGemCombinationDataList = null;
    private static List<GemType> sortedGemDataList = null;

    public static final Map<String, Class<? extends GenericFilter>> filterDeserializerMap = new HashMap<>();
    public static final Map<String, Class<? extends GenericGemEffect>> gemEffectDeserializerMap = new HashMap<>();
    public static final Map<String, Class<? extends GenericActivator>> activatorDeserializerMap = new HashMap<>();
    public static final Map<String, Class<? extends GenericTarget>> targetDeserializerMap = new HashMap<>();
    public static final Map<String, Class<? extends GenericCondition>> conditionDeserializerMap = new HashMap<>();
    public static final Map<String, Class<? extends ISlotType>> slotTypeDeserializerMap = new HashMap<>();
    public static final Map<String, Function<NBTTagCompound, ? extends GenericSocket>> socketNBTDeserializerMap = new HashMap<>();
    
    public static boolean completedLoadingGems = false;
    public static boolean completedLoadingCombinations = false;

    public static void preInit(File file) {
        File modFolder = new File(file, Socketed.MODID);
        if(!modFolder.exists() || !modFolder.isDirectory()) {
            if(!modFolder.mkdir()) {
                Socketed.LOGGER.error("Could not create general configuration folder");
                return;
            }
        }

        gemTypesFolder = new File(modFolder, "gemtypes");
        if(!gemTypesFolder.exists() || !gemTypesFolder.isDirectory()) {
            if(!gemTypesFolder.mkdir()) {
                Socketed.LOGGER.error("Could not create gem type configuration folder");
            }
        }

        gemCombinationFolder = new File(modFolder, "gemcombinations");
        if(!gemCombinationFolder.exists() || !gemCombinationFolder.isDirectory()) {
            if(!gemCombinationFolder.mkdir()) {
                Socketed.LOGGER.error("Could not create gem combination type configuration folder");
            }
        }
    }
    
    public static void init() {
        SocketedUtil.registerFilterType(ItemFilter.TYPE_NAME, ItemFilter.class, Socketed.MODID);
        SocketedUtil.registerFilterType(OreFilter.TYPE_NAME, OreFilter.class, Socketed.MODID);
        
        SocketedUtil.registerEffectType(AttributeGemEffect.TYPE_NAME, AttributeGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(PotionGemEffect.TYPE_NAME, PotionGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(IgniteGemEffect.TYPE_NAME, IgniteGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(KnockbackGemEffect.TYPE_NAME, KnockbackGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(UndyingTotemGemEffect.TYPE_NAME, UndyingTotemGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(CancelEventGemEffect.TYPE_NAME, CancelEventGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(MultiEffectGemEffect.TYPE_NAME, MultiEffectGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(ExtraIFrameGemEffect.TYPE_NAME, ExtraIFrameGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(BypassIFrameGemEffect.TYPE_NAME, BypassIFrameGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(ExplosionGemEffect.TYPE_NAME, ExplosionGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(LoseTargetGemEffect.TYPE_NAME, LoseTargetGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(PlusEnchantmentGemEffect.TYPE_NAME, PlusEnchantmentGemEffect.class, Socketed.MODID);
        SocketedUtil.registerEffectType(ExecuteCommandEffect.TYPE_NAME, ExecuteCommandEffect.class, Socketed.MODID);
        
        SocketedUtil.registerActivator(PassiveActivator.TYPE_NAME, PassiveActivator.class, Socketed.MODID);
        SocketedUtil.registerActivator(AttackingActivator.TYPE_NAME, AttackingActivator.class, Socketed.MODID);
        SocketedUtil.registerActivator(AttackedActivator.TYPE_NAME, AttackedActivator.class, Socketed.MODID);
        SocketedUtil.registerActivator(DeathTotemCheckActivator.TYPE_NAME, DeathTotemCheckActivator.class, Socketed.MODID);
        SocketedUtil.registerActivator(MultiEffectActivator.TYPE_NAME, MultiEffectActivator.class, Socketed.MODID);
        SocketedUtil.registerActivator(TargetedActivator.TYPE_NAME, TargetedActivator.class, Socketed.MODID);
        
        SocketedUtil.registerTarget(SelfTarget.TYPE_NAME, SelfTarget.class, Socketed.MODID);
        SocketedUtil.registerTarget(OtherTarget.TYPE_NAME, OtherTarget.class, Socketed.MODID);
        SocketedUtil.registerTarget(SelfAOETarget.TYPE_NAME, SelfAOETarget.class, Socketed.MODID);
        SocketedUtil.registerTarget(OtherAOETarget.TYPE_NAME, OtherAOETarget.class, Socketed.MODID);
        
        SocketedUtil.registerCondition(MultiCondition.TYPE_NAME, MultiCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(ChanceCondition.TYPE_NAME, ChanceCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(PotionActiveCondition.TYPE_NAME, PotionActiveCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(HealthPercentCondition.TYPE_NAME, HealthPercentCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(ElevationCondition.TYPE_NAME, ElevationCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(IsBurningCondition.TYPE_NAME, IsBurningCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(IsWetCondition.TYPE_NAME, IsWetCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(LightLevelCondition.TYPE_NAME, LightLevelCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(IsBossCondition.TYPE_NAME, IsBossCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(BiomeCondition.TYPE_NAME, BiomeCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(BiomeTypeCondition.TYPE_NAME, BiomeTypeCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(DamageSourceCondition.TYPE_NAME, DamageSourceCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(InvertedCondition.TYPE_NAME, InvertedCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(IsSpecificEntityCondition.TYPE_NAME, IsSpecificEntityCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(DistanceCondition.TYPE_NAME, DistanceCondition.class, Socketed.MODID);
        SocketedUtil.registerCondition(AttackCooldownCondition.TYPE_NAME, AttackCooldownCondition.class, Socketed.MODID);
        
        SocketedUtil.registerSlotTypes(SocketedSlotTypes.class, Socketed.MODID);
        
        SocketedUtil.registerSocket(GenericSocket.TYPE_NAME, GenericSocket::new, Socketed.MODID);
        SocketedUtil.registerSocket(TieredSocket.TYPE_NAME, TieredSocket::new, Socketed.MODID);
        
        DefaultJsonConfig.initializeBuiltinEntries();
    }

    public static void postInit() {
        loadGemTypeData();
        completedLoadingGems = true;
        loadGemCombinationData();
        completedLoadingCombinations = true;
    }

    public static Map<String, GemType> getGemData() {
        return gemTypesDataMap;
    }
    
    public static Map<String, GemCombinationType> getGemCombinationData() {
        return gemCombinationDataMap;
    }

    private static void initDefaultGemTypes() {
        Socketed.LOGGER.info("Initializing default Socketed gem type configs");
        Map<String, GemType> defaultData = DefaultJsonConfig.getDefaultGemTypes();
        for(Map.Entry<String, GemType> entry : defaultData.entrySet()) {
            try {
                Gson gson = new GsonBuilder().registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).setPrettyPrinting().create();
                JsonElement elem = gson.toJsonTree(entry.getValue());
                String entryString = gson.toJson(elem);
                File file = new File(gemTypesFolder, String.format("%s.json", entry.getKey()));
                if(!file.createNewFile()) Socketed.LOGGER.error("Failed to create new gem type file, " + entry.getKey());
                else if(!file.setWritable(true)) Socketed.LOGGER.error("Failed to set new gem type file writeable, " + entry.getKey());
                else {
                    PrintWriter writer = new PrintWriter(file);
                    writer.write(entryString);
                    writer.flush();
                    writer.close();
                }
            }
            catch(Exception e) {
                Socketed.LOGGER.error("Failed to generate default gem type file, " + entry.getKey() + ", " + e);
            }
        }
    }

    private static void initDefaultGemCombinations() {
        Socketed.LOGGER.info("Initializing default Socketed gem combination type configs");
        Map<String, GemCombinationType> defaultCombinationData = DefaultJsonConfig.getDefaultGemCombinationTypes();
        for(Map.Entry<String, GemCombinationType> entry : defaultCombinationData.entrySet()) {
            try {
                Gson gson = new GsonBuilder().registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).setPrettyPrinting().create();
                JsonElement elem = gson.toJsonTree(entry.getValue());
                String entryString = gson.toJson(elem);
                File file = new File(gemCombinationFolder, String.format("%s.json", entry.getKey()));
                if(!file.createNewFile()) Socketed.LOGGER.error("Failed to create new gem combination type file, " + entry.getKey());
                else if(!file.setWritable(true)) Socketed.LOGGER.error("Failed to set new gem combination type file writeable, " + entry.getKey());
                else {
                    PrintWriter writer = new PrintWriter(file);
                    writer.write(entryString);
                    writer.flush();
                    writer.close();
                }
            }
            catch(Exception e) {
                Socketed.LOGGER.error("Failed to generate default gem combination type file, " + entry.getKey() + ", " + e);
            }
        }
    }

    private static void loadGemTypeData() {
        Socketed.LOGGER.info("=== Starting Socketed Gem Type Data Loading ===");

        if(gemTypesFolder == null || !gemTypesFolder.exists() || !gemTypesFolder.isDirectory()) {
            Socketed.LOGGER.error("Failed to load gem type config, folder does not exist");
            Socketed.LOGGER.info("=== Finishing Socketed Gem Type Data Loading ===");
            return;
        }

        try {
            FilterDeserializer filterDeserializer = new FilterDeserializer();
            for(Map.Entry<String, Class<? extends GenericFilter>> entry : filterDeserializerMap.entrySet()) {
                filterDeserializer.registerType(entry.getKey(), entry.getValue());
            }
            EffectDeserializer effectDeserializer = new EffectDeserializer();
            for(Map.Entry<String, Class<? extends GenericGemEffect>> entry : gemEffectDeserializerMap.entrySet()) {
                effectDeserializer.registerType(entry.getKey(), entry.getValue());
            }
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(GenericFilter.class, filterDeserializer)
                    .registerTypeAdapter(GenericGemEffect.class, effectDeserializer)
                    .create();

            File[] files = gemTypesFolder.listFiles();
            if(files == null) {
                Socketed.LOGGER.error("Failed to load gem type config, folder is invalid");
                Socketed.LOGGER.info("=== Finishing Socketed Gem Type Data Loading ===");
                return;
            }
            if(files.length == 0) {
                initDefaultGemTypes();
                files = gemTypesFolder.listFiles();
                if(files == null || files.length == 0) {
                    Socketed.LOGGER.error("Failed to load default gem type config, folder is invalid");
                    Socketed.LOGGER.info("=== Finishing Socketed Gem Type Data Loading ===");
                    return;
                }
            }
            if(ForgeConfig.COMMON.onlyUseDefaults) {
                Map<String, GemType> defaultMap = DefaultJsonConfig.getDefaultGemTypes();
                for(Map.Entry<String, GemType> entry : defaultMap.entrySet()) {
                    GemType gemType = entry.getValue();
                    gemType.setName(entry.getKey());
                    if(gemType.validate()) gemTypesDataMap.put(gemType.getName(), gemType);
                    else Socketed.LOGGER.warn("Failed to load default gem type, validation failed: " + gemType.getName());
                }
            }
            else {
                for(File file : files) {
                    if(file.isDirectory()) continue;
                    JsonElement elem = getJson(file);
                    if(elem == null) {
                        Socketed.LOGGER.warn("Failed to parse gem type config file: " + file.getName());
                        continue;
                    }
                    try {
                        GemType gemType = gson.fromJson(elem, GemType.class);
                        if(gemType == null) Socketed.LOGGER.warn("Failed to load gem type config file, invalid file: " + file.getName());
                        else {
                            gemType.setName(file.getName().split("\\.json")[0]);
                            if(gemType.validate()) gemTypesDataMap.put(gemType.getName(), gemType);
                            else Socketed.LOGGER.warn("Failed to load gem type config file, validation failed: " + file.getName());
                        }
                    }
                    catch(Exception e) {
                        Socketed.LOGGER.warn("Failed to load gem type config file: " + file.getName() + ", " + e);
                    }
                }
            }
        }
        catch(Exception e) {
            Socketed.LOGGER.error("Failed to load gem type config: " + e);
        }

        Socketed.LOGGER.info("=== Finishing Socketed Gem Type Data Loading ===");
    }

    private static void loadGemCombinationData() {
        Socketed.LOGGER.info("=== Starting Socketed Gem Combination Type Data Loading ===");

        if(gemCombinationFolder == null || !gemCombinationFolder.exists() || !gemCombinationFolder.isDirectory()) {
            Socketed.LOGGER.error("Failed to load gem combination type config, folder does not exist");
            Socketed.LOGGER.info("=== Finishing Socketed Gem Combination Type Data Loading ===");
            return;
        }

        try {
            FilterDeserializer filterDeserializer = new FilterDeserializer();
            for(Map.Entry<String, Class<? extends GenericFilter>> entry : filterDeserializerMap.entrySet()) {
                filterDeserializer.registerType(entry.getKey(), entry.getValue());
            }
            EffectDeserializer effectDeserializer = new EffectDeserializer();
            for(Map.Entry<String, Class<? extends GenericGemEffect>> entry : gemEffectDeserializerMap.entrySet()) {
                effectDeserializer.registerType(entry.getKey(), entry.getValue());
            }
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(GenericFilter.class, filterDeserializer)
                    .registerTypeAdapter(GenericGemEffect.class, effectDeserializer)
                    .create();
            
            File[] files = gemCombinationFolder.listFiles();
            if(files == null) {
                Socketed.LOGGER.error("Failed to load gem combination type config, folder is invalid");
                Socketed.LOGGER.info("=== Finishing Socketed Gem Combination Type Data Loading ===");
                return;
            }
            if(files.length == 0) {
                initDefaultGemCombinations();
                files = gemCombinationFolder.listFiles();
                if(files == null || files.length == 0) {
                    Socketed.LOGGER.error("Failed to load gem combination type config, folder is invalid");
                    Socketed.LOGGER.info("=== Finishing Socketed Gem Combination Type Data Loading ===");
                    return;
                }
            }
            if(ForgeConfig.COMMON.onlyUseDefaults) {
                Map<String, GemCombinationType> defaultMap = DefaultJsonConfig.getDefaultGemCombinationTypes();
                for(Map.Entry<String, GemCombinationType> entry : defaultMap.entrySet()) {
                    GemCombinationType gemCombination = entry.getValue();
                    gemCombination.setName(entry.getKey());
                    if(gemCombination.validate()) gemCombinationDataMap.put(gemCombination.getName(), gemCombination);
                    else Socketed.LOGGER.warn("Failed to load default gem combination type, validation failed: " + gemCombination.getName());
                }
            }
            else {
                for(File file : files) {
                    if(file.isDirectory()) continue;
                    JsonElement elem = getJson(file);
                    if(elem == null) {
                        Socketed.LOGGER.warn("Failed to parse gem combination type config file: " + file.getName());
                        continue;
                    }
                    try {
                        GemCombinationType gemCombination = gson.fromJson(elem, GemCombinationType.class);
                        if(gemCombination == null) Socketed.LOGGER.warn("Failed to load gem combination type config file, invalid file: " + file.getName());
                        else {
                            gemCombination.setName(file.getName().split("\\.json")[0]);
                            if(gemCombination.validate()) gemCombinationDataMap.put(gemCombination.getName(), gemCombination);
                            else Socketed.LOGGER.warn("Failed to load gem combination type config file, validation failed: " + file.getName());
                        }
                    }
                    catch(Exception e) {
                        Socketed.LOGGER.warn("Failed to load gem combination type config file: " + file.getName() + ", " + e);
                    }
                }
            }
        }
        catch(Exception e) {
            Socketed.LOGGER.error("Failed to load gem combination type config: " + e);
        }

        Socketed.LOGGER.info("=== Finishing Socketed Gem Combination Type Data Loading ===");
    }

    @Nullable
    @SuppressWarnings("UnstableApiUsage")
    private static JsonElement getJson(File file) {
        if(file == null || !file.exists()) {
            Socketed.LOGGER.warn("Failed to load Socketed config file, file does not exist");
            return null;
        }

        try {
            if(!file.setReadable(true)) {
                Socketed.LOGGER.warn("Failed to load Socketed config file, no permission to read the file: " + file.getName());
                return null;
            }
            String fileString = Files.toString(file, Charset.defaultCharset());
            return new JsonParser().parse(fileString);
        }
        catch(Exception e) {
            Socketed.LOGGER.warn("Failed to load Socketed config file: " + file.getName() + ", " + e);
        }
        return null;
    }
    
    //Order matters bc a list of gems might fit multiple gem combinations. more strict combinations will be found first and higher gem count combinations too
    public static List<GemCombinationType> getSortedGemCombinationData() {
        if(sortedGemCombinationDataList == null) {
            sortedGemCombinationDataList = new ArrayList<>(gemCombinationDataMap.values());
            sortedGemCombinationDataList.sort(Comparator.comparingInt(v -> - v.getGemTypes().size()));
            sortedGemCombinationDataList.sort((v1,v2) -> Boolean.compare(!v1.getIsStrictOrder(),!v2.getIsStrictOrder()));
            sortedGemCombinationDataList.sort((v1,v2) -> Boolean.compare(!v1.getIsStrictSocketCount(),!v2.getIsStrictSocketCount()));
        }
        return sortedGemCombinationDataList;
    }
    
    //One itemstack may be valid for multiple filters, so sort by tier to prefer higher tier gemtypes first
    public static List<GemType> getSortedGemDataList() {
        if(sortedGemDataList == null) {
            sortedGemDataList = new ArrayList<>(gemTypesDataMap.values());
            sortedGemDataList.sort(Comparator.comparingInt(v -> - v.getTier()));
        }
        return sortedGemDataList;
    }
}