package socketed.common.socket.gem.effect.activatable.activator;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.CancelEventCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;
import socketed.common.socket.gem.effect.activatable.condition.DamageSourceCondition;

import javax.annotation.Nullable;

public class BlockingActivator extends GenericActivator {

	public static final String TYPE_NAME = "Blocking";

	@SerializedName("Directly Activated")
	protected Boolean directlyActivated;

	public BlockingActivator(@Nullable GenericCondition condition, boolean directlyActivated) {
		super(condition);
		this.directlyActivated = directlyActivated;
	}

	/**
	 * @param effect the effect parent of this activator
	 * @param callback the CancelEventCallback with the blocked DamageSource, allows effects to prevent the block
	 * @param player the player that is the source of the effect and that is blocking
	 * @param attacker the entity whose attack was blocked
	 * @param directlyActivated if the source of the effect is the shield being used rather than the player's effect cache
	 */
	protected void attemptBlockingActivation(ActivatableGemEffect effect, CancelEventCallback callback, EntityPlayer player, EntityLivingBase attacker, boolean directlyActivated) {
		if(directlyActivated != this.directlyActivated) return;
		if(this.testCondition(callback, player, attacker)) {
			effect.affectTargets(callback, player, attacker);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.activator.blocking");
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

	/**
	 * Called when vanilla has determined the player is blocking the attack with their shield
	 * @return true if the block should still happen, false if an effect canceled it
	 */
	public static boolean onShieldBlock(EntityPlayer player, DamageSource source) {
		if(player.world.isRemote) return true;

		boolean isMelee = DamageSourceCondition.isDamageSourceMelee(source);
		boolean isRanged = !isMelee && DamageSourceCondition.isDamageSourceRanged(source);
		EntityLivingBase attacker;
		if(isMelee) attacker = (EntityLivingBase)source.getImmediateSource();
		else if(isRanged) attacker = (EntityLivingBase)source.getTrueSource();
		else return true;
		if(attacker == player) return true;

		CancelEventCallback callback = new CancelEventCallback(false, source);

		//Handle cached effects
		ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
		if(cachedEffects != null) {
			for(ActivatableGemEffect effect : cachedEffects.getEffectsForActivator(BlockingActivator.class)) {
				((BlockingActivator)effect.getActivator()).attemptBlockingActivation(effect, callback, player, attacker, false);
			}
		}
		if(callback.isCancelled()) return false;

		//Handle direct activation effects from the shield being used
		ItemStack shield = player.getActiveItemStack();
		if(shield.isEmpty()) return true;
		ICapabilitySocketable sockets = shield.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
		if(sockets == null) return true;

		for(GenericGemEffect effect : sockets.getAllActiveEffects(player.getActiveHand() == EnumHand.OFF_HAND ? SocketedSlotTypes.OFFHAND : SocketedSlotTypes.MAINHAND)) {
			if(effect instanceof ActivatableGemEffect && ((ActivatableGemEffect)effect).getActivator() instanceof BlockingActivator) {
				BlockingActivator activator = (BlockingActivator)((ActivatableGemEffect)effect).getActivator();
				activator.attemptBlockingActivation((ActivatableGemEffect)effect, callback, player, attacker, true);
			}
		}
		return !callback.isCancelled();
	}
}