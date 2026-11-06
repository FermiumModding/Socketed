package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CleanseGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Cleanse";

	public enum CleanseType {
		HARMFUL,
		BENEFICIAL,
		ALL
	}

	@SerializedName("Cleanse Type")
	private CleanseType cleanseType;

	@SerializedName("Potion Name")
	private final String potionName;

	private transient Potion potion;

	public CleanseGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, CleanseType cleanseType, @Nullable String potionName, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.cleanseType = cleanseType;
		this.potionName = potionName;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		if(this.potion != null) return I18n.format("socketed.tooltip.effect.cleanse.potion", I18n.format(this.potion.getName()));
		return I18n.format("socketed.tooltip.effect.cleanse." + this.cleanseType.name().toLowerCase(Locale.ROOT));
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;

		if(this.potion != null) {
			effectTarget.removePotionEffect(this.potion);
			return;
		}

		List<PotionEffect> effects = new ArrayList<>(effectTarget.getActivePotionEffects());
		for(PotionEffect effect : effects) {
			//Skip removing effects that are set as not being curable
			if(effect.getCurativeItems().isEmpty()) continue;
			boolean harmful = effect.getPotion().isBadEffect();
			if(this.cleanseType == CleanseType.ALL || (this.cleanseType == CleanseType.HARMFUL) == harmful) {
				effectTarget.removePotionEffect(effect.getPotion());
			}
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Cleanse Type: Optional, default HARMFUL
	 * Potion Name: Optional, overrides Cleanse Type
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.cleanseType == null) this.cleanseType = CleanseType.HARMFUL;
			if(this.potionName != null && !this.potionName.isEmpty()) {
				this.potion = Potion.getPotionFromResourceLocation(this.potionName);
				if(this.potion == null) {
					Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, " + this.potionName + ", potion does not exist");
					return false;
				}
			}
			return true;
		}
		return false;
	}
}