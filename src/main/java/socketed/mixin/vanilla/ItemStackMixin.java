package socketed.mixin.vanilla;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import socketed.common.attributes.SocketedAttributes;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;
import socketed.common.config.ForgeConfig;
import socketed.common.socket.gem.effect.AttributeGemEffect;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    
    @Shadow public abstract boolean isItemStackDamageable();
    @Shadow public abstract int getMaxDamage();

    @ModifyVariable(
            method = "attemptDamageItem",
            at = @At(value = "HEAD"),
            argsOnly = true
    )
    private int socketed_vanillaItemStack_attemptDamageItem(int amount, @Local(argsOnly = true) Random rand, @Local(argsOnly = true) EntityPlayerMP player) {
        if(player == null) return amount;
        if(!this.isItemStackDamageable()) return amount;
        
        IAttributeInstance instance = player.getAttributeMap().getAttributeInstance(SocketedAttributes.DURABILITY);
        if(instance == null) return amount;
        
        float durabilityAttribute = (float)instance.getAttributeValue();

        //Avoid math problems. With -100% durability and lower the item just breaks gg
        if(durabilityAttribute <= 0) return this.getMaxDamage();

        //With low integer values of amount (amount is always 1 for tools/weapons), this calc can fail bc of truncation
        float theoreticalNewAmount = (float)amount / durabilityAttribute;
        //Sanity check
        if(!Float.isFinite(theoreticalNewAmount)) return amount;
        int actualNewAmount = (int)theoreticalNewAmount;
        //So we just do some rng magic to fix it (same method as furnace xp calculation, way faster than what Unbreaking does)
        //(this is bernoulli, unbreaking does an approximately normal distribution)
        if(rand.nextFloat() < theoreticalNewAmount - actualNewAmount) {
            actualNewAmount++;
        }
        //Note: different to unbreaking we don't differentiate between armor and tools/weapons
        return Math.max(0, actualNewAmount);
    }
    
    /**
     * Handling for Damage/Speed/Reach Attributes for hand slots and Armor/Armor Toughness for body slots
     * Damage/Speed/Reach when socketed on either hand need to only apply modifiers to the Mainhand EntityEquipmentSlot
     * In order to work around Vanilla handling of attacking and for compat with mods like RLCombat
     * RLCombat has builtin handling for applying Mainhand attributes during offhand attacks
     * Technically makes it not possible to apply those attributes as "Mainhand Only" or "Offhand Only" but our abilities are limited for compat
     * "Mainhand Only" or "Offhand Only" for non-Damage/Speed/Reach attribute effects or non-attribute effects is still possible
     * Armor/Armor Toughness needs to be applied as attribute specific to the slot for compat with mods like First Aid
     */
    @ModifyReturnValue(
            method = "getAttributeModifiers",
            at = @At("RETURN")
    )
    private Multimap<String, AttributeModifier> socketed_vanillaItemStack_getAttributeModifiers(Multimap<String, AttributeModifier> original, EntityEquipmentSlot slot) {
        ItemStack stack = (ItemStack)(Object)this;
        if(stack.isEmpty()) return original;

        boolean isHand = slot == EntityEquipmentSlot.MAINHAND;
        boolean isBody = slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR;
        if(!isHand && !isBody) return original;

        EntityEquipmentSlot defaultSlot = EntityLiving.getSlotForItemStack(stack);
        if(defaultSlot != slot) return original;

        ICapabilitySocketable cap = stack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        if(cap == null || cap.getGemCount() <= 0) return original;

        Multimap<String, AttributeModifier> modified = HashMultimap.create(original);
        List<String> allowedAttributes = isHand ?
                Arrays.asList(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), SharedMonsterAttributes.ATTACK_SPEED.getName(), EntityPlayer.REACH_DISTANCE.getName()) :
                Arrays.asList(SharedMonsterAttributes.ARMOR.getName(), SharedMonsterAttributes.ARMOR_TOUGHNESS.getName());
        ISlotType slotType = isHand ? SocketedSlotTypes.HAND : SocketedSlotTypes.fromEntityEquipmentSlot(slot);

        for(GenericGemEffect effect : cap.getAllActiveEffects(slotType)) {
            if(!(effect instanceof AttributeGemEffect)) continue;
            AttributeGemEffect attrEffect = (AttributeGemEffect)effect;
            AttributeModifier modifier = attrEffect.getModifier();
            if(modifier == null) continue;
            if(allowedAttributes.contains(attrEffect.getAttribute())) {
                modified.put(attrEffect.getAttribute(), modifier);
            }
        }
        return modified;
    }

    @Inject(
            method = "isItemEnchantable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void socketed_vanillaItemStack_isItemEnchantable(CallbackInfoReturnable<Boolean> cir) {
        if(ForgeConfig.COMMON.enchantmentLock) {
            if(((ItemStack)(Object)this).getMaxStackSize() == 1) {
                ICapabilitySocketable cap = ((ItemStack)(Object)this).getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
                if(cap != null && cap.getSocketCount() > 0) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}