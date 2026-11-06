package socketed.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
	 */
	@Inject(
			method = "readNBTShareTag",
			at = @At("HEAD"),
			remap = false
	)
	private void socketed_vanillaItem_readNBTShareTag(ItemStack stack, NBTTagCompound nbt, CallbackInfo ci) {
		CapabilitySocketableHandler.readSyncTag(stack, nbt);
	}
}