package socketed.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.socket.GenericSocket;
import socketed.api.socket.TieredSocket;
import socketed.common.config.ForgeConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AddSocketCommand extends CommandBase {
    
    @Override
    public String getName() {
        return "socketed";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/socketed add <type> <optional:tier>" + "\n" +
                "/socketed replace <index> <type> <optional:tier>" + "\n" +
                "/socketed lock <index>" + "\n" +
                "/socketed disable <index>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] argString) throws WrongUsageException {
        if(!(sender.getCommandSenderEntity() instanceof EntityPlayer)) return;
        if(sender.getEntityWorld().isRemote) return;
        if(argString.length < 1) return;
        EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
        ItemStack mainhand = player.getHeldItemMainhand().copy();
        ICapabilitySocketable cap = mainhand.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        if(cap == null) return;
		switch(argString[0]) {
			case "add": {
                if(argString.length > 1) {
                    if(argString[1].equals("generic")) {
                        cap.addSocket(new GenericSocket());
                        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mainhand);
                        sender.sendMessage(new TextComponentString("Added generic socket"));
                        return;
                    }
                    else if(argString[1].equals("tiered")) {
                        int tier = 0;
                        try {
                            tier = Math.min(3, Math.max(0, Integer.parseInt(argString[2])));
                        }
                        catch(Exception ignored) {}
                        cap.addSocket(new TieredSocket(tier));
                        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mainhand);
                        sender.sendMessage(new TextComponentString("Added tier " + tier + " socket"));
                        return;
                    }
                    throw new WrongUsageException("Invalid command usage, must specify socket type <generic/tiered>");
                }
                throw new WrongUsageException("Invalid command usage", new Object[]{argString});
            }
			case "replace": {
                if(argString.length > 2) {
                    int index;
                    try {
                        index = Integer.parseInt(argString[1]);
                    }
                    catch(Exception ignored) {
                        index = -1;
                    }
                    if(cap.getSocketAt(index) == null) {
                        throw new WrongUsageException("Invalid command usage, specified index has no socket");
                    }
                    
                    if(argString[2].equals("generic")) {
                        cap.replaceSocketAt(new GenericSocket(), index);
                        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mainhand);
                        sender.sendMessage(new TextComponentString("Replaced index " + index + " with generic socket"));
                        return;
                    }
                    else if(argString[2].equals("tiered")) {
                        int tier = 0;
                        try {
                            tier = MathHelper.clamp(Integer.parseInt(argString[3]), 0, ForgeConfig.COMMON.maxSocketTier);
                        }
                        catch(Exception ignored) {}
                        cap.replaceSocketAt(new TieredSocket(tier), index);
                        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mainhand);
                        sender.sendMessage(new TextComponentString("Replaced index " + index + " with tier " + tier + " socket"));
                        return;
                    }
                    throw new WrongUsageException("Invalid command usage, must specify socket type <generic/tiered>");
                }
                throw new WrongUsageException("Invalid command usage", new Object[]{argString});
            }
			case "lock": {
                if(argString.length > 1) {
                    int index;
                    try {
                        index = Integer.parseInt(argString[1]);
                    }
                    catch(Exception ignored) {
                        index = -1;
                    }
                    GenericSocket socket = cap.getSocketAt(index);
                    if(socket == null) {
                        throw new WrongUsageException("Invalid command usage, specified index has no socket");
                    }
                    
                    socket.setLocked(!socket.isLocked());
                    cap.setCachedNBT(null); //mark dirty
                    player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mainhand);
                    sender.sendMessage(new TextComponentString("Toggled locked at index " + index));
                    return;
                }
                throw new WrongUsageException("Invalid command usage", new Object[]{argString});
            }
			case "disable": {
                if(argString.length > 1) {
                    int index;
                    try {
                        index = Integer.parseInt(argString[1]);
                    }
                    catch(Exception ignored) {
                        index = -1;
                    }
                    GenericSocket socket = cap.getSocketAt(index);
                    if(socket == null) {
                        throw new WrongUsageException("Invalid command usage, specified index has no socket");
                    }
                    
                    socket.setDisabled(!socket.isDisabled());
                    cap.setCachedNBT(null); //mark dirty
                    player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mainhand);
                    sender.sendMessage(new TextComponentString("Toggled disabled at index " + index));
                    return;
                }
                throw new WrongUsageException("Invalid command usage", new Object[]{argString});
            }
			default: {
                throw new WrongUsageException("Invalid command usage" + "\n" + getUsage(sender));
            }
		}
    }

    @Override @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) return CommandBase.getListOfStringsMatchingLastWord(args, "add", "replace", "lock", "disable");

        List<String> completions = new ArrayList<>();

        if(!(sender.getCommandSenderEntity() instanceof EntityPlayer)) return completions;
        if(sender.getEntityWorld().isRemote) return completions;
        EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();

        ICapabilitySocketable cap = player.getHeldItemMainhand().getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        if(cap == null) return completions;

        if(args[0].equals("add")) {
            if(args.length == 2) {
                completions.addAll(CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList("tiered", "generic")));
            } else if(args.length == 3) {
                if(!args[1].equals("tiered")) return completions;
                completions.addAll(CommandBase.getListOfStringsMatchingLastWord(args, IntStream.range(0, ForgeConfig.COMMON.maxSocketTier).boxed().collect(Collectors.toList())));
            }
        } else {
            if (args.length == 2) {
                completions.addAll(CommandBase.getListOfStringsMatchingLastWord(args, IntStream.range(0, cap.getSockets().size()).boxed().collect(Collectors.toList())));
            } else if(args.length == 3) {
                if(!args[0].equals("replace")) return completions;
                completions.addAll(CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList("tiered", "generic")));
            } else if(args.length == 4) {
                if(!args[0].equals("replace")) return completions;
                if(!args[2].equals("tiered")) return completions;
                completions.addAll(CommandBase.getListOfStringsMatchingLastWord(args, IntStream.range(0, ForgeConfig.COMMON.maxSocketTier).boxed().collect(Collectors.toList())));
            }
        }
        return completions;
    }
}