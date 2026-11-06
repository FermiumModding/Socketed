package socketed.mixin.vanilla;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLivingBase.class)
public interface IEntityLivingBaseMixin {
	@Invoker("canDropLoot")
	boolean socketed$invokeCanDropLoot();
	@Accessor("lastDamage")
	float socketed$getLastDamage();
	@Accessor("lastDamage")
	void socketed$setLastDamage(float lastDamage);
}