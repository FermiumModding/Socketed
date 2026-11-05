package socketed.client.gui.editor;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.socket.gem.filter.GenericFilter;
import socketed.api.socket.gem.util.RandomValueRange;
import socketed.common.config.JsonConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describes how json values should be edited, built by reflection from the same classes and registries used for loading
 * This allows types registered by addons to be edited without any additional handling
 */
@SideOnly(Side.CLIENT)
public class EditorSchema {
    public enum Kind { TEXT, INTEGER, DECIMAL, RANGE, BOOLEAN, CHOICE, POLYMORPHIC, LIST, OBJECT, RAW }

    public final Kind kind;
    //CHOICE
    public final List<String> choices;
    //POLYMORPHIC
    public final String typeField;
    public final Map<String, ? extends Class<?>> types;
    //LIST
    public final EditorSchema element;
    //OBJECT
    public final Class<?> objectClass;
    //Color of the selected type of POLYMORPHIC
    public final int highlight;

    private EditorSchema(Kind kind, List<String> choices, String typeField, Map<String, ? extends Class<?>> types, EditorSchema element, Class<?> objectClass, int highlight) {
        this.kind = kind;
        this.choices = choices;
        this.typeField = typeField;
        this.types = types;
        this.element = element;
        this.objectClass = objectClass;
        this.highlight = highlight;
    }

    private static EditorSchema simple(Kind kind) {
        return new EditorSchema(kind, null, null, null, null, null, GuiEditorBase.COLOR_TEXT);
    }

    private static EditorSchema choice(List<String> choices) {
        return new EditorSchema(Kind.CHOICE, choices, null, null, null, null, GuiEditorBase.COLOR_TEXT);
    }

    private static EditorSchema polymorphic(String typeField, Map<String, ? extends Class<?>> types, int highlight) {
        return new EditorSchema(Kind.POLYMORPHIC, null, typeField, new TreeMap<>(types), null, null, highlight);
    }

    public static EditorSchema object(Class<?> objectClass) {
        return new EditorSchema(Kind.OBJECT, null, null, null, null, objectClass, GuiEditorBase.COLOR_TEXT);
    }

    public static EditorSchema of(Type type) {
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType)type;
            if(parameterized.getRawType() == List.class) {
                return new EditorSchema(Kind.LIST, null, null, null, of(parameterized.getActualTypeArguments()[0]), null, GuiEditorBase.COLOR_TEXT);
            }
            return simple(Kind.RAW);
        }
        if(!(type instanceof Class)) return simple(Kind.RAW);

        Class<?> clazz = (Class<?>)type;
        if(clazz == String.class) return simple(Kind.TEXT);
        if(clazz == Integer.class || clazz == int.class || clazz == Long.class || clazz == long.class) return simple(Kind.INTEGER);
        if(clazz == Float.class || clazz == float.class || clazz == Double.class || clazz == double.class) return simple(Kind.DECIMAL);
        if(clazz == Boolean.class || clazz == boolean.class) return simple(Kind.BOOLEAN);
        if(clazz == RandomValueRange.class) return simple(Kind.RANGE);
        if(clazz == ISlotType.class) {
            List<String> choices = new ArrayList<>(JsonConfig.slotTypeDeserializerMap.keySet());
            Collections.sort(choices);
            return choice(choices);
        }
        if(clazz.isEnum()) return choice(enumChoices(clazz));
        if(clazz == GenericGemEffect.class) return polymorphic(GenericGemEffect.TYPE_FIELD, JsonConfig.gemEffectDeserializerMap, GuiEditorBase.COLOR_EFFECT);
        if(clazz == GenericFilter.class) return polymorphic(GenericFilter.TYPE_FIELD, JsonConfig.filterDeserializerMap, GuiEditorBase.COLOR_FILTER);
        if(clazz == GenericActivator.class) return polymorphic(GenericActivator.TYPE_FIELD, JsonConfig.activatorDeserializerMap, GuiEditorBase.COLOR_ACTIVATOR);
        if(clazz == GenericTarget.class) return polymorphic(GenericTarget.TYPE_FIELD, JsonConfig.targetDeserializerMap, GuiEditorBase.COLOR_TARGET);
        if(clazz == GenericCondition.class) return polymorphic(GenericCondition.TYPE_FIELD, JsonConfig.conditionDeserializerMap, GuiEditorBase.COLOR_CONDITION);
        return simple(Kind.RAW);
    }

    private static List<String> enumChoices(Class<?> enumClass) {
        List<String> choices = new ArrayList<>();
        for(Object constant : enumClass.getEnumConstants()) {
            String name = ((Enum<?>)constant).name();
            try {
                SerializedName serializedName = enumClass.getField(name).getAnnotation(SerializedName.class);
                if(serializedName != null) name = serializedName.value();
            }
            catch(NoSuchFieldException ignored) {}
            choices.add(name);
        }
        return choices;
    }

    public static Map<String, EditorSchema> fieldsOf(Class<?> clazz) {
        Map<String, EditorSchema> fields = new LinkedHashMap<>();
        if(clazz == null || clazz == Object.class) return fields;
        fields.putAll(fieldsOf(clazz.getSuperclass()));
        for(Field field : clazz.getDeclaredFields()) {
            if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            fields.put(serializedName != null ? serializedName.value() : field.getName(), of(field.getGenericType()));
        }
        return fields;
    }
}