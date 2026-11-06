package socketed.api.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ActivatableGemEffect extends GenericGemEffect {

    @SerializedName("Activator")
    protected final GenericActivator activator;
    
    @SerializedName("Targets")
    protected final List<GenericTarget> targets;
    
    @SerializedName("Tooltip Key")
    protected final String tooltipKey;
    
    protected ActivatableGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, String tooltipKey) {
        super(slotType);
        this.activator = activator;
        this.targets = targets;
        this.tooltipKey = tooltipKey;
    }
    
    @Nonnull
    public GenericActivator getActivator() {
        return this.activator;
    }
    
    @Nonnull
    public List<GenericTarget> getTargets() {
        return this.targets;
    }
    
    @Nullable
    public String getTooltipKey() {
        return this.tooltipKey;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getTooltip(boolean onItem) {
        StringBuilder tooltip;
        if(this.getTooltipKey() == null) {
            tooltip = new StringBuilder(this.getTooltipString()).append(" ").append(this.activator.getTooltipString());
            for(GenericTarget target : this.targets) {
                tooltip.append(" ").append(target.getTooltipString());
            }
        }
        else tooltip = new StringBuilder(I18n.format(this.getTooltipKey()));
        return tooltip.toString();
    }
    
    @SideOnly(Side.CLIENT)
    public abstract String getTooltipString();
    
    /**
     * Handles allowing targets to apply this effect to specific entities
     * @param callback callback info container for additional effect manipulation for use with specific activators
     * @param playerSource the player that is the source of this effect
     * @param effectTarget the entity involved in this effect being triggered, may be the same as the player
     */
    public void affectTargets(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
        performingDepth.merge(playerSource, 1, Integer::sum);
        try {
            for(GenericTarget target : this.targets) {
                target.affectTarget(this, callback, playerSource, effectTarget);
            }
        }
        finally {
            performingDepth.computeIfPresent(playerSource, (player, depth) -> depth <= 1 ? null : depth - 1);
        }
    }

    private static final Map<EntityPlayer, Integer> performingDepth = new HashMap<>();

    /**
     * Used to prevent damage dealt by a player's effects (such as Lightning or Explosion) from triggering that player's attacking effects
     * @return true if the player's effects are currently being performed
     */
    public static boolean isPerformingEffects(EntityPlayer player) {
        return performingDepth.containsKey(player);
    }
    
    /**
     * Handles applying this effect to a specific entity
     * @param callback callback info container for additional effect manipulation for use with specific activators
     * @param playerSource the player that is the source of this effect
     * @param effectTarget the entity that the effect should be performed on, may be the same as the player
     */
    public abstract void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget);
    
    /**
     * Activator: Required
     * Targets: Required, at least one
     * TooltipKey: Optional, default null
     */
    @Override
    public boolean validate() {
        if(super.validate()) {
            if(this.activator == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, invalid activator");
            else if(!this.activator.validate()) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, invalid activator");
            else if(this.targets == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, invalid targets list");
            else if(this.targets.isEmpty()) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, must define at least one target");
            else {
                for(GenericTarget target : this.targets) {
                    if(!target.validate()) {
                        Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, target invalid");
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}