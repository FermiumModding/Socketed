package socketed.api.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import socketed.Socketed;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Item matching system based on EnchantmentControl
 */
public enum ItemMatcherType {

	/**
	 * Matches items by exact item id, such as minecraft:stick
	 */
	ITEMID {
		@Override
		public Predicate<Item> createMatcher(String values) {
			Set<String> itemIds = splitValues(values);
			return item -> {
				ResourceLocation location = item.getRegistryName();
				return location != null && itemIds.contains(location.toString());
			};
		}
	},

	/**
	 * Matches all items from the given mod ids, such as minecraft
	 */
	MODID {
		@Override
		public Predicate<Item> createMatcher(String values) {
			Set<String> modIds = splitValues(values);
			return item -> {
				ResourceLocation location = item.getRegistryName();
				return location != null && modIds.contains(location.getNamespace());
			};
		}
	},

	/**
	 * Matches items that are an instance of the given classes, such as net.minecraft.item.ItemSword
	 */
	CLASS {
		@Override
		public Predicate<Item> createMatcher(String values) {
			Set<Class<?>> classes = new HashSet<>();
			for(String className : splitValues(values)) {
				try {
					classes.add(Class.forName(className, false, ItemMatcherType.class.getClassLoader()));
				}
				catch(ClassNotFoundException | LinkageError e) {
					Socketed.LOGGER.log(Level.WARN, "Could not find class for Socketable Item Type: " + className);
				}
			}
			if(classes.isEmpty()) throw new IllegalArgumentException("no valid classes found");
			return item -> classes.stream().anyMatch(clazz -> clazz.isInstance(item));
		}
	},

	/**
	 * Matches item ids against a single regex, multiple values are not split as regexes can contain commas
	 */
	REGEX {
		@Override
		public Predicate<Item> createMatcher(String values) {
			if(values.trim().isEmpty()) throw new IllegalArgumentException("no values provided");
			Pattern pattern = Pattern.compile(values.trim());
			return item -> {
				ResourceLocation location = item.getRegistryName();
				return location != null && pattern.matcher(location.toString()).matches();
			};
		}
	};

	/**
	 * @param values comma separated values for this matcher, or a single regex for REGEX
	 * @throws IllegalArgumentException if the values are not valid for this matcher
	 */
	public abstract Predicate<Item> createMatcher(String values);

	private static Set<String> splitValues(String values) {
		Set<String> set = new HashSet<>();
		Arrays.stream(values.split(",")).map(String::trim).filter(value -> !value.isEmpty()).forEach(set::add);
		if(set.isEmpty()) throw new IllegalArgumentException("no values provided");
		return set;
	}
}