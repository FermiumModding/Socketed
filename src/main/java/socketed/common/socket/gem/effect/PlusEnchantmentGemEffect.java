package socketed.common.socket.gem.effect;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.util.SocketedUtil;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nullable;

public class PlusEnchantmentGemEffect extends GenericGemEffect {
    
    public static final String TYPE_NAME = "Enchantment";

    @SerializedName("Enchantment Name")
    private final String enchantmentName;

    @SerializedName("Amount")
    private final Integer amount;

    private transient Enchantment enchantment = null;

    public PlusEnchantmentGemEffect(ISlotType slotType, String enchantmentName, int amount) {
        super(slotType);
        this.enchantmentName = enchantmentName;
        this.amount = amount;
    }

    @Nullable
    public Enchantment getEnchantment() {
        return this.enchantment;
    }

    public int getAmount() {
        return this.amount;
    }
    
    public static int getModifiedLevel(Enchantment enchantment, int level, ItemStack stack) {
        if(level <= 0) return level;
        ICapabilitySocketable sockets = stack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        if(sockets == null) return level;

        int addedAmount = SocketedUtil.filterForEffectType(sockets.getAllPossibleEffects(), PlusEnchantmentGemEffect.class)
                .filter(effect -> enchantment.equals(effect.getEnchantment()))
                .mapToInt(PlusEnchantmentGemEffect::getAmount)
                .sum();

        //No clamping above
        return Math.max(level + addedAmount, enchantment.getMinLevel());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getTooltip(boolean onItem) {
        return I18n.format("socketed.tooltip.effect.plusenchantment", this.amount, I18n.format(this.enchantment.getName()));
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Enchantment Name: Required, modid:enchname
     * Amount: Required, any integer
     */
    @Override
    public boolean validate() {
        if(super.validate()) {
            if(this.enchantmentName == null || this.enchantmentName.isEmpty()) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, enchantment name null or empty");
            else {
                this.enchantment = Enchantment.getEnchantmentByLocation(this.enchantmentName);
                if(this.enchantment == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, " + this.enchantmentName + " enchantment does not exist");
                else if (this.amount == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, " + this.enchantmentName + ", amount invalid");
                else return true;
            }
        }
        return false;
    }
}