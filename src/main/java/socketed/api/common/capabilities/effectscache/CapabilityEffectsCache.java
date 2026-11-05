package socketed.api.common.capabilities.effectscache;

import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.slot.ISlotType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityEffectsCache implements ICapabilityEffectsCache {
    private final List<GenericGemEffect> activeEffects = new ArrayList<>();
    private final List<GenericGemEffect> activeEffectsView = Collections.unmodifiableList(this.activeEffects);
    private final Map<ISlotType, List<GenericGemEffect>> slotEffects = new HashMap<>();
    private final Map<Class<? extends GenericActivator>, List<ActivatableGemEffect>> effectsByActivator = new HashMap<>();

    public CapabilityEffectsCache(){
    }

    @Override
    public List<GenericGemEffect> getActiveEffects() {
        return activeEffectsView;
    }

    @Override
    public List<ActivatableGemEffect> getEffectsForActivator(Class<? extends GenericActivator> activatorType) {
        List<ActivatableGemEffect> effects = effectsByActivator.get(activatorType);
        if(effects == null) {
            effects = new ArrayList<>();
            for(GenericGemEffect effect : activeEffects) {
                if(effect instanceof ActivatableGemEffect && activatorType.isInstance(((ActivatableGemEffect)effect).getActivator())) {
                    effects.add((ActivatableGemEffect)effect);
                }
            }
            effectsByActivator.put(activatorType, effects);
        }
        return effects;
    }

    @Override
    public void removeEffects(List<GenericGemEffect> oldEffects) {
        activeEffects.removeAll(oldEffects);
        effectsByActivator.clear();
    }

    @Override
    public void addEffects(List<GenericGemEffect> newEffects) {
        activeEffects.addAll(newEffects);
        effectsByActivator.clear();
    }

    @Override
    public void setSlotEffects(ISlotType slotType, List<GenericGemEffect> effects) {
        List<GenericGemEffect> previousEffects = slotEffects.put(slotType, effects);
        if(previousEffects != null) {
            for(GenericGemEffect effect : previousEffects) {
                activeEffects.remove(effect);
            }
        }
        activeEffects.addAll(effects);
        effectsByActivator.clear();
    }
}