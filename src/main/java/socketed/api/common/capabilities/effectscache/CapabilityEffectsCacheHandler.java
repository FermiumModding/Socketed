package socketed.api.common.capabilities.effectscache;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import socketed.Socketed;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.common.socket.gem.effect.AttributeGemEffect;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CapabilityEffectsCacheHandler {
    
    public static final ResourceLocation CAP_EFFECTS_CACHE_KEY = new ResourceLocation(Socketed.MODID, "effects_cache");
    
    @CapabilityInject(ICapabilityEffectsCache.class)
    public static Capability<ICapabilityEffectsCache> CAP_EFFECTS_CACHE;
    
    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(ICapabilityEffectsCache.class, new Storage(), CapabilityEffectsCache::new);
    }
    
    @Mod.EventBusSubscriber
    public static class EventHandler {
        
        //No need to subscribe to PlayerEvent.Clone (death/end portal) because the new player entity will fire living equipment change events anyway
        @SubscribeEvent
        public static void attachCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if(!(event.getObject() instanceof EntityPlayer)) return;
            //For now, assume effects being cached are server-side only
            if(event.getObject().world.isRemote) return;
            if(event.getObject().hasCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null)) return;
            event.addCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE_KEY, new CapabilityEffectsCacheHandler.Provider());
        }
        
        private static final List<IAttribute> handSkipAttributes = Arrays.asList(SharedMonsterAttributes.ATTACK_DAMAGE, SharedMonsterAttributes.ATTACK_SPEED, EntityPlayer.REACH_DISTANCE);
        private static final List<IAttribute> bodySkipAttributes = Arrays.asList(SharedMonsterAttributes.ARMOR, SharedMonsterAttributes.ARMOR_TOUGHNESS);
        
        //This handling assumes that gems cant be added or removed while wearing the item
        //This doesn't fire when swapping from one item to another of same item type in creative mode for whatever reason
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
            if(!(event.getEntityLiving() instanceof EntityPlayer)) return;
            if(event.getEntityLiving().world.isRemote) return;
            EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            
            EntityEquipmentSlot slot = event.getSlot();
            ItemStack stackOld = event.getFrom();
            ItemStack stackNew = event.getTo();
            //Reskillable changes the value behind the "to" itemstack to empty if requirements are not met, but it shouldn't matter
            //As its unequipping the armor only on next tick, so event gets triggered twice (equip, unequip)
            
            //As this event is only posted related to EntityEquipmentSlot, switching based on it doesnt matter much
            //Mods that add slots will need their own equipment change handling
            ISlotType slotType = SocketedSlotTypes.fromEntityEquipmentSlot(slot);
            if(slotType == SocketedSlotTypes.NONE) return;
            
            //TODO: Retest and ensure attributes applied server-side-only are synced automatically to client
            ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
            if(cachedEffects == null) return;
            
            //Remove all modifiers that were from removed item and uncache previous active non-attribute effects
            List<GenericGemEffect> effectsToUncache = new ArrayList<>();
            ICapabilitySocketable capOld = stackOld.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
            if(capOld != null) {
                List<GenericGemEffect> effectsOld = capOld.getAllActiveEffects(slotType);
                for(GenericGemEffect effect : effectsOld) {
                    if(effect instanceof AttributeGemEffect) {
                        AttributeGemEffect attrEffect = (AttributeGemEffect)effect;
                        AttributeModifier modifier = attrEffect.getModifier();
                        if(modifier == null) continue;
                        
                        String attribute = attrEffect.getAttribute();
                        IAttributeInstance attrInstance = player.getAttributeMap().getAttributeInstanceByName(attribute);
                        if(attrInstance == null) continue;
                        
                        //Damage/Speed/Reach Attributes applied on weapons themselves are handled in ItemMixin for proper handling/compat with offhand mods like RLCombat
                        if(slotType.isSlotValid(SocketedSlotTypes.HAND) && handSkipAttributes.contains(attrInstance.getAttribute())) {
                            continue;
                        }
                        
                        //Armor/Armor Toughness Attributes applied on armor themselves are handled in ItemMixin for proper handling/compat with slot-specific mods like FirstAid
                        if(slotType.isSlotValid(SocketedSlotTypes.BODY) && bodySkipAttributes.contains(attrInstance.getAttribute())) {
                            continue;
                        }
                        
                        if(attrInstance.hasModifier(modifier)) {
                            attrInstance.removeModifier(modifier);
                        }
                    }
                    else {
                        //Attribute effects are handled above, so only non-attribute effects are cached in the player capability
                        effectsToUncache.add(effect);
                    }
                }
                cachedEffects.removeEffects(effectsToUncache);
            }
            
            //Apply new modifiers from new item and cache active non-attribute effects
            List<GenericGemEffect> effectsToCache = new ArrayList<>();
            ICapabilitySocketable capNew = stackNew.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
            if(capNew != null) {
                List<GenericGemEffect> effectsNew = capNew.getAllActiveEffects(slotType);
                for(GenericGemEffect effect : effectsNew) {
                    if(effect instanceof AttributeGemEffect) {
                        AttributeGemEffect attrEffect = (AttributeGemEffect)effect;
                        AttributeModifier modifier = attrEffect.getModifier();
                        if(modifier == null) continue;
                        
                        String attribute = attrEffect.getAttribute();
                        IAttributeInstance attrInstance = player.getAttributeMap().getAttributeInstanceByName(attribute);
                        if(attrInstance == null) continue;
                        
                        //Damage/Speed/Reach Attributes applied on weapons themselves are handled in ItemMixin for proper handling/compat with offhand mods like RLCombat
                        if(slotType.isSlotValid(SocketedSlotTypes.HAND) && handSkipAttributes.contains(attrInstance.getAttribute())) {
                            continue;
                        }
                        
                        //Armor/Armor Toughness Attributes applied on armor themselves are handled in ItemMixin for proper handling/compat with slot-specific mods like FirstAid
                        if(slotType.isSlotValid(SocketedSlotTypes.BODY) && bodySkipAttributes.contains(attrInstance.getAttribute())) {
                            continue;
                        }
                        
                        if(!attrInstance.hasModifier(modifier)) {
                            attrInstance.applyModifier(modifier);
                        }
                    }
                    else {
                        //Attribute effects are handled above, so only non-attribute effects are cached in the player capability
                        effectsToCache.add(effect);
                    }
                }
                cachedEffects.addEffects(effectsToCache);
            }
        }
    }

    public static class Provider implements ICapabilityProvider {
        
        private final ICapabilityEffectsCache instance;

        public Provider() {
            this.instance = new CapabilityEffectsCache();
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAP_EFFECTS_CACHE;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAP_EFFECTS_CACHE ? CAP_EFFECTS_CACHE.cast(instance) : null;
        }
    }

    public static class Storage implements Capability.IStorage<ICapabilityEffectsCache> {
        //Unused

        @Override
        public NBTBase writeNBT(Capability<ICapabilityEffectsCache> capability, ICapabilityEffectsCache instance, EnumFacing side) {
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<ICapabilityEffectsCache> capability, ICapabilityEffectsCache instance, EnumFacing side, NBTBase nbt) {
            //no op, player fires living equipment change event on join anyway
        }
    }
}