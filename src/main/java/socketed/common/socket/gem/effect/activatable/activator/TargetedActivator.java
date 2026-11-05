package socketed.common.socket.gem.effect.activatable.activator;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class TargetedActivator extends GenericActivator {

	public static final String TYPE_NAME = "Targeted";

	public TargetedActivator(@Nullable GenericCondition condition) {
		super(condition);
	}
	
	/**
	 * Called on LivingSetAttackTargetEvent for players
	 * No callback
	 * @param effect the effect parent of this activator
	 * @param player the player that is the source of the effect
     * @param attacker the entity that is targeting the player
	 */
	protected void attemptTargetedActivation(ActivatableGemEffect effect, EntityPlayer player, EntityLivingBase attacker) {
		if(this.testCondition(null, player, attacker)) {
			effect.affectTargets(null, player, attacker);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.targeted");
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
	@Mod.EventBusSubscriber
	public static class EventHandler {
		
		/**
		 * Event handling for TargetedActivators
		 */
		@SubscribeEvent
		public static void onPlayerTargeted(LivingSetAttackTargetEvent event) {
			if(!(event.getTarget() instanceof EntityPlayer)) return;
			if(event.getTarget().world.isRemote) return;
			EntityPlayer player = (EntityPlayer)event.getTarget();
			if(event.getEntityLiving() == null) return;
			EntityLivingBase attacker = event.getEntityLiving();

			//No interference if the player is the revenge target
			if(attacker.getRevengeTarget() == player) return;
			//Copied from LevelUp/RLTweaker, probably for an edge case idk
			if(attacker.getRevengeTimer() == attacker.ticksExisted) return;
			
			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;

			for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(TargetedActivator.class)) {
				((TargetedActivator)effect.getActivator()).attemptTargetedActivation(effect, player, attacker);
			}
		}
	}
}