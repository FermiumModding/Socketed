package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.ForgeEventFactory;
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

public class LightningGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Lightning";

	//recursion
	private static boolean striking = false;

	@SerializedName("Damage")
	private Float damage;

	@SerializedName("Sets Fire")
	private Boolean setsFire;

	@SerializedName("Natural Strike")
	private Boolean naturalStrike;

	public LightningGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, float damage, boolean setsFire, boolean naturalStrike, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.damage = damage;
		this.setsFire = setsFire;
		this.naturalStrike = naturalStrike;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.lightning", ItemStack.DECIMALFORMAT.format(this.damage));
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote || striking) return;
		striking = true;
		try {
			EntityLightningBolt bolt = new EntityLightningBolt(effectTarget.world, effectTarget.posX, effectTarget.posY, effectTarget.posZ, true);
			effectTarget.world.addWeatherEffect(bolt);
			if(this.damage > 0) {
				effectTarget.attackEntityFrom(new EntityDamageSourceIndirect(DamageSource.LIGHTNING_BOLT.getDamageType(), bolt, playerSource), this.damage);
			}
			if(this.setsFire) effectTarget.setFire(8);
			if(this.naturalStrike && !effectTarget.isDead && effectTarget.getHealth() > 0.0F && !ForgeEventFactory.onEntityStruckByLightning(effectTarget, bolt)) {
				effectTarget.onStruckByLightning(bolt);
			}
		}
		finally {
			striking = false;
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Damage: Optional, default 5 (same as vanilla lightning)
	 * Sets Fire: Optional, default false, sets the target on fire for 8 seconds like vanilla lightning
	 * Natural Strike: Optional, default false
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.damage == null) this.damage = 5.0F;
			if(this.setsFire == null) this.setsFire = false;
			if(this.naturalStrike == null) this.naturalStrike = false;
			if(!Float.isFinite(this.damage) || this.damage < 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, damage can not be negative");
			else return true;
		}
		return false;
	}
}