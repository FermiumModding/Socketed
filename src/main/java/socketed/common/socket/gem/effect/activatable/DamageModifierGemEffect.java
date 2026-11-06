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
import socketed.common.socket.gem.effect.activatable.activator.AttackActivator;

import javax.annotation.Nullable;
import java.util.List;

public class DamageModifierGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Damage Modifier";

	public enum Operation {
		ADD,
		MULTIPLY
	}

	@SerializedName("Amount")
	private final Float amount;

	@SerializedName("Operation")
	private Operation operation;

	public DamageModifierGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, float amount, Operation operation, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.amount = amount;
		this.operation = operation;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		if(this.operation == Operation.MULTIPLY) {
			return I18n.format("socketed.tooltip.effect.damagemodifier.multiply", ItemStack.DECIMALFORMAT.format(this.amount));
		}
		return I18n.format("socketed.tooltip.effect.damagemodifier.add", (this.amount >= 0 ? "+" : "") + ItemStack.DECIMALFORMAT.format(this.amount));
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;
		if(!(callback instanceof GenericEventCallback)) return;
		Event event = ((GenericEventCallback<?>)callback).getEvent();

		if(event instanceof LivingHurtEvent) {
			LivingHurtEvent hurtEvent = (LivingHurtEvent)event;
			if(hurtEvent.getEntityLiving() != effectTarget) return;
			hurtEvent.setAmount(this.modify(hurtEvent.getAmount()));
		}
		else if(event instanceof LivingDamageEvent) {
			LivingDamageEvent damageEvent = (LivingDamageEvent)event;
			if(damageEvent.getEntityLiving() != effectTarget) return;
			damageEvent.setAmount(this.modify(damageEvent.getAmount()));
		}
	}

	private float modify(float damage) {
		float result = this.operation == Operation.MULTIPLY ? damage * this.amount : damage + this.amount;
		return Math.max(0.0F, result);
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Amount: Required
	 * Operation: Optional, default ADD
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.operation == null) this.operation = Operation.ADD;

			if(this.amount == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, amount must be defined");
			else if(!Float.isFinite(this.amount)) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, amount must be finite");
			else if(this.operation == Operation.MULTIPLY && this.amount < 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, multiply amount can not be negative");
			else {
				if(this.getActivator() instanceof AttackActivator && ((AttackActivator)this.getActivator()).getEventType() == AttackActivator.EventType.ATTACK) {
					Socketed.LOGGER.warn("Possible invalid " + this.getTypeName() + " Effect, the ATTACK event type has no damage to modify, use HURT or DAMAGE");
				}
				return true;
			}
		}
		return false;
	}
}