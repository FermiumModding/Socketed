package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;

import javax.annotation.Nullable;

public class DamageSourceCondition extends GenericCondition {
	public static final String TYPE_NAME = "Damage Source";

	@SerializedName("Require Melee")
	protected Boolean requireMelee;
	@SerializedName("Require Ranged")
	protected Boolean requireRanged;
	@SerializedName("Required Damage Type")
	protected String requiredDamageType;

	public DamageSourceCondition(boolean allowsMelee, boolean allowsRanged, String allowedDamageType) {
		super();
		this.requireMelee = allowsMelee;
		this.requireRanged = allowsRanged;
		this.requiredDamageType = allowedDamageType;
	}
	
	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(!(callback instanceof GenericEventCallback)) return false;
		try {
			Event event = ((GenericEventCallback<?>)callback).getEvent();
			DamageSource source = getSource(event);
			if(source == null) return false;
			
			if(this.requireMelee && (!isDamageSourceMelee(source) || source.isExplosion())) return false;
			else if(this.requireRanged && !isDamageSourceRanged(source)) return false;
			else return this.requiredDamageType.isEmpty() || source.getDamageType().equals(requiredDamageType);
		}
		catch(Exception exception) {
			return false;
		}
    }
	
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Require Melee: optional, default false
	 * Require Ranged: optional, default false
	 * Required Damage Type: optional, default empty
	 */
	@Override
	public boolean validate() {
		if(this.requireMelee == null) this.requireMelee = false;
		if(this.requireRanged == null) this.requireRanged = false;
		if(this.requiredDamageType == null) this.requiredDamageType = "";

		return true;
	}

	private static DamageSource getSource(Event event) throws ReflectiveOperationException {
		if(event instanceof LivingHurtEvent) return ((LivingHurtEvent)event).getSource();
		if(event instanceof LivingDamageEvent) return ((LivingDamageEvent)event).getSource();
		if(event instanceof LivingAttackEvent) return ((LivingAttackEvent)event).getSource();
		//Other events with a source such as DDD GatherDefenses, DDD DetermineDamage
		return (DamageSource)event.getClass().getMethod("getSource").invoke(event);
	}

	public static boolean isDamageSourceMelee(DamageSource source) {
		if(source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage()) return false;
		return source.getImmediateSource() instanceof EntityLivingBase;
	}

	public static boolean isDamageSourceRanged(DamageSource source) {
		return !(source.getImmediateSource() instanceof EntityLivingBase) && source.getTrueSource() instanceof EntityLivingBase;
	}
}