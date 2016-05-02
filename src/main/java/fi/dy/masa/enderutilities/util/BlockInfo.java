package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockInfo
{
    public Block block;
    public ResourceLocation resource;
    public int blockMeta;
    public int itemMeta;

    public BlockInfo(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        int blockMeta = block.getMetaFromState(state);
        int itemMeta = 0;

        @SuppressWarnings("deprecation")
        ItemStack stack = block.getItem(world, pos, state);
        if (stack != null)
        {
            itemMeta = stack.getMetadata();
        }

        this.block = block;
        this.resource = ForgeRegistries.BLOCKS.getKey(block);
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
    }

    public BlockInfo(Block block, int blockMeta, int itemMeta)
    {
        this.block = block;
        this.resource = ForgeRegistries.BLOCKS.getKey(block);
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
    }

    public BlockInfo(ResourceLocation resource, int blockMeta, int itemMeta)
    {
        this.block = ForgeRegistries.BLOCKS.getValue(resource);
        this.resource = resource;
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
    }

    @Override
    public String toString()
    {
        return String.format("BlockInfo: {block rl: %s, blockMeta: %d, itemMeta: %d}", this.resource, this.blockMeta, this.itemMeta);
    }
}
