package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.EntityPropertyCondition;

import javax.annotation.Nullable;

public class CreatureAttributeCondition extends EntityPropertyCondition {

	public static final String TYPE_NAME = "Creature Attribute";

	/**
	 * UNDEFINED, UNDEAD, ARTHROPOD, ILLAGER
	 */
	@SerializedName("Creature Attribute")
	protected final EnumCreatureAttribute creatureAttribute;

	public CreatureAttributeCondition(EnumCreatureAttribute creatureAttribute, boolean checkForPlayer) {
		super(checkForPlayer);
		this.creatureAttribute = creatureAttribute;
	}

	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		return determineAffectedEntity(playerSource, effectTarget).getCreatureAttribute() == this.creatureAttribute;
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Creature Attribute: Required, one of UNDEFINED, UNDEAD, ARTHROPOD, ILLAGER
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.creatureAttribute == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, creature attribute must be one of UNDEFINED, UNDEAD, ARTHROPOD, ILLAGER");
			else return true;
		}
		return false;
	}
}