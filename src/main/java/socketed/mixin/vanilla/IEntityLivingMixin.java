package socketed.mixin.vanilla;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLiving.class)
public interface IEntityLivingMixin {
	@Invoker("getLootTable")
	ResourceLocation socketed$invokeGetLootTable();
	@Accessor("deathLootTable")
	ResourceLocation socketed$getDeathLootTable();
}