package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class TimeOfDayCondition extends GenericCondition {

	public static final String TYPE_NAME = "Time Of Day";

	public enum TimePreset {
		DAY(0, 12000),
		SUNSET(12000, 13000),
		NIGHT(13000, 23000),
		SUNRISE(23000, 24000);

		private final int start;
		private final int end;

		TimePreset(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}

	@SerializedName("Time")
	protected TimePreset preset;

	@SerializedName("Start Time")
	protected Integer startTime;

	@SerializedName("End Time")
	protected Integer endTime;

	public TimeOfDayCondition(@Nullable TimePreset preset, @Nullable Integer startTime, @Nullable Integer endTime) {
		super();
		this.preset = preset;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		World world = effectTarget.world;
		if(!world.provider.isSurfaceWorld()) return false;
		int time = (int)(world.getWorldTime() % 24000L);
		if(this.startTime <= this.endTime) return time >= this.startTime && time < this.endTime;
		return time >= this.startTime || time < this.endTime;
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Time: Optional, DAY, SUNSET, NIGHT, or SUNRISE, overrides Start/End Time
	 * Start Time/End Time: Required if Time is not defined, between 0 and 24000
	 */
	@Override
	public boolean validate() {
		if(this.preset != null) {
			this.startTime = this.preset.start;
			this.endTime = this.preset.end;
			return true;
		}
		if(this.startTime == null || this.endTime == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, must define Time or both Start Time and End Time");
		else if(this.startTime < 0 || this.startTime > 24000 || this.endTime < 0 || this.endTime > 24000) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, start and end time must be between 0 and 24000");
		else if(this.startTime.equals(this.endTime)) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, start and end time can not be the same");
		else return true;
		return false;
	}
}