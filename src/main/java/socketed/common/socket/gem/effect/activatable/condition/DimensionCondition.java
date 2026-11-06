package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldProvider;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class DimensionCondition extends GenericCondition {

	public static final String TYPE_NAME = "Dimension";

	@SerializedName("Dimension Id")
	protected final Integer dimensionId;

	@SerializedName("Dimension Name")
	protected final String dimensionName;

	public DimensionCondition(@Nullable Integer dimensionId, @Nullable String dimensionName) {
		super();
		this.dimensionId = dimensionId;
		this.dimensionName = dimensionName;
	}

	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		WorldProvider provider = effectTarget.world.provider;
		if(this.dimensionId != null) return provider.getDimension() == this.dimensionId;
		return provider.getDimensionType().getName().equals(this.dimensionName);
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Dimension Id or Dimension Name: Required, Id is used if both are defined
	 */
	@Override
	public boolean validate() {
		if(this.dimensionId == null && (this.dimensionName == null || this.dimensionName.isEmpty())) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, must define a dimension id or dimension name");
		else return true;
		return false;
	}
}