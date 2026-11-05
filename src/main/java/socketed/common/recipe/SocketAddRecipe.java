package socketed.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.common.config.ForgeConfig;
import socketed.common.item.ItemSocketGeneric;
import socketed.api.socket.GenericSocket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SocketAddRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return validInput(inv) != null;
    }
    
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        Integer[] slots = validInput(inv);
        if(slots == null) return ItemStack.EMPTY;
        
        ItemStack output = inv.getStackInSlot(slots[0]).copy();
        ICapabilitySocketable cap = output.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        //Check not needed, but for sanity
        if(cap == null) return ItemStack.EMPTY;
        
        ItemStack socketItemStack = inv.getStackInSlot(slots[1]);
        //Check not needed, but for sanity
        if(!(socketItemStack.getItem() instanceof ItemSocketGeneric)) return ItemStack.EMPTY;
        GenericSocket newSocket = ((ItemSocketGeneric)socketItemStack.getItem()).getNewSocket();
        
        cap.addSocket(newSocket);
        return output;
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
    
    @Nullable
    private Integer[] validInput(InventoryCrafting inv) {
        int numStacks = 0;
        int itemSlot = -1;
        int socketSlot = -1;
        List<Integer> occupiedSlots = new ArrayList<>();
        
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(!inv.getStackInSlot(i).isEmpty()) {
                numStacks++;
                occupiedSlots.add(i);
            }
        }
        if(numStacks != 2) return null;
        
        for(int i : occupiedSlots) {
            ItemStack itemStack = inv.getStackInSlot(i);
            
            if(itemStack.getItem() instanceof ItemSocketGeneric) socketSlot = i;
            else if(itemStack.getMaxStackSize() == 1) {
                ICapabilitySocketable cap = itemStack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
                boolean locked = ForgeConfig.COMMON.enchantmentLock && itemStack.isItemEnchanted();
                if(cap != null && !locked && cap.getSocketCount() < ForgeConfig.COMMON.maxSockets) itemSlot = i;
                else return null;
            }
            else return null;
        }
        Integer[] slots = new Integer[2];
        slots[0] = itemSlot;
        slots[1] = socketSlot;
        return (itemSlot != -1 && socketSlot != -1) ? slots : null;
    }
    
    @Override
    public boolean isDynamic() {
        return true;
    }
}