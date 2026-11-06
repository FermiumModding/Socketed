package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.callback.GenericEventCallback;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.filter.GenericFilter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockCondition extends GenericCondition {

	public static final String TYPE_NAME = "Block";

	@SerializedName("Block Filters")
	protected List<GenericFilter> filters;

	public BlockCondition(List<GenericFilter> filters) {
		super();
		this.filters = filters;
	}

	@Override
	public boolean testCondition(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
		if(!(callback instanceof GenericEventCallback)) return false;
		Event event = ((GenericEventCallback<?>)callback).getEvent();
		if(!(event instanceof BlockEvent)) return false;

		ItemStack stack = getStackForBlock(((BlockEvent)event).getState());
		if(stack.isEmpty()) return false;
		for(GenericFilter filter : this.filters) {
			if(filter.matches(stack)) return true;
		}
		return false;
	}

	public static ItemStack getStackForBlock(IBlockState state) {
		Block block = state.getBlock();
		Item item = Item.getItemFromBlock(block);
		if(item == Items.AIR) return ItemStack.EMPTY;
		int meta = item.getHasSubtypes() ? block.damageDropped(state) : 0;
		return new ItemStack(item, 1, meta);
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/**
	 * Block Filters: Required, at least one valid filter, invalid filters are discarded
	 */
	@Override
	public boolean validate() {
		if(this.filters == null || this.filters.isEmpty()) {
			Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, must define at least one block filter");
			return false;
		}
		List<GenericFilter> validFilters = new ArrayList<>();
		for(GenericFilter filter : this.filters) {
			if(filter != null && filter.validate()) validFilters.add(filter);
			else Socketed.LOGGER.warn("Invalid filter in " + this.getTypeName() + " Condition, discarding");
		}
		if(validFilters.isEmpty()) {
			Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Condition, no valid block filters");
			return false;
		}
		this.filters = validFilters;
		return true;
	}
}