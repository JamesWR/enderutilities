package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemIceMelter extends ItemEnderUtilities
{
    public ItemIceMelter()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ICE_MELTER);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName();
        return stack.getMetadata() == 1 ? name + "_super" : name + "_basic";
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.getBlockState(pos).getBlock() == Blocks.ICE)
        {
            if (worldIn.isRemote == false)
            {
                int num = 8;
                float velocity = 0.2f;
                // The Super variant (meta = 1) doesn't cause a block update
                int flag = stack.getMetadata() == 1 ? 2 : 3;

                if (worldIn.provider.doesWaterVaporize() == false)
                {
                    worldIn.setBlockState(pos, Blocks.WATER.getDefaultState(), flag);
                }
                else
                {
                    worldIn.setBlockToAir(pos);
                    num = 32;
                    velocity = 0.4f;
                }

                Effects.spawnParticlesFromServer(worldIn.provider.getDimension(), pos.up(), EnumParticleTypes.SMOKE_LARGE, num, 0.5f, velocity);
                worldIn.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.MASTER, 0.8f, 1.0f);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Regular version
        list.add(new ItemStack(this, 1, 1)); // "Super" version
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ModelResourceLocation[] {
                new ModelResourceLocation(rl, "type=basic"),
                new ModelResourceLocation(rl, "type=super")
        };
    }
}