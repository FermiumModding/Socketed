package socketed.common.socket.gem.filter;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import socketed.Socketed;
import socketed.api.socket.gem.filter.GenericFilter;

import javax.annotation.Nonnull;

public class OreFilter extends GenericFilter {

    public static final String TYPE_NAME = "Ore Dictionary";

    @SerializedName("Ore Dictionary Name")
    private final String dictName;

    @SerializedName("Strict Metadata")
    private Boolean strict;

    public OreFilter(String name) {
        this(name, false);
    }

    public OreFilter(String name, boolean strict) {
        super();
        this.dictName = name;
        this.strict = strict;
    }
    
    @Nonnull
    public String getName() {
        return this.dictName;
    }

    @Override
    public boolean matches(ItemStack input) {
        if(input == null || input.isEmpty()) return false;
        NonNullList<ItemStack> list = OreDictionary.getOres(this.dictName, false);
        for(ItemStack stack : list) {
            if(stack.getItem().equals(input.getItem()) && (!this.strict || stack.getMetadata() == OreDictionary.WILDCARD_VALUE || stack.getMetadata() == input.getMetadata())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    /**
     * DictName: Required
     * Strict: Optional, default true
     */
    @Override
    public boolean validate() {
        if(this.strict == null) this.strict = true;
        
        if(this.dictName == null || this.dictName.isEmpty()) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Filter, dictionary name null or empty");
        else {
            //Warn but don't invalidate, in case ore dictionaries get added after loading
            if(!OreDictionary.doesOreNameExist(this.dictName)) Socketed.LOGGER.warn(this.getTypeName() + " Filter " + this.dictName + " may be invalid, dictionary does not exist");
            else if(OreDictionary.getOres(this.dictName, false).isEmpty()) Socketed.LOGGER.warn(this.getTypeName() + " Filter " + this.dictName + " may be invalid, dictionary exists but is empty");
            
            return true;
        }
        return false;
    }
}