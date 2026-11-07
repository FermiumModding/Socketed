package socketed.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.common.config.*;
import socketed.common.loot.DefaultSocketsGenerator;
import socketed.common.loot.IItemCreationContext;
import socketed.api.socket.GenericSocket;
import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemType;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.socket.gem.filter.GenericFilter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class SocketedUtil {
    
    private static final List<ISlotType> registeredSlots = new ArrayList<>();
    
    //
    // Registrations
    //
    
    public static void registerFilterType(String typeName, Class<? extends GenericFilter> typeClass, String modid) {
        Socketed.LOGGER.log(Level.INFO, "Registering Filter " + typeName + " from " + modid);
        JsonConfig.filterDeserializerMap.put(typeName, typeClass);
    }

    public static void registerEffectType(String typeName, Class<? extends GenericGemEffect> typeClass, String modid) {
        Socketed.LOGGER.log(Level.INFO, "Registering Effect " + typeName + " from " + modid);
        JsonConfig.gemEffectDeserializerMap.put(typeName, typeClass);
    }
    
    public static void registerActivator(String typeName, Class<? extends GenericActivator> typeClass, String modid) {
        Socketed.LOGGER.log(Level.INFO, "Registering Activator " + typeName + " from " + modid);
        JsonConfig.activatorDeserializerMap.put(typeName, typeClass);
    }
    
    public static void registerTarget(String typeName, Class<? extends GenericTarget> typeClass, String modid) {
        Socketed.LOGGER.log(Level.INFO, "Registering Target " + typeName + " from " + modid);
        JsonConfig.targetDeserializerMap.put(typeName, typeClass);
    }
    
    public static void registerCondition(String typeName, Class<? extends GenericCondition> typeClass, String modid) {
        Socketed.LOGGER.log(Level.INFO, "Registering Condition " + typeName + " from " + modid);
        JsonConfig.conditionDeserializerMap.put(typeName, typeClass);
    }

    public static <T extends Enum<T> & ISlotType> void registerSlotTypes(Class<T> typeClass, String modid) {
        for(T type : typeClass.getEnumConstants()) {
            Socketed.LOGGER.log(Level.INFO, "Registering Slot Type " + type.name() + " from " + modid);
            registeredSlots.add(type);
            JsonConfig.slotTypeDeserializerMap.put(type.name(), typeClass);
        }
    }
    
    public static void registerSocket(String typeName, Function<NBTTagCompound, ? extends GenericSocket> fromNBTFunction, String modid) {
        Socketed.LOGGER.log(Level.INFO, "Registering Socket " + typeName + " from " + modid);
        JsonConfig.socketNBTDeserializerMap.put(typeName, fromNBTFunction);
    }
    
    /**
     * Used to allow addons to define their own item type definitions and rolls for socketing and loot
     */
    public static void registerForcedItemType(String name, Predicate<Item> canSocket, int rolls) {
        Socketed.LOGGER.log(Level.INFO, "Registering Forced Socketable Item Type: " + name);
        SocketableConfig.forcedItemTypes.put(name, new SocketableConfig.SocketableType(canSocket));
        AddSocketsConfig.forcedItemTypeRolls.put(name, rolls);
        ForgeConfig.reset();
    }
    
    /**
     * Used to allow addons to define their own item type definitions and rolls for socketing and loot
     */
    public static void registerForcedItemType(String name, String regex, int rolls) {
        Socketed.LOGGER.log(Level.INFO, "Registering Forced Socketable Item Type: " + name);
        SocketableConfig.forcedItemTypes.put(name, new SocketableConfig.SocketableType(regex));
        AddSocketsConfig.forcedItemTypeRolls.put(name, rolls);
        ForgeConfig.reset();
    }
    
    public static void registerDefaultGemType(String name, GemType gemType) {
        DefaultJsonConfig.registerDefaultGemType(name, gemType);
    }
    
    public static void registerDefaultGemCombinationType(String name, GemCombinationType gemCombinationType) {
        DefaultJsonConfig.registerDefaultGemCombinationType(name, gemCombinationType);
    }
    
    //
    // Utility
    //
    
    /**
     * @return if the given ItemStack would be a valid gem
     */
    public static boolean stackIsGem(ItemStack stack) {
        if(stack.isEmpty()) return false;
        if(!SocketedUtil.hasCompletedLoading(true)) return false;
        return SocketedUtil.getGemTypeFromItemStack(stack) != null;
    }
    
    @Nullable
    public static GemType getGemTypeFromItemStack(ItemStack itemStack) {
        if(itemStack.isEmpty()) return null;
        if(!SocketedUtil.hasCompletedLoading(true)) return null;
        for(GemType gemType : JsonConfig.getSortedGemDataList()) {
            if(gemType.matches(itemStack)) return gemType;
        }
        return null;
    }
    
    @Nullable
    public static GemType getGemTypeFromName(String gemTypeName) {
        if(gemTypeName == null || gemTypeName.isEmpty()) return null;
        if(!SocketedUtil.hasCompletedLoading(false)) return null;
        return JsonConfig.getGemData().get(gemTypeName);
    }
    
    @Nullable
    public static GemCombinationType getGemCombinationTypeFromName(String gemCombinationTypeName) {
        if(gemCombinationTypeName == null || gemCombinationTypeName.isEmpty()) return null;
        if(!SocketedUtil.hasCompletedLoading(true)) return null;
        return JsonConfig.getGemCombinationData().get(gemCombinationTypeName);
    }
    
    public static int getMaxSocketTier() {
        return ForgeConfig.COMMON.maxSocketTier;
    }
    
    /**
     * Note: Use this if possible over ISlotType::isStackValid to allow for config override compatibility
     * @return true if the given ItemStack is valid for the given ISlotType
     */
    //TODO: allow for config override?
    public static boolean isStackValidForSlot(ItemStack stack, ISlotType slotType) {
        if(stack.isEmpty()) return false;
        return slotType.isStackValid(stack);
    }
    
    public static List<ISlotType> getAllSlotTypes() {
        return registeredSlots;
    }
    
    /**
     * @return true if the provided ISlotTypes match each other (Equals or inherits)
     */
    public static boolean doSlotsMatch(ISlotType slotType1, ISlotType slotType2) {
        return slotType1.isSlotValid(slotType2) && slotType2.isSlotValid(slotType1);
    }
    
    public static void addSocketsToStack(ItemStack stack, IItemCreationContext context) {
        if(SocketedUtil.hasCompletedLoading(true)) {
            DefaultSocketsGenerator.addSockets(stack, context);
        }
    }
    
    public static void addSocketsToStackRandomly(ItemStack stack, int maxSockets, int rollAmount, float rollChance) {
        if(SocketedUtil.hasCompletedLoading(true)) {
            DefaultSocketsGenerator.addSocketsRandomly(stack, maxSockets, rollAmount, rollChance);
        }
    }
    
    public static boolean canStackHaveSockets(ItemStack stack) {
        if(SocketedUtil.hasCompletedLoading(true)) {
            return ForgeConfig.SOCKETABLES.canSocket(stack);
        }
        return false;
    }
    
    //Some ItemStacks/tooltips are created during loading causing data to be read before loading is finished
    //These ItemStacks would not involve sockets anyways so ignore them if accessed during loading
    public static boolean hasCompletedLoading(boolean fully) {
        return fully ? JsonConfig.completedLoadingCombinations : JsonConfig.completedLoadingGems;
    }

    /**
     * Filters a List of GemEffects for all ActivatableGemEffects that use a specified Activator
     * @return a filtered List of ActivatableGemEffects
     */
    public static <T extends GenericActivator> Stream<ActivatableGemEffect> filterForActivator(List<GenericGemEffect> effects, Class<T> activatorType){
        return effects.stream()
                .filter(effect -> effect instanceof ActivatableGemEffect)
                //filter for activator being of provided type
                .filter(effect -> activatorType.isInstance(((ActivatableGemEffect) effect).getActivator()))
                //cast to ActivatableGemEffect
                .map(ActivatableGemEffect.class::cast);

    }

    /**
     * Filters a List of GemEffects for all GemEffects of a specified type
     * @return a filtered List of GenericGemEffects
     */
    public static <T extends GenericGemEffect> Stream<T> filterForEffectType(List<GenericGemEffect> effects, Class<T> effectType){
        return effects.stream()
                //filter for effect being of provided type
                .filter(effect -> effectType.isAssignableFrom(effect.getClass()))
                //cast the resulting list to that effect type
                .map(effectType::cast);
    }

    /**
     * @return if the DamageSource is a melee attack, the immediate source being a living entity, excluding thorns
     */
    public static boolean isDamageSourceMelee(DamageSource source) {
        if(source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage()) return false;
        return source.getImmediateSource() instanceof EntityLivingBase;
    }

    /**
     * @return if the DamageSource is a ranged attack, the true source being a living entity but not the immediate source
     */
    public static boolean isDamageSourceRanged(DamageSource source) {
        return !(source.getImmediateSource() instanceof EntityLivingBase) && source.getTrueSource() instanceof EntityLivingBase;
    }

    /**
     * @return an ItemStack representing the given block state, or ItemStack.EMPTY if the block has no item form
     */
    public static ItemStack getStackForBlock(IBlockState state) {
        Block block = state.getBlock();
        Item item = Item.getItemFromBlock(block);
        if(item == Items.AIR) return ItemStack.EMPTY;
        int meta = item.getHasSubtypes() ? block.damageDropped(state) : 0;
        return new ItemStack(item, 1, meta);
    }

    //
    // Attack Strength
    //

    private static final Map<EntityPlayer, RecordedStrength> recordedStrengths = new WeakHashMap<>();

    /**
     * Attack strength is recorded on attack, as the player's cooldown is reset before most damage events fire
     * @return the player's attack strength recorded this tick, or their current cooled attack strength if none was recorded
     */
    public static float getAttackStrength(EntityPlayer player) {
        RecordedStrength recorded = recordedStrengths.get(player);
        if(recorded != null && recorded.tick == player.ticksExisted) return recorded.strength;
        return player.getCooledAttackStrength(0.5F);
    }

    /**
     * Records the player's attack strength for the current tick, used for compat with mods that handle attacks differently such as RLCombat
     */
    public static void recordAttackStrength(EntityPlayer player, float strength) {
        recordedStrengths.put(player, new RecordedStrength(strength, player.ticksExisted));
    }

    private static class RecordedStrength {
        private final float strength;
        private final int tick;

        private RecordedStrength(float strength, int tick) {
            this.strength = strength;
            this.tick = tick;
        }
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            EntityPlayer player = event.getEntityPlayer();
            if(player == null || player.world.isRemote) return;
            recordAttackStrength(player, player.getCooledAttackStrength(0.5F));
        }
    }
}