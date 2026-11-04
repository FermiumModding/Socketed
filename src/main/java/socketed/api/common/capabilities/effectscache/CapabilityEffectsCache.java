package socketed.api.common.capabilities.effectscache;

import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.slot.ISlotType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityEffectsCache implements ICapabilityEffectsCache {
    private final List<GenericGemEffect> activeEffects = new ArrayList<>();
    private final Map<ISlotType, List<GenericGemEffect>> slotEffects = new HashMap<>();

    public CapabilityEffectsCache(){
    }

    @Override
    public List<GenericGemEffect> getActiveEffects() {
        return activeEffects;
    }

    @Override
    public void removeEffects(List<GenericGemEffect> oldEffects) {
        activeEffects.removeAll(oldEffects);
    }

    @Override
    public void addEffects(List<GenericGemEffect> newEffects) {
        activeEffects.addAll(newEffects);
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
    }
}