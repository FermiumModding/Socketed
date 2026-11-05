package socketed.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import socketed.common.socket.gem.effect.PlusEnchantmentGemEffect;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

	@ModifyReturnValue(
			method = "getEnchantmentLevel",
			at = @At("RETURN")
	)
	private static int modifyEnchantmentLevel(int original, @Local(argsOnly = true) Enchantment enchantment, @Local(argsOnly = true) ItemStack stack){
		return PlusEnchantmentGemEffect.getModifiedLevel(enchantment, original, stack);
	}

	@ModifyArg(
			method = "applyEnchantmentModifier(Lnet/minecraft/enchantment/EnchantmentHelper$IModifier;Lnet/minecraft/item/ItemStack;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper$IModifier;calculateModifier(Lnet/minecraft/enchantment/Enchantment;I)V"),
			index = 1
	)
	private static int socketed_vanillaEnchantmentHelper_applyEnchantmentModifier(Enchantment enchantment, int level, @Local(argsOnly = true) ItemStack stack) {
		return PlusEnchantmentGemEffect.getModifiedLevel(enchantment, level, stack);
	}
}