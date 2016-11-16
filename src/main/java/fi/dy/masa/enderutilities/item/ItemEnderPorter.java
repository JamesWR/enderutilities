package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class ItemEnderPorter extends ItemLocationBoundModular
{
    public static final int ENDER_CHARGE_COST_INTER_DIM_TP = 5000;
    public static final int ENDER_CHARGE_COST_CROSS_DIM_TP = 25000;
    private static final int USE_TIME = 40;

    public ItemEnderPorter()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_PORTER);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // damage 1: Ender Porter (Advanced)
        if (stack.getMetadata() == 1)
        {
            return super.getUnlocalizedName() + "_advanced";
        }

        return super.getUnlocalizedName();
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (player == null || player.getEntityWorld().isRemote || player.isSneaking() == false
            || OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return false;
        }

        TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        // The basic version can only teleport inside the same dimension
        if (target != null && EntityUtils.doesEntityStackHaveBlacklistedEntities(entity) == false
            && (stack.getMetadata() == 1 || target.dimension == entity.dimension))
        {
            int cost = (target.dimension == entity.dimension ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);
            if (UtilItemModular.useEnderCharge(stack, cost, true) == false)
            {
                return false;
            }

            // If the target entity is a player, then they have to be sneaking too
            if ((entity instanceof EntityPlayer) == false || ((EntityPlayer)entity).isSneaking())
            {
                UtilItemModular.useEnderCharge(stack, cost, false);
                TeleportEntity.teleportEntityUsingModularItem(entity, stack, true, true);
                return true;
            }
        }

        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        // This needs to also happen on the client, otherwise the in-use will derp up

        if (player == null || OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // Don't activate when sneaking and looking at a block, aka. binding to a new location
        if (player.isSneaking())
        {
            RayTraceResult rayTraceResult = this.rayTrace(world, player, true);
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }
        }

        TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        // The basic version can only teleport inside the same dimension
        if (target != null && EntityUtils.doesEntityStackHaveBlacklistedEntities(player) == false
            && (stack.getMetadata() == 1 || target.dimension == player.dimension))
        {
            int cost = (target.dimension == player.dimension ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);
            if (UtilItemModular.useEnderCharge(stack, cost, true) == false)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }

            player.setActiveHand(hand);

            if (world.isRemote == false)
            {
                Effects.playSoundEffectServer(world, player.posX, player.posY, player.posZ,
                    SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.MASTER, 0.08f, 1.2f);
            }

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase livingBase, int itemInUseCount)
    {
        if ((livingBase instanceof EntityPlayer) == false ||
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, (EntityPlayer) livingBase) == false)
        {
            return;
        }

        EntityPlayer player = (EntityPlayer) livingBase;
        int useTime = USE_TIME;
        // Use a shorter delay in creative mode
        if (player.capabilities.isCreativeMode)
        {
            useTime >>= 2;
        }

        if ((this.getMaxItemUseDuration(stack) - itemInUseCount) >= useTime)
        {
            TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            if (target == null || (stack.getMetadata() == 0 && target.dimension != player.dimension))
            {
                return;
            }

            int cost = (target.dimension == player.dimension ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);
            if (UtilItemModular.useEnderCharge(stack, cost, false) == false)
            {
                return;
            }

            TeleportEntity.teleportEntityUsingModularItem(player, stack, true, true);
        }
    }

    @Override
    public boolean useBindLocking(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 10;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 9;
        }

        return 0;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        return new ResourceLocation[] {
                new ResourceLocation(this.getRegistryName() + "_basic"),
                new ResourceLocation(this.getRegistryName() + "_advanced")
        };
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation("underutilities:usetime"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0.0F;
                }
                else
                {
                    ItemStack itemstack = entityIn.getActiveItemStack();
                    return itemstack != null && itemstack.getItem() == ItemEnderPorter.this ? (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 60.0F : 0.0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation("underutilities:inuse"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }
}
