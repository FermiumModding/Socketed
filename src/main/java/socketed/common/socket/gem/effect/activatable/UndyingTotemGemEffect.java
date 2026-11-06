package socketed.common.socket.gem.effect.activatable;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.common.socket.gem.effect.activatable.activator.DeathTotemCheckActivator;

import javax.annotation.Nullable;
import java.util.List;

public class UndyingTotemGemEffect extends ActivatableGemEffect {
	
	public static final String TYPE_NAME = "Undying Totem";
	
	public UndyingTotemGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.undyingtotem");
	}
	
	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(effectTarget != null && !effectTarget.world.isRemote) {
			effectTarget.setHealth(1.0F);
			effectTarget.clearActivePotions();
			effectTarget.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 900, 1));
			effectTarget.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 100, 1));
			if(effectTarget instanceof EntityPlayer) effectTarget.world.setEntityState(effectTarget, (byte)35);
		}
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.getActivator() instanceof DeathTotemCheckActivator) {
				Socketed.LOGGER.warn("Possible invalid " + this.getTypeName() + " Effect, does not prevent death on its own, combine it with a " + CancelEventGemEffect.TYPE_NAME + " Effect in a " + MultiEffectGemEffect.TYPE_NAME + " Effect");
			}
			return true;
		}
		return false;
	}
}