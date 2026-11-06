package socketed.common.socket.gem.effect.activatable.activator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;

import javax.annotation.Nullable;

public class RangedShotActivator extends GenericActivator {

	public static final String TYPE_NAME = "Ranged Shot";

	@SerializedName("Directly Activated")
	protected Boolean directlyActivated;

	@SerializedName("Minimum Charge")
	protected Float minimumCharge;

	public RangedShotActivator(@Nullable GenericCondition condition, boolean directlyActivated, float minimumCharge) {
		super(condition);
		this.directlyActivated = directlyActivated;
		this.minimumCharge = minimumCharge;
	}

	/**
	 * Called on ArrowLooseEvent
	 * @param effect the effect parent of this activator
	 * @param callback the ArrowLooseEvent callback, allows effects to cancel the shot
	 * @param player the player that is the source of the effect and that is shooting
	 * @param velocity the arrow velocity from the bow's charge, between 0 and 1
	 * @param directlyActivated if the source of the effect is the bow being shot rather than the player's effect cache
	 */
	protected void attemptRangedShotActivation(ActivatableGemEffect effect, GenericEventCallback<ArrowLooseEvent> callback, EntityPlayer player, float velocity, boolean directlyActivated) {
		if(directlyActivated != this.directlyActivated) return;
		if(velocity < this.minimumCharge) return;
		if(this.testCondition(callback, player, player)) {
			effect.affectTargets(callback, player, player);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.rangedshot");
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Directly Activated: Optional, default false
	 * Minimum Charge: Optional, default 0, between 0 and 1 (1 is a fully drawn bow)
	 */
	@Override
	public boolean validate() {
		if(this.directlyActivated == null) this.directlyActivated = false;
		if(this.minimumCharge == null) this.minimumCharge = 0.0F;

		if(this.minimumCharge < 0 || this.minimumCharge > 1) {
			Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Activator, minimum charge must be between 0.0 and 1.0");
			return false;
		}
		return super.validate();
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {

		/**
		 * Event handling for RangedShotActivators
		 */
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onArrowLoose(ArrowLooseEvent event) {
			EntityPlayer player = event.getEntityPlayer();
			if(player == null || player.world.isRemote) return;
			if(!event.hasAmmo()) return;
			if(event.getCharge() < 0) return;
			float velocity = ItemBow.getArrowVelocity(event.getCharge());
			if(velocity < 0.1F) return;

			GenericEventCallback<ArrowLooseEvent> callback = new GenericEventCallback<>(event);

			//Handle cached effects
			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects != null) {
				for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(RangedShotActivator.class)) {
					((RangedShotActivator)effect.getActivator()).attemptRangedShotActivation(effect, callback, player, velocity, false);
				}
			}

			if(event.isCanceled()) return;

			//Handle direct activation effects from the bow being shot
			ItemStack bow = event.getBow();
			if(bow.isEmpty()) return;
			ICapabilitySocketable sockets = bow.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
			if(sockets == null) return;

			EnumHand hand = player.getActiveHand();
			for(GenericGemEffect effect : sockets.getAllActiveEffects(hand == EnumHand.OFF_HAND ? SocketedSlotTypes.OFFHAND : SocketedSlotTypes.MAINHAND)) {
				if(effect instanceof ActivatableGemEffect && ((ActivatableGemEffect)effect).getActivator() instanceof RangedShotActivator) {
					RangedShotActivator activator = (RangedShotActivator)((ActivatableGemEffect)effect).getActivator();
					activator.attemptRangedShotActivation((ActivatableGemEffect)effect, callback, player, velocity, true);
				}
			}
		}
	}
}