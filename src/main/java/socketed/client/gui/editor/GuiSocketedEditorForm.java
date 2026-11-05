package socketed.client.gui.editor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemType;
import socketed.common.config.JsonEditorStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Edits the json of a single gem type or gem combination
 * The left pane is an outline of the nested parts such as effects and activators, the right pane has the fields of the selected part
 * The json itself is edited rather than the loaded object, so that anything the schema doesn't know of isn't lost
 */
@SideOnly(Side.CLIENT)
public class GuiSocketedEditorForm extends GuiEditorBase {
    private static final int MAX_PANEL_WIDTH = 720;
    private static final int TREE_ROW_HEIGHT = 14;
    private static final int TREE_INDENT = 8;
    private static final int TREE_ARROW_WIDTH = 8;
    private static final int CELL_HEIGHT = 34;
    private static final int CELL_GAP = 8;
    private static final int WIDGET_HEIGHT = 18;
    private static final int TWO_COLUMN_WIDTH = 300;
    private static final String UNSET = "<unset>";
    private static final String GEM_TYPES_KEY = "Gem Types";

    private final GuiSocketedEditor parent;
    private final boolean combination;
    private final JsonObject root;
    @Nullable
    private String name;
    private String newName = "";
    private boolean dirty = false;
    private boolean confirmDiscard = false;
    private String status = "";
    private int statusColor = COLOR_TEXT;

    private int left;
    private int right;
    private int top;
    private int bottom;
    private int contentTop;
    private int contentBottom;
    private int treeRight;
    private int detailLeft;
    private int cellsTop;

    private final List<TreeNode> nodes = new ArrayList<>();
    private final List<TreeNode> visibleNodes = new ArrayList<>();
    private final Set<String> collapsedPaths = new HashSet<>();
    private final List<Cell> cells = new ArrayList<>();
    private final Map<String, String> invalidTexts = new HashMap<>();
    private String selectedPath = "";
    private TreeNode selected;
    private int treeScroll = 0;
    private int detailScroll = 0;
    private int cellCount = 0;

    private FlatButton saveButton;
    private FlatButton backButton;
    private FlatButton removeNodeButton;

    private interface Slot {
        @Nullable JsonElement get();
        void set(@Nullable JsonElement value);
    }

    private static class ObjectSlot implements Slot {
        private final JsonObject object;
        private final String key;

        private ObjectSlot(JsonObject object, String key) {
            this.object = object;
            this.key = key;
        }

        @Override
        public JsonElement get() {
            JsonElement element = this.object.get(this.key);
            return element == null || element.isJsonNull() ? null : element;
        }

        @Override
        public void set(JsonElement value) {
            if(value == null) this.object.remove(this.key);
            else this.object.add(this.key, value);
        }
    }

    private static class ArraySlot implements Slot {
        private final JsonArray array;
        private final int index;

        private ArraySlot(JsonArray array, int index) {
            this.array = array;
            this.index = index;
        }

        @Override
        public JsonElement get() {
            JsonElement element = this.array.get(this.index);
            return element.isJsonNull() ? null : element;
        }

        @Override
        public void set(JsonElement value) {
            this.array.set(this.index, value == null ? JsonNull.INSTANCE : value);
        }
    }

    private static class TreeNode {
        private final String path;
        private final String label;
        @Nullable
        private final String value;
        private final int depth;
        private final Slot slot;
        private final EditorSchema schema;
        @Nullable
        private final JsonObject object;
        @Nullable
        private final Map<String, EditorSchema> fields;
        private final boolean gemTypeList;
        @Nullable
        private final Runnable remove;
        private boolean hasChildren = false;

        private TreeNode(String path, String label, @Nullable String value, int depth, Slot slot, EditorSchema schema, @Nullable JsonObject object, @Nullable Map<String, EditorSchema> fields, boolean gemTypeList, @Nullable Runnable remove) {
            this.path = path;
            this.label = label;
            this.value = value;
            this.depth = depth;
            this.slot = slot;
            this.schema = schema;
            this.object = object;
            this.fields = fields;
            this.gemTypeList = gemTypeList;
            this.remove = remove;
        }

        private int getValueColor() {
            return UNSET.equals(this.value) ? COLOR_TEXT_DISABLED : this.schema.highlight;
        }

        private boolean isCollapsible() {
            return this.hasChildren && this.depth > 0;
        }
    }

    private class Cell {
        private final String label;
        private final int x;
        private final int y;
        private final int width;
        @Nullable
        private GuiTextField field = null;
        @Nullable
        private Predicate<String> commit = null;
        @Nullable
        private String path = null;
        @Nullable
        private FlatButton button = null;
        @Nullable
        private FlatButton remove = null;

        private Cell(String label, int x, int y, int width) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
        }
    }

    public GuiSocketedEditorForm(GuiSocketedEditor parent, boolean combination, @Nullable String name, @Nullable String json) {
        this.parent = parent;
        this.combination = combination;
        this.name = name;
        this.root = json != null ? new JsonParser().parse(json).getAsJsonObject() : createTemplate(combination);
    }

    private static JsonObject createTemplate(boolean combination) {
        JsonObject object = new JsonObject();
        object.addProperty("Display Name", "");
        object.addProperty("Text Color", "GRAY");
        if(combination) {
            object.addProperty("Strict Order", false);
            object.addProperty("Strict Count", false);
            object.addProperty("Allows Wrapping", true);
            object.addProperty("Replaces Original Effects", true);
            object.add(GEM_TYPES_KEY, new JsonArray());
        }
        else {
            object.addProperty("Gem Tier", 0);
            object.add("Filters", new JsonArray());
        }
        object.add("Effects", new JsonArray());
        return object;
    }

    @Override
    public void initGui() {
        super.initGui();
        int panelWidth = Math.min(MAX_PANEL_WIDTH, this.width - 16);
        this.left = (this.width - panelWidth) / 2;
        this.right = this.left + panelWidth;
        this.top = 8;
        this.bottom = this.height - 8;
        this.contentTop = this.top + 22;
        this.contentBottom = this.bottom - 28;
        this.treeRight = this.left + 4 + MathHelper.clamp(panelWidth / 3, 120, 220);
        this.detailLeft = this.treeRight + 6;
        this.cellsTop = this.contentTop + 22;

        this.saveButton = new FlatButton(this.left + 6, this.bottom - 24, 90, 18, I18n.format("socketed.editor.save"), this::save);
        this.backButton = new FlatButton(this.right - 96, this.bottom - 24, 90, 18, I18n.format(this.confirmDiscard ? "socketed.editor.discard" : "socketed.editor.back"), this::back);
        this.rebuild();
    }

    private void rebuild() {
        this.nodes.clear();
        Class<?> rootClass = this.combination ? GemCombinationType.class : GemType.class;
        Map<String, EditorSchema> rootFields = EditorSchema.fieldsOf(rootClass);
        this.nodes.add(new TreeNode("", I18n.format("socketed.editor.general"), null, 0, null, EditorSchema.object(rootClass), this.root, rootFields, false, null));
        this.addChildNodes(this.root, rootFields, "", 1);
        for(int i = 0; i + 1 < this.nodes.size(); i++) {
            this.nodes.get(i).hasChildren = this.nodes.get(i + 1).depth > this.nodes.get(i).depth;
        }

        String path = this.selectedPath;
        TreeNode found = this.findNode(path);
        while(found == null) {
            int separator = path.lastIndexOf('/');
            path = separator < 0 ? "" : path.substring(0, separator);
            found = this.findNode(path);
        }
        this.selected = found;
        this.selectedPath = path;

        for(int separator = path.lastIndexOf('/'); separator > 0; separator = path.lastIndexOf('/', separator - 1)) {
            this.collapsedPaths.remove(path.substring(0, separator));
        }
        this.updateVisibleNodes();
        this.clampTreeScroll();
        this.buildDetail();
    }

    private void updateVisibleNodes() {
        this.visibleNodes.clear();
        int hiddenBelowDepth = -1;
        for(TreeNode node : this.nodes) {
            if(hiddenBelowDepth >= 0) {
                if(node.depth > hiddenBelowDepth) continue;
                hiddenBelowDepth = -1;
            }
            this.visibleNodes.add(node);
            if(node.isCollapsible() && this.collapsedPaths.contains(node.path)) hiddenBelowDepth = node.depth;
        }
    }

    private void toggleCollapsed(TreeNode node) {
        if(!this.collapsedPaths.remove(node.path)) {
            this.collapsedPaths.add(node.path);
            if(this.selectedPath.startsWith(node.path + "/")) {
                this.selectedPath = node.path;
                this.detailScroll = 0;
            }
        }
        this.rebuild();
    }

    private void shiftPaths(String listPath, int removedIndex) {
        Set<String> collapsed = new HashSet<>();
        for(String path : this.collapsedPaths) {
            String shifted = shiftPath(path, listPath, removedIndex);
            if(shifted != null) collapsed.add(shifted);
        }
        this.collapsedPaths.clear();
        this.collapsedPaths.addAll(collapsed);

        Map<String, String> invalid = new HashMap<>();
        for(Map.Entry<String, String> entry : this.invalidTexts.entrySet()) {
            String shifted = shiftPath(entry.getKey(), listPath, removedIndex);
            if(shifted != null) invalid.put(shifted, entry.getValue());
        }
        this.invalidTexts.clear();
        this.invalidTexts.putAll(invalid);
    }

    @Nullable
    private static String shiftPath(String path, String listPath, int removedIndex) {
        String prefix = listPath + "/";
        if(!path.startsWith(prefix)) return path;
        String relative = path.substring(prefix.length());
        int separator = relative.indexOf('/');
        int index = Integer.parseInt(separator < 0 ? relative : relative.substring(0, separator));
        if(index == removedIndex) return null;
        String remainder = separator < 0 ? "" : relative.substring(separator);
        return prefix + (index > removedIndex ? index - 1 : index) + remainder;
    }

    @Nullable
    private TreeNode findNode(String path) {
        for(TreeNode node : this.nodes) {
            if(node.path.equals(path)) return node;
        }
        return null;
    }

    private void addChildNodes(JsonObject object, @Nullable Map<String, EditorSchema> fields, String path, int depth) {
        if(fields == null) return;
        for(Map.Entry<String, EditorSchema> field : fields.entrySet()) {
            this.addNode(new ObjectSlot(object, field.getKey()), field.getValue(), field.getKey(), path + "/" + field.getKey(), depth, field.getKey(), null);
        }
    }

    private void addNode(Slot slot, EditorSchema schema, String label, String path, int depth, @Nullable String key, @Nullable Runnable remove) {
        JsonElement value = slot.get();
        if(schema.kind == EditorSchema.Kind.LIST && (value == null || value.isJsonArray())) {
            int size = value == null ? 0 : value.getAsJsonArray().size();
            boolean gemTypeList = this.combination && GEM_TYPES_KEY.equals(key);
            this.nodes.add(new TreeNode(path, label + " (" + size + ")", null, depth, slot, schema, null, null, gemTypeList, remove));
            if(value == null || !isNodeKind(schema.element)) return;
            JsonArray array = value.getAsJsonArray();
            for(int i = 0; i < array.size(); i++) {
                int index = i;
                this.addNode(new ArraySlot(array, i), schema.element, "#" + (i + 1), path + "/" + i, depth + 1, null, () -> this.removeFromList(array, index, path));
            }
        }
        else if(schema.kind == EditorSchema.Kind.POLYMORPHIC && (value == null || value.isJsonObject())) {
            JsonObject object = value == null ? null : value.getAsJsonObject();
            String type = getPolymorphicType(object, schema);
            Class<?> typeClass = schema.types.get(type);
            Map<String, EditorSchema> fields = typeClass == null ? null : EditorSchema.fieldsOf(typeClass);
            this.nodes.add(new TreeNode(path, label, type, depth, slot, schema, object, fields, false, remove));
            if(object != null) this.addChildNodes(object, fields, path, depth + 1);
        }
    }

    private static boolean isNodeKind(EditorSchema schema) {
        return schema.kind == EditorSchema.Kind.LIST || schema.kind == EditorSchema.Kind.POLYMORPHIC;
    }

    private static String getPolymorphicType(@Nullable JsonObject object, EditorSchema schema) {
        JsonElement type = object == null ? null : object.get(schema.typeField);
        return type != null && type.isJsonPrimitive() ? type.getAsString() : UNSET;
    }

    private void removeFromList(JsonArray array, int index, String listPath) {
        array.remove(index);
        this.shiftPaths(listPath, index);
        this.selectedPath = listPath;
        this.markDirty();
        this.rebuild();
    }

    private void select(String path) {
        this.selectedPath = path;
        this.detailScroll = 0;
        this.rebuild();
    }

    private void buildDetail() {
        this.cells.clear();
        this.cellCount = 0;
        TreeNode node = this.selected;

        this.removeNodeButton = null;
        if(node.remove != null) {
            this.removeNodeButton = new FlatButton(this.right - 76, this.contentTop + 2, 70, 16, I18n.format("socketed.editor.remove"), node.remove);
        }

        if(node.path.isEmpty() && this.name == null) {
            this.addTextCell(I18n.format("socketed.editor.name"), this.newName, "/name", value -> {
                this.newName = value.trim();
                return true;
            }, null);
        }
        if(node.schema.kind == EditorSchema.Kind.LIST) this.buildListDetail(node);
        else {
            if(node.schema.kind == EditorSchema.Kind.POLYMORPHIC) this.addTypeCell(node);
            if(node.object != null) this.buildObjectDetail(node.object, node.fields, node.schema.typeField, node.path);
        }
        this.clampDetailScroll();
    }

    private void addTypeCell(TreeNode node) {
        String current = getPolymorphicType(node.object, node.schema);
        List<String> options = new ArrayList<>();
        if(node.remove == null) options.add(UNSET);
        options.addAll(node.schema.types.keySet());
        if(!options.contains(current)) options.add(current);

        this.addDropdownCell(I18n.format("socketed.editor.type"), options, current, selected -> {
            if(selected.equals(current)) return;
            if(selected.equals(UNSET)) node.slot.set(null);
            else {
                JsonObject replacement = new JsonObject();
                replacement.addProperty(node.schema.typeField, selected);
                node.slot.set(replacement);
            }
            this.invalidTexts.keySet().removeIf(path -> path.startsWith(node.path + "/"));
            this.collapsedPaths.removeIf(path -> path.startsWith(node.path + "/"));
            this.markDirty();
            this.rebuild();
        }, node.schema.highlight, null);
    }

    private void buildObjectDetail(JsonObject object, @Nullable Map<String, EditorSchema> fields, @Nullable String typeField, String path) {
        if(fields != null) {
            for(Map.Entry<String, EditorSchema> field : fields.entrySet()) {
                if(field.getKey().equals(typeField)) continue;
                String fieldPath = path + "/" + field.getKey();
                TreeNode child = this.findNode(fieldPath);
                if(child != null) this.addLinkCell(field.getKey(), child, null);
                else this.addScalarCell(new ObjectSlot(object, field.getKey()), field.getValue(), field.getKey(), fieldPath, null);
            }
        }
        List<String> extras = new ArrayList<>();
        for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if(entry.getKey().equals(typeField)) continue;
            if(fields == null || !fields.containsKey(entry.getKey())) extras.add(entry.getKey());
        }
        for(String extra : extras) {
            this.addTextCellForKind(new ObjectSlot(object, extra), EditorSchema.Kind.RAW, extra, path + "/" + extra, null);
        }
    }

    private void buildListDetail(TreeNode node) {
        Cell addCell = this.addCell("", false);
        addCell.button = new FlatButton(addCell.x, 0, addCell.width, WIDGET_HEIGHT, I18n.format("socketed.editor.add"), () -> {
            JsonElement current = node.slot.get();
            JsonArray array = current != null ? current.getAsJsonArray() : new JsonArray();
            array.add(createDefault(node.schema.element));
            node.slot.set(array);
            if(isNodeKind(node.schema.element)) {
                this.selectedPath = node.path + "/" + (array.size() - 1);
                this.detailScroll = 0;
            }
            this.markDirty();
            this.rebuild();
        });

        JsonElement value = node.slot.get();
        if(value == null) return;
        JsonArray array = value.getAsJsonArray();
        for(int i = 0; i < array.size(); i++) {
            int index = i;
            String label = "#" + (i + 1);
            String path = node.path + "/" + i;
            Runnable remove = () -> this.removeFromList(array, index, node.path);
            TreeNode child = this.findNode(path);
            if(child != null) this.addLinkCell(label, child, remove);
            else if(node.gemTypeList) this.addGemTypeCell(new ArraySlot(array, i), label, remove);
            else this.addScalarCell(new ArraySlot(array, i), node.schema.element, label, path, remove);
        }
    }

    private void addGemTypeCell(Slot slot, String label, Runnable remove) {
        JsonElement value = slot.get();
        String current = value != null && value.isJsonPrimitive() ? value.getAsString() : UNSET;
        List<String> options = this.parent.getGemTypeNames();
        if(!options.contains(current)) options.add(0, current);
        this.addDropdownCell(label, options, current, selected -> {
            slot.set(selected.equals(UNSET) ? null : new JsonPrimitive(selected));
            this.markDirty();
            this.buildDetail();
        }, COLOR_TEXT, remove);
    }

    private void addScalarCell(Slot slot, EditorSchema schema, String label, String path, @Nullable Runnable remove) {
        JsonElement value = slot.get();
        boolean mismatch = value != null && !value.isJsonPrimitive();
        if(!mismatch && schema.kind == EditorSchema.Kind.BOOLEAN) {
            this.addChoiceCell(slot, Arrays.asList("true", "false"), label, remove, text -> new JsonPrimitive(Boolean.parseBoolean(text)));
        }
        else if(!mismatch && schema.kind == EditorSchema.Kind.CHOICE) {
            this.addChoiceCell(slot, schema.choices, label, remove, JsonPrimitive::new);
        }
        else {
            boolean structural = isNodeKind(schema) || schema.kind == EditorSchema.Kind.OBJECT;
            this.addTextCellForKind(slot, mismatch || structural ? EditorSchema.Kind.RAW : schema.kind, label, path, remove);
        }
    }

    private void addChoiceCell(Slot slot, List<String> choices, String label, @Nullable Runnable remove, Function<String, JsonElement> toJson) {
        JsonElement value = slot.get();
        String current = value != null && value.isJsonPrimitive() ? value.getAsString() : UNSET;
        List<String> options = new ArrayList<>();
        options.add(UNSET);
        options.addAll(choices);
        if(!options.contains(current)) options.add(current);

        this.addDropdownCell(label, options, current, selected -> {
            slot.set(selected.equals(UNSET) ? null : toJson.apply(selected));
            this.markDirty();
            this.buildDetail();
        }, COLOR_TEXT, remove);
    }

    private void addTextCellForKind(Slot slot, EditorSchema.Kind kind, String label, String path, @Nullable Runnable remove) {
        JsonElement value = slot.get();
        String initial = value == null ? "" : value.isJsonPrimitive() ? value.getAsString() : value.toString();
        this.addTextCell(label, initial, path, text -> commitScalar(slot, kind, text), remove);
    }

    private static boolean commitScalar(Slot slot, EditorSchema.Kind kind, String text) {
        String trimmed = text.trim();
        if(trimmed.isEmpty()) {
            slot.set(null);
            return true;
        }
        try {
            switch(kind) {
                case INTEGER:
                    slot.set(new JsonPrimitive(Long.parseLong(trimmed)));
                    return true;
                case DECIMAL:
                    slot.set(new JsonPrimitive(Double.parseDouble(trimmed)));
                    return true;
                case RANGE:
                    if(trimmed.startsWith("[")) {
                        String[] split = trimmed.substring(1).split("]")[0].split(":");
                        Float.parseFloat(split[0].trim());
                        Float.parseFloat(split[1].trim());
                        slot.set(new JsonPrimitive(trimmed));
                    }
                    else slot.set(new JsonPrimitive(Double.parseDouble(trimmed)));
                    return true;
                case RAW:
                    slot.set(new JsonParser().parse(trimmed));
                    return true;
                default:
                    slot.set(new JsonPrimitive(text));
                    return true;
            }
        }
        catch(Exception e) {
            return false;
        }
    }

    private static JsonElement createDefault(EditorSchema schema) {
        switch(schema.kind) {
            case POLYMORPHIC:
                JsonObject object = new JsonObject();
                if(!schema.types.isEmpty()) object.addProperty(schema.typeField, schema.types.keySet().iterator().next());
                return object;
            case LIST:
                return new JsonArray();
            default:
                return JsonNull.INSTANCE;
        }
    }

    private Cell addCell(String label, boolean hasRemove) {
        int paneWidth = this.right - 4 - this.detailLeft - 8;
        int columns = paneWidth >= TWO_COLUMN_WIDTH ? 2 : 1;
        int cellWidth = (paneWidth - (columns - 1) * CELL_GAP) / columns;
        int x = this.detailLeft + 4 + (this.cellCount % columns) * (cellWidth + CELL_GAP);
        int y = (this.cellCount / columns) * CELL_HEIGHT;
        this.cellCount++;
        Cell cell = new Cell(label, x, y, hasRemove ? cellWidth - 20 : cellWidth);
        this.cells.add(cell);
        return cell;
    }

    private void addRemove(Cell cell, @Nullable Runnable remove) {
        if(remove != null) cell.remove = new FlatButton(cell.x + cell.width + 2, 0, 18, WIDGET_HEIGHT, "x", remove);
    }

    private void addTextCell(String label, String initial, String path, Predicate<String> commit, @Nullable Runnable remove) {
        Cell cell = this.addCell(label, remove != null);
        cell.path = path;
        cell.commit = commit;
        cell.field = new GuiTextField(0, this.fontRenderer, cell.x + 1, 0, cell.width - 2, WIDGET_HEIGHT - 2);
        cell.field.setMaxStringLength(4096);
        String invalid = this.invalidTexts.get(path);
        cell.field.setText(invalid != null ? invalid : initial);
        cell.field.setCursorPositionZero();
        if(invalid != null) cell.field.setTextColor(COLOR_INVALID);
        this.addRemove(cell, remove);
    }

    private void addDropdownCell(String label, List<String> options, String current, Consumer<String> onSelect, int color, @Nullable Runnable remove) {
        Cell cell = this.addCell(label, remove != null);
        cell.button = new FlatButton(cell.x, 0, cell.width, WIDGET_HEIGHT, current + "  v", null) {
            @Override
            public boolean click(int mouseX, int mouseY) {
                if(!this.isInside(mouseX, mouseY)) return false;
                playClickSound();
                openDropdown(this.x, this.y, this.width, this.height, options, current, option -> UNSET.equals(option) ? COLOR_TEXT_DISABLED : color, onSelect);
                return true;
            }
        };
        cell.button.textColor = UNSET.equals(current) ? COLOR_TEXT_DISABLED : color;
        this.addRemove(cell, remove);
    }

    private void addLinkCell(String label, TreeNode child, @Nullable Runnable remove) {
        Cell cell = this.addCell(label, remove != null);
        String text = child.value != null ? child.value : child.label;
        cell.button = new FlatButton(cell.x, 0, cell.width, WIDGET_HEIGHT, text + "  >", () -> this.select(child.path));
        if(child.value != null) cell.button.textColor = child.getValueColor();
        this.addRemove(cell, remove);
    }

    private void markDirty() {
        this.dirty = true;
        this.confirmDiscard = false;
        this.backButton.label = I18n.format("socketed.editor.back");
        this.status = "";
    }

    private void setStatus(String status, int color) {
        this.status = status;
        this.statusColor = color;
    }

    private void save() {
        Iterator<String> invalid = this.invalidTexts.keySet().iterator();
        if(invalid.hasNext()) {
            String path = invalid.next();
            String label = path.substring(path.lastIndexOf('/') + 1);
            this.select(path.substring(0, path.lastIndexOf('/')));
            this.setStatus(I18n.format("socketed.editor.invalidrow", label), COLOR_INVALID);
            return;
        }
        boolean isNew = this.name == null;
        String saveName = isNew ? this.newName : this.name;
        if(saveName.isEmpty()) {
            this.select("");
            this.setStatus(I18n.format("socketed.editor.namerequired"), COLOR_INVALID);
            return;
        }
        String json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this.root);
        String error = JsonEditorStorage.save(this.combination, isNew, saveName, json);
        if(error != null) {
            this.setStatus(error, COLOR_INVALID);
            return;
        }
        this.name = saveName;
        this.dirty = false;
        this.confirmDiscard = false;
        this.backButton.label = I18n.format("socketed.editor.back");
        this.parent.setEntry(this.combination, saveName, json);
        if(isNew) this.buildDetail();
        this.setStatus(I18n.format("socketed.editor.saved"), COLOR_SUCCESS);
    }

    private void back() {
        if(this.dirty && !this.confirmDiscard) {
            this.confirmDiscard = true;
            this.backButton.label = I18n.format("socketed.editor.discard");
            return;
        }
        this.mc.displayGuiScreen(this.parent);
    }

    private int getTreeRowY(int index) {
        return this.contentTop + index * TREE_ROW_HEIGHT - this.treeScroll;
    }

    private int getTreeArrowX(TreeNode node) {
        return this.left + 8 + Math.max(0, node.depth - 1) * TREE_INDENT;
    }

    private int getTreeLabelX(TreeNode node) {
        return this.getTreeArrowX(node) + (node.depth > 0 ? TREE_ARROW_WIDTH : 0);
    }

    private boolean isInTree(int mouseX, int mouseY) {
        return mouseX >= this.left + 4 && mouseX < this.treeRight && mouseY >= this.contentTop && mouseY < this.contentBottom;
    }

    private boolean isInCells(int mouseX, int mouseY) {
        return mouseX >= this.detailLeft && mouseX < this.right - 4 && mouseY >= this.cellsTop && mouseY < this.contentBottom;
    }

    private void clampTreeScroll() {
        int content = this.visibleNodes.size() * TREE_ROW_HEIGHT;
        this.treeScroll = MathHelper.clamp(this.treeScroll, 0, Math.max(0, content - (this.contentBottom - this.contentTop)));
    }

    private void clampDetailScroll() {
        int rows = this.cells.isEmpty() ? 0 : this.cells.get(this.cells.size() - 1).y + CELL_HEIGHT;
        this.detailScroll = MathHelper.clamp(this.detailScroll, 0, Math.max(0, rows - (this.contentBottom - this.cellsTop)));
    }

    private void positionCells() {
        for(Cell cell : this.cells) {
            int widgetY = this.cellsTop + cell.y + 10 - this.detailScroll;
            if(cell.field != null) cell.field.y = widgetY + 1;
            if(cell.button != null) cell.button.y = widgetY;
            if(cell.remove != null) cell.remove.y = widgetY;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for(Cell cell : this.cells) {
            if(cell.field != null) cell.field.updateCursorCounter();
        }
    }

    @Override
    protected void onWheel(int mouseX, int mouseY, int direction) {
        if(this.isInTree(mouseX, mouseY)) {
            this.treeScroll += direction * TREE_ROW_HEIGHT;
            this.clampTreeScroll();
        }
        else {
            this.detailScroll += direction * CELL_HEIGHT;
            this.clampDetailScroll();
        }
    }

    @Override
    protected void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.positionCells();
        boolean inCells = this.isInCells(mouseX, mouseY);
        for(Cell cell : this.cells) {
            if(cell.field == null) continue;
            if(inCells) cell.field.mouseClicked(mouseX, mouseY, mouseButton);
            else cell.field.setFocused(false);
        }
        if(this.saveButton.click(mouseX, mouseY) || this.backButton.click(mouseX, mouseY)) return;
        if(this.removeNodeButton != null && this.removeNodeButton.click(mouseX, mouseY)) return;

        if(this.isInTree(mouseX, mouseY)) {
            int index = (mouseY - this.contentTop + this.treeScroll) / TREE_ROW_HEIGHT;
            if(index >= 0 && index < this.visibleNodes.size()) {
                TreeNode node = this.visibleNodes.get(index);
                this.playClickSound();
                if(node.isCollapsible() && mouseX < this.getTreeLabelX(node)) this.toggleCollapsed(node);
                else this.select(node.path);
            }
            return;
        }
        if(!inCells) return;
        for(Cell cell : new ArrayList<>(this.cells)) {
            if(cell.button != null && cell.button.click(mouseX, mouseY)) return;
            if(cell.remove != null && cell.remove.click(mouseX, mouseY)) return;
        }
    }

    @Override
    protected void onKey(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            this.back();
            return;
        }
        if(keyCode == Keyboard.KEY_TAB) {
            this.focusNextField();
            return;
        }
        for(Cell cell : this.cells) {
            if(cell.field != null && cell.field.isFocused() && cell.field.textboxKeyTyped(typedChar, keyCode)) {
                boolean valid = cell.commit == null || cell.commit.test(cell.field.getText());
                cell.field.setTextColor(valid ? 0xE0E0E0 : COLOR_INVALID);
                if(valid) this.invalidTexts.remove(cell.path);
                else this.invalidTexts.put(cell.path, cell.field.getText());
                this.markDirty();
                return;
            }
        }
    }

    private void focusNextField() {
        List<GuiTextField> fields = new ArrayList<>();
        for(Cell cell : this.cells) {
            if(cell.field != null) fields.add(cell.field);
        }
        if(fields.isEmpty()) return;
        int next = 0;
        for(int i = 0; i < fields.size(); i++) {
            if(fields.get(i).isFocused()) {
                fields.get(i).setFocused(false);
                next = (i + 1) % fields.size();
                break;
            }
        }
        fields.get(next).setFocused(true);
    }

    @Override
    protected void drawContent(int mouseX, int mouseY) {
        Gui.drawRect(this.left, this.top, this.right, this.bottom, COLOR_PANEL);
        String title = this.name != null ? this.name : I18n.format(this.combination ? "socketed.editor.newcombination" : "socketed.editor.newgemtype");
        this.fontRenderer.drawString(title, this.left + 6, this.top + 7, COLOR_TEXT);
        String type = I18n.format(this.combination ? "socketed.editor.combinations" : "socketed.editor.gemtypes");
        this.fontRenderer.drawString(type, this.right - 6 - this.fontRenderer.getStringWidth(type), this.top + 7, COLOR_TEXT_DIM);

        this.drawTree(mouseX, mouseY);
        this.drawDetail(mouseX, mouseY);

        this.saveButton.draw(mouseX, mouseY, true);
        this.backButton.draw(mouseX, mouseY, true);
        if(!this.status.isEmpty()) {
            int available = this.backButton.x - this.saveButton.x - this.saveButton.width - 8;
            String text = this.fontRenderer.trimStringToWidth(this.status, available);
            int center = (this.saveButton.x + this.saveButton.width + this.backButton.x) / 2;
            this.fontRenderer.drawString(text, center - this.fontRenderer.getStringWidth(text) / 2, this.bottom - 19, this.statusColor);
        }
    }

    private void drawTree(int mouseX, int mouseY) {
        int paneLeft = this.left + 4;
        this.drawPane(paneLeft, this.contentTop, this.treeRight, this.contentBottom);
        this.beginClip(paneLeft, this.contentTop, this.treeRight - paneLeft, this.contentBottom - this.contentTop);
        boolean canHover = !this.isDropdownOpen() && this.isInTree(mouseX, mouseY);
        for(int i = 0; i < this.visibleNodes.size(); i++) {
            TreeNode node = this.visibleNodes.get(i);
            int y = this.getTreeRowY(i);
            if(y + TREE_ROW_HEIGHT < this.contentTop || y >= this.contentBottom) continue;
            boolean hovered = canHover && mouseY >= y && mouseY < y + TREE_ROW_HEIGHT;
            if(node == this.selected) {
                Gui.drawRect(paneLeft, y, this.treeRight, y + TREE_ROW_HEIGHT, COLOR_BUTTON_HOVER);
                Gui.drawRect(paneLeft, y, paneLeft + 2, y + TREE_ROW_HEIGHT, COLOR_ACCENT);
            }
            else if(hovered) Gui.drawRect(paneLeft, y, this.treeRight, y + TREE_ROW_HEIGHT, COLOR_BUTTON);

            int labelX = this.getTreeLabelX(node);
            if(node.isCollapsible()) {
                this.drawTreeArrow(this.getTreeArrowX(node), y, this.collapsedPaths.contains(node.path), hovered && mouseX < labelX ? COLOR_TEXT : COLOR_TEXT_DIM);
            }
            boolean invalid = this.hasInvalid(node);
            int labelColor = invalid ? COLOR_INVALID : node.value != null ? COLOR_TEXT_DIM : COLOR_TEXT;
            this.drawNodeText(node, labelX, y + 3, this.treeRight - labelX - 2, labelColor, invalid ? COLOR_INVALID : node.getValueColor());
        }
        this.endClip();
    }

    private void drawTreeArrow(int x, int y, boolean collapsed, int color) {
        for(int i = 0; i < 3; i++) {
            if(collapsed) Gui.drawRect(x + 1 + i, y + 5 + i, x + 2 + i, y + 10 - i, color);
            else Gui.drawRect(x + i, y + 6 + i, x + 5 - i, y + 7 + i, color);
        }
    }

    private void drawNodeText(TreeNode node, int x, int y, int maxWidth, int labelColor, int valueColor) {
        if(node.value == null) {
            this.fontRenderer.drawString(this.fontRenderer.trimStringToWidth(node.label, maxWidth), x, y, labelColor);
            return;
        }
        String label = this.fontRenderer.trimStringToWidth(node.label + ": ", maxWidth);
        this.fontRenderer.drawString(label, x, y, labelColor);
        int labelWidth = this.fontRenderer.getStringWidth(label);
        if(labelWidth < maxWidth) {
            this.fontRenderer.drawString(this.fontRenderer.trimStringToWidth(node.value, maxWidth - labelWidth), x + labelWidth, y, valueColor);
        }
    }

    private boolean hasInvalid(TreeNode node) {
        boolean collapsed = node.isCollapsible() && this.collapsedPaths.contains(node.path);
        for(String path : this.invalidTexts.keySet()) {
            if(path.substring(0, path.lastIndexOf('/')).equals(node.path)) return true;
            if(collapsed && path.startsWith(node.path + "/")) return true;
        }
        return false;
    }

    private void drawDetail(int mouseX, int mouseY) {
        int paneRight = this.right - 4;
        this.drawPane(this.detailLeft, this.contentTop, paneRight, this.contentBottom);
        int headingWidth = (this.removeNodeButton != null ? this.removeNodeButton.x : paneRight) - this.detailLeft - 8;
        this.drawNodeText(this.selected, this.detailLeft + 4, this.contentTop + 6, headingWidth, COLOR_TEXT, this.selected.getValueColor());
        if(this.removeNodeButton != null) this.removeNodeButton.draw(mouseX, mouseY, true);
        Gui.drawRect(this.detailLeft, this.cellsTop - 2, paneRight, this.cellsTop - 1, COLOR_BORDER);

        this.positionCells();
        boolean canHover = this.isInCells(mouseX, mouseY);
        this.beginClip(this.detailLeft, this.cellsTop, paneRight - this.detailLeft, this.contentBottom - this.cellsTop);
        for(Cell cell : this.cells) {
            int y = this.cellsTop + cell.y - this.detailScroll;
            if(y + CELL_HEIGHT < this.cellsTop || y >= this.contentBottom) continue;
            this.fontRenderer.drawString(this.fontRenderer.trimStringToWidth(cell.label, cell.width), cell.x, y, COLOR_TEXT_DIM);
            if(cell.field != null) cell.field.drawTextBox();
            if(cell.button != null) cell.button.draw(mouseX, mouseY, canHover);
            if(cell.remove != null) cell.remove.draw(mouseX, mouseY, canHover);
        }
        this.endClip();
    }
}