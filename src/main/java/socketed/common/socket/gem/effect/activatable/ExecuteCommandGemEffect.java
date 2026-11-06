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
import socketed.Socketed;
import socketed.api.socket.gem.effect.activatable.ActivatableGemEffect;
import socketed.api.socket.gem.effect.activatable.activator.GenericActivator;
import socketed.api.socket.gem.effect.activatable.callback.IEffectCallback;
import socketed.api.socket.gem.effect.activatable.target.GenericTarget;
import socketed.api.socket.gem.effect.slot.ISlotType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public class ExecuteCommandGemEffect extends ActivatableGemEffect {
    public static final String TYPE_NAME = "Execute Command";

    private static final Pattern UNSAFE_NAME_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}_-]");

    @SerializedName("Command")
    private final String command;

    protected ExecuteCommandGemEffect(ISlotType slotType, GenericActivator activator, List<GenericTarget> targets, String tooltipKey, String command) {
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
                .replace("%playerName%", sanitizeName(playerSource.getName()))
                .replace("%playerUUID%", playerSource.getUniqueID().toString())
                .replace("%playerX%", ""+playerSource.getPosition().getX())
                .replace("%playerY%", ""+playerSource.getPosition().getY())
                .replace("%playerZ%", ""+playerSource.getPosition().getZ())
                .replace("%playerPos%", playerSource.getPosition().getX() + " " + playerSource.getPosition().getY() + " " + playerSource.getPosition().getZ())
                .replace("%targetName%", sanitizeName(effectTarget.getName()))
                .replace("%targetUUID%", effectTarget.getUniqueID().toString())
                .replace("%targetX%", ""+effectTarget.getPosition().getX())
                .replace("%targetY%", ""+effectTarget.getPosition().getY())
                .replace("%targetZ%", ""+effectTarget.getPosition().getZ())
                .replace("%targetPos%", effectTarget.getPosition().getX() + " " + effectTarget.getPosition().getY() + " " + effectTarget.getPosition().getZ());

        playerSource.getServer().commandManager.executeCommand(new CommandSenderWrapper(playerSource), actualCommand);
    }

    /**
     * Commands run with server permissions and entity names can be set by players, such as with name tags
     * Only keeps characters that can't add arguments, selectors, or nbt to a command
     */
    private static String sanitizeName(String name) {
        return UNSAFE_NAME_CHARACTERS.matcher(name).replaceAll("");
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Command: Required
     */
    @Override
    public boolean validate() {
        if(super.validate()) {
            if(this.command == null) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, command must be defined");
            else if(this.command.isEmpty()) Socketed.LOGGER.warn("Invalid " + this.getTypeName() + " Effect, command can't be empty");
            else return true;
        }
        return false;
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
        @Override public void sendMessage(ITextComponent component) {}
        @Override public boolean canUseCommand(int permLevel, String commandName) {return true;}
        @Override public BlockPos getPosition() {return actualSender.getPosition();}
        @Override public Vec3d getPositionVector() {return actualSender.getPositionVector();}
        @Override public World getEntityWorld() {return actualSender.getEntityWorld();}
        @Override public Entity getCommandSenderEntity() {return actualSender.getCommandSenderEntity();}
        @Override public void setCommandStat(CommandResultStats.Type type, int amount) {actualSender.setCommandStat(type, amount);}
        @Override public MinecraftServer getServer() {return actualSender.getServer();}
    }
}
