package fi.dy.masa.enderutilities.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.block.base.BlockProperties;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class BlockEnergyBridge extends BlockEnderUtilitiesTileEntity
{
    public static final PropertyDirection FACING = BlockProperties.FACING;

    public static final PropertyEnum<BlockEnergyBridge.EnumMachineType> TYPE =
            PropertyEnum.<BlockEnergyBridge.EnumMachineType>create("type", BlockEnergyBridge.EnumMachineType.class);

    public BlockEnergyBridge(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(TYPE, BlockEnergyBridge.EnumMachineType.RESONATOR)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { TYPE, FACING });
    }

    @Override
    protected String[] getUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER,
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER,
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR
        };
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        return new TileEntityEnergyBridge();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).tryAssembleMultiBlock(worldIn, pos);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    // FIXME
    /*@Override
    public void onBlockPreDestroy(World world, BlockPos pos, int oldMeta)
    {
        super.onBlockPreDestroy(world, pos, oldMeta);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).disassembleMultiblock(world, pos, oldMeta);
            }
        }
    }*/

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, EnumMachineType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderUtilities)
        {
            state = state.withProperty(FACING, EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation()));
        }

        return state;
    }

    @Override
    public int getLightValue(IBlockAccess worldIn, BlockPos pos)
    {
        return 15;
    }

    @SideOnly(Side.CLIENT)
    public static void getSubBlocks(int blockIndex, Block block, Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < 3; meta++)
        {
            list.add(new ItemStack(block, 1, meta));
        }
    }

    public static enum EnumMachineType implements IStringSerializable
    {
        RESONATOR (ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR),
        RECEIVER (ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER),
        TRANSMITTER (ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER);

        private final String name;

        private EnumMachineType(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return this.name;
        }

        public String getName()
        {
            return this.name;
        }

        public int getMeta()
        {
            return this.ordinal();
        }

        public static EnumMachineType fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : RESONATOR;
        }
    }
}
