package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.common.socket.gem.effect.activatable.activator.BlockBreakActivator;
import socketed.common.socket.gem.effect.activatable.activator.KillActivator;
import socketed.common.socket.gem.effect.activatable.activator.MultiEffectActivator;
import socketed.common.socket.gem.effect.activatable.condition.BlockCondition;
import socketed.mixin.vanilla.IEntityLivingBaseMixin;
import socketed.mixin.vanilla.IEntityLivingMixin;

import javax.annotation.Nullable;
import java.util.List;

public class BonusDropsGemEffect extends ActivatableGemEffect {

	public static final String TYPE_NAME = "Bonus Drops";

	@SerializedName("Rolls")
	private Integer rolls;

	public BonusDropsGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, int rolls, String tooltipKey) {
		super(slotType, activator, targets, tooltipKey);
		this.rolls = rolls;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTooltipString() {
		return I18n.format("socketed.tooltip.effect.bonusdrops", this.rolls);
	}

	@Override
	public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(playerSource == null || effectTarget == null || playerSource.world.isRemote) return;
		if(!(callback instanceof GenericEventCallback)) return;
		Event event = ((GenericEventCallback<?>)callback).getEvent();

		if(event instanceof BlockEvent.BreakEvent && effectTarget == playerSource) {
			this.rollBlockDrops(playerSource, (BlockEvent.BreakEvent)event);
		}
		else if(event instanceof LivingDeathEvent && effectTarget == ((LivingDeathEvent)event).getEntityLiving()) {
			this.rollLootTable(playerSource, (LivingDeathEvent)event);
		}
	}

	private void rollBlockDrops(EntityPlayer player, BlockEvent.BreakEvent event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		IBlockState state = event.getState();
		Block block = state.getBlock();
		if(player.isCreative()) return;
		if(block.hasTileEntity(state)) return;
		if(!ForgeHooks.canHarvestBlock(block, player, world, pos)) return;

		ItemStack tool = player.getHeldItemMainhand();
		boolean silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0 && block.canSilkHarvest(world, pos, state, player);
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);

		for(int i = 0; i < this.rolls; i++) {
			if(silkTouch) {
				ItemStack silkDrop = BlockCondition.getStackForBlock(state);
				if(!silkDrop.isEmpty()) Block.spawnAsEntity(world, pos, silkDrop);
			}
			else {
				NonNullList<ItemStack> drops = NonNullList.create();
				block.getDrops(drops, world, pos, state, fortune);
				for(ItemStack drop : drops) {
					if(!drop.isEmpty()) Block.spawnAsEntity(world, pos, drop);
				}
			}
		}
	}

	private void rollLootTable(EntityPlayer player, LivingDeathEvent event) {
		if(!(event.getEntityLiving() instanceof EntityLiving)) return;
		EntityLiving killed = (EntityLiving)event.getEntityLiving();
		if(!(killed.world instanceof WorldServer)) return;
		if(!((IEntityLivingBaseMixin)killed).socketed$invokeCanDropLoot()) return;
		if(!killed.world.getGameRules().getBoolean("doMobLoot")) return;

		ResourceLocation location = ((IEntityLivingMixin)killed).socketed$getDeathLootTable();
		if(location == null) location = ((IEntityLivingMixin)killed).socketed$invokeGetLootTable();
		if(location == null) return;

		LootTable table = killed.world.getLootTableManager().getLootTableFromLocation(location);
		LootContext context = new LootContext.Builder((WorldServer)killed.world)
				.withLootedEntity(killed)
				.withDamageSource(event.getSource())
				.withPlayer(player)
				.withLuck(player.getLuck())
				.build();

		for(int i = 0; i < this.rolls; i++) {
			for(ItemStack drop : table.generateLootForPools(killed.getRNG(), context)) {
				if(!drop.isEmpty()) killed.entityDropItem(drop, 0.0F);
			}
		}
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Rolls: Optional, default 1
	 */
	@Override
	public boolean validate() {
		if(super.validate()) {
			if(this.rolls == null) this.rolls = 1;
			if(this.rolls < 1) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, rolls can not be less than 1");
			else {
				GenericActivator activator = this.getActivator();
				if(!(activator instanceof BlockBreakActivator) && !(activator instanceof KillActivator) && !(activator instanceof MultiEffectActivator)) {
					Socketed.LOGGER.warn("Possible invalid " + this.getTypeName() + " Effect, requires the Block Break or Kill activator to have any effect");
				}
				return true;
			}
		}
		return false;
	}
}