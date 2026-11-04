package socketed.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.common.container.ContainerSocketing;
import socketed.api.socket.GenericSocket;
import socketed.api.socket.gem.GemType;
import socketed.api.util.SocketedUtil;
import socketed.mixin.vanilla.IGuiContainerMixin;

import java.util.Collections;

@SideOnly(Side.CLIENT)
public class GuiSocketing extends GuiContainer {
    
    private static final ResourceLocation SOCKETING_GUI_TEXTURE = new ResourceLocation(Socketed.MODID, "textures/gui/container/socketing.png");
    private static final ResourceLocation ICON_LOCKED_TEXTURE = new ResourceLocation(Socketed.MODID, "textures/gui/container/icon_locked.png");
    private static final ResourceLocation ICON_DISABLED_TEXTURE = new ResourceLocation(Socketed.MODID, "textures/gui/container/icon_disabled.png");
    private static final ResourceLocation SLOT_INVALID_TEXTURE = new ResourceLocation(Socketed.MODID, "textures/gui/container/slot_invalid.png");
    
    private final InventoryPlayer playerInventory;
    
    //Same height as double chest
    private static final int guiHeight = 222;
    private static final int FILLED_SOCKET_FRAME_TIME = 2;

    public GuiSocketing(InventoryPlayer playerInv) {
        super(new ContainerSocketing(playerInv));
        this.playerInventory = playerInv;
        this.ySize = guiHeight;
    }

    public void initGui() {
        super.initGui();
    }
    
    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if(this.mc.player.inventory.getItemStack().isEmpty() && ((IGuiContainerMixin)this).getHoveredSlot() != null) {
            Slot slot = ((IGuiContainerMixin)this).getHoveredSlot();
            if(slot.getHasStack()) {
                this.renderToolTip(slot.getStack(), mouseX, mouseY);
            }
            else if(slot instanceof ContainerSocketing.SlotGem) {
                ItemStack socketable = this.inventorySlots.getSlot(0).getStack();
                if(!socketable.isEmpty()) {
                    ICapabilitySocketable itemSockets = socketable.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
                    if(itemSockets != null && itemSockets.getSocketCount() > 0) {
                        GenericSocket socket = itemSockets.getSocketAt(slot.getSlotIndex() - 1);
                        if(socket != null) {
                            GuiUtils.drawHoveringText(Collections.singletonList("  " + TextFormatting.BOLD + socket.getSocketTooltip() + TextFormatting.RESET), mouseX, mouseY, width, height, 300, fontRenderer);
                        }
                    }
                }
            }
        }
    }
    
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = I18n.format("socketed.socketinggui.displayname");
        this.fontRenderer.drawString(s, 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

        ItemStack socketable = this.inventorySlots.getSlot(0).getStack();
        if(!socketable.isEmpty()) {
            ICapabilitySocketable itemSockets = socketable.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
            if(itemSockets != null && itemSockets.getSocketCount() == 0) {
                String noSockets = I18n.format("socketed.socketinggui.nosockets");
                this.fontRenderer.drawString(noSockets, ContainerSocketing.xCent + 8 - this.fontRenderer.getStringWidth(noSockets) / 2, ContainerSocketing.yCent - 11, 0xAA0000);
            }
        }
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(SOCKETING_GUI_TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        ItemStack socketable = this.inventorySlots.getSlot(0).getStack();
        if(!socketable.isEmpty()) {
            ICapabilitySocketable itemSockets = socketable.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
            if(itemSockets != null && itemSockets.getSocketCount() > 0) {
                int socketCount = itemSockets.getSocketCount();
                int xCent = ContainerSocketing.xCent - 1;
                int yCent = ContainerSocketing.yCent - 1;
                double angleIncr = 2.0D * Math.PI / Math.max(1, socketCount);
                
                ItemStack cursorStack = this.playerInventory.getItemStack();
                GemType gemType = SocketedUtil.getGemTypeFromItemStack(cursorStack);
                for(int socketIndex = 0; socketIndex < socketCount; socketIndex++) {
                    GenericSocket socket = itemSockets.getSocketAt(socketIndex);
                    if(socket == null) continue;
                    int x = this.guiLeft + xCent + (int)(ContainerSocketing.radius * Math.sin(socketIndex * angleIncr));
                    int y = this.guiTop + yCent - (int)(ContainerSocketing.radius * Math.cos(socketIndex * angleIncr));
                    
                    this.mc.getTextureManager().bindTexture(socket.getSocketTexture());
                    if(socket.isEmpty()) {
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x - 7, y - 7, 0, 0, 32, 32, 32, 32);
                    }
                    else {
                        int frame = (int)(this.mc.world.getTotalWorldTime() / FILLED_SOCKET_FRAME_TIME % 16);
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x - 7, y - 7, (frame % 4) * 32, (frame / 4) * 32, 32, 32, 128, 128);
                    }
                    
                    boolean disabled = socket.isDisabled();
                    boolean locked = socket.isLocked();
                    
                    if(locked || (!cursorStack.isEmpty() && !(gemType != null && socket.acceptsGemType(gemType, false) && gemType.hasEffectsForStack(socketable)))) {
                        this.mc.getTextureManager().bindTexture(SLOT_INVALID_TEXTURE);
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x + 1, y + 1, 0, 0, 16, 16, 16, 16);
                    }
                    
                    if(disabled && locked) {
                        this.mc.getTextureManager().bindTexture(ICON_DISABLED_TEXTURE);
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x - 1, y + 17 + 1, 3, 3, 10, 10, 16, 16);
                        this.mc.getTextureManager().bindTexture(ICON_LOCKED_TEXTURE);
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x + 9, y + 17, 3, 3, 10, 10, 16, 16);
                    }
                    else if(disabled) {
                        this.mc.getTextureManager().bindTexture(ICON_DISABLED_TEXTURE);
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x + 4, y + 17 + 1, 3, 3, 10, 10, 16, 16);
                    }
                    else if(locked) {
                        this.mc.getTextureManager().bindTexture(ICON_LOCKED_TEXTURE);
                        GuiSocketing.drawModalRectWithCustomSizedTexture(x + 4, y + 17, 3, 3, 10, 10, 16, 16);
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}