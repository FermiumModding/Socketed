package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class DistanceCondition extends GenericCondition {
	public static final String TYPE_NAME = "Distance";

	@SerializedName("Distance")
	protected final Float distance;

	public DistanceCondition(float distance) {
		super();
		this.distance = distance;
	}
	
	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		return effectTarget.getDistanceSq(playerSource) <= this.distance * this.distance;
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
	@Override
	public boolean validate() {
		if(this.distance == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, distance null");
		else if(this.distance < 0) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, distance must be positive or zero");
		else return true;
		return false;
	}
}