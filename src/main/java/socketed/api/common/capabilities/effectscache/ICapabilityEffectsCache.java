package socketed.api.common.capabilities.effectscache;

import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.ISlotType;

import java.util.List;

public interface ICapabilityEffectsCache {

    /**
     *
     */
    List<GenericGemEffect> getActiveEffects();

    /**
     *
     */
    void removeEffects(List<GenericGemEffect> oldEffects);

    /**
     *
     */
    void addEffects(List<GenericGemEffect> newEffects);

    /**
     * Replaces the effects cached for the given slot
     */
    void setSlotEffects(ISlotType slotType, List<GenericGemEffect> effects);
}