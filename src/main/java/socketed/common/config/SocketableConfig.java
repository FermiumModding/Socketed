package socketed.common.config;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import org.apache.logging.log4j.Level;
import socketed.Socketed;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SocketableConfig {
	
	private static final Map<String, SocketableType> defaultItemTypes = new HashMap<>();
	public static final Map<String, SocketableType> forcedItemTypes = new HashMap<>();
	
	static {
		SocketableConfig.defaultItemTypes.put("HELMET", new SocketableType(v -> v instanceof ItemArmor && ((ItemArmor)v).armorType.equals(EntityEquipmentSlot.HEAD)));
		SocketableConfig.defaultItemTypes.put("CHESTPLATE", new SocketableType(v -> v instanceof ItemArmor && ((ItemArmor)v).armorType.equals(EntityEquipmentSlot.CHEST)));
		SocketableConfig.defaultItemTypes.put("LEGGINGS", new SocketableType(v -> v instanceof ItemArmor && ((ItemArmor)v).armorType.equals(EntityEquipmentSlot.LEGS)));
		SocketableConfig.defaultItemTypes.put("BOOTS", new SocketableType(v -> v instanceof ItemArmor && ((ItemArmor)v).armorType.equals(EntityEquipmentSlot.FEET)));
		SocketableConfig.defaultItemTypes.put("SWORD", new SocketableType(v -> v instanceof ItemSword));
		SocketableConfig.defaultItemTypes.put("FISHING_ROD", new SocketableType(v -> v instanceof ItemFishingRod));
		SocketableConfig.defaultItemTypes.put("BOW", new SocketableType(v -> v instanceof ItemBow));
		SocketableConfig.defaultItemTypes.put("AXE", new SocketableType(v -> v instanceof ItemAxe));
		SocketableConfig.defaultItemTypes.put("PICKAXE", new SocketableType(v -> v instanceof ItemPickaxe));
		SocketableConfig.defaultItemTypes.put("HOE", new SocketableType(v -> v instanceof ItemHoe));
		SocketableConfig.defaultItemTypes.put("SHOVEL", new SocketableType(v -> v instanceof ItemSpade));
		SocketableConfig.defaultItemTypes.put("SHIELD", new SocketableType(v -> v instanceof ItemShield));
	}
	
	@Config.Comment(
			"Defaults are predefined item types that can receive sockets, remove the entry to disable" + "\n" +
					"Additional item types can be defined to allow for socketing other items" + "\n" +
					"Additional Types Pattern: <TYPENAME>;<Item ID Regex>")
	@Config.Name("Socketable Item Type Definitions")
	public String[] itemTypesConfig = {
			"HELMET",
			"CHESTPLATE",
			"LEGGINGS",
			"BOOTS",
			"SWORD",
			"FISHING_ROD",
			"BOW",
			"AXE",
			"PICKAXE",
			"HOE",
			"SHOVEL",
			"SHIELD",
			//"BATTLEAXE;(mujmajnkraftsbettersurvival\\:item.*battleaxe)|(spartan(defiled|fire|weaponry)\\:battleaxe.*)",
			//"BS_WEAPON;mujmajnkraftsbettersurvival\\:item.*(dagger|nunchaku|hammer|battleaxe)",
			//"SW_CROSSBOW;spartan(defiled|fire|weaponry)\\:crossbow.*"
	};
	
	@Config.Comment("If socketable item type definitions added by addon mods should be used")
	@Config.Name("Allow Forced Socketable Item Types")
	public boolean allowForcedItemTypes = true;
	
	private Map<String, SocketableType> enabledItemTypes = null;
	private final Map<Item, String> cachedItemResults = new HashMap<>();

	@Nullable
	public String getSocketableType(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return null;
		if(this.enabledItemTypes == null) this.enabledItemTypes = this.parseItemTypes();
		//Cache results to help reduce impact of repeated regex checks
		if(this.cachedItemResults.containsKey(stack.getItem())) {
			return this.cachedItemResults.get(stack.getItem());
		}
		else {
			for(HashMap.Entry<String, SocketableType> entry : this.enabledItemTypes.entrySet()) {
				if(entry.getValue().canSocket(stack.getItem())) {
					this.cachedItemResults.put(stack.getItem(), entry.getKey());
					return entry.getKey();
				}
			}
			//Absence of a key means it has not been parsed, cache null if there is no match
			this.cachedItemResults.put(stack.getItem(), null);
			return null;
		}
	}
	
	public boolean canSocket(ItemStack stack) {
		return this.getSocketableType(stack) != null;
	}
	
	private HashMap<String, SocketableType> parseItemTypes() {
		HashMap<String, SocketableType> map = new HashMap<>();
		for(String s : this.itemTypesConfig) {
			String[] split = s.split(";");
			if(split.length == 1) {
				SocketableType type = SocketableConfig.defaultItemTypes.get(s);
				if(type != null) map.put(s, type);
				else Socketed.LOGGER.log(Level.WARN, "Invalid Socketable Item Type Entry: " + s);
			}
			else if(split.length == 2) {
				map.put(split[0], new SocketableType(split[1]));
			}
			else Socketed.LOGGER.log(Level.WARN, "Invalid Socketable Item Type Entry: " + s);
		}
		if(ForgeConfig.SOCKETABLES.allowForcedItemTypes) map.putAll(SocketableConfig.forcedItemTypes);
		return map;
	}
	
	protected void reset() {
		this.enabledItemTypes = null;
		this.cachedItemResults.clear();
	}

	//TODO: Changed from ItemStack to Item to allow for caching easier, theoretically ItemStack data shouldn't make a difference from an Item for socketable, change in future if really required
	public static class SocketableType {
		private final Predicate<Item> canSocket;

		public SocketableType(Predicate<Item> canSocket) {
			this.canSocket = canSocket;
		}
		
		public SocketableType(String regex) {
			this.canSocket = item -> {
				ResourceLocation location = item.getRegistryName();
				return location != null && location.toString().matches(regex);
			};
		}

		public boolean canSocket(Item item) {
			return this.canSocket.test(item);
		}
	}
}