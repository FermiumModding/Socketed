package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nullable;
import java.util.List;

public class DisableShieldGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Disable Shield";

	@SerializedName("Duration")
	private Integer duration;

	public DisableShieldGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, int duration, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.duration = duration;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.disableshield", ItemStack.DECIMALFORMAT.format(this.duration / 20.0F));
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;
		if(!effectTarget.isHandActive()) return;
		ItemStack active = effectTarget.getActiveItemStack();
		if(active.isEmpty() || !active.getItem().isShield(active, effectTarget)) return;

		if(effectTarget instanceof EntityPlayer) {
			((EntityPlayer)effectTarget).getCooldownTracker().setCooldown(active.getItem(), this.duration);
		}
		effectTarget.resetActiveHand();
		effectTarget.world.setEntityState(effectTarget, (byte)30);
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Duration: Optional, default 100 (same as axes)
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.duration == null) this.duration = 100;
			if(this.duration < 1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, duration can not be less than 1");
			else return true;
		}
		return false;
	}
}