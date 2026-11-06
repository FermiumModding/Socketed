package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class MoonPhaseCondition extends GenericCondition {

	public static final String TYPE_NAME = "Moon Phase";

	//Ordered by vanilla moon phase index 0-7
	public enum MoonPhase {
		FULL_MOON,
		WANING_GIBBOUS,
		THIRD_QUARTER,
		WANING_CRESCENT,
		NEW_MOON,
		WAXING_CRESCENT,
		FIRST_QUARTER,
		WAXING_GIBBOUS
	}

	@SerializedName("Moon Phase")
	protected final MoonPhase moonPhase;

	public MoonPhaseCondition(MoonPhase moonPhase) {
		super();
		this.moonPhase = moonPhase;
	}

	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		World world = effectTarget.world;
		if(!world.provider.isSurfaceWorld()) return false;
		return world.provider.getMoonPhase(world.getWorldTime()) == this.moonPhase.ordinal();
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Moon Phase: Required
	 */
	@Override
	public boolean validate() {
		if(this.moonPhase == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, moon phase must be one of FULL_MOON, WANING_GIBBOUS, THIRD_QUARTER, WANING_CRESCENT, NEW_MOON, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS");
		else return true;
		return false;
	}
}