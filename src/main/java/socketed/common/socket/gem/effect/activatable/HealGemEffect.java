package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nullable;
import java.util.List;

public class HealGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Heal";

	@SerializedName("Amount")
	private Float amount;

	@SerializedName("Damage Percent")
	private Float damagePercent;

	public HealGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, float amount, float damagePercent, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.amount = amount;
		this.damagePercent = damagePercent;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		String flat = ItemStack.DECIMALFORMAT.format(this.amount);
		String percent = ItemStack.DECIMALFORMAT.format(this.damagePercent * 100.0F);
		if(this.amount > 0 && this.damagePercent > 0) return I18n.format("socketed.tooltip.effect.heal.both", flat, percent);
		if(this.damagePercent > 0) return I18n.format("socketed.tooltip.effect.heal.percent", percent);
		return I18n.format("socketed.tooltip.effect.heal", flat);
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;

		float healAmount = this.amount;
		if(this.damagePercent > 0 && callback instanceof GenericEventCallback) {
			healAmount += getEventDamage(((GenericEventCallback<?>)callback).getEvent()) * this.damagePercent;
		}
		if(healAmount > 0) effectTarget.heal(healAmount);
	}

	private static float getEventDamage(Event event) {
		if(event instanceof LivingHurtEvent) return ((LivingHurtEvent)event).getAmount();
		if(event instanceof LivingDamageEvent) return ((LivingDamageEvent)event).getAmount();
		return 0.0F;
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Amount: Optional, default 0
	 * Damage Percent: Optional, default 0, between 0 and 1, percent of the activating event's damage, requires a HURT or DAMAGE event
	 * At least one must be above 0
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.amount == null) this.amount = 0.0F;
			if(this.damagePercent == null) this.damagePercent = 0.0F;

			if(!Float.isFinite(this.amount) || this.amount < 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, amount can not be negative");
			else if(!Float.isFinite(this.damagePercent) || this.damagePercent < 0 || this.damagePercent > 1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, damage percent must be between 0.0 and 1.0");
			else if(this.amount <= 0 && this.damagePercent <= 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, amount or damage percent must be above 0");
			else return true;
		}
		return false;
	}
}