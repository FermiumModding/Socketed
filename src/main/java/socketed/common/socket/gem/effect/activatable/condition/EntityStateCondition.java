package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.EntityPropertyCondition;

import javax.annotation.Nullable;
import java.util.Arrays;

public class EntityStateCondition extends EntityPropertyCondition {

	public static final String TYPE_NAME = "Entity State";

	public enum EntityState {
		BURNING,
		MOUNTED,
		WET,
		SNEAKING,
		SPRINTING,
		ON_GROUND,
		AIRBORNE,
		IN_WATER,
		IN_LAVA,
		ON_LADDER,
		ELYTRA_FLYING,
		USING_ITEM,
		BLOCKING
	}

	@SerializedName("State")
	protected final EntityState state;

	public EntityStateCondition(EntityState state, boolean checkForPlayer) {
		super(checkForPlayer);
		this.state = state;
	}

	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		EntityLivingBase entity = determineAffectedEntity(playerSource, effectTarget);
		switch(this.state) {
			case BURNING: return entity.isBurning();
			case MOUNTED: return entity.isRiding();
			case WET: return entity.isWet();
			case SNEAKING: return entity.isSneaking();
			case SPRINTING: return entity.isSprinting();
			case ON_GROUND: return entity.onGround;
			case AIRBORNE: return !entity.onGround && !entity.isInWater() && !entity.isInLava() && !entity.isOnLadder() && !entity.isRiding();
			case IN_WATER: return entity.isInWater();
			case IN_LAVA: return entity.isInLava();
			case ON_LADDER: return entity.isOnLadder();
			case ELYTRA_FLYING: return entity.isElytraFlying();
			case USING_ITEM: return entity.isHandActive();
			case BLOCKING: return entity.isActiveItemStackBlocking();
			default: return false;
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * State: Required
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.state == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, state must be one of " + Arrays.toString(EntityState.values()));
			else return true;
		}
		return false;
	}
}