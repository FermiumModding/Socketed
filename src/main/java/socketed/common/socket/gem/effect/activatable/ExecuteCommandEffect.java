package socketed.common.socket.gem.effect.activatable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nullable;
import java.util.List;

public class ExecuteCommandEffect extends ActivatableGemEffect {
    public static final String TYPE_NAME = "Execute Command";

    @SerializedName("Command")
    private final String command;

    protected ExecuteCommandEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, String tooltipKey, String command) {
        super(slotType, activator, targets, tooltipKey);
        this.command = command;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getTooltipString() {
        return I18n.format("socketed.tooltip.effect.executecommand", this.command);
    }

    @Override
    public void performEffect(@Nullable IEffectCallback callback, EntityPlayer playerSource, EntityLivingBase effectTarget) {
        if(playerSource.world.isRemote) return;

        String actualCommand = this.command
                .replaceAll("%playerName%", playerSource.getName())
                .replaceAll("%playerUUID%", playerSource.getUniqueID().toString())
                .replaceAll("%playerX%", ""+playerSource.getPosition().getX())
                .replaceAll("%playerY%", ""+playerSource.getPosition().getY())
                .replaceAll("%playerZ%", ""+playerSource.getPosition().getZ())
                .replaceAll("%playerPos%", playerSource.getPosition().getX() + " " + playerSource.getPosition().getY() + " " + playerSource.getPosition().getZ())
                .replaceAll("%targetName%", effectTarget.getName())
                .replaceAll("%targetUUID%", effectTarget.getUniqueID().toString())
                .replaceAll("%targetX%", ""+effectTarget.getPosition().getX())
                .replaceAll("%targetY%", ""+effectTarget.getPosition().getY())
                .replaceAll("%targetZ%", ""+effectTarget.getPosition().getZ())
                .replaceAll("%targetPos%", effectTarget.getPosition().getX() + " " + effectTarget.getPosition().getY() + " " + effectTarget.getPosition().getZ());

        playerSource.getServer().commandManager.executeCommand(new CommandSenderWrapper(playerSource), actualCommand);
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    public static class CommandSenderWrapper implements ICommandSender {

        private final ICommandSender actualSender;

        public CommandSenderWrapper(ICommandSender commandSender) {
            this.actualSender = commandSender;
        }

        @Override
        public boolean sendCommandFeedback() {
            return false; //main reason this class exists
        }

        //Pass-Through:
        @Override public String getName() {return actualSender.getName();}
        @Override public ITextComponent getDisplayName() {return actualSender.getDisplayName();}
        @Override public void sendMessage(ITextComponent component) {actualSender.sendMessage(component);}
        @Override public boolean canUseCommand(int permLevel, String commandName) {return actualSender.canUseCommand(permLevel, commandName);}
        @Override public BlockPos getPosition() {return actualSender.getPosition();}
        @Override public Vec3d getPositionVector() {return actualSender.getPositionVector();}
        @Override public World getEntityWorld() {return actualSender.getEntityWorld();}
        @Override public Entity getCommandSenderEntity() {return actualSender.getCommandSenderEntity();}
        @Override public void setCommandStat(CommandResultStats.Type type, int amount) {actualSender.setCommandStat(type, amount);}
        @Override public MinecraftServer getServer() {return actualSender.getServer();}
    }
}
