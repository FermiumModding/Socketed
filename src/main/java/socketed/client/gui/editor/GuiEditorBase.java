package socketed.client.gui.editor;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Shared theme, widgets and input handling for the editor screens
 * Widgets are drawn manually rather than using GuiButton, as they need to scroll and clip inside of panes
 */
@SideOnly(Side.CLIENT)
public abstract class GuiEditorBase extends GuiScreen {
    protected static final int COLOR_PANEL = 0xF01A1E24;
    protected static final int COLOR_PANE = 0xFF0F1216;
    protected static final int COLOR_BORDER = 0xFF3A4450;
    protected static final int COLOR_BUTTON = 0xFF2A323C;
    protected static final int COLOR_BUTTON_HOVER = 0xFF3C4856;
    protected static final int COLOR_ACCENT = 0xFF3789C6;
    protected static final int COLOR_TEXT = 0xFFE6E6E6;
    protected static final int COLOR_TEXT_DIM = 0xFF96A0AC;
    protected static final int COLOR_TEXT_DISABLED = 0xFF5C6670;
    protected static final int COLOR_INVALID = 0xFFFF5555;
    protected static final int COLOR_SUCCESS = 0xFF55FF55;
    protected static final int COLOR_EFFECT = 0xFFFFC857;
    protected static final int COLOR_ACTIVATOR = 0xFF5FD7E8;
    protected static final int COLOR_TARGET = 0xFF8FE36B;
    protected static final int COLOR_CONDITION = 0xFFE98CF0;
    protected static final int COLOR_FILTER = 0xFFA6B1FF;

    @Nullable
    private EditorDropdown dropdown = null;

    protected class FlatButton {
        public int x;
        public int y;
        public int width;
        public int height;
        public String label;
        public final Runnable action;
        public boolean enabled = true;
        public boolean selected = false;
        public int textColor = COLOR_TEXT;

        public FlatButton(int x, int y, int width, int height, String label, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.action = action;
        }

        public boolean isInside(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
        }

        public boolean click(int mouseX, int mouseY) {
            if(!this.enabled || !this.isInside(mouseX, mouseY)) return false;
            playClickSound();
            this.action.run();
            return true;
        }

        public void draw(int mouseX, int mouseY, boolean canHover) {
            boolean hovered = canHover && !isDropdownOpen() && this.enabled && this.isInside(mouseX, mouseY);
            Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, this.selected ? COLOR_ACCENT : COLOR_BORDER);
            Gui.drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON);
            String text = fontRenderer.trimStringToWidth(this.label, this.width - 6);
            int color = !this.enabled ? COLOR_TEXT_DISABLED : this.textColor;
            fontRenderer.drawString(text, this.x + (this.width - fontRenderer.getStringWidth(text)) / 2, this.y + (this.height - 8) / 2, color);
        }
    }

    protected void playClickSound() {
        this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    protected boolean isDropdownOpen() {
        return this.dropdown != null;
    }

    protected void closeDropdown() {
        this.dropdown = null;
    }

    protected void openDropdown(int x, int y, int width, int height, List<String> options, String current, ToIntFunction<String> optionColor, Consumer<String> onSelect) {
        this.dropdown = new EditorDropdown(this.fontRenderer, x, y, width, height, this.width, this.height, options, current, optionColor, onSelect);
    }

    protected void beginClip(int x, int y, int width, int height) {
        int scale = new ScaledResolution(this.mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, this.mc.displayHeight - (y + height) * scale, Math.max(0, width) * scale, Math.max(0, height) * scale);
    }

    protected void endClip() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawPane(int left, int top, int right, int bottom) {
        Gui.drawRect(left - 1, top - 1, right + 1, bottom + 1, COLOR_BORDER);
        Gui.drawRect(left, top, right, bottom, COLOR_PANE);
    }

    protected abstract void drawContent(int mouseX, int mouseY);

    protected abstract void onMouseClick(int mouseX, int mouseY, int mouseButton);

    protected abstract void onKey(char typedChar, int keyCode);

    protected abstract void onWheel(int mouseX, int mouseY, int direction);

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.closeDropdown();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawContent(mouseX, mouseY);
        if(this.dropdown != null) this.dropdown.draw(mouseX, mouseY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if(wheel == 0) return;
        int direction = wheel > 0 ? -1 : 1;
        if(this.dropdown != null) this.dropdown.scroll(direction);
        else {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.onWheel(mouseX, mouseY, direction);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(this.dropdown != null) {
            EditorDropdown open = this.dropdown;
            this.closeDropdown();
            if(open.click(mouseX, mouseY)) this.playClickSound();
            return;
        }
        this.onMouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(this.dropdown != null) {
            if(keyCode == Keyboard.KEY_ESCAPE || this.dropdown.key(typedChar, keyCode)) this.closeDropdown();
            return;
        }
        this.onKey(typedChar, keyCode);
    }
}