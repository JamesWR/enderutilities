package fi.dy.masa.enderutilities.block;

import java.util.Arrays;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityElevator;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockElevator extends BlockEnderUtilitiesTileEntity
{
    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color", EnumDyeColor.class);

    public BlockElevator(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { COLOR }, new IUnlistedProperty<?>[] { CAMOBLOCKSTATE, CAMOBLOCKSTATEEXTENDED });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        String[] names = new String[EnumDyeColor.values().length];
        Arrays.fill(names, this.blockName);
        return names;
    }

    @Override
    protected String[] generateTooltipNames()
    {
        // Use a common tooltip by adding exactly one entry into the array
        return new String[] { "ender_elevator" };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityElevator();
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new ItemBlockEnderUtilities(this)
        {
            @Override
            public String getItemStackDisplayName(ItemStack stack)
            {
                String name = super.getItemStackDisplayName(stack);
                return name.replace("{COLOR}", EnumDyeColor.byMetadata(stack.getMetadata()).getName());
            }
        };
    }

    @Override
    protected boolean isCamoBlock()
    {
        return true;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(COLOR).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(COLOR).getMetadata();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // Don't try to set the facing as the elevator doesn't have one, which is what the super would do
        return state;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(player, ItemDye.class);

        if (stack.isEmpty() == false)
        {
            EnumDyeColor stackColor = EnumDyeColor.byDyeDamage(stack.getMetadata());

            if (state.getValue(COLOR) != stackColor)
            {
                if (world.isRemote == false)
                {
                    world.setBlockState(pos, state.withProperty(COLOR, stackColor), 3);
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);

                    if (player.capabilities.isCreativeMode == false)
                    {
                        stack.shrink(1);
                    }
                }

                return true;
            }

            return false;
        }
        else
        {
            return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        }
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

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        return false;
    }
}
