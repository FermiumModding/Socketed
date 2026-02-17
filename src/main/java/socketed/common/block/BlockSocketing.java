package socketed.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import socketed.Socketed;

public class BlockSocketing extends Block {
    public BlockSocketing(String name) {
        super(Material.WOOD);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setRegistryName(Socketed.MODID, name);
        this.setTranslationKey(Socketed.MODID + "." + name);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        //if (world.isRemote) return true;

        playerIn.openGui(Socketed.instance,0,world,pos.getX(),pos.getY(), pos.getZ());
        return true;
    }
}
