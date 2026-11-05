package socketed.client.gui.editor;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

@SideOnly(Side.CLIENT)
public class EditorDropdown {
    private static final int ROW_HEIGHT = 12;
    private static final int MAX_VISIBLE = 12;

    private final FontRenderer font;
    private final List<String> options;
    private final String current;
    private final ToIntFunction<String> optionColor;
    private final Consumer<String> onSelect;
    private final int anchorX;
    private final int anchorY;
    private final int anchorWidth;
    private final int anchorHeight;
    private final int screenWidth;
    private final int screenHeight;

    private final List<String> filtered = new ArrayList<>();
    private String filter = "";
    private int scroll = 0;
    private int left;
    private int top;
    private int width;
    private int visibleRows;

    public EditorDropdown(FontRenderer font, int x, int y, int width, int height, int screenWidth, int screenHeight, List<String> options, String current, ToIntFunction<String> optionColor, Consumer<String> onSelect) {
        this.font = font;
        this.anchorX = x;
        this.anchorY = y;
        this.anchorWidth = width;
        this.anchorHeight = height;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.options = options;
        this.current = current;
        this.optionColor = optionColor;
        this.onSelect = onSelect;
        this.refilter();
        int index = this.filtered.indexOf(current);
        if(index >= this.visibleRows) this.scroll = index - this.visibleRows + 1;
        this.clampScroll();
    }

    private void refilter() {
        this.filtered.clear();
        String lowered = this.filter.toLowerCase(Locale.ROOT);
        for(String option : this.options) {
            if(option.toLowerCase(Locale.ROOT).contains(lowered)) this.filtered.add(option);
        }
        this.scroll = 0;
        this.layout();
    }

    private void layout() {
        int filterRows = this.filter.isEmpty() ? 0 : 1;
        int wanted = Math.min(MAX_VISIBLE, Math.max(1, this.filtered.size()));
        int below = this.screenHeight - (this.anchorY + this.anchorHeight) - 4;
        int above = this.anchorY - 4;
        boolean openBelow = below >= (wanted + filterRows) * ROW_HEIGHT + 2 || below >= above;
        int available = (openBelow ? below : above) / ROW_HEIGHT - filterRows;
        this.visibleRows = Math.max(1, Math.min(wanted, available));

        int height = (this.visibleRows + filterRows) * ROW_HEIGHT + 2;
        this.top = openBelow ? this.anchorY + this.anchorHeight : this.anchorY - height;

        int widest = this.anchorWidth;
        for(String option : this.options) widest = Math.max(widest, this.font.getStringWidth(option) + 12);
        this.width = Math.min(widest, this.screenWidth - 8);
        this.left = Math.max(4, Math.min(this.anchorX, this.screenWidth - this.width - 4));
    }

    private void clampScroll() {
        this.scroll = Math.max(0, Math.min(this.scroll, this.filtered.size() - this.visibleRows));
    }

    public void scroll(int direction) {
        this.scroll += direction;
        this.clampScroll();
    }

    private int getRowsTop() {
        return this.top + 1 + (this.filter.isEmpty() ? 0 : ROW_HEIGHT);
    }

    private int getHeight() {
        return (this.visibleRows + (this.filter.isEmpty() ? 0 : 1)) * ROW_HEIGHT + 2;
    }

    public boolean click(int mouseX, int mouseY) {
        if(mouseX < this.left || mouseX >= this.left + this.width || mouseY < this.getRowsTop()) return false;
        int index = this.scroll + (mouseY - this.getRowsTop()) / ROW_HEIGHT;
        if(index >= this.scroll + this.visibleRows || index >= this.filtered.size()) return false;
        this.onSelect.accept(this.filtered.get(index));
        return true;
    }

    public boolean key(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            if(this.filtered.isEmpty()) return false;
            this.onSelect.accept(this.filtered.get(0));
            return true;
        }
        if(keyCode == Keyboard.KEY_BACK) {
            if(!this.filter.isEmpty()) {
                this.filter = this.filter.substring(0, this.filter.length() - 1);
                this.refilter();
            }
        }
        else if(ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            this.filter += typedChar;
            this.refilter();
        }
        return false;
    }

    public void draw(int mouseX, int mouseY) {
        int height = this.getHeight();
        Gui.drawRect(this.left, this.top, this.left + this.width, this.top + height, GuiEditorBase.COLOR_ACCENT);
        Gui.drawRect(this.left + 1, this.top + 1, this.left + this.width - 1, this.top + height - 1, GuiEditorBase.COLOR_PANE);

        if(!this.filter.isEmpty()) {
            this.font.drawString(this.font.trimStringToWidth(this.filter, this.width - 8), this.left + 4, this.top + 3, GuiEditorBase.COLOR_TEXT_DIM);
        }
        int rowsTop = this.getRowsTop();
        if(this.filtered.isEmpty()) {
            this.font.drawString("-", this.left + 4, rowsTop + 2, GuiEditorBase.COLOR_TEXT_DISABLED);
            return;
        }
        boolean scrollable = this.filtered.size() > this.visibleRows;
        int rowRight = this.left + this.width - (scrollable ? 4 : 1);
        for(int i = 0; i < this.visibleRows && i + this.scroll < this.filtered.size(); i++) {
            String option = this.filtered.get(i + this.scroll);
            int y = rowsTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= this.left && mouseX < rowRight && mouseY >= y && mouseY < y + ROW_HEIGHT;
            if(hovered) Gui.drawRect(this.left + 1, y, rowRight, y + ROW_HEIGHT, GuiEditorBase.COLOR_BUTTON_HOVER);
            else if(option.equals(this.current)) Gui.drawRect(this.left + 1, y, rowRight, y + ROW_HEIGHT, GuiEditorBase.COLOR_BUTTON);
            this.font.drawString(this.font.trimStringToWidth(option, rowRight - this.left - 6), this.left + 4, y + 2, this.optionColor.applyAsInt(option));
        }
        if(scrollable) {
            int trackHeight = this.visibleRows * ROW_HEIGHT;
            int barHeight = Math.max(6, trackHeight * this.visibleRows / this.filtered.size());
            int barTop = rowsTop + (trackHeight - barHeight) * this.scroll / (this.filtered.size() - this.visibleRows);
            Gui.drawRect(this.left + this.width - 3, barTop, this.left + this.width - 1, barTop + barHeight, GuiEditorBase.COLOR_BORDER);
        }
    }
}