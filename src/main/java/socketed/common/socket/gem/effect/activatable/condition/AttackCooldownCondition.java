package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;

public class AttackCooldownCondition extends GenericCondition {
	public static final String TYPE_NAME = "Attack Cooldown";

	@SerializedName("Attack Cooldown")
	protected final Float cooldown;

	public AttackCooldownCondition(float cooldown) {
		super();
		this.cooldown = cooldown;
	}
	
	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		return playerSource.getCooledAttackStrength(0.5F) >= this.cooldown; //TODO: RLCombat compat needed probably?
	}
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
	@Override
	public boolean validate() {
		if(this.cooldown == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, cooldown null");
		else if(this.cooldown < 0 || this.cooldown > 1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, cooldown must be between zero and one");
		else return true;
		return false;
	}
}
