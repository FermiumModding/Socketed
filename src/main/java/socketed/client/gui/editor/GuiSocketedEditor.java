package socketed.client.gui.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lists the gem types and gem combinations to select one for editing
 */
@SideOnly(Side.CLIENT)
public class GuiSocketedEditor extends GuiEditorBase {
    private static final int MAX_PANEL_WIDTH = 720;
    private static final int ENTRY_WIDTH = 150;
    private static final int ENTRY_HEIGHT = 20;
    private static final int ENTRY_GAP = 4;

    private final Map<String, String> gemTypes;
    private final Map<String, String> gemCombinations;
    private boolean showCombinations = false;
    private int scroll = 0;
    private int rowCount = 0;

    private int left;
    private int right;
    private int top;
    private int bottom;
    private int gridTop;
    private int gridBottom;

    private final List<FlatButton> buttons = new ArrayList<>();
    private final List<FlatButton> entries = new ArrayList<>();
    private GuiTextField search;

    public GuiSocketedEditor(Map<String, String> gemTypes, Map<String, String> gemCombinations) {
        this.gemTypes = gemTypes;
        this.gemCombinations = gemCombinations;
    }

    public void setEntry(boolean combination, String name, String json) {
        (combination ? this.gemCombinations : this.gemTypes).put(name, json);
    }

    public List<String> getGemTypeNames() {
        List<String> names = new ArrayList<>(this.gemTypes.keySet());
        Collections.sort(names);
        return names;
    }

    @Override
    public void initGui() {
        super.initGui();
        int panelWidth = Math.min(MAX_PANEL_WIDTH, this.width - 16);
        this.left = (this.width - panelWidth) / 2;
        this.right = this.left + panelWidth;
        this.top = 8;
        this.bottom = this.height - 8;
        this.gridTop = this.top + 44;
        this.gridBottom = this.bottom - 28;

        String previous = this.search == null ? "" : this.search.getText();
        this.search = new GuiTextField(0, this.fontRenderer, this.right - 134, this.top + 22, 128, 16);
        this.search.setText(previous);
        this.search.setFocused(true);
        this.buildButtons();
        this.buildEntries();
    }

    private void buildButtons() {
        this.buttons.clear();
        FlatButton gemsTab = new FlatButton(this.left + 6, this.top + 22, 90, 16, I18n.format("socketed.editor.gemtypes"), () -> this.setTab(false));
        FlatButton combinationsTab = new FlatButton(this.left + 98, this.top + 22, 90, 16, I18n.format("socketed.editor.combinations"), () -> this.setTab(true));
        gemsTab.selected = !this.showCombinations;
        combinationsTab.selected = this.showCombinations;
        this.buttons.add(gemsTab);
        this.buttons.add(combinationsTab);
        this.buttons.add(new FlatButton(this.left + 6, this.bottom - 24, 90, 18, I18n.format("socketed.editor.new"),
                () -> this.mc.displayGuiScreen(new GuiSocketedEditorForm(this, this.showCombinations, null, null))));
        this.buttons.add(new FlatButton(this.right - 96, this.bottom - 24, 90, 18, I18n.format("socketed.editor.close"), () -> this.mc.displayGuiScreen(null)));
    }

    private void buildEntries() {
        this.entries.clear();
        int gridWidth = this.right - this.left - 12;
        int columns = Math.max(1, (gridWidth + ENTRY_GAP) / (ENTRY_WIDTH + ENTRY_GAP));
        int entryWidth = (gridWidth - (columns - 1) * ENTRY_GAP) / columns;

        Map<String, String> source = this.showCombinations ? this.gemCombinations : this.gemTypes;
        List<String> names = new ArrayList<>(source.keySet());
        Collections.sort(names);
        String filter = this.search.getText().trim().toLowerCase(Locale.ROOT);

        int index = 0;
        for(String name : names) {
            if(!name.toLowerCase(Locale.ROOT).contains(filter)) continue;
            String json = source.get(name);
            boolean valid = isJsonObject(json);
            int x = this.left + 6 + (index % columns) * (entryWidth + ENTRY_GAP);
            FlatButton button = new FlatButton(x, 0, entryWidth, ENTRY_HEIGHT, valid ? name : I18n.format("socketed.editor.unreadable", name),
                    () -> this.mc.displayGuiScreen(new GuiSocketedEditorForm(this, this.showCombinations, name, json)));
            button.enabled = valid;
            this.entries.add(button);
            index++;
        }
        this.rowCount = (index + columns - 1) / columns;
        this.columns = columns;
        this.clampScroll();
    }

    private int columns = 1;

    private static boolean isJsonObject(String json) {
        try {
            JsonElement element = new JsonParser().parse(json);
            return element.isJsonObject();
        }
        catch(Exception e) {
            return false;
        }
    }

    private void setTab(boolean combinations) {
        this.showCombinations = combinations;
        this.scroll = 0;
        this.buildButtons();
        this.buildEntries();
    }

    private void clampScroll() {
        int content = this.rowCount * (ENTRY_HEIGHT + ENTRY_GAP);
        this.scroll = MathHelper.clamp(this.scroll, 0, Math.max(0, content - (this.gridBottom - this.gridTop)));
    }

    private boolean isInGrid(int mouseX, int mouseY) {
        return mouseX >= this.left && mouseX < this.right && mouseY >= this.gridTop && mouseY < this.gridBottom;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.search.updateCursorCounter();
    }

    @Override
    protected void onWheel(int mouseX, int mouseY, int direction) {
        this.scroll += direction * (ENTRY_HEIGHT + ENTRY_GAP);
        this.clampScroll();
    }

    @Override
    protected void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.search.mouseClicked(mouseX, mouseY, mouseButton);
        for(FlatButton button : this.buttons) {
            if(button.click(mouseX, mouseY)) return;
        }
        if(!this.isInGrid(mouseX, mouseY)) return;
        for(FlatButton entry : this.entries) {
            if(entry.click(mouseX, mouseY)) return;
        }
    }

    @Override
    protected void onKey(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if(this.search.textboxKeyTyped(typedChar, keyCode)) {
            this.scroll = 0;
            this.buildEntries();
        }
    }

    @Override
    protected void drawContent(int mouseX, int mouseY) {
        Gui.drawRect(this.left, this.top, this.right, this.bottom, COLOR_PANEL);
        this.fontRenderer.drawString(I18n.format("socketed.editor.title"), this.left + 6, this.top + 7, COLOR_TEXT);
        String notice = I18n.format("socketed.editor.restartnotice");
        this.fontRenderer.drawString(notice, this.right - 6 - this.fontRenderer.getStringWidth(notice), this.top + 7, COLOR_TEXT_DIM);

        for(FlatButton button : this.buttons) button.draw(mouseX, mouseY, true);
        this.search.drawTextBox();
        if(this.search.getText().isEmpty() && !this.search.isFocused()) {
            this.fontRenderer.drawString(I18n.format("socketed.editor.search"), this.search.x + 4, this.search.y + 4, COLOR_TEXT_DISABLED);
        }

        this.drawPane(this.left + 4, this.gridTop - 2, this.right - 4, this.gridBottom);
        this.beginClip(this.left + 4, this.gridTop - 2, this.right - this.left - 8, this.gridBottom - this.gridTop + 2);
        boolean canHover = this.isInGrid(mouseX, mouseY);
        for(int i = 0; i < this.entries.size(); i++) {
            FlatButton entry = this.entries.get(i);
            entry.y = this.gridTop + (i / this.columns) * (ENTRY_HEIGHT + ENTRY_GAP) - this.scroll;
            if(entry.y + entry.height >= this.gridTop && entry.y < this.gridBottom) entry.draw(mouseX, mouseY, canHover);
        }
        this.endClip();
    }
}