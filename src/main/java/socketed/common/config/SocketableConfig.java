package socketed.common.config;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraftforge.common.config.Config;
import org.apache.logging.log4j.Level;
import socketed.Socketed;
import socketed.api.util.ItemMatcherType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
					"Additional Types Pattern: <TYPENAME>;<MATCHER>;<Values>" + "\n" +
					"Available Matchers:" + "\n" +
					"  ITEMID: matches exact item ids, comma separated, such as minecraft:stick,minecraft:blaze_rod" + "\n" +
					"  MODID: matches all items from the given mod ids, comma separated, such as spartanweaponry,spartanfire" + "\n" +
					"  CLASS: matches items that are an instance of the given classes, comma separated, such as net.minecraft.item.ItemTool" + "\n" +
					"  REGEX: matches item ids against a single regex, use | for alternatives" + "\n" +
					"If an item matches multiple entries, the first one in this list is used")
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
			//"BATTLEAXE;REGEX;(mujmajnkraftsbettersurvival\\:item.*battleaxe)|(spartan(defiled|fire|weaponry)\\:battleaxe.*)",
			//"BS_WEAPON;REGEX;mujmajnkraftsbettersurvival\\:item.*(dagger|nunchaku|hammer|battleaxe)",
			//"SW_CROSSBOW;REGEX;spartan(defiled|fire|weaponry)\\:crossbow.*"
	};
	
	@Config.Comment("If socketable item type definitions added by addon mods should be used")
	@Config.Name("Allow Forced Socketable Item Types")
	public boolean allowForcedItemTypes = true;
	
	private volatile Map<String, SocketableType> enabledItemTypes = null;
	private final Map<Item, Optional<String>> cachedItemResults = new ConcurrentHashMap<>();

	@Nullable
	public String getSocketableType(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return null;
		//Cache results to help reduce impact of repeated regex checks
		Optional<String> cached = this.cachedItemResults.get(stack.getItem());
		if(cached != null) return cached.orElse(null);

		Map<String, SocketableType> itemTypes = this.enabledItemTypes;
		if(itemTypes == null) {
			itemTypes = this.parseItemTypes();
			this.enabledItemTypes = itemTypes;
		}
		String result = null;
		for(Map.Entry<String, SocketableType> entry : itemTypes.entrySet()) {
			if(entry.getValue().canSocket(stack.getItem())) {
				result = entry.getKey();
				break;
			}
		}
		//Absence of a key means it has not been parsed, an empty value means there is no match
		this.cachedItemResults.put(stack.getItem(), Optional.ofNullable(result));
		return result;
	}
	
	public boolean canSocket(ItemStack stack) {
		return this.getSocketableType(stack) != null;
	}
	
	private Map<String, SocketableType> parseItemTypes() {
		Map<String, SocketableType> map = new LinkedHashMap<>();
		for(String s : this.itemTypesConfig) {
			String[] split = s.trim().split(";", 3);
			if(split.length == 1) {
				SocketableType type = SocketableConfig.defaultItemTypes.get(split[0]);
				if(type != null) map.put(split[0], type);
				else Socketed.LOGGER.log(Level.WARN, "Invalid Socketable Item Type Entry: " + s);
			}
			else {
				try {
					if(split.length == 2) {
						//old config handling
						Socketed.LOGGER.log(Level.WARN, "Legacy Socketable Item Type Entry: " + s + ", should be updated to " + split[0] + ";REGEX;" + split[1]);
						map.put(split[0], new SocketableType(split[1]));
					}
					else {
						ItemMatcherType matcherType = ItemMatcherType.valueOf(split[1].trim().toUpperCase(Locale.ROOT));
						map.put(split[0], new SocketableType(matcherType.createMatcher(split[2])));
					}
				}
				catch(IllegalArgumentException e) {
					Socketed.LOGGER.log(Level.WARN, "Invalid Socketable Item Type Entry: " + s + ", " + e.getMessage());
				}
			}
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
			this.canSocket = ItemMatcherType.REGEX.createMatcher(regex);
		}

		public boolean canSocket(Item item) {
			return this.canSocket.test(item);
		}
	}
}