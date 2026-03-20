package socketed.common.socket.gem.effect.activatable.target;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;

import javax.annotation.Nullable;
import java.util.List;

public class OtherAOETarget extends GenericTarget {
	
	public static final String TYPE_NAME = "Other AOE";
	
	@SerializedName("Block Range")
	protected final Integer blockRange;
	@SerializedName("Max Targets")
	protected Integer maxTargets = -1;
	
	public OtherAOETarget(@Nullable GenericCondition condition, int blockRange) {
		super(condition);
		this.blockRange = blockRange;
	}
	
	@Override
	public void affectTarget(ActivatableGemEffect effect, @Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		List<EntityLivingBase> entitiesNearby = playerSource.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(effectTarget.getPosition()).grow(this.blockRange));
		int targetedCount = 0;
		for(EntityLivingBase entity : entitiesNearby) {
			if(this.maxTargets >= 0 && targetedCount >= this.maxTargets) return;
			if(entity != playerSource && entity != effectTarget) {
				if(this.testCondition(callback, playerSource, entity)) {
					effect.performEffect(callback, playerSource, entity);
					targetedCount++;
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.target.otheraoe");
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
	/**
	 * BlockRange: Required
	 * Max Targets: Optional, default -1
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.blockRange == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Target, block range must be defined");
			else if(this.blockRange < 1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Target, block range must be greater than 0");
			else if(this.maxTargets == null) this.maxTargets = -1;
			else if(this.maxTargets < -1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Target, max targets must be greater or equal -1");
			else return true;
		}
		return false;
	}
}