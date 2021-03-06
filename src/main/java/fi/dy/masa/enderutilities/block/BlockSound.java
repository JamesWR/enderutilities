package fi.dy.masa.enderutilities.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntitySoundBlock;

public class BlockSound extends BlockEnderUtilitiesTileEntity
{
    public BlockSound(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState());
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntitySoundBlock();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // Don't try to set the facing as the elevator doesn't have one, which is what the super would do
        return state;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (world.isRemote == false)
        {
            TileEntitySoundBlock te = getTileEntitySafely(world, pos, TileEntitySoundBlock.class);

            if (te != null)
            {
                // Stop a playing sound when the block is broken
                te.sendPlaySoundPacket(true);
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation)
    {
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror)
    {
        return state;
    }
}
