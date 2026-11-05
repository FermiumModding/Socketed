package socketed.common.config;

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import socketed.Socketed;
import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemType;
import socketed.api.socket.gem.effect.GenericGemEffect;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class JsonEditorStorage {
    private static final Pattern VALID_NEW_NAME = Pattern.compile("[A-Za-z0-9_\\-]+");
    private static final String EXTENSION = ".json";

    private static File getFolder(boolean combination) {
        return combination ? JsonConfig.getGemCombinationFolder() : JsonConfig.getGemTypesFolder();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Map<String, String> readAll(boolean combination) {
        Map<String, String> map = new HashMap<>();
        File folder = getFolder(combination);
        File[] files = folder == null ? null : folder.listFiles();
        if(files == null) return map;
        for(File file : files) {
            if(file.isDirectory() || !file.getName().endsWith(EXTENSION)) continue;
            try {
                map.put(file.getName().substring(0, file.getName().length() - EXTENSION.length()), Files.toString(file, StandardCharsets.UTF_8));
            }
            catch(Exception e) {
                Socketed.LOGGER.warn("Failed to read file for editor: " + file.getName() + ", " + e);
            }
        }
        return map;
    }

    @Nullable
    @SuppressWarnings("UnstableApiUsage")
    public static String save(boolean combination, boolean isNew, String name, String json) {
        File folder = getFolder(combination);
        if(folder == null) return "Config folder does not exist";

        String[] existing = folder.list();
        boolean exists = existing != null && Arrays.asList(existing).contains(name + EXTENSION);
        if(isNew) {
            if(!VALID_NEW_NAME.matcher(name).matches()) return "Name can only contain letters, numbers, _ and -";
            if(exists || new File(folder, name + EXTENSION).exists()) return "Name is already used";
        }
        else if(!exists) return "File no longer exists";

        JsonObject object;
        try {
            object = new JsonParser().parse(json).getAsJsonObject();
            String error = combination ? validateCombination(name, object) : validateGemType(name, object);
            if(error != null) return error;
        }
        catch(Exception e) {
            return "Invalid data: " + e;
        }

        try {
            String output = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(object);
            File temp = File.createTempFile("socketed_editor", ".tmp", folder.getParentFile());
            Files.write(output.getBytes(StandardCharsets.UTF_8), temp);
            java.nio.file.Files.move(temp.toPath(), new File(folder, name + EXTENSION).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return null;
        }
        catch(Exception e) {
            Socketed.LOGGER.error("Failed to save editor file: " + name + ", " + e);
            return "Failed to write file: " + e;
        }
    }

    @Nullable
    private static String validateGemType(String name, JsonObject object) {
        GemType gemType = JsonConfig.createGson().fromJson(object, GemType.class);
        gemType.setName(name);
        if(!gemType.validate()) return "Gem type is not valid, see the log for details";
        if(gemType.getEffects().size() != size(object, "Effects")) return "An effect is not valid, see the log for details";
        if(gemType.getFilters().size() != size(object, "Filters")) return "A filter is not valid, see the log for details";
        return null;
    }

    @Nullable
    private static String validateCombination(String name, JsonObject object) {
        GemCombinationType combination = JsonConfig.createGson().fromJson(object, GemCombinationType.class);
        combination.setName(name);

        if(combination.getDisplayName() == null || combination.getDisplayName().isEmpty()) return "Display name is required";
        List<String> gemTypes = combination.getGemTypes();
        if(gemTypes == null || gemTypes.isEmpty()) return "At least one gem type is required";
        Map<String, String> gemFiles = readAll(false);
        for(String gemType : gemTypes) {
            if(!gemFiles.containsKey(gemType) && !JsonConfig.getGemData().containsKey(gemType)) return "Gem type does not exist: " + gemType;
        }
        List<GenericGemEffect> effects = combination.getEffects();
        if(effects == null || effects.isEmpty()) return "At least one effect is required";
        for(GenericGemEffect effect : effects) {
            if(effect == null || !effect.validate()) return "An effect is not valid, see the log for details";
        }
        return null;
    }

    private static int size(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element instanceof JsonArray ? ((JsonArray)element).size() : 0;
    }
}