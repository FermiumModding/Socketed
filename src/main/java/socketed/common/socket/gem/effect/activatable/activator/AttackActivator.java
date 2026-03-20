package socketed.common.socket.gem.effect.activatable.activator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.util.SocketedUtil;
import socketed.common.socket.gem.effect.activatable.condition.DamageSourceCondition;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;

import javax.annotation.Nullable;

public abstract class AttackActivator extends GenericActivator {
	
	public enum EventType {
		ATTACK,
		HURT,
		DAMAGE
	}

	@SerializedName("Directly Activated")
	protected Boolean directlyActivated;
	@SerializedName("Event Type")
	protected EventType eventType;

	public AttackActivator(@Nullable GenericCondition condition, boolean directlyActivated, EventType eventType) {
		super(condition);
		this.directlyActivated = directlyActivated;
		this.eventType = eventType;
	}

	/**
	 * DirectlyActivated: Optional, default false
	 * Event Type: Optional, default HURT
	 */
	@Override
	public boolean validate() {
		if(this.directlyActivated == null) this.directlyActivated = false;
		if(this.eventType == null) this.eventType = EventType.HURT;

		return super.validate();
	}
	
	/**
	 * Called during LivingAttackEvent/LivingHurtEvent/LivingDamageEvent
	 * Target and attacker can not be the same entity
	 * If the callback is cancelled, cancels the LivingHurtEvent
	 * @param effect the effect parent of this activator
	 * @param callback the GenericEvent callback to allow for the effect to cancel the LivingHurtEvent and conditions to check the DamageSource
	 * @param player the player that is the source of the effect
	 * @param other the other entity involved in the attack event, can be attacker (Attacked) or target (Attacking)
	 * @param directlyActivated if the source of the effect is the attacker's weapon rather than the player's effect cache
	 */
	public void attemptAttackActivation(ActivatableGemEffect effect, GenericEventCallback<? extends Event> callback, EntityPlayer player, EntityLivingBase other, boolean directlyActivated, EventType type) {
		//Check if direct activation is required
		if(directlyActivated != this.directlyActivated) return;
		//Check if it's in the correct event
		if(type != this.eventType) return;
		if(this.testCondition(callback, player, other)) {
			effect.affectTargets(callback, player, other);
		}
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {
		
		/**
		 * Event handling for AttackActivators
		 */
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingAttack(LivingAttackEvent event) {
			handleEvent(event, event.getSource(), EventType.ATTACK, new GenericEventCallback<>(event));
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingHurt(LivingHurtEvent event) {
			handleEvent(event, event.getSource(), EventType.HURT, new GenericEventCallback<>(event));
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingDamage(LivingDamageEvent event) {
			handleEvent(event, event.getSource(), EventType.DAMAGE, new GenericEventCallback<>(event));
		}

		private static void handleEvent(LivingEvent event, DamageSource source, EventType type, GenericEventCallback<? extends LivingEvent> callback) {
			if(event.getEntityLiving() == null) return;
			if(event.getEntityLiving().world.isRemote) return;
			if(source == null) return;
			
			//Prioritize checking the direct attacker, prevent ranged pet attacks from triggering attacker effects such as lycanites
			boolean isMelee = DamageSourceCondition.isDamageSourceMelee(source);
			boolean isRanged = !isMelee && DamageSourceCondition.isDamageSourceRanged(source);
			
			EntityLivingBase target = event.getEntityLiving();
			EntityLivingBase attacker;
			if(isMelee) attacker = (EntityLivingBase)source.getImmediateSource();
			else if(isRanged) attacker = (EntityLivingBase)source.getTrueSource();
			else return;
			
			//Dont trigger on self damage if that manages to happen
			if(target == attacker) return;

			//AttackedActivator handling
			if(target instanceof EntityPlayer) {
				handleAttacked(callback, (EntityPlayer)target, attacker, type);
			}
			
			//Return before offensive effects if defensive effects cancelled attack
			if(callback.getEvent().isCanceled()) {
				return;
			}
			
			//AttackingActivator handling
			if(attacker instanceof EntityPlayer) {
				handleAttacking(callback, (EntityPlayer)attacker, target, type);
			}
		}
		
		private static void handleAttacked(GenericEventCallback<? extends LivingEvent> callback, EntityPlayer player, EntityLivingBase attacker, EventType type) {
			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;
			
			//Handle cached effects
			SocketedUtil.filterForActivator(cachedEffects.getActiveEffects(), AttackedActivator.class)
					.forEach(effect -> {
						AttackedActivator activator = (AttackedActivator) effect.getActivator();
						activator.attemptAttackActivation(effect, callback, player, attacker, false, type);
					});

			//TODO: FirstAid directlyActivated compat
			//TODO: could do direct activation of active shield
		}
		
		private static void handleAttacking(GenericEventCallback<? extends LivingEvent> callback, EntityPlayer player, EntityLivingBase target, EventType type) {
			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;
			
			//Handle cached effects
			SocketedUtil.filterForActivator(cachedEffects.getActiveEffects(), AttackingActivator.class)
					.forEach(effect ->{
						AttackingActivator activator = (AttackingActivator) effect.getActivator();
						activator.attemptAttackActivation(effect, callback, player, target, false, type);
					});
			
			//Handle direct activation effects
			//RLCombat swaps offhand into mainhand before posting LivingAttack for offhand attacks
			ItemStack weaponStack = player.getHeldItemMainhand();
			if(weaponStack.isEmpty()) return;
			
			ICapabilitySocketable sockets = weaponStack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
			if(sockets == null) return;
			
			//TODO: RLCombat compat to get whether the attack is being posted from mainhand or offhand for more accurate slot type check
			SocketedUtil.filterForActivator(sockets.getAllActiveEffects(SocketedSlotTypes.HAND), AttackingActivator.class)
					.forEach(effect -> {
						AttackingActivator activator = (AttackingActivator) effect.getActivator();
						activator.attemptAttackActivation(effect, callback, player, target, true, type);
					});
		}
	}
}