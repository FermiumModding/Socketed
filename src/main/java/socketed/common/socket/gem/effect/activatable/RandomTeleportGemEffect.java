package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
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

public class RandomTeleportGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Random Teleport";

	@SerializedName("Range")
	private Integer range;

	public RandomTeleportGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, int range, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.range = range;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.randomteleport", this.range);
	}

	//chorus fruit effect
	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;
		World world = effectTarget.world;
		double startX = effectTarget.posX;
		double startY = effectTarget.posY;
		double startZ = effectTarget.posZ;
		
		for(int i = 0; i < 16; i++) {
			double x = startX + (effectTarget.getRNG().nextDouble() - 0.5D) * 2.0D * this.range;
			double y = MathHelper.clamp(startY + (effectTarget.getRNG().nextInt(2 * this.range) - this.range), 0.0D, world.getActualHeight() - 1);
			double z = startZ + (effectTarget.getRNG().nextDouble() - 0.5D) * 2.0D * this.range;

			EnderTeleportEvent event = new EnderTeleportEvent(effectTarget, x, y, z, 0.0F);
			if(MinecraftForge.EVENT_BUS.post(event)) return;

			if(effectTarget.isRiding()) effectTarget.dismountRidingEntity();
			if(effectTarget.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ())) {
				world.playSound(null, startX, startY, startZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, effectTarget.getSoundCategory(), 1.0F, 1.0F);
				effectTarget.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
				return;
			}
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Range: Optional, default 8 (same as chorus fruit), max distance in each direction
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.range == null) this.range = 8;
			if(this.range < 1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, range can not be less than 1");
			else return true;
		}
		return false;
	}
}