package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

public class LaunchGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Launch";

	@SerializedName("Strength")
	private Float strength;

	public LaunchGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, float strength, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.strength = strength;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.launch");
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;
		effectTarget.motionY = Math.max(effectTarget.motionY, 0.0D) + this.strength;
		effectTarget.isAirBorne = true;
		effectTarget.velocityChanged = true;
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Strength: Required, above 0
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.strength == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, strength must be defined");
			else if(!Float.isFinite(this.strength) || this.strength <= 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, strength must be above 0");
			else return true;
		}
		return false;
	}
}