package socketed.common.socket.gem.effect.activatable.activator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class FallActivator extends GenericActivator {

	public static final String TYPE_NAME = "Fall";

	@SerializedName("Minimum Distance")
	protected Float minimumDistance;

	public FallActivator(@Nullable GenericCondition condition, float minimumDistance) {
		super(condition);
		this.minimumDistance = minimumDistance;
	}

	protected void attemptFallActivation(ActivatableGemEffect effect, GenericEventCallback<LivingFallEvent> callback, EntityPlayer player, float distance) {
		if(distance < this.minimumDistance) return;
		if(this.testCondition(callback, player, player)) {
			effect.affectTargets(callback, player, player);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.fall", ItemStack.DECIMALFORMAT.format(this.minimumDistance));
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Minimum Distance: Optional, default 3.0 (vanilla fall damage starts above 3 blocks)
	 */
	@Override
	public boolean validate() {
		if(this.minimumDistance == null) this.minimumDistance = 3.0F;
		if(this.minimumDistance < 0) {
			Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Activator, minimum distance can not be negative");
			return false;
		}
		return super.validate();
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {

		/**
		 * Event Handling for FallActivators
		 */
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onLivingFall(LivingFallEvent event) {
			if(!(event.getEntityLiving() instanceof EntityPlayer)) return;
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			if(player.world.isRemote) return;

			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects == null) return;

			GenericEventCallback<LivingFallEvent> callback = new GenericEventCallback<>(event);
			for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(FallActivator.class)) {
				((FallActivator)effect.getActivator()).attemptFallActivation(effect, callback, player, event.getDistance());
			}
		}
	}
}