package socketed.api.socket.gem.effect;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GenericGemEffect {
    
    public static final String TYPE_FIELD = "Effect Type";

    @SerializedName(TYPE_FIELD)
    protected final String type = this.getTypeName();
    
    @SerializedName("Equipment Slot Type")
    protected final ISlotType slotType;
    
    protected GenericGemEffect(ISlotType slotType) {
        this.slotType = slotType;
    }

    @Nonnull
    public ISlotType getSlotType() {
        return this.slotType;
    }
    
    @SideOnly(Side.CLIENT)
    public abstract String getTooltip(boolean onItem);
    
    /**
     * @return the user readable type name for the subclass used for deserialization
     */
    public abstract String getTypeName();
    
    /**
     * Instantiates this effect for application to sockets
     * Allows for storing variable values such as RandomValueRange if needed
     * @return an instantiated copy of this, or this itself if instantiation is not needed
     */
    public GenericGemEffect instantiate() {
        return this;
    }

    /**
     * Is called when the stack with this effect is equipped to the players equipment slots
     * Use this to add various temporary changes
     */
    public void onEquip(EntityPlayer player, ItemStack stack) {}

    /**
     * Is called when the stack with this effect is unequipped from the players equipment slots
     * Use this to remove various temporary changes that were added by this gem effect
     */
    public void onUnequip(EntityPlayer player, ItemStack stack) {}
    
    /**
     * Attempts to validate this effect and setup caches from parsed values, such as Potion references
     * @return false if any required value is invalid, which should result in discarding this effect
     *
     * SlotType: Required
     */
    public boolean validate() {
        if(this.slotType == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, invalid slot type");
        else return true;
        return false;
    }
    
    /**
     * Writes this effect to NBT to allow for storing values from instantiated effects
     * @return null if this effect is non-instantiated
     */
    public NBTTagCompound writeToNBT() {
        return new NBTTagCompound();
    }
    
    /**
     * Reads this effect from NBT to allow for storing values from instantiated effects
     */
    public void readFromNBT(NBTTagCompound nbt) {
        /*noop*/
    }
}
