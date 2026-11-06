package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.EntityPropertyCondition;

import javax.annotation.Nullable;

public class WeatherCondition extends EntityPropertyCondition {

	public static final String TYPE_NAME = "Weather";

	public enum WeatherType {
		CLEAR,
		RAIN,
		THUNDER
	}

	@SerializedName("Weather")
	protected final WeatherType weather;

	@SerializedName("Require Exposed")
	protected Boolean requireExposed;

	public WeatherCondition(WeatherType weather, boolean requireExposed, boolean checkForPlayer) {
		super(checkForPlayer);
		this.weather = weather;
		this.requireExposed = requireExposed;
	}

	/**
	 * RAIN is true during both rain and thunderstorms, THUNDER only during thunderstorms
	 * With Require Exposed, the entity must be able to see the sky, and for RAIN/THUNDER rain must be falling at its position
	 */
	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		EntityLivingBase entity = determineAffectedEntity(playerSource, effectTarget);
		World world = entity.world;
		BlockPos pos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);

		switch(this.weather) {
			case CLEAR:
				if(world.isRaining()) return false;
				return !this.requireExposed || world.canSeeSky(pos);
			case RAIN:
				if(!world.isRaining()) return false;
				return !this.requireExposed || world.isRainingAt(pos);
			case THUNDER:
				if(!world.isThundering()) return false;
				return !this.requireExposed || world.isRainingAt(pos);
			default:
				return false;
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Weather: Required, one of CLEAR, RAIN, THUNDER
	 * Require Exposed: Optional, default false
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.requireExposed == null) this.requireExposed = false;

			if(this.weather == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, weather must be one of CLEAR, RAIN, THUNDER");
			else return true;
		}
		return false;
	}
}