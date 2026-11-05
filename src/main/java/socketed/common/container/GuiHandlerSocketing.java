package socketed.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import socketed.client.gui.GuiSocketing;

import javax.annotation.Nullable;

public class GuiHandlerSocketing implements IGuiHandler {
    
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerSocketing(player.inventory, world, new BlockPos(x, y, z));
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GuiSocketing(player.inventory, world, new BlockPos(x, y, z));
    }
}