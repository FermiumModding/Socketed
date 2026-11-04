package socketed.common.socket.gem.effect.activatable.condition;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.EntityPropertyCondition;

import javax.annotation.Nullable;

public class IsMountedCondition extends EntityPropertyCondition {

	public static final String TYPE_NAME = "Is Mounted";

	public IsMountedCondition(boolean checkForPlayer) {
		super(checkForPlayer);
	}
	
	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		return determineAffectedEntity(playerSource, effectTarget).isRiding();
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
}