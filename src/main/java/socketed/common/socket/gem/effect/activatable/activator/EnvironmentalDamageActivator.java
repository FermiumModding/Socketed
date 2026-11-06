package socketed.common.socket.gem.effect.activatable.activator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.common.socket.gem.effect.activatable.activator.AttackActivator.EventType;

import javax.annotation.Nullable;

public class EnvironmentalDamageActivator extends GenericActivator {

	public static final String TYPE_NAME = "Environmental Damage";

	@SerializedName("Event Type")
	protected EventType eventType;

	public EnvironmentalDamageActivator(@Nullable GenericCondition condition, EventType eventType) {
		super(condition);
		this.eventType = eventType;
	}

	public EventType getEventType() {
		return this.eventType;
	}

	/**
	 * Target is always the player taking damage
	 * If the callback is canceled, cancels the damage event
	 * @param effect the effect parent of this activator
	 * @param callback the GenericEvent callback to allow for the effect to cancel the event and conditions such as Damage Source to check the DamageSource
	 * @param player the player that is the source of the effect and that is taking damage
	 * @param type the event type that is being handled
	 */
	protected void attemptEnvironmentalDamageActivation(ActivatableGemEffect effect, GenericEventCallback<? extends LivingEvent> callback, EntityPlayer player, EventType type) {
		if(type != this.eventType) return;
		if(this.testCondition(callback, player, player)) {
			effect.affectTargets(callback, player, player);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.environmentaldamage");
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Event Type: Optional, default HURT
	 * ATTACK can be used to fully prevent the damage when canceled, but fires even while the player is invulnerable
	 */
	@Override
	public boolean validate() {
		if(this.eventType == null) this.eventType = EventType.HURT;
		return super.validate();
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {

		/**
		 * Event handling for EnvironmentalDamageActivators
		 */
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingAttack(LivingAttackEvent event) {
			if(event.getAmount() <= 0.0F) return;
			handleEvent(event, event.getSource(), EventType.ATTACK, new GenericEventCallback<>(event));
		}

		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingHurt(LivingHurtEvent event) {
			if(event.getAmount() <= 0.0F) return;
			handleEvent(event, event.getSource(), EventType.HURT, new GenericEventCallback<>(event));
		}

		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingDamage(LivingDamageEvent event) {
			handleEvent(event, event.getSource(), EventType.DAMAGE, new GenericEventCallback<>(event));
		}

		private static void handleEvent(LivingEvent event, DamageSource source, EventType type, GenericEventCallback<? extends LivingEvent> callback) {
			if(!(event.getEntityLiving() instanceof EntityPlayer)) return;
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			if(player.world.isRemote) return;
			if(source == null) return;
			if(source.getTrueSource() instanceof EntityLivingBase || source.getImmediateSource() instanceof EntityLivingBase) return;

			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;

			for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(EnvironmentalDamageActivator.class)) {
				((EnvironmentalDamageActivator)effect.getActivator()).attemptEnvironmentalDamageActivation(effect, callback, player, type);
				if(callback.isCancelled()) return;
			}
		}
	}
}