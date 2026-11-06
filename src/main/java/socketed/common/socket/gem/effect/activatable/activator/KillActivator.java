package socketed.common.socket.gem.effect.activatable.activator;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.CancelEventCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class KillActivator extends GenericActivator {

	public static final String TYPE_NAME = "Kill";

	public KillActivator(@Nullable GenericCondition condition) {
		super(condition);
	}

	/**
	 * Called on LivingDeathEvent when the player is the true source of the killing damage (melee or ranged)
	 * If the callback is cancelled, cancels the death and sets the killed entity's health to a minimum of 1.0
	 * @param effect the effect parent of this activator
	 * @param callback the CancelEventCallback to allow for the effect to cancel the death
	 * @param player the player that is the source of the effect
	 * @param killed the entity that was killed
	 */
	protected void attemptKillActivation(ActivatableGemEffect effect, CancelEventCallback callback, EntityPlayer player, EntityLivingBase killed) {
		if(this.testCondition(callback, player, killed)) {
			effect.affectTargets(callback, player, killed);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.kill");
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {

		/**
		 * Event handling for KillActivators
		 * LOW priority and ignores canceled events so it does not trigger if another mod prevents the death
		 */
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingDeath(LivingDeathEvent event) {
			EntityLivingBase killed = event.getEntityLiving();
			if(killed == null || killed.world.isRemote) return;
			if(event.getSource() == null) return;
			if(!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
			if(player == killed) return;

			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;

			CancelEventCallback callback = new CancelEventCallback(false);
			for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(KillActivator.class)) {
				((KillActivator)effect.getActivator()).attemptKillActivation(effect, callback, player, killed);

				if(callback.isCancelled()) {
					event.setCanceled(true);
					if(killed.getHealth() <= 0.0F) killed.setHealth(1.0F);
					return;
				}
			}
		}
	}
}