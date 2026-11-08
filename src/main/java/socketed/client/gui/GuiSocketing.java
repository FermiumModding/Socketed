package socketed.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
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

    private static final int[][] CIRCUIT_COLORS = {{0x37, 0x89, 0xC6}, {0x53, 0xAA, 0xFF}, {0x5A, 0xD2, 0xED}};
    private static final int[] CIRCUIT_SHADOW_COLOR = {0x51, 0x6B, 0x7E};
    private static final double CIRCUIT_PULSE_TICKS = 40.0D;
    private static final double CIRCUIT_POLYGON_INSET = 4.0D;
    private static final double CIRCUIT_CIRCLE_OUTSET = 4.0D;

    public GuiSocketing(InventoryPlayer playerInv, World world, BlockPos pos) {
        super(new ContainerSocketing(playerInv, world, pos));
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

    private void drawSocketCircuit(int socketCount, double angleIncr, float partialTicks) {
        int xCenter = this.guiLeft + ContainerSocketing.xCent + 8;
        int yCenter = this.guiTop + ContainerSocketing.yCent + 8;
        double radius = ContainerSocketing.radius;
        double time = this.mc.world.getTotalWorldTime() + partialTicks;
        double circleRadius = radius + CIRCUIT_CIRCLE_OUTSET;
        int circleSteps = (int)Math.ceil(2.0D * Math.PI * circleRadius * 2.0D);

        GlStateManager.disableTexture2D();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        
        boolean shadow = true;
        for(int step = 0; step < circleSteps; step++) {
            double angle = 2.0D * Math.PI * step / circleSteps;
            this.addCircuitPixel(buffer, (int)Math.round(circleRadius * Math.sin(angle)), (int)Math.round(-circleRadius * Math.cos(angle)), xCenter, yCenter, shadow, time);
        }
        for(int socketIndex = 0; socketIndex < socketCount; socketIndex++) {
            int xEnd = (int)(radius * Math.sin(socketIndex * angleIncr));
            int yEnd = -(int)(radius * Math.cos(socketIndex * angleIncr));
            this.addCircuitLine(buffer, 0, 0, xEnd, yEnd, xCenter, yCenter, shadow, time);
            if(socketCount >= 3) {
                double polygonRadius = radius - CIRCUIT_POLYGON_INSET;
                int xCorner = (int)(polygonRadius * Math.sin(socketIndex * angleIncr));
                int yCorner = -(int)(polygonRadius * Math.cos(socketIndex * angleIncr));
                int xNext = (int)(polygonRadius * Math.sin((socketIndex + 1) * angleIncr));
                int yNext = -(int)(polygonRadius * Math.cos((socketIndex + 1) * angleIncr));
                this.addCircuitLine(buffer, xCorner, yCorner, xNext, yNext, xCenter, yCenter, shadow, time);
            }
        }
        //Redraw actual circuit over shadows
        shadow = false;
        for(int step = 0; step < circleSteps; step++) {
            double angle = 2.0D * Math.PI * step / circleSteps;
            this.addCircuitPixel(buffer, (int)Math.round(circleRadius * Math.sin(angle)), (int)Math.round(-circleRadius * Math.cos(angle)), xCenter, yCenter, shadow, time);
        }
        for(int socketIndex = 0; socketIndex < socketCount; socketIndex++) {
            int xEnd = (int)(radius * Math.sin(socketIndex * angleIncr));
            int yEnd = -(int)(radius * Math.cos(socketIndex * angleIncr));
            this.addCircuitLine(buffer, 0, 0, xEnd, yEnd, xCenter, yCenter, shadow, time);
            if(socketCount >= 3) {
                double polygonRadius = radius - CIRCUIT_POLYGON_INSET;
                int xCorner = (int)(polygonRadius * Math.sin(socketIndex * angleIncr));
                int yCorner = -(int)(polygonRadius * Math.cos(socketIndex * angleIncr));
                int xNext = (int)(polygonRadius * Math.sin((socketIndex + 1) * angleIncr));
                int yNext = -(int)(polygonRadius * Math.cos((socketIndex + 1) * angleIncr));
                this.addCircuitLine(buffer, xCorner, yCorner, xNext, yNext, xCenter, yCenter, shadow, time);
            }
        }
        
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    private void addCircuitLine(BufferBuilder buffer, int xStart, int yStart, int xEnd, int yEnd, int xCenter, int yCenter, boolean shadow, double time) {
        int steps = Math.max(1, Math.max(Math.abs(xEnd - xStart), Math.abs(yEnd - yStart)));
        for(int step = 0; step <= steps; step++) {
            int x = xStart + (int)Math.round((double)(xEnd - xStart) * step / steps);
            int y = yStart + (int)Math.round((double)(yEnd - yStart) * step / steps);
            this.addCircuitPixel(buffer, x, y, xCenter, yCenter, shadow, time);
        }
    }

    private void addCircuitPixel(BufferBuilder buffer, int xOffset, int yOffset, int xCenter, int yCenter, boolean shadow, double time) {
        if(shadow) {
            this.addCircuitQuad(buffer, xOffset, yOffset + 1, xCenter, yCenter, CIRCUIT_SHADOW_COLOR);
            this.addCircuitQuad(buffer, xOffset + 1, yOffset + 1, xCenter, yCenter, CIRCUIT_SHADOW_COLOR);
            return;
        }
        double distance = Math.sqrt(xOffset * xOffset + yOffset * yOffset);
        double phase = 2.0D * Math.PI * (time / CIRCUIT_PULSE_TICKS - distance / ContainerSocketing.radius);
        double position = (0.5D + 0.5D * Math.sin(phase)) * (CIRCUIT_COLORS.length - 1);
        int index = Math.min((int)position, CIRCUIT_COLORS.length - 2);
        double blend = position - index;
        int[] color = new int[3];
        for(int i = 0; i < 3; i++) {
            color[i] = (int)(CIRCUIT_COLORS[index][i] + (CIRCUIT_COLORS[index + 1][i] - CIRCUIT_COLORS[index][i]) * blend);
        }
        this.addCircuitQuad(buffer, xOffset, yOffset, xCenter, yCenter, color);
    }
    
    private void addCircuitQuad(BufferBuilder buffer, int xOffset, int yOffset, int xCenter, int yCenter, int[] color) {
        if(xOffset >= -9 && xOffset <= 8 && yOffset >= -9 && yOffset <= 8) return;
        double xPos = xCenter + xOffset;
        double yPos = yCenter + yOffset;
        buffer.pos(xPos, yPos + 1, this.zLevel).color(color[0], color[1], color[2], 255).endVertex();
        buffer.pos(xPos + 1, yPos + 1, this.zLevel).color(color[0], color[1], color[2], 255).endVertex();
        buffer.pos(xPos + 1, yPos, this.zLevel).color(color[0], color[1], color[2], 255).endVertex();
        buffer.pos(xPos, yPos, this.zLevel).color(color[0], color[1], color[2], 255).endVertex();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(SOCKETING_GUI_TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        ItemStack socketable = this.inventorySlots.getSlot(0).getStack();
        ICapabilitySocketable itemSockets = socketable.isEmpty() ? null : socketable.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        int socketCount = itemSockets == null ? 0 : itemSockets.getSocketCount();
        double angleIncr = 2.0D * Math.PI / Math.max(1, socketCount);
        
        //outer circle always renders
        this.drawSocketCircuit(socketCount, angleIncr, partialTicks);
        
        if(socketCount > 0) {
            int xCent = ContainerSocketing.xCent - 1;
            int yCent = ContainerSocketing.yCent - 1;
            
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
                
                if(locked || (!cursorStack.isEmpty() && !(gemType != null && socket.acceptsGemType(gemType, false) && gemType.hasEffectsForStack(socketable) && itemSockets.isUnderGemTypeCap(gemType, socketIndex)))) {
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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}