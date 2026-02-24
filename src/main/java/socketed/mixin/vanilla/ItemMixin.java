package socketed.mixin.vanilla;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.common.socket.gem.effect.AttributeGemEffect;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin {
	
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
			at = @At("RETURN"),
			remap = false
	)
	private Multimap<String,AttributeModifier> socketed_vanillaItem_getAttributeModifiers(Multimap<String, AttributeModifier> original, EntityEquipmentSlot slot, ItemStack stack) {
		if(stack.isEmpty()) return original;
		
		boolean isHand = slot == EntityEquipmentSlot.MAINHAND;
		boolean isBody = slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
		if(!isHand && !isBody) return original;
		
		EntityEquipmentSlot defaultSlot = EntityLiving.getSlotForItemStack(stack);
		if(defaultSlot != slot) return original;
		
		ICapabilitySocketable cap = stack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
		if(cap != null) {
			if(isHand) {
				List<GenericGemEffect> effects = cap.getAllActiveEffects(SocketedSlotTypes.HAND);
				for(GenericGemEffect effect : effects) {
					if(effect instanceof AttributeGemEffect) {
						AttributeGemEffect attrEffect = (AttributeGemEffect)effect;
						AttributeModifier modifier = attrEffect.getModifier();
						if(modifier == null) continue;
						
						String attribute = attrEffect.getAttribute();
						if(attribute.equals(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
							original.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), modifier);
						}
						else if(attribute.equals(SharedMonsterAttributes.ATTACK_SPEED.getName())) {
							original.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), modifier);
						}
						else if(attribute.equals(EntityPlayer.REACH_DISTANCE.getName())) {
							original.put(EntityPlayer.REACH_DISTANCE.getName(), modifier);
						}
					}
				}
			}
			else {
				List<GenericGemEffect> effects = null;
				switch(slot) {
					case HEAD: effects = cap.getAllActiveEffects(SocketedSlotTypes.HEAD); break;
					case CHEST: effects = cap.getAllActiveEffects(SocketedSlotTypes.CHEST); break;
					case LEGS: effects = cap.getAllActiveEffects(SocketedSlotTypes.LEGS); break;
					case FEET: effects = cap.getAllActiveEffects(SocketedSlotTypes.FEET); break;
				}
				for(GenericGemEffect effect : effects) {
					if(effect instanceof AttributeGemEffect) {
						AttributeGemEffect attrEffect = (AttributeGemEffect)effect;
						AttributeModifier modifier = attrEffect.getModifier();
						if(modifier == null) continue;
						
						String attribute = attrEffect.getAttribute();
						if(attribute.equals(SharedMonsterAttributes.ARMOR.getName())) {
							original.put(SharedMonsterAttributes.ARMOR.getName(), modifier);
						}
						else if(attribute.equals(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName())) {
							original.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), modifier);
						}
					}
				}
			}
		}
		return original;
	}
	
	/**
	 * Writes and attaches the socketable capability to an ItemStack's normal nbt tag to allow for syncing
	 */
	@ModifyReturnValue(
			method = "getNBTShareTag",
			at = @At("RETURN"),
			remap = false
	)
	private NBTTagCompound socketed_vanillaItem_getNBTShareTag(NBTTagCompound original, ItemStack stack) {
		if(original != null && stack.getMaxStackSize() == 1 && original.getBoolean("CanSocket")) {
			ICapabilitySocketable cap = stack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
			if(cap != null) {
				NBTBase capNBT = CapabilitySocketableHandler.CAP_SOCKETABLE.writeNBT(cap, null);
				if(capNBT != null) {
					NBTTagCompound newTag = original.copy();
					newTag.setTag("SocketCap", capNBT);
					return newTag;
				}
			}
		}
		return original;
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
		if(nbt != null && stack.getMaxStackSize() == 1 && nbt.getBoolean("CanSocket")) {
			NBTBase capNBT = nbt.getTag("SocketCap");
			if(capNBT instanceof NBTTagCompound) {
				ICapabilitySocketable cap = stack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
				if(cap != null) {
					CapabilitySocketableHandler.CAP_SOCKETABLE.readNBT(cap, null, capNBT);
					//Tag is only needed for sync, remove it to avoid bloating the non-capability tag
					nbt.removeTag("SocketCap");
				}
			}
		}
	}
}