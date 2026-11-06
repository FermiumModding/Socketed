package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public class AttackCooldownCondition extends GenericCondition {
	public static final String TYPE_NAME = "Attack Cooldown";
	private static final Map<EntityPlayer, RecordedStrength> recordedStrengths = new WeakHashMap<>();

	@SerializedName("Attack Cooldown")
	protected final Float cooldown;

	public AttackCooldownCondition(float cooldown) {
		super();
		this.cooldown = cooldown;
	}
	
	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		return getAttackStrength(playerSource) >= this.cooldown;
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
	
	public static float getAttackStrength(EntityPlayer player) {
		RecordedStrength recorded = recordedStrengths.get(player);
		if(recorded != null && recorded.tick == player.ticksExisted) return recorded.strength;
		return player.getCooledAttackStrength(0.5F);
	}
	
	public static void recordStrength(EntityPlayer player, float strength) {
		recordedStrengths.put(player, new RecordedStrength(strength, player.ticksExisted));
	}

	@Mod.EventBusSubscriber
	public static class EventHandler {
		
		@SubscribeEvent
		public static void onAttackEntity(AttackEntityEvent event) {
			EntityPlayer player = event.getEntityPlayer();
			if(player == null || player.world.isRemote) return;
			recordStrength(player, player.getCooledAttackStrength(0.5F));
		}
	}
	
	private static class RecordedStrength {
		private final float strength;
		private final int tick;
		
		private RecordedStrength(float strength, int tick) {
			this.strength = strength;
			this.tick = tick;
		}
	}
}