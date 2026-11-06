package socketed.mixin.vanilla;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.common.config.ForgeConfig;

@Mixin(ContainerRepair.class)
public abstract class ContainerRepairMixin {

    @Shadow @Final private IInventory outputSlot;
    @Shadow @Final private IInventory inputSlots;
    @Shadow public int maximumCost;
    
    @Inject(
            method = "updateRepairOutput",
            at = @At("RETURN")
    )
    private void socketed_vanillaContainerRepair_updateRepairOutput(CallbackInfo ci) {
        if(!ForgeConfig.COMMON.enchantmentLock) return;
        ItemStack output = this.outputSlot.getStackInSlot(0);
        if(output.isEmpty() || !output.isItemEnchanted()) return;

        ICapabilitySocketable cap = output.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        if(cap == null || cap.getSocketCount() <= 0) return;

        ItemStack input = this.inputSlots.getStackInSlot(0);
        if(EnchantmentHelper.getEnchantments(output).equals(EnchantmentHelper.getEnchantments(input))) return;

        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
        this.maximumCost = 0;
    }
}