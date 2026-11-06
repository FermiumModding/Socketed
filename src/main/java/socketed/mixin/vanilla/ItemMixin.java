package socketed.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;

@Mixin(Item.class)
public abstract class ItemMixin {
	
	/**
	 * Writes and attaches the socketable capability to an ItemStack's normal nbt tag to allow for syncing
	 */
	@ModifyReturnValue(
			method = "getNBTShareTag",
			at = @At("RETURN"),
			remap = false
	)
	private NBTTagCompound socketed_vanillaItem_getNBTShareTag(NBTTagCompound original, ItemStack stack) {
		return CapabilitySocketableHandler.writeSyncTag(stack, original);
	}
	
	/**
	 * Reads the socketable capability from an ItemStack's normal nbt tag to allow for syncing
	 * Modifies the tag, as a tag created only for syncing should not be set on the stack
	 */
	@ModifyVariable(
			method = "readNBTShareTag",
			at = @At("HEAD"),
			argsOnly = true,
			remap = false
	)
	private NBTTagCompound socketed_vanillaItem_readNBTShareTag(NBTTagCompound nbt, @Local(argsOnly = true) ItemStack stack) {
		return CapabilitySocketableHandler.readSyncTag(stack, nbt);
	}
}