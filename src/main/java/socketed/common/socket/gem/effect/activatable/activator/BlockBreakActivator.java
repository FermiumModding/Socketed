package socketed.common.socket.gem.effect.activatable.activator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

public class BlockBreakActivator extends GenericActivator {

	public static final String TYPE_NAME = "Block Break";

	@SerializedName("Directly Activated")
	protected Boolean directlyActivated;

	public BlockBreakActivator(@Nullable GenericCondition condition, boolean directlyActivated) {
		super(condition);
		this.directlyActivated = directlyActivated;
	}

	/**
	 * Called on BlockEvent.BreakEvent
	 * @param effect the effect parent of this activator
	 * @param callback the BreakEvent callback, allows conditions such as Block to check the block and effects to cancel the break
	 * @param player the player that is the source of the effect and that is breaking the block
	 * @param directlyActivated if the source of the effect is the tool breaking the block rather than the player's effect cache
	 */
	protected void attemptBlockBreakActivation(ActivatableGemEffect effect, GenericEventCallback<BlockEvent.BreakEvent> callback, EntityPlayer player, boolean directlyActivated) {
		if(directlyActivated != this.directlyActivated) return;
		if(this.testCondition(callback, player, player)) {
			effect.affectTargets(callback, player, player);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.blockbreak");
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Directly Activated: Optional, default false
	 */
	@Override
	public boolean validate() {
		if(this.directlyActivated == null) this.directlyActivated = false;
		return super.validate();
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {

		/**
		 * Event handling for BlockBreakActivators
		 */
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onBlockBreak(BlockEvent.BreakEvent event) {
			EntityPlayer player = event.getPlayer();
			if(player == null || player.world.isRemote) return;

			GenericEventCallback<BlockEvent.BreakEvent> callback = new GenericEventCallback<>(event);

			//Handle cached effects
			ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
			if(cachedEffects != null) {
				for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(BlockBreakActivator.class)) {
					((BlockBreakActivator)effect.getActivator()).attemptBlockBreakActivation(effect, callback, player, false);
				}
			}

			if(event.isCanceled()) return;

			//Handle direct activation effects from the tool breaking the block
			ItemStack tool = player.getHeldItemMainhand();
			if(tool.isEmpty()) return;
			ICapabilitySocketable sockets = tool.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
			if(sockets == null) return;

			for(GenericGemEffect effect : sockets.getAllActiveEffects(SocketedSlotTypes.MAINHAND)) {
				if(effect instanceof ActivatableGemEffect && ((ActivatableGemEffect)effect).getActivator() instanceof BlockBreakActivator) {
					BlockBreakActivator activator = (BlockBreakActivator)((ActivatableGemEffect)effect).getActivator();
					activator.attemptBlockBreakActivation((ActivatableGemEffect)effect, callback, player, true);
				}
			}
		}
	}
}