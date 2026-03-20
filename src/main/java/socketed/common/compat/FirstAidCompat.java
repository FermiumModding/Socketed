package socketed.common.compat;

import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import socketed.api.common.capabilities.effectscache.CapabilityEffectsCacheHandler;
import socketed.api.common.capabilities.effectscache.ICapabilityEffectsCache;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;
import socketed.api.util.SocketedUtil;
import socketed.common.socket.gem.effect.activatable.activator.AttackActivator;
import socketed.common.socket.gem.effect.activatable.activator.AttackedActivator;
import socketed.common.socket.gem.effect.activatable.condition.DamageSourceCondition;

import java.util.HashSet;
import java.util.Set;

public class FirstAidCompat {
    @SubscribeEvent
    public static void onFirstAidLivingDamage(FirstAidLivingDamageEvent event) {
        AbstractPlayerDamageModel modelBefore = event.getBeforeDamage();
        AbstractPlayerDamageModel modelAfter = event.getAfterDamage();

        Set<EntityEquipmentSlot> hitParts = new HashSet<>();
        for(EnumPlayerPart part : EnumPlayerPart.values()) {
            AbstractDamageablePart partBefore = modelBefore.getFromEnum(part);
            AbstractDamageablePart partAfter = modelAfter.getFromEnum(part);
            boolean gotHit = partBefore.getAbsorption() > partAfter.getAbsorption() || partBefore.currentHealth > partAfter.currentHealth;

            if(gotHit) {
                hitParts.add(part.slot);
            }
        }

        handleEvent(event, event.getSource(), new GenericEventCallback<>(event), hitParts);
    }

    public static void handleEvent(FirstAidLivingDamageEvent event, DamageSource source, GenericEventCallback<? extends LivingEvent> callback, Set<EntityEquipmentSlot> hitParts) {
        if(event.getEntityLiving() == null) return;
        if(event.getEntityLiving().world.isRemote) return;
        if(!(event.getEntityLiving() instanceof EntityPlayer)) return; //First Aid only works for players
        if(source == null) return;

        //Prioritize checking the direct attacker, prevent ranged pet attacks from triggering attacker effects such as lycanites
        boolean isMelee = DamageSourceCondition.isDamageSourceMelee(source);
        boolean isRanged = !isMelee && DamageSourceCondition.isDamageSourceRanged(source);

        EntityLivingBase target = event.getEntityLiving();
        EntityLivingBase attacker;
        if(isMelee) attacker = (EntityLivingBase)source.getImmediateSource();
        else if(isRanged) attacker = (EntityLivingBase)source.getTrueSource();
        else return;

        //Dont trigger on self damage if that manages to happen
        if(target == attacker) return;

        //AttackedActivator handling
        handleAttacked(callback, (EntityPlayer)target, attacker, hitParts);
    }

    private static void handleAttacked(GenericEventCallback<? extends LivingEvent> callback, EntityPlayer player, EntityLivingBase attacker, Set<EntityEquipmentSlot> hitEquipmentSlots) {
        ICapabilityEffectsCache cachedEffects = player.getCapability(CapabilityEffectsCacheHandler.CAP_EFFECTS_CACHE, null);
        if(cachedEffects == null) return;

        //Handle cached effects
        SocketedUtil.filterForActivator(cachedEffects.getActiveEffects(), AttackedActivator.class)
                .filter(effect -> hitEquipmentSlots.stream().anyMatch(slot -> effect.getSlotType().isSlotValid(SocketedSlotTypes.fromEntityEquipmentSlot(slot))))
                .forEach(effect -> {
                    AttackedActivator activator = (AttackedActivator) effect.getActivator();
                    activator.attemptAttackActivation(effect, callback, player, attacker, true, AttackActivator.EventType.DAMAGE);
                });

        //TODO: could do direct activation of active shield
    }
}
