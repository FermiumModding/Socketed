package socketed.api.common.capabilities.effectscache;

import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.slot.ISlotType;

import java.util.List;

public interface ICapabilityEffectsCache {

    /**
     * Read only view of active effects
     */
    List<GenericGemEffect> getActiveEffects();

    /**
     * Cached active effects for given activator
     */
    List<ActivatableGemEffect> getEffectsForActivator(Class<? extends GenericActivator> activatorType);

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