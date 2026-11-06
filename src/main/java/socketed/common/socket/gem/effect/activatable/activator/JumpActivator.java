package socketed.common.socket.gem.effect.activatable.activator;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class JumpActivator extends GenericActivator {

	public static final String TYPE_NAME = "Jump";

	public JumpActivator(@Nullable GenericCondition condition) {
		super(condition);
	}

	protected void attemptJumpActivation(ActivatableGemEffect effect, GenericEventCallback<LivingEvent.LivingJumpEvent> callback, EntityPlayer player) {
		if(this.testCondition(callback, player, player)) {
			effect.affectTargets(callback, player, player);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.jump");
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {
		
		/**
		 * Event Handling for JumpActivators
		 */
		@SubscribeEvent
		public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
			if(!(event.getEntityLiving() instanceof EntityPlayer)) return;
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			if(player.world.isRemote) return;

			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;

			GenericEventCallback<LivingEvent.LivingJumpEvent> callback = new GenericEventCallback<>(event);
			for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(JumpActivator.class)) {
				((JumpActivator)effect.getActivator()).attemptJumpActivation(effect, callback, player);
			}
		}
	}
}