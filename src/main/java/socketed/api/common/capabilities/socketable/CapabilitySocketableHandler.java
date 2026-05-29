package socketed.api.common.capabilities.socketable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import socketed.Socketed;
import socketed.api.socket.GenericSocket;
import socketed.api.socket.gem.GemCombinationInstance;
import socketed.api.util.SocketedUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class CapabilitySocketableHandler {
    
    public static final ResourceLocation CAP_SOCKETABLE_KEY = new ResourceLocation(Socketed.MODID, "socketable");
    
    @CapabilityInject(ICapabilitySocketable.class)
    public static Capability<ICapabilitySocketable> CAP_SOCKETABLE;
    
    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(ICapabilitySocketable.class, new Storage(), CapabilitySocketable::new);
    }
    
    @Mod.EventBusSubscriber
    public static class EventHandler {
        
        @SubscribeEvent
        public static void attachCapabilitiesEventItemStack(AttachCapabilitiesEvent<ItemStack> event) {
            ItemStack stack = event.getObject();
            if(stack.isEmpty()) return;
            if(stack.getMaxStackSize() > 1) return;
            if(!SocketedUtil.hasCompletedLoading(true)) return;
            if(stack.hasCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null)) return;

            //Allowed item types that can get sockets
            if(SocketedUtil.canStackHaveSockets(stack)) {
                event.addCapability(CapabilitySocketableHandler.CAP_SOCKETABLE_KEY, new CapabilitySocketableHandler.Provider(stack));
                
                //This tag is only used for syncing, as share tag will not be sent to client if a stack does not have a non-capability tag
                NBTTagCompound tag = stack.getTagCompound();
                if(tag == null) tag = new NBTTagCompound();
                tag.setBoolean("CanSocket", true);
                stack.setTagCompound(tag);
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
        
        private final ICapabilitySocketable instance;

        public Provider(ItemStack stack) {
            this.instance = new CapabilitySocketable(stack);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAP_SOCKETABLE;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAP_SOCKETABLE ? CAP_SOCKETABLE.cast(instance) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return (NBTTagCompound)CAP_SOCKETABLE.writeNBT(instance, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            CAP_SOCKETABLE.readNBT(instance, null, nbt);
        }
    }

    public static class Storage implements Capability.IStorage<ICapabilitySocketable> {
        
        @Override
        public NBTBase writeNBT(Capability<ICapabilitySocketable> capability, ICapabilitySocketable instance, EnumFacing side) {
            //If capability hasn't changed, return the cached NBT
            NBTTagCompound cachedNBT = instance.getCachedNBT();
            if(cachedNBT != null)
                return cachedNBT;

            NBTTagCompound nbt = new NBTTagCompound();

            NBTTagList socketTagList = new NBTTagList();
            //Don't store NBT to stacks that could get sockets but don't have anything yet
            if (!instance.getSockets().isEmpty()) {
                for (GenericSocket socket : new ArrayList<>(instance.getSockets()))
                    socketTagList.appendTag(socket.writeToNBT());
                nbt.setTag("Sockets", socketTagList);
            }

            NBTTagList gemCombinationTagList = new NBTTagList();
            //Don't store NBT to stacks that could get sockets but don't have anything yet
            if(!instance.getGemCombinations().isEmpty()) {
                for (GemCombinationInstance gemCombination : new ArrayList<>(instance.getGemCombinations()))
                    gemCombinationTagList.appendTag(gemCombination.writeToNBT());
                if (!gemCombinationTagList.isEmpty()) nbt.setTag("GemCombinations", gemCombinationTagList);
            }

            instance.setCachedNBT(nbt); //cache new NBT
            return nbt;
        }

        @Override
        public void readNBT(Capability<ICapabilitySocketable> capability, ICapabilitySocketable instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound tags = (NBTTagCompound)nbt;
            
            //Reset cap before reading incase an existing cap is being read to such as during sync
            instance.resetCap();
            
            boolean read = false;
            if(tags.hasKey("Sockets")) {
                NBTTagList socketsNBT = tags.getTagList("Sockets",10);
                for(int socketIndex = 0; socketIndex < socketsNBT.tagCount(); socketIndex++) {
                    NBTTagCompound socketNBT = socketsNBT.getCompoundTagAt(socketIndex);
                    String socketType = socketNBT.getString("SocketType");
                    instance.addSocketFromNBT(socketType, socketNBT);
                    read = true;
                }
            }
            if(tags.hasKey("GemCombinations")) {
                NBTTagList combinationsNBT = tags.getTagList("GemCombinations",10);
                for(int combinationIndex = 0; combinationIndex < combinationsNBT.tagCount(); combinationIndex++) {
                    instance.addCombinationFromNBT(combinationsNBT.getCompoundTagAt(combinationIndex));
                    read = true;
                }
            }
            //Refresh combinations after reading to account for combination changes on update
            if(read) instance.refreshCombinations();

            //After reading the cached NBT is null=invalid, will be set on next itemStack comparison
        }
    }
}