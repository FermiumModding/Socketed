package socketed.api.socket;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.GemInstance;
import socketed.api.socket.gem.GemType;
import socketed.api.socket.gem.effect.GenericGemEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class GenericSocket {
    
    public static final String TYPE_NAME = "Generic";
    
    private GemInstance gem = null;
    private NBTTagCompound unresolvedGem = null;
    private boolean overridden = false;
    private boolean disabled = false;
    private boolean locked = false;
    
    /**
     * Constructs an empty socket
     */
    public GenericSocket() { }

    /**
     * Constructs a socket holding the provided gem
     */
    public GenericSocket(GemInstance gem) {
        this.gem = gem;
    }

    /**
     * Constructs a socket from provided nbt
     */
    public GenericSocket(NBTTagCompound nbt) {
        if(nbt.hasKey("Gem")) {
            GemInstance gem = new GemInstance(nbt.getCompoundTag("Gem"));
            if(gem.validate()) this.gem = gem;
            else this.unresolvedGem = nbt.getCompoundTag("Gem");
        }
        if(nbt.hasKey("Overridden")) this.overridden = nbt.getBoolean("Overridden");
        if(nbt.hasKey("Disabled")) this.disabled = nbt.getBoolean("Disabled");
        if(nbt.hasKey("Locked")) this.locked = nbt.getBoolean("Locked");
    }

    /**
     * Sockets marked as overridden will not have effects
     * Used when Gem Combinations are set to override
     * Value will change dependent on combination refresh checks
     */
    public boolean isOverridden() {
        return this.overridden;
    }

    /**
     * Marks the socket as overridden
     */
    public void setOverridden(boolean overridden) {
        this.overridden = overridden;
    }
    
    /**
     * Sockets marked as disabled will not have effects
     * Used when sockets are broken or otherwise should not function
     */
    public boolean isDisabled() {
        return this.disabled;
    }
    
    /**
     * Marks the socket as disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    /**
     * Sockets marked as locked can not have gems added or removed
     * Used when sockets are broken or otherwise should not be modifiable
     */
    public boolean isLocked() {
        return this.locked;
    }
    
    /**
     * Marks the socket as locked
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Whether this socket would accept the given gem
     * The results of this should match as expected with setGem, except for removing gems
     */
    public boolean acceptsGem(GemInstance gem, boolean ignoreLocked) {
        return gem != null && (ignoreLocked || !this.isLocked());
    }
    
    /**
     * Whether this socket would accept the given gem type
     * Note: prefer only using this for quick checks such as gui rendering and not actual socketing
     * Currently it is fine but there may be cases where the GemType on a GemInstance ends up different than expected
     */
    public boolean acceptsGemType(GemType gemType, boolean ignoreLocked) {
        return gemType != null && (ignoreLocked || !this.isLocked());
    }
    
    /**
     * Whether this socket has space for a gem or is already filled
     */
    public boolean isEmpty() {
        return this.gem == null;
    }

    /**
     * @return The gem this socket is filled with
     */
    @Nullable
    public GemInstance getGem() {
        return this.gem;
    }

    /**
     * Attempts to fill the socket with the given gem
     * The results of this should match as expected with acceptsGem, except for removing gems
     * @param gem The gem this socket should get filled with, null to empty the socket
     * @param ignoreLocked if this operation should bypass the locked state of the socket
     * @return true if the operation is successful, false if not
     */
    public boolean setGem(@Nullable GemInstance gem, boolean ignoreLocked) {
        if(!ignoreLocked && this.isLocked()) return false;
        if(gem == null || this.acceptsGem(gem, ignoreLocked)) {
            this.gem = gem;
            this.unresolvedGem = null;
            return true;
        }
        return false;
    }

    /**
     * Gets the current active effects of this socket
     * Will return empty if there is no gem or if the socket is overridden or disabled
     */
    @Nonnull
    public List<GenericGemEffect> getActiveEffects() {
        if(this.gem != null && !this.isOverridden() && !this.isDisabled()) return this.gem.getGemEffects();
        return Collections.emptyList();
    }

    /**
     * @return NBTTagCompound of the socket including its gem NBT if not empty
     */
    @Nonnull
    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("SocketType", GenericSocket.TYPE_NAME);
        if(this.gem != null) nbt.setTag("Gem", this.gem.writeToNBT());
        else if(this.unresolvedGem != null) nbt.setTag("Gem", this.unresolvedGem);
        nbt.setBoolean("Overridden", this.overridden);
        nbt.setBoolean("Disabled", this.disabled);
        nbt.setBoolean("Locked", this.locked);
        return nbt;
    }
    
    protected static final ResourceLocation GENERIC_EMPTY_TEXTURE = new ResourceLocation(Socketed.MODID, "textures/gui/container/socket_generic_empty.png");
    protected static final ResourceLocation GENERIC_FILLED_TEXTURE = new ResourceLocation(Socketed.MODID, "textures/gui/container/socket_generic_filled.png");
    
    /**
     * @return The resourcelocation of the texture to render in the socketing gui
     */
    @SideOnly(Side.CLIENT)
    @Nonnull
    public ResourceLocation getSocketTexture() {
        if(this.isEmpty()) return GENERIC_EMPTY_TEXTURE;
        return GENERIC_FILLED_TEXTURE;
    }
    
    /**
     * @return the tooltip to be displayed when hovering over this socket in gui while empty
     */
    @SideOnly(Side.CLIENT)
    @Nonnull
    public String getSocketTooltip() {
        return TextFormatting.GOLD + I18n.format("socketed.tooltip.generic");
    }
}