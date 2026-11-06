package socketed.mixin.vanilla;

import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.world.DifficultyInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import socketed.common.loot.DefaultSocketsGenerator;
import socketed.api.util.SocketedUtil;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin {
	
	/**
	 * In at least Vanilla, everytime setEquipmentBasedOnDifficulty is called, setEnchantmentBasedOnDifficulty is called right after
	 * Multiple classes override setEquipmentBasedOnDifficulty, making it more difficult to use
	 * However, setEnchantmentBasedOnDifficulty is only overridden by EntityWitherSkeleton
	 */
	@Inject(
			method = "setEnchantmentBasedOnDifficulty",
			at = @At("RETURN")
	)
	private void socketed_vanillaEntityLiving_setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty, CallbackInfo ci) {
		EntityLiving entity = (EntityLiving)(Object)this;
		SocketedUtil.addSocketsToStack(entity.getHeldItemMainhand(), DefaultSocketsGenerator.SocketedItemCreationContext.MOB_DROP);
		for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if(slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
				SocketedUtil.addSocketsToStack(entity.getItemStackFromSlot(slot), DefaultSocketsGenerator.SocketedItemCreationContext.MOB_DROP);
			}
		}
	}
}