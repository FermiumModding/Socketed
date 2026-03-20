package socketed.api.socket.gem.effect.slot;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import socketed.api.util.SocketedUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum SocketedSlotTypes implements ISlotType {
	
	@SerializedName("ALL")
	ALL {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return true;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type != NONE;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			HashSet<ItemStack> set = new HashSet<>();
			for(ISlotType type : SocketedUtil.getAllSlotTypes()) {
				if(this != type) set.addAll(type.getEquippedStacks(player));
			}
			return set;
		}
		
		@Override
		public String getTooltipKey() {
			return "all";
		}
	},
	@SerializedName("NONE")
	NONE {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return false;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(ItemStack.EMPTY);
		}
		
		@Override
		public String getTooltipKey() {
			return "none";
		}
	},
	
	@SerializedName("BODY")
	BODY {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
			return slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == HEAD || type == CHEST || type == LEGS || type == FEET || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			HashSet<ItemStack> set = new HashSet<>();
			set.addAll(HEAD.getEquippedStacks(player));
			set.addAll(CHEST.getEquippedStacks(player));
			set.addAll(LEGS.getEquippedStacks(player));
			set.addAll(FEET.getEquippedStacks(player));
			return set;
		}
		
		@Override
		public String getTooltipKey() {
			return "body";
		}
	},
	@SerializedName("HEAD")
	HEAD {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.HEAD;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == BODY || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
		}
		
		@Override
		public String getTooltipKey() {
			return "head";
		}
	},
	@SerializedName("CHEST")
	CHEST {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.CHEST;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == BODY || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
		}
		
		@Override
		public String getTooltipKey() {
			return "chest";
		}
	},
	@SerializedName("LEGS")
	LEGS {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.LEGS;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == BODY || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS));
		}
		
		@Override
		public String getTooltipKey() {
			return "legs";
		}
	},
	@SerializedName("FEET")
	FEET {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.FEET;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == BODY || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(player.getItemStackFromSlot(EntityEquipmentSlot.FEET));
		}
		
		@Override
		public String getTooltipKey() {
			return "feet";
		}
	},
	
	@SerializedName("HAND")
	HAND {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
			return slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == MAINHAND || type == OFFHAND || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			HashSet<ItemStack> set = new HashSet<>();
			set.addAll(MAINHAND.getEquippedStacks(player));
			set.addAll(OFFHAND.getEquippedStacks(player));
			return set;
		}
		
		@Override
		public String getTooltipKey() {
			return "hand";
		}
	},
	@SerializedName("MAINHAND")
	MAINHAND {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.MAINHAND;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == HAND || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
		}
		
		@Override
		public String getTooltipKey() {
			return "mainhand";
		}
	},
	@SerializedName("OFFHAND")
	OFFHAND {
		
		@Override
		public boolean isStackValid(ItemStack stack) {
			return EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.OFFHAND;
		}
		
		@Override
		public boolean isSlotValid(ISlotType type) {
			return type == this || type == HAND || type == ALL;
		}
		
		@Override
		public Set<ItemStack> getEquippedStacks(EntityPlayer player) {
			return Collections.singleton(player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND));
		}
		
		@Override
		public String getTooltipKey() {
			return "offhand";
		}
	};

	public static SocketedSlotTypes fromEntityEquipmentSlot(EntityEquipmentSlot slot) {
		switch (slot) {
			case MAINHAND: return MAINHAND;
			case OFFHAND: return OFFHAND;
			case HEAD: return HEAD;
			case CHEST: return CHEST;
			case LEGS: return LEGS;
			case FEET: return FEET;
			default: return NONE;
		}
	}
}