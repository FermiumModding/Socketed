package socketed.common.config;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.util.text.TextFormatting;
import socketed.api.socket.gem.effect.activatable.condition.ComparingCondition;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.common.attributes.SocketedAttributes;
import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemType;
import socketed.common.socket.gem.effect.AttributeGemEffect;
import socketed.common.socket.gem.effect.PlusEnchantmentGemEffect;
import socketed.common.socket.gem.effect.activatable.*;
import socketed.common.socket.gem.effect.activatable.activator.*;
import socketed.common.socket.gem.effect.activatable.condition.*;
import socketed.common.socket.gem.effect.activatable.target.*;
import socketed.api.socket.gem.effect.slot.SocketedSlotTypes;
import socketed.common.socket.gem.filter.ItemFilter;
import socketed.common.socket.gem.filter.OreFilter;
import socketed.api.socket.gem.util.RandomValueRange;

import java.util.*;

public abstract class DefaultJsonConfig {
    
    private static final Map<String, GemType> defaultGemTypes = new HashMap<>();
    private static final Map<String, GemCombinationType> defaultGemCombinationTypes = new HashMap<>();

    public static Map<String, GemType> getDefaultGemTypes() {
        return defaultGemTypes;
    }
    
    public static Map<String, GemCombinationType> getDefaultGemCombinationTypes() {
        return defaultGemCombinationTypes;
    }
    
    public static void registerDefaultGemType(String name, GemType gemType) {
        defaultGemTypes.put(name, gemType);
    }
    
    public static void registerDefaultGemCombinationType(String name, GemCombinationType gemCombinationType) {
        defaultGemCombinationTypes.put(name, gemCombinationType);
    }

    public static void initializeBuiltinEntries() {
        //Reused objects
        List<GenericTarget> ONLYSELF = Collections.singletonList(new SelfTarget(null));
        List<GenericTarget> ONLYOTHER = Collections.singletonList(new OtherTarget(null));
        List<GenericTarget> SELFAOE_NEAR = Collections.singletonList(new SelfAOETarget(null, 4));
        List<GenericTarget> SELFAOE_NEAR_HITSELF = new ArrayList<>(ONLYSELF); SELFAOE_NEAR_HITSELF.addAll(SELFAOE_NEAR);
        List<GenericTarget> OTHERAOE_NEAR = Arrays.asList(new OtherTarget(null), new OtherAOETarget(null, 3));
        MultiEffectActivator MULTIACT = new MultiEffectActivator(null);
        PassiveActivator PASSIVEFAST = new PassiveActivator(null, 39);

        //Tier 0
        registerDefaultGemType("poison_potato", new GemType("socketed.tooltip.default.poison_potato", 0, TextFormatting.DARK_GREEN,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HEAD, PASSIVEFAST, SELFAOE_NEAR_HITSELF,
                        MobEffects.POISON.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.poison_potato.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:poisonous_potato", 0 , false))));

        registerDefaultGemType("spider_eye", new GemType("socketed.tooltip.default.spider_eye", 0, TextFormatting.DARK_RED,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.CHEST, PASSIVEFAST, SELFAOE_NEAR_HITSELF,
                        MobEffects.WEAKNESS.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.spider_eye.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:spider_eye", 0 , false))));

        registerDefaultGemType("slimeball", new GemType("socketed.tooltip.default.slimeball", 0, TextFormatting.GREEN,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.LEGS, PASSIVEFAST, SELFAOE_NEAR_HITSELF,
                        MobEffects.SLOWNESS.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.slimeball.desc"
                )),
                Collections.singletonList(new OreFilter("slimeball"))));

        registerDefaultGemType("sugar", new GemType("socketed.tooltip.default.sugar", 0, TextFormatting.WHITE,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.FEET, PASSIVEFAST, SELFAOE_NEAR_HITSELF,
                        MobEffects.SPEED.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.sugar.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:sugar", 0 , false))));

        registerDefaultGemType("dead_bush", new GemType("socketed.tooltip.default.dead_bush", 0, TextFormatting.GOLD,
                Collections.singletonList(new AttributeGemEffect(SocketedSlotTypes.HAND, SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new RandomValueRange(0.5F, false), 0)),
                Collections.singletonList(new ItemFilter("minecraft:deadbush", 0 , false))));

        registerDefaultGemType("iron", new GemType("socketed.tooltip.default.iron", 0, TextFormatting.GRAY,
                Arrays.asList(
                        new AttributeGemEffect(SocketedSlotTypes.HAND, SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new RandomValueRange(1, 1, true), 0),
                        new AttributeGemEffect(SocketedSlotTypes.BODY, SharedMonsterAttributes.ARMOR.getName(), new RandomValueRange(1, false), 0)),
                Collections.singletonList(new OreFilter("blockIron"))));

        registerDefaultGemType("coal", new GemType("socketed.tooltip.default.coal", 0, TextFormatting.DARK_GRAY,
                Collections.singletonList(new IgniteGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new ChanceCondition(0.2F), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        3,
                        "socketed.tooltip.default.coal.desc"
                )),
                Collections.singletonList(new OreFilter("blockCoal"))));

        registerDefaultGemType("gunpowder", new GemType("socketed.tooltip.default.gunpowder", 0, TextFormatting.RED,
                Collections.singletonList(new ExplosionGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new ChanceCondition(0.1F), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        1, false, false,
                        "socketed.tooltip.default.gunpowder.desc"
                )),
                Collections.singletonList(new OreFilter("gunpowder"))));

        registerDefaultGemType("feather", new GemType("socketed.tooltip.default.feather", 0, TextFormatting.WHITE,
                Collections.singletonList(new CancelEventGemEffect(
                        SocketedSlotTypes.FEET,
                        new FallActivator(new ChanceCondition(0.5F), 4.0F),
                        ONLYSELF,
                        "socketed.tooltip.default.feather.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:feather", 0, false))));

        registerDefaultGemType("snowball", new GemType("socketed.tooltip.default.snowball", 0, TextFormatting.AQUA,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new ChanceCondition(0.25F), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        MobEffects.SLOWNESS.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.snowball.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:snowball", 0, false))));

        registerDefaultGemType("ink_sac", new GemType("socketed.tooltip.default.ink_sac", 0, TextFormatting.DARK_BLUE,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.CHEST,
                        new AttackedActivator(new ChanceCondition(0.2F), false, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        MobEffects.BLINDNESS.getRegistryName().toString(), 0, 60,
                        "socketed.tooltip.default.ink_sac.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:dye", 0, true))));

        registerDefaultGemType("leather", new GemType("socketed.tooltip.default.leather", 0, TextFormatting.GOLD,
                Collections.singletonList(new ExtraIFrameGemEffect(
                        SocketedSlotTypes.BODY,
                        new AttackedActivator(new ChanceCondition(0.1F), false, AttackActivator.EventType.HURT),
                        ONLYSELF,
                        5,
                        "socketed.tooltip.default.leather.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:leather", 0, false))));

        registerDefaultGemType("cactus", new GemType("socketed.tooltip.default.cactus", 0, TextFormatting.DARK_GREEN,
                Collections.singletonList(new DamageGemEffect(
                        SocketedSlotTypes.CHEST,
                        new AttackedActivator(new DamageSourceCondition(true, false, ""), false, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        1.0F, "thorns", false, false,
                        "socketed.tooltip.default.cactus.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:cactus", 0, false))));

        //Tier 1

        registerDefaultGemType("blazerod", new GemType("socketed.tooltip.default.blazerod", 1, TextFormatting.DARK_RED,
                Collections.singletonList(new IgniteGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new ChanceCondition(0.5F), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        6,
                        "socketed.tooltip.default.blazerod.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:blaze_rod", 0 , false))));

        registerDefaultGemType("glowstone", new GemType("socketed.tooltip.default.glowstone", 1, TextFormatting.YELLOW,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HEAD, PASSIVEFAST, SELFAOE_NEAR,
                        MobEffects.GLOWING.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.glowstone.desc"
                )),
                Collections.singletonList(new OreFilter("dustGlowstone"))));

        registerDefaultGemType("glowstone_block", new GemType("socketed.tooltip.default.glowstone_block", 1, TextFormatting.YELLOW,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HEAD, PASSIVEFAST,
                        Collections.singletonList(new SelfAOETarget(null, 12)),
                        MobEffects.GLOWING.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.glowstone_block.desc"
                )),
                Collections.singletonList(new OreFilter("glowstone"))));

        registerDefaultGemType("redstone", new GemType("socketed.tooltip.default.redstone", 1, TextFormatting.RED,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.CHEST, PASSIVEFAST, ONLYSELF,
                        MobEffects.HASTE.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.redstone.desc"
                )),
                Collections.singletonList(new OreFilter("blockRedstone"))));

        registerDefaultGemType("cobweb", new GemType("socketed.tooltip.default.cobweb", 1,
                TextFormatting.WHITE,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.LEGS,
                        new AttackedActivator(new ChanceCondition(0.2F), false, AttackActivator.EventType.HURT),
                        Collections.singletonList(new SelfAOETarget(null, 6)),
                        MobEffects.SLOWNESS.getRegistryName().toString(), 0, 80,
                        "socketed.tooltip.default.cobweb.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:web", 0, false))));

        registerDefaultGemType("emerald", new GemType("socketed.tooltip.default.emerald", 1, TextFormatting.GREEN,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.BODY, PASSIVEFAST, ONLYSELF,
                        MobEffects.LUCK.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.emerald.desc"
                )),
                Collections.singletonList(new OreFilter("gemEmerald"))));

        registerDefaultGemType("gold_carrot", new GemType("socketed.tooltip.default.gold_carrot", 1, TextFormatting.YELLOW,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HEAD,
                        new PassiveActivator(
                                new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                        new ElevationCondition(60, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true),
                                        new LightLevelCondition(7, ComparingCondition.ConditionComparisonType.LESS, true))),
                                40),
                        ONLYSELF,
                        MobEffects.NIGHT_VISION.getRegistryName().toString(), 0, 300,
                        "socketed.tooltip.default.gold_carrot.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:golden_carrot",0,false))));

        registerDefaultGemType("piston", new GemType("socketed.tooltip.default.piston", 1, TextFormatting.AQUA,
                Collections.singletonList(new KnockbackGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(null, true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        2.0F, false,
                        "socketed.tooltip.default.piston.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:piston", 0, false))));

        registerDefaultGemType("sticky_piston", new GemType("socketed.tooltip.default.sticky_piston", 1, TextFormatting.GREEN,
                Collections.singletonList(new KnockbackGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(null, true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        2.0F, true,
                        "socketed.tooltip.default.sticky_piston.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:sticky_piston", 0, false))));

        registerDefaultGemType("diamond", new GemType("socketed.tooltip.default.diamond", 1,
                TextFormatting.AQUA,
                Arrays.asList(
                        new AttributeGemEffect(SocketedSlotTypes.HAND, SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new RandomValueRange(2, false), 0),
                        new AttributeGemEffect(SocketedSlotTypes.BODY, SharedMonsterAttributes.ARMOR.getName(), new RandomValueRange(1, false), 0),
                        new AttributeGemEffect(SocketedSlotTypes.BODY, SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new RandomValueRange(1, false), 0)),
                Collections.singletonList(new OreFilter("gemDiamond"))));

        registerDefaultGemType("flint", new GemType("socketed.tooltip.default.flint", 1, TextFormatting.GRAY,
                Collections.singletonList(new PlusEnchantmentGemEffect(
                        SocketedSlotTypes.HAND,
                        "minecraft:power", 1)),
                Collections.singletonList(new ItemFilter("minecraft:flint", 0, false))));

        registerDefaultGemType("pumpkin", new GemType("socketed.tooltip.default.pumpkin", 1, TextFormatting.GOLD,
                Collections.singletonList(new LoseTargetGemEffect(
                        SocketedSlotTypes.HEAD,
                        new TargetedActivator(new IsSpecificEntityCondition("minecraft:enderman")),
                        ONLYOTHER,
                        "socketed.tooltip.default.pumpkin.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:pumpkin", 0, false))));

        registerDefaultGemType("glistering_melon", new GemType("socketed.tooltip.default.glistering_melon", 1, TextFormatting.RED,
                Collections.singletonList(new HealGemEffect(
                        SocketedSlotTypes.BODY,
                        new KillActivator(null),
                        ONLYSELF,
                        2.0F, 0.0F,
                        "socketed.tooltip.default.glistering_melon.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:speckled_melon", 0, false))));

        registerDefaultGemType("magma_cream", new GemType("socketed.tooltip.default.magma_cream", 1, TextFormatting.GOLD,
                Collections.singletonList(new MultiEffectGemEffect(
                        SocketedSlotTypes.BODY,
                        new EnvironmentalDamageActivator(
                                new MultiCondition(MultiCondition.ConditionLogicType.OR, Arrays.asList(
                                        new DamageSourceCondition(false, false, "lava"),
                                        new DamageSourceCondition(false, false, "inFire"),
                                        new DamageSourceCondition(false, false, "onFire"))),
                                AttackActivator.EventType.HURT),
                        ONLYSELF,
                        Arrays.asList(
                                new DamageModifierGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        0.5F, DamageModifierGemEffect.Operation.MULTIPLY,
                                        ""
                                ),
                                new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        MobEffects.FIRE_RESISTANCE.getRegistryName().toString(), 0, 60,
                                        ""
                                )
                        ),
                        "socketed.tooltip.default.magma_cream.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:magma_cream", 0, false))));

        registerDefaultGemType("prismarine_crystals", new GemType("socketed.tooltip.default.prismarine_crystals", 1, TextFormatting.AQUA,
                Collections.singletonList(new CancelEventGemEffect(
                        SocketedSlotTypes.HEAD,
                        new EnvironmentalDamageActivator(
                                new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                        new DamageSourceCondition(false, false, "drown"),
                                        new ChanceCondition(0.5F))),
                                AttackActivator.EventType.ATTACK),
                        ONLYSELF,
                        "socketed.tooltip.default.prismarine_crystals.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:prismarine_crystals", 0, false))));

        registerDefaultGemType("prismarine_shard", new GemType("socketed.tooltip.default.prismarine_shard", 1, TextFormatting.DARK_AQUA,
                Collections.singletonList(new DamageModifierGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new EntityStateCondition(EntityStateCondition.EntityState.IN_WATER, false), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        1.25F, DamageModifierGemEffect.Operation.MULTIPLY,
                        "socketed.tooltip.default.prismarine_shard.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:prismarine_shard", 0, false))));

        registerDefaultGemType("packed_ice", new GemType("socketed.tooltip.default.packed_ice", 1, TextFormatting.AQUA,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.FEET,
                        new PassiveActivator(new BiomeTypeCondition("SNOWY"), 39),
                        ONLYSELF,
                        MobEffects.SPEED.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.packed_ice.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:packed_ice", 0, false))));

        registerDefaultGemType("firework", new GemType("socketed.tooltip.default.firework", 1, TextFormatting.RED,
                Collections.singletonList(new LaunchGemEffect(
                        SocketedSlotTypes.FEET,
                        new JumpActivator(new EntityStateCondition(EntityStateCondition.EntityState.SPRINTING, true)),
                        ONLYSELF,
                        0.3F,
                        "socketed.tooltip.default.firework.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:fireworks", 0, false))));

        registerDefaultGemType("spectral_arrow", new GemType("socketed.tooltip.default.spectral_arrow", 1, TextFormatting.YELLOW,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new DamageSourceCondition(false, true, ""), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        MobEffects.GLOWING.getRegistryName().toString(), 0, 200,
                        "socketed.tooltip.default.spectral_arrow.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:spectral_arrow", 0, false))));

        registerDefaultGemType("magma_block", new GemType("socketed.tooltip.default.magma_block", 1, TextFormatting.DARK_RED,
                Collections.singletonList(new IgniteGemEffect(
                        SocketedSlotTypes.HAND,
                        new BlockingActivator(new DamageSourceCondition(true, false, ""), true),
                        ONLYOTHER,
                        3,
                        "socketed.tooltip.default.magma_block.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:magma", 0, false))));

        //Tier 2

        registerDefaultGemType("rabbitfoot", new GemType("socketed.tooltip.default.rabbitfoot", 2, TextFormatting.YELLOW,
                Arrays.asList(
                        new AttributeGemEffect(SocketedSlotTypes.FEET, SharedMonsterAttributes.LUCK.getName(), new RandomValueRange(1, false), 0),
                        new PotionGemEffect(
                                SocketedSlotTypes.FEET, PASSIVEFAST, ONLYSELF,
                                MobEffects.JUMP_BOOST.getRegistryName().toString(), 0, 40,
                                "socketed.tooltip.default.rabbitfoot.desc"
                        ),
                        new PotionGemEffect(
                                SocketedSlotTypes.FEET,
                                new PassiveActivator(
                                        new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                                new MoonPhaseCondition(MoonPhaseCondition.MoonPhase.FULL_MOON),
                                                new TimeOfDayCondition(TimeOfDayCondition.TimePreset.NIGHT, null, null))),
                                        39),
                                ONLYSELF,
                                MobEffects.LUCK.getRegistryName().toString(), 0, 40,
                                "socketed.tooltip.default.rabbitfoot.desc.2"
                        )),
                Collections.singletonList(new ItemFilter("minecraft:rabbit_foot", 0, false))));

        registerDefaultGemType("gapple", new GemType("socketed.tooltip.default.gapple", 2, TextFormatting.YELLOW,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.ALL,
                        new PassiveActivator(new ChanceCondition(0.2F), 200),
                        ONLYSELF,
                        MobEffects.ABSORPTION.getRegistryName().toString(), 0, 200,
                        "socketed.tooltip.default.gapple.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:golden_apple", 0, true))));

        registerDefaultGemType("sponge", new GemType("socketed.tooltip.default.sponge", 2, TextFormatting.AQUA,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.HEAD, PASSIVEFAST, ONLYSELF,
                        MobEffects.WATER_BREATHING.getRegistryName().toString(), 0, 40,
                        "socketed.tooltip.default.sponge.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:sponge", 0, true))));

        registerDefaultGemType("anvil", new GemType("socketed.tooltip.default.anvil", 2, TextFormatting.GRAY,
                Arrays.asList(
                        new KnockbackGemEffect(
                                SocketedSlotTypes.CHEST,
                                new AttackedActivator(null, false, AttackActivator.EventType.HURT),
                                ONLYOTHER,
                                4.0F, false,
                                "socketed.tooltip.default.anvil.desc"
                        ),
                        new DisableShieldGemEffect(
                                SocketedSlotTypes.HAND,
                                new AttackingActivator(new ChanceCondition(0.25F), true, AttackActivator.EventType.ATTACK),
                                ONLYOTHER,
                                100,
                                "socketed.tooltip.default.anvil.desc.2"
                        )),
                Collections.singletonList(new ItemFilter("minecraft:anvil", 0, true))));

        registerDefaultGemType("witherskull", new GemType("socketed.tooltip.default.witherskull", 2, TextFormatting.DARK_GRAY,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.BODY,
                        new AttackedActivator(new HealthPercentCondition(0.5F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), false, AttackActivator.EventType.HURT),
                        SELFAOE_NEAR,
                        MobEffects.WITHER.getRegistryName().toString(), 0, 100,
                        "socketed.tooltip.default.witherskull.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:skull", 1, true))));

        registerDefaultGemType("pufferfish", new GemType("socketed.tooltip.default.pufferfish", 2, TextFormatting.GREEN,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.BODY,
                        new AttackedActivator(new HealthPercentCondition(0.5F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), false, AttackActivator.EventType.HURT),
                        SELFAOE_NEAR,
                        MobEffects.POISON.getRegistryName().toString(), 0, 100,
                        "socketed.tooltip.default.pufferfish.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:fish", 3, true))));

        registerDefaultGemType("sealantern", new GemType("socketed.tooltip.default.sealantern", 2, TextFormatting.AQUA,
                Collections.singletonList(new PotionGemEffect(
                        SocketedSlotTypes.BODY,
                        new AttackedActivator(new HealthPercentCondition(0.5F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), false, AttackActivator.EventType.HURT),
                        SELFAOE_NEAR,
                        MobEffects.BLINDNESS.getRegistryName().toString(), 0, 100,
                        "socketed.tooltip.default.sealantern.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:sea_lantern", 0, false))));

        registerDefaultGemType("tropicalfish", new GemType("socketed.tooltip.default.tropicalfish", 2, TextFormatting.AQUA,
                Collections.singletonList(new AttributeGemEffect(SocketedSlotTypes.FEET, EntityLivingBase.SWIM_SPEED.getName(), new RandomValueRange(0.3F, false), 1)),
                Collections.singletonList(new ItemFilter("minecraft:fish", 2, true))));

        registerDefaultGemType("obsidian", new GemType("socketed.tooltip.default.obsidian", 2, TextFormatting.DARK_GRAY,
                Arrays.asList(
                        new PotionGemEffect(
                                SocketedSlotTypes.HAND,
                                new AttackingActivator(new ChanceCondition(0.2F), true, AttackActivator.EventType.HURT),
                                ONLYSELF,
                                MobEffects.STRENGTH.getRegistryName().toString(), 0, 100,
                                "socketed.tooltip.default.obsidian.desc"
                        ),
                        new AttributeGemEffect(SocketedSlotTypes.BODY, SocketedAttributes.DURABILITY.getName(), new RandomValueRange(0.08F, false), 1)),
                Collections.singletonList(new OreFilter("obsidian"))));

        registerDefaultGemType("tnt", new GemType("socketed.tooltip.default.tnt", 2, TextFormatting.RED,
                Collections.singletonList(new ExplosionGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new ChanceCondition(0.2F), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        2, false, true,
                        "socketed.tooltip.default.tnt.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:tnt", 0, false))));

        registerDefaultGemType("quartz", new GemType("socketed.tooltip.default.quartz", 2, TextFormatting.WHITE,
                Collections.singletonList(new AttributeGemEffect(SocketedSlotTypes.HAND, SocketedAttributes.XP.getName(), new RandomValueRange(0.1F, false), 1)),
                Collections.singletonList(new OreFilter("gemQuartz"))));

        registerDefaultGemType("emeraldblock", new GemType("socketed.tooltip.default.emeraldblock", 2, TextFormatting.GREEN,
                Collections.singletonList(new AttributeGemEffect(SocketedSlotTypes.HEAD, SocketedAttributes.TRADECOST.getName(), new RandomValueRange(-0.2F, false), 2)),
                Collections.singletonList(new OreFilter("blockEmerald"))));

        registerDefaultGemType("ender_pearl", new GemType("socketed.tooltip.default.ender_pearl", 2, TextFormatting.DARK_AQUA,
                Collections.singletonList(new MultiEffectGemEffect(
                        SocketedSlotTypes.CHEST,
                        new AttackedActivator(
                                new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                        new DamageSourceCondition(false, true, ""),
                                        new ChanceCondition(0.3F))),
                                false, AttackActivator.EventType.HURT),
                        ONLYSELF,
                        Arrays.asList(
                                new CancelEventGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        ""
                                ),
                                new RandomTeleportGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        8,
                                        ""
                                )
                        ),
                        "socketed.tooltip.default.ender_pearl.desc"
                )),
                Collections.singletonList(new OreFilter("enderpearl"))));

        registerDefaultGemType("chorus_fruit", new GemType("socketed.tooltip.default.chorus_fruit", 2, TextFormatting.LIGHT_PURPLE,
                Collections.singletonList(new RandomTeleportGemEffect(
                        SocketedSlotTypes.CHEST,
                        new AttackedActivator(
                                new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                        new HealthPercentCondition(0.3F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true),
                                        new ChanceCondition(0.2F))),
                                false, AttackActivator.EventType.HURT),
                        ONLYSELF,
                        16,
                        "socketed.tooltip.default.chorus_fruit.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:chorus_fruit", 0, false))));

        registerDefaultGemType("slime_block", new GemType("socketed.tooltip.default.slime_block", 2, TextFormatting.GREEN,
                Collections.singletonList(new MultiEffectGemEffect(
                        SocketedSlotTypes.FEET,
                        new FallActivator(null, 3.0F),
                        ONLYSELF,
                        Arrays.asList(
                                new CancelEventGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        ""
                                ),
                                new LaunchGemEffect(SocketedSlotTypes.ALL,
                                        new MultiEffectActivator(new InvertedCondition(new EntityStateCondition(EntityStateCondition.EntityState.SNEAKING, true))),
                                        ONLYSELF,
                                        0.5F,
                                        ""
                                )
                        ),
                        "socketed.tooltip.default.slime_block.desc"
                )),
                Collections.singletonList(new OreFilter("blockSlime"))));

        registerDefaultGemType("milk_bucket", new GemType("socketed.tooltip.default.milk_bucket", 2, TextFormatting.WHITE,
                Collections.singletonList(new CleanseGemEffect(
                        SocketedSlotTypes.BODY,
                        new PassiveActivator(null, 200),
                        ONLYSELF,
                        CleanseGemEffect.CleanseType.HARMFUL, null,
                        "socketed.tooltip.default.milk_bucket.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:milk_bucket", 0, false))));

        registerDefaultGemType("fermented_spider_eye", new GemType("socketed.tooltip.default.fermented_spider_eye", 2, TextFormatting.DARK_PURPLE,
                Collections.singletonList(new CleanseGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new ChanceCondition(0.15F), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        CleanseGemEffect.CleanseType.BENEFICIAL, null,
                        "socketed.tooltip.default.fermented_spider_eye.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:fermented_spider_eye", 0, false))));

        registerDefaultGemType("nether_wart", new GemType("socketed.tooltip.default.nether_wart", 2, TextFormatting.DARK_RED,
                Collections.singletonList(new HealGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new AttackCooldownCondition(0.9F), true, AttackActivator.EventType.HURT),
                        ONLYSELF,
                        0.0F, 0.1F,
                        "socketed.tooltip.default.nether_wart.desc"
                )),
                Collections.singletonList(new OreFilter("cropNetherWart"))));

        registerDefaultGemType("gold_block", new GemType("socketed.tooltip.default.gold_block", 2, TextFormatting.GOLD,
                Collections.singletonList(new BonusDropsGemEffect(
                        SocketedSlotTypes.HAND,
                        new KillActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                new ChanceCondition(0.15F),
                                new InvertedCondition(new IsBossCondition())))),
                        ONLYOTHER,
                        1,
                        "socketed.tooltip.default.gold_block.desc"
                )),
                Collections.singletonList(new OreFilter("blockGold"))));

        registerDefaultGemType("lapis_block", new GemType("socketed.tooltip.default.lapis_block", 2, TextFormatting.BLUE,
                Collections.singletonList(new BonusDropsGemEffect(
                        SocketedSlotTypes.HAND,
                        new BlockBreakActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                new BlockCondition(Arrays.asList(
                                        new OreFilter("oreCoal"),
                                        new OreFilter("oreLapis"),
                                        new OreFilter("oreRedstone"),
                                        new OreFilter("oreDiamond"),
                                        new OreFilter("oreEmerald"),
                                        new OreFilter("oreQuartz"))),
                                new ChanceCondition(0.1F))),
                                true),
                        ONLYSELF,
                        1,
                        "socketed.tooltip.default.lapis_block.desc"
                )),
                Collections.singletonList(new OreFilter("blockLapis"))));

        registerDefaultGemType("soul_sand", new GemType("socketed.tooltip.default.soul_sand", 2, TextFormatting.DARK_GRAY,
                Collections.singletonList(new DamageModifierGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                new TimeOfDayCondition(TimeOfDayCondition.TimePreset.NIGHT, null, null),
                                new LightLevelCondition(7, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true))),
                                true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        1.2F, DamageModifierGemEffect.Operation.MULTIPLY,
                        "socketed.tooltip.default.soul_sand.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:soul_sand", 0, false))));

        registerDefaultGemType("nether_brick", new GemType("socketed.tooltip.default.nether_brick", 2, TextFormatting.DARK_RED,
                Collections.singletonList(new DamageModifierGemEffect(
                        SocketedSlotTypes.BODY,
                        new AttackedActivator(new DimensionCondition(-1, null), false, AttackActivator.EventType.HURT),
                        ONLYSELF,
                        0.85F, DamageModifierGemEffect.Operation.MULTIPLY,
                        "socketed.tooltip.default.nether_brick.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:nether_brick", 0, false))));

        //Tier 3

        registerDefaultGemType("ghasttear", new GemType("socketed.tooltip.default.ghasttear", 3, TextFormatting.WHITE,
                Collections.singletonList(new MultiEffectGemEffect(
                        SocketedSlotTypes.CHEST, PASSIVEFAST, ONLYSELF,
                        Arrays.asList(
                                new PotionGemEffect(
                                        SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        MobEffects.REGENERATION.getRegistryName().toString(), 0, 50,
                                        "socketed.tooltip.default.ghasttear.desc.1"
                                ),
                                new PotionGemEffect(
                                        SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        MobEffects.HEALTH_BOOST.getRegistryName().toString(), 0, 50,
                                        "socketed.tooltip.default.ghasttear.desc.2"
                                )
                        ),
                        null
                
                )),
                Collections.singletonList(new ItemFilter("minecraft:ghast_tear", 0, false))));

        registerDefaultGemType("totem", new GemType("socketed.tooltip.default.totem", 3, TextFormatting.YELLOW,
                Collections.singletonList(new MultiEffectGemEffect(
                            SocketedSlotTypes.OFFHAND,
                            new DeathTotemCheckActivator(new ChanceCondition(0.2F)),
                            ONLYSELF,
                            Arrays.asList(
                                    new UndyingTotemGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                                              null
                                    ),
                                    new CancelEventGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                                             null
                                    )
                            ),
                        "socketed.tooltip.default.totem.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:totem_of_undying", 0, false))));

        registerDefaultGemType("endcrystal", new GemType("socketed.tooltip.default.endcrystal", 3, TextFormatting.LIGHT_PURPLE,
                Collections.singletonList(new MultiEffectGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(new ChanceCondition(0.05F), new InvertedCondition(new IsBossCondition()))), true, AttackActivator.EventType.ATTACK),
                        ONLYOTHER,
                        Arrays.asList(
                                new UndyingTotemGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYOTHER,
                                                          null
                                ),
                                new CancelEventGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYOTHER,
                                                         null
                                )
                        ),
                        "socketed.tooltip.default.endcrystal.desc"
                
                )),
                Collections.singletonList(new ItemFilter("minecraft:end_crystal", 0, false))));

        registerDefaultGemType("beacon", new GemType("socketed.tooltip.default.beacon", 3, TextFormatting.WHITE,
                Arrays.asList(
                        new MultiEffectGemEffect(SocketedSlotTypes.HEAD, new PassiveActivator(new LightLevelCondition(15, ComparingCondition.ConditionComparisonType.EQUAL, true), 9), Collections.singletonList(new SelfTarget(null)), Arrays.asList(
                                new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, Collections.singletonList(new SelfAOETarget(null, 32)), MobEffects.INVISIBILITY.getRegistryName().toString(), 0, 10,
                                        ""
                                ),
                                new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, Collections.singletonList(new SelfAOETarget(null, 32)), MobEffects.GLOWING.getRegistryName().toString(), 0, 10,
                                        ""
                                )
                        ),
                                "socketed.tooltip.default.beacon.desc.1"
                        ),
                        new PotionGemEffect(SocketedSlotTypes.HEAD, new PassiveActivator(new LightLevelCondition(7, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), 9), ONLYSELF, MobEffects.INVISIBILITY.getRegistryName().toString(), 0, 10,
                                "socketed.tooltip.default.beacon.desc.2"
                        )
                ),
                Collections.singletonList(new ItemFilter("minecraft:beacon", 0, false))));

        registerDefaultGemType("godapple", new GemType("socketed.tooltip.default.godapple", 3, TextFormatting.GOLD,
                Arrays.asList(
                        new PotionGemEffect(
                            SocketedSlotTypes.ALL,
                            new PassiveActivator(null, 400),
                            ONLYSELF,
                            MobEffects.ABSORPTION.getRegistryName().toString(), 1, 200,
                                "socketed.tooltip.default.godapple.desc.1"
                        ),
                        new PotionGemEffect(
                            SocketedSlotTypes.ALL,
                            new PassiveActivator(new HealthPercentCondition(0.25F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), 400),
                            ONLYSELF,
                            MobEffects.FIRE_RESISTANCE.getRegistryName().toString(), 0, 400,
                                "socketed.tooltip.default.godapple.desc.2"
                        )),
                Collections.singletonList(new ItemFilter("minecraft:golden_apple", 1, true))));

        registerDefaultGemType("netherstar", new GemType("socketed.tooltip.default.netherstar", 3,
                TextFormatting.WHITE,
                Arrays.asList(
                        new PotionGemEffect(
                                SocketedSlotTypes.BODY,
                                new AttackedActivator(null, false, AttackActivator.EventType.HURT),
                                OTHERAOE_NEAR,
                                MobEffects.WITHER.getRegistryName().toString(), 0, 200,
                                "socketed.tooltip.default.netherstar.desc"
                        ),
                        new AttributeGemEffect(SocketedSlotTypes.HAND, SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new RandomValueRange(5, false), 0)),
                Collections.singletonList(new ItemFilter("minecraft:nether_star", 0, false))));

        registerDefaultGemType("skullzombie", new GemType("socketed.tooltip.default.skullzombie", 3, TextFormatting.DARK_GREEN,
                Collections.singletonList(new LoseTargetGemEffect(
                        SocketedSlotTypes.BODY,
                        new TargetedActivator(new IsSpecificEntityCondition("minecraft:zombie")),
                        ONLYOTHER,
                        "socketed.tooltip.default.skullzombie.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:skull", 2, true))));

        registerDefaultGemType("skullcreeper", new GemType("socketed.tooltip.default.skullcreeper", 3, TextFormatting.GREEN,
                Collections.singletonList(new LoseTargetGemEffect(
                        SocketedSlotTypes.BODY,
                        new TargetedActivator(new IsSpecificEntityCondition("minecraft:creeper")),
                        ONLYOTHER,
                        "socketed.tooltip.default.skullcreeper.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:skull", 4, true))));

        registerDefaultGemType("skullskeleton", new GemType("socketed.tooltip.default.skullskeleton", 3, TextFormatting.WHITE,
                Collections.singletonList(new LoseTargetGemEffect(
                        SocketedSlotTypes.BODY,
                        new TargetedActivator(new IsSpecificEntityCondition("minecraft:skeleton")),
                        ONLYOTHER,
                        "socketed.tooltip.default.skullskeleton.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:skull", 0, true))));

        registerDefaultGemType("end_rod", new GemType("socketed.tooltip.default.end_rod", 3, TextFormatting.WHITE,
                Collections.singletonList(new LightningGemEffect(
                        SocketedSlotTypes.HAND,
                        new AttackingActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                                new WeatherCondition(WeatherCondition.WeatherType.RAIN, true, false),
                                new ChanceCondition(0.25F))),
                                true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        5.0F, false, false,
                        "socketed.tooltip.default.end_rod.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:end_rod", 0, false))));

        registerDefaultGemType("dragon_breath", new GemType("socketed.tooltip.default.dragon_breath", 3, TextFormatting.LIGHT_PURPLE,
                Collections.singletonList(new DamageGemEffect(
                        SocketedSlotTypes.BODY,
                        new AttackedActivator(new HealthPercentCondition(0.5F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), false, AttackActivator.EventType.HURT),
                        Arrays.asList(new OtherTarget(null), new SelfAOETarget(null, 4)),
                        4.0F, "magic", true, false,
                        "socketed.tooltip.default.dragon_breath.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:dragon_breath", 0, false))));

        registerDefaultGemType("fire_charge", new GemType("socketed.tooltip.default.fire_charge", 3, TextFormatting.GOLD,
                Collections.singletonList(new DamageModifierGemEffect(
                        SocketedSlotTypes.MAINHAND,
                        new AttackingActivator(new InvertedCondition(new DistanceCondition(16.0F)), true, AttackActivator.EventType.HURT),
                        ONLYOTHER,
                        1.3F, DamageModifierGemEffect.Operation.MULTIPLY,
                        "socketed.tooltip.default.fire_charge.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:fire_charge", 0, false))));

        registerDefaultGemType("skulldragon", new GemType("socketed.tooltip.default.skulldragon", 3, TextFormatting.DARK_PURPLE,
                Collections.singletonList(new MultiEffectGemEffect(
                        SocketedSlotTypes.HEAD,
                        new PassiveActivator(new DimensionCondition(1, null), 39),
                        ONLYSELF,
                        Arrays.asList(
                                new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        MobEffects.STRENGTH.getRegistryName().toString(), 0, 40,
                                        ""
                                ),
                                new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                                        MobEffects.RESISTANCE.getRegistryName().toString(), 0, 40,
                                        ""
                                )
                        ),
                        "socketed.tooltip.default.skulldragon.desc"
                )),
                Collections.singletonList(new ItemFilter("minecraft:skull", 5, true))));

        //Combinations

        registerDefaultGemCombinationType("toxic", new GemCombinationType("socketed.tooltip.default.toxic", TextFormatting.GREEN,
            false, false, false, true,
            Arrays.asList("sealantern", "witherskull", "pufferfish"),
            Collections.singletonList(new MultiEffectGemEffect(
                SocketedSlotTypes.BODY,
                new AttackedActivator(new HealthPercentCondition(0.75F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true), false, AttackActivator.EventType.HURT),
                ONLYSELF,
                Arrays.asList(
                    new PotionGemEffect(
                        SocketedSlotTypes.ALL, MULTIACT, SELFAOE_NEAR,
                        MobEffects.POISON.getRegistryName().toString(), 1, 200,
                        ""
                    ),
                    new PotionGemEffect(
                        SocketedSlotTypes.ALL, MULTIACT, SELFAOE_NEAR,
                        MobEffects.WITHER.getRegistryName().toString(), 1, 200,
                            ""
                    ),
                    new PotionGemEffect(
                        SocketedSlotTypes.ALL, MULTIACT,SELFAOE_NEAR,
                        MobEffects.BLINDNESS.getRegistryName().toString(), 0, 200,
                            ""
                    )
                ),
                    "socketed.tooltip.default.toxic.desc"
            ))
        ));

        registerDefaultGemCombinationType("ender", new GemCombinationType("socketed.tooltip.default.ender", TextFormatting.DARK_AQUA,
            false, false, false, false,
            Arrays.asList("ender_pearl", "chorus_fruit"),
            Collections.singletonList(new MultiEffectGemEffect(
                SocketedSlotTypes.CHEST,
                new AttackedActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                        new HealthPercentCondition(0.25F, ComparingCondition.ConditionComparisonType.LESS_EQUAL, true),
                        new ChanceCondition(0.35F))),
                        false, AttackActivator.EventType.HURT),
                ONLYSELF,
                Arrays.asList(
                    new CancelEventGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        ""
                    ),
                    new RandomTeleportGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        24,
                        ""
                    ),
                    new ExtraIFrameGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        20,
                        ""
                    )
                ),
                "socketed.tooltip.default.ender.desc"
            ))
        ));

        registerDefaultGemCombinationType("springloaded", new GemCombinationType("socketed.tooltip.default.springloaded", TextFormatting.GREEN,
            false, false, false, false,
            Arrays.asList("slime_block", "firework"),
            Collections.singletonList(new LaunchGemEffect(
                SocketedSlotTypes.FEET,
                new JumpActivator(new EntityStateCondition(EntityStateCondition.EntityState.SNEAKING, true)),
                ONLYSELF,
                0.8F,
                "socketed.tooltip.default.springloaded.desc"
            ))
        ));

        registerDefaultGemCombinationType("tidal", new GemCombinationType("socketed.tooltip.default.tidal", TextFormatting.AQUA,
            false, false, false, false,
            Arrays.asList("prismarine_crystals", "sponge"),
            Collections.singletonList(new MultiEffectGemEffect(
                SocketedSlotTypes.HEAD,
                new PassiveActivator(new EntityStateCondition(EntityStateCondition.EntityState.IN_WATER, true), 39),
                ONLYSELF,
                Arrays.asList(
                    new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        MobEffects.HASTE.getRegistryName().toString(), 0, 40,
                        ""
                    ),
                    new PotionGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        MobEffects.NIGHT_VISION.getRegistryName().toString(), 0, 300,
                        ""
                    )
                ),
                "socketed.tooltip.default.tidal.desc"
            ))
        ));

        registerDefaultGemCombinationType("draconic", new GemCombinationType("socketed.tooltip.default.draconic", TextFormatting.DARK_PURPLE,
            false, false, false, false,
            Arrays.asList("skulldragon", "dragon_breath"),
            Arrays.asList(
                new PotionGemEffect(
                    SocketedSlotTypes.HEAD,
                    new PassiveActivator(new DimensionCondition(1, null), 39),
                    ONLYSELF,
                    MobEffects.REGENERATION.getRegistryName().toString(), 0, 50,
                    "socketed.tooltip.default.draconic.desc.1"
                ),
                new DamageGemEffect(
                    SocketedSlotTypes.HEAD,
                    new AttackedActivator(new DimensionCondition(1, null), false, AttackActivator.EventType.HURT),
                    ONLYOTHER,
                    2.0F, "magic", true, false,
                    "socketed.tooltip.default.draconic.desc.2"
                )
            )
        ));

        registerDefaultGemCombinationType("alchemist", new GemCombinationType("socketed.tooltip.default.alchemist", TextFormatting.LIGHT_PURPLE,
            false, false, false, false,
            Arrays.asList("glistering_melon", "magma_cream", "milk_bucket"),
            Collections.singletonList(new MultiEffectGemEffect(
                SocketedSlotTypes.BODY,
                new KillActivator(null),
                ONLYSELF,
                Arrays.asList(
                    new HealGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        4.0F, 0.0F,
                        ""
                    ),
                    new CleanseGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        CleanseGemEffect.CleanseType.HARMFUL, null,
                        ""
                    )
                ),
                "socketed.tooltip.default.alchemist.desc"
            ))
        ));

        registerDefaultGemCombinationType("fortress", new GemCombinationType("socketed.tooltip.default.fortress", TextFormatting.DARK_RED,
            false, false, false, false,
            Arrays.asList("nether_brick", "magma_cream"),
            Collections.singletonList(new PotionGemEffect(
                SocketedSlotTypes.BODY,
                new PassiveActivator(new DimensionCondition(-1, null), 39),
                ONLYSELF,
                MobEffects.FIRE_RESISTANCE.getRegistryName().toString(), 0, 40,
                "socketed.tooltip.default.fortress.desc"
            ))
        ));

        registerDefaultGemCombinationType("tempest", new GemCombinationType("socketed.tooltip.default.tempest", TextFormatting.WHITE,
            false, false, false, false,
            Arrays.asList("end_rod", "fire_charge", "spectral_arrow"),
            Collections.singletonList(new LightningGemEffect(
                SocketedSlotTypes.MAINHAND,
                new AttackingActivator(new MultiCondition(MultiCondition.ConditionLogicType.AND, Arrays.asList(
                        new DamageSourceCondition(false, true, ""),
                        new WeatherCondition(WeatherCondition.WeatherType.RAIN, true, false))),
                        true, AttackActivator.EventType.HURT),
                ONLYOTHER,
                4.0F, false, false,
                "socketed.tooltip.default.tempest.desc"
            ))
        ));

        registerDefaultGemCombinationType("vampiric", new GemCombinationType("socketed.tooltip.default.vampiric", TextFormatting.DARK_RED,
            false, false, false, false,
            Arrays.asList("nether_wart", "fermented_spider_eye"),
            Collections.singletonList(new MultiEffectGemEffect(
                SocketedSlotTypes.HAND,
                new AttackingActivator(new AttackCooldownCondition(0.9F), true, AttackActivator.EventType.HURT),
                ONLYOTHER,
                Arrays.asList(
                    new HealGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYSELF,
                        0.0F, 0.1F,
                        ""
                    ),
                    new CleanseGemEffect(SocketedSlotTypes.ALL, MULTIACT, ONLYOTHER,
                        CleanseGemEffect.CleanseType.BENEFICIAL, null,
                        ""
                    )
                ),
                "socketed.tooltip.default.vampiric.desc"
            ))
        ));

        registerDefaultGemCombinationType("withering", new GemCombinationType("socketed.tooltip.default.withering", TextFormatting.DARK_GRAY,
            false, false, false, false,
            Arrays.asList("soul_sand", "nether_wart", "blazerod"),
            Collections.singletonList(new PotionGemEffect(
                SocketedSlotTypes.HAND,
                new AttackingActivator(null, true, AttackActivator.EventType.HURT),
                ONLYOTHER,
                MobEffects.WITHER.getRegistryName().toString(), 0, 60,
                "socketed.tooltip.default.withering.desc"
            ))
        ));

        registerDefaultGemCombinationType("heavy", new GemCombinationType("socketed.tooltip.default.heavy", TextFormatting.GRAY,
            false, false, false, false,
            Arrays.asList("anvil", "iron", "obsidian"),
            Arrays.asList(
                new DisableShieldGemEffect(
                    SocketedSlotTypes.HAND,
                    new AttackingActivator(null, true, AttackActivator.EventType.ATTACK),
                    ONLYOTHER,
                    100,
                    "socketed.tooltip.default.heavy.desc.1"
                ),
                new KnockbackGemEffect(
                    SocketedSlotTypes.HAND,
                    new AttackingActivator(null, true, AttackActivator.EventType.HURT),
                    ONLYOTHER,
                    1.0F, false,
                    "socketed.tooltip.default.heavy.desc.2"
                )
            )
        ));
    }
}