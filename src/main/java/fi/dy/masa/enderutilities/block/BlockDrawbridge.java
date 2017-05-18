package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityDrawbridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockDrawbridge extends BlockEnderUtilitiesInventory
{
    public static final PropertyBool ADVANCED = PropertyBool.create("advanced");

    public BlockDrawbridge(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.propFacing = FACING;
        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(ADVANCED, false)
                .withProperty(FACING, DEFAULT_FACING));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { ADVANCED, FACING }, new IUnlistedProperty<?>[] { CAMOBLOCK });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_DRAWBRIDGE,
                ReferenceNames.NAME_TILE_DRAWBRIDGE + "_advanced"
        };
    }

    @Override
    protected String[] generateTooltipNames()
    {
        // Use a common tooltip by adding exactly one entry into the array
        return new String[] { this.blockName };
    }

    @Override
    public ItemBlock createItemBlock()
    {
        ItemBlockEnderUtilities item = new ItemBlockEnderUtilities(this);
        item.setHasPlacementProperties(true);
        item.addPlacementProperty("drawbridge.delay", Constants.NBT.TAG_INT, 1, 72000);
        item.addPlacementProperty("drawbridge.length", Constants.NBT.TAG_BYTE, 1, 64);
        return item;
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        TileEntityDrawbridge te = new TileEntityDrawbridge();
        te.setIsAdvanced(state.getValue(ADVANCED));
        return te;
    }

    @Override
    protected EnumFacing getPlacementFacing(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        return BlockPistonBase.getFacingFromEntity(pos, placer);
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state) & 0x1;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.getFront(meta >> 1))
                .withProperty(ADVANCED, (meta & 0x1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int meta = state.getValue(FACING).getIndex() << 1;
        return state.getValue(ADVANCED) ? meta | 0x1 : meta;
    }

    @Override
    public IBlockState getExtendedState(IBlockState oldState, IBlockAccess world, BlockPos pos)
    {
        TileEntityDrawbridge te = getTileEntitySafely(world, pos, TileEntityDrawbridge.class);

        if (te != null)
        {
            IExtendedBlockState state = (IExtendedBlockState) oldState;
            return state.withProperty(CAMOBLOCK, te.getCamoState());
        }

        return oldState;
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random)
    {
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if (world.isRemote == false)
        {
            TileEntityDrawbridge te = getTileEntitySafely(world, pos, TileEntityDrawbridge.class);

            if (te != null)
            {
                te.onScheduledBlockUpdate(world, pos, state, rand);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntityDrawbridge te = getTileEntitySafely(world, pos, TileEntityDrawbridge.class);

        if (te != null && this.isTileEntityValid(te))
        {
            if (te.onRightClickBlock(player, hand, side, hitX, hitY, hitZ))
            {
                return true;
            }
            else
            {
                if (world.isRemote == false)
                {
                    player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, world, pos.getX(), pos.getY(), pos.getZ());
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }
}
