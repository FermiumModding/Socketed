package socketed.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.PacketUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;

@Mixin(PacketUtil.class)
public abstract class PacketUtilMixin {

	/**
	 * Client to server item packets (creative inventory, window clicks) send the raw tag instead of the share tag.
	 * Attach the socketable capability so creative doesn't wipe it and clicked stacks match the server's share tag
	 */
	@WrapOperation(
			method = "writeItemStackFromClientToServer",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getTagCompound()Lnet/minecraft/nbt/NBTTagCompound;", remap = true),
			remap = false
	)
	private static NBTTagCompound socketed_forgePacketUtil_writeItemStackFromClientToServer(ItemStack stack, Operation<NBTTagCompound> original) {
		return CapabilitySocketableHandler.writeSyncTag(stack, original.call(stack));
	}
}
