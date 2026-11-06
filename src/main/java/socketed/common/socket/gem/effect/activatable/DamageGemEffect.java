package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.mixin.vanilla.IEntityLivingBaseMixin;

import javax.annotation.Nullable;
import java.util.List;

public class DamageGemEffect extends ActivatableGemEffect {
	public static final String TYPE_NAME = "Damage";

	//recursion
	private static boolean damaging = false;

	@SerializedName("Amount")
	private Float amount;

	@SerializedName("Damage Type")
	private String damageType;

	@SerializedName("Bypass Armor")
	private Boolean bypassArmor;

	@SerializedName("Bypass IFrames")
	private Boolean bypassIFrames;

	public DamageGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, float amount, String damageType, boolean bypassArmor, boolean bypassIFrames, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.amount = amount;
		this.damageType = damageType;
		this.bypassArmor = bypassArmor;
		this.bypassIFrames = bypassIFrames;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.damage", ItemStack.DECIMALFORMAT.format(this.amount));
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote || damaging) return;
		if(effectTarget.isDead || effectTarget.getHealth() <= 0.0F) return;

		EntityDamageSource source = new EntityDamageSource(this.damageType, playerSource);
		if("thorns".equals(this.damageType)) source.setIsThornsDamage();
		if(this.bypassArmor) source.setDamageBypassesArmor();

		int prevHurtResistantTime = effectTarget.hurtResistantTime;
		float prevLastDamage = ((IEntityLivingBaseMixin)effectTarget).socketed$getLastDamage();
		damaging = true;
		try {
			if(this.bypassIFrames) effectTarget.hurtResistantTime = 0;
			effectTarget.attackEntityFrom(source, this.amount);
		}
		finally {
			damaging = false;
			if(this.bypassIFrames) {
				effectTarget.hurtResistantTime = prevHurtResistantTime;
				((IEntityLivingBaseMixin)effectTarget).socketed$setLastDamage(prevLastDamage);
			}
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Amount: Required, greater than 0
	 * Damage Type: Optional, default "player", "thorns" will not trigger attack activators
	 * Bypass Armor: Optional, default false
	 * Bypass IFrames: Optional, default false, allows the damage to apply to targets that were just hit
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.damageType == null || this.damageType.isEmpty()) this.damageType = "player";
			if(this.bypassArmor == null) this.bypassArmor = false;
			if(this.bypassIFrames == null) this.bypassIFrames = false;
			if(this.amount == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, amount must be defined");
			else if(!Float.isFinite(this.amount) || this.amount <= 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, amount must be greater than 0");
			else return true;
		}
		return false;
	}
}