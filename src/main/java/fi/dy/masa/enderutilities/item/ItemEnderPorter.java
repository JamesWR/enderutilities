package fi.dy.masa.enderutilities.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
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

    public ItemEnderPorter(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.commonTooltip = "enderutilities.tooltips.itemlocationboundmodular";
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        if (this.isAdvancedPorter(stack))
        {
            return super.getTranslationKey() + "_advanced";
        }

        return super.getTranslationKey();
    }

    private boolean isAdvancedPorter(ItemStack stack)
    {
        // damage 1: Advanced Ender Porter
        return stack.getMetadata() == 1;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (player == null || player.getEntityWorld().isRemote || player.isSneaking() == false ||
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return false;
        }

        return this.doTeleport(stack, entity, true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
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
        int playerDim = player.getEntityWorld().provider.getDimension();

        // The basic version can only teleport inside the same dimension
        if (target != null && EntityUtils.doesEntityStackHaveBlacklistedEntities(player) == false
            && (this.isAdvancedPorter(stack) || target.dimension == playerDim))
        {
            int cost = (target.dimension == playerDim ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);

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
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, livingBase) == false)
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
            this.doTeleport(stack, player, false);
        }
    }

    private boolean doTeleport(ItemStack stack, Entity entity, boolean targetMustSneakIfPlayer)
    {
        TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        int entityDim = entity.getEntityWorld().provider.getDimension();

        // The basic version can only teleport inside the same dimension
        if (target != null && EntityUtils.doesEntityStackHaveBlacklistedEntities(entity) == false &&
            (this.isAdvancedPorter(stack) || target.dimension == entityDim))
        {
            int cost = (target.dimension == entityDim ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);

            if (UtilItemModular.useEnderCharge(stack, cost, true))
            {
                // If the target entity is a player, then they have to be sneaking too
                if (targetMustSneakIfPlayer == false || ((entity instanceof EntityPlayer) == false || entity.isSneaking()))
                {
                    UtilItemModular.useEnderCharge(stack, cost, false);
                    TeleportEntity.teleportEntityUsingModularItem(entity, stack, true, true);
                    return true;
                }
            }
        }

        return false;
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

    @Override
    public void getSubItemsCustom(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

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
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "usetime"), new IItemPropertyGetter()
        {
            @Override
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0.0F;
                }
                else
                {
                    ItemStack stackActive = entityIn.getActiveItemStack();
                    return stackActive.isEmpty() == false && stackActive.getItem() == EnderUtilitiesItems.ENDER_PORTER ?
                            (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 60.0F : 0.0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "inuse"), new IItemPropertyGetter()
        {
            @Override
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }
}
