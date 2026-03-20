package socketed.common.socket.gem.effect;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;
import socketed.api.socket.gem.util.RandomValueRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AttributeGemEffect extends GenericGemEffect {

    public static final String TYPE_NAME = "Attribute";
    
    @SerializedName("Attribute Name")
    private final String attributeName;

    @SerializedName("Modifier Amount")
    private final RandomValueRange amountRange;

    @SerializedName("Modifier Operation")
    private final Integer operation;
    
    private transient AttributeModifier modifier = null;

    public AttributeGemEffect(ISlotType slotType, String attribute, RandomValueRange amountRange, int operation) {
        super(slotType);
        this.attributeName = attribute;
        this.amountRange = amountRange;
        this.operation = operation;
    }

    public AttributeGemEffect(AttributeGemEffect effect) {
        super(effect.getSlotType());
        this.attributeName = effect.attributeName;
        this.amountRange = effect.amountRange;
        this.operation = effect.operation;

        //Only instantiated AttributeGemEffects have a valid modifier
        this.modifier = new AttributeModifier(UUID.randomUUID(), Socketed.MODID + "GemEffect", this.amountRange.generateValue(), this.operation);
    }
    
    @Nonnull
    public String getAttribute() {
        return this.attributeName;
    }
    
    @Nonnull
    public RandomValueRange getAmountRange() {
        return this.amountRange;
    }

    public int getOperation() {
        return this.operation;
    }

    @Nullable
    public AttributeModifier getModifier() {
        return this.modifier;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getTooltip(boolean onItem) {
        //TODO: merge with RandomValueRange.getTooltip(operation)
        if(onItem) {
            AttributeModifier modifier = this.getModifier();
            if(modifier == null) return "";
            double amount = modifier.getAmount() * (modifier.getOperation() == 0 ? 1.0D : 100.0D);
            if(amount > 0.0D) return I18n.format("attribute.modifier.plus." + modifier.getOperation(), ItemStack.DECIMALFORMAT.format(amount), I18n.format("attribute.name." + this.getAttribute()));
            else if (amount < 0.0D) return I18n.format("attribute.modifier.take." + modifier.getOperation(), ItemStack.DECIMALFORMAT.format(-amount), I18n.format("attribute.name." + this.getAttribute()));
            else return "";
        }
        else {
            if(this.getAmountRange().getMax() == this.getAmountRange().getMin()) {
                double amount = this.getAmountRange().getMin() * (this.getOperation() == 0 ? 1.0D : 100.0D);
                if(amount > 0.0D) return I18n.format("attribute.modifier.plus." + this.getOperation(), ItemStack.DECIMALFORMAT.format(amount), I18n.format("attribute.name." + this.getAttribute()));
                else if(amount < 0.0D) return I18n.format("attribute.modifier.take." + this.getOperation(), ItemStack.DECIMALFORMAT.format(-amount), I18n.format("attribute.name." + this.getAttribute()));
                else return "";
            }
            else {
                double min = this.getAmountRange().getMin() * (this.getOperation() == 0 ? 1.0D : 100.0D);
                double max = this.getAmountRange().getMax() * (this.getOperation() == 0 ? 1.0D : 100.0D);
                if(min >= 0.0D) return I18n.format("socketed.modifier.plus.plus." + this.getOperation(), ItemStack.DECIMALFORMAT.format(min), ItemStack.DECIMALFORMAT.format(max), I18n.format("attribute.name." + this.getAttribute()));
                else if(min < 0.0D && max >=0.0D) return I18n.format("socketed.modifier.take.plus." + this.getOperation(), ItemStack.DECIMALFORMAT.format(-min), ItemStack.DECIMALFORMAT.format(max), I18n.format("attribute.name." + this.getAttribute()));
                else if(min < 0.0D && max < 0.0D) return I18n.format("socketed.modifier.take.take." + this.getOperation(), ItemStack.DECIMALFORMAT.format(-min), ItemStack.DECIMALFORMAT.format(-max), I18n.format("attribute.name." + this.getAttribute()));
                return "";
            }
        }
    }
    
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    @Override
    public AttributeGemEffect instantiate() {
        return new AttributeGemEffect(this);
    }

    private static final List<IAttribute> handSkipAttributes = Arrays.asList(SharedMonsterAttributes.ATTACK_DAMAGE, SharedMonsterAttributes.ATTACK_SPEED, EntityPlayer.REACH_DISTANCE);
    private static final List<IAttribute> bodySkipAttributes = Arrays.asList(SharedMonsterAttributes.ARMOR, SharedMonsterAttributes.ARMOR_TOUGHNESS);

    @Override
    public void onEquip(EntityPlayer player, ItemStack stack) {
        if(modifier == null) return;

        String attribute = this.getAttribute();
        IAttributeInstance attrInstance = player.getAttributeMap().getAttributeInstanceByName(attribute);
        if(attrInstance == null) return;

        //Damage/Speed/Reach Attributes applied on weapons themselves are handled in ItemMixin for proper handling/compat with offhand mods like RLCombat
        if(slotType.isSlotValid(SocketedSlotTypes.HAND) && handSkipAttributes.contains(attrInstance.getAttribute())) {
            return;
        }

        //Armor/Armor Toughness Attributes applied on armor themselves are handled in ItemMixin for proper handling/compat with slot-specific mods like FirstAid
        if(slotType.isSlotValid(SocketedSlotTypes.BODY) && bodySkipAttributes.contains(attrInstance.getAttribute())) {
            return;
        }

        if(!attrInstance.hasModifier(modifier)) {
            attrInstance.applyModifier(modifier);
        }
    }

    @Override
    public void onUnequip(EntityPlayer player, ItemStack stack) {
        if(modifier == null) return;

        String attribute = this.getAttribute();
        IAttributeInstance attrInstance = player.getAttributeMap().getAttributeInstanceByName(attribute);
        if(attrInstance == null) return;

        //Damage/Speed/Reach Attributes applied on weapons themselves are handled in ItemMixin for proper handling/compat with offhand mods like RLCombat
        if(slotType.isSlotValid(SocketedSlotTypes.HAND) && handSkipAttributes.contains(attrInstance.getAttribute())) {
            return;
        }

        //Armor/Armor Toughness Attributes applied on armor themselves are handled in ItemMixin for proper handling/compat with slot-specific mods like FirstAid
        if(slotType.isSlotValid(SocketedSlotTypes.BODY) && bodySkipAttributes.contains(attrInstance.getAttribute())) {
            return;
        }

        if(attrInstance.hasModifier(modifier)) {
            attrInstance.removeModifier(modifier);
        }
    }

    /**
     * AttributeName: Required
     * AmountRage: Required
     * Operation: Required
     */
    @Override
    public boolean validate() {
        if(super.validate()) {
            if(this.attributeName == null || this.attributeName.isEmpty()) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, attribute name null or empty");
            else if(this.amountRange == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, " + this.attributeName + ", amount range invalid");
            else if(this.operation == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, " + this.attributeName + ", operation must be defined");
            else if(this.operation < 0 || this.operation > 2) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, " + this.attributeName + ", operation must be 0, 1, or 2");
            else return true;
        }
        return false;
    }
    
    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if(this.modifier != null) {
            nbt.setString("UUID", this.modifier.getID().toString());
            nbt.setDouble("Amount", this.modifier.getAmount());
        }
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if(nbt.hasKey("UUID")) {
            this.modifier = new AttributeModifier(UUID.fromString(nbt.getString("UUID")),Socketed.MODID + "GemEffect", nbt.getDouble("Amount"), this.operation);
        }
    }
}
