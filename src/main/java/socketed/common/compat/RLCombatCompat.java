package socketed.common.compat;

import bettercombat.mod.event.RLCombatModifyDamageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import socketed.common.socket.gem.effect.activatable.condition.AttackCooldownCondition;

import java.util.Map;
import java.util.WeakHashMap;

public class RLCombatCompat {
    private static final Map<EntityPlayer, RecordedHand> recordedHands = new WeakHashMap<>();

    //true if the player's last RLCombat attack this tick was offhand
    public static boolean isOffhandAttack(EntityPlayer player) {
        RecordedHand recorded = recordedHands.get(player);
        return recorded != null && recorded.tick == player.ticksExisted && recorded.offhand;
    }

    //record hand sensitive cooldown strength and attacking hand
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRLCombatModifyDamagePre(RLCombatModifyDamageEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if(player == null || player.world.isRemote) return;
        AttackCooldownCondition.recordStrength(player, event.getCooledStrength());
        recordedHands.put(player, new RecordedHand(event.getOffhand(), player.ticksExisted));
    }

    private static class RecordedHand {
        private final boolean offhand;
        private final int tick;

        private RecordedHand(boolean offhand, int tick) {
            this.offhand = offhand;
            this.tick = tick;
        }
    }
}
