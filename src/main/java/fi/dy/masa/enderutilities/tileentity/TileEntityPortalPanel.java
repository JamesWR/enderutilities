package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiPortalPanel;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerPortalPanel;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class TileEntityPortalPanel extends TileEntityEnderUtilitiesInventory
{
    private final ItemHandlerWrapper inventoryWrapper;
    private byte activeTarget;
    private boolean active;

    public TileEntityPortalPanel()
    {
        super(ReferenceNames.NAME_TILE_PORTAL_PANEL);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 16, 1, false, "Items", this);
        this.inventoryWrapper = new ItemHandlerWrapper(this.itemHandlerBase);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerBase, this.inventoryWrapper);
    }

    public void setActiveTarget(int target)
    {
        this.activeTarget = (byte)MathHelper.clamp_int(target, 0, 7);
    }

    public void toggleActive()
    {
        this.active = this.tryActivatePortal();
    }

    public int getColor(int target)
    {
        if (target >= 0 && target < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(target + 8);

            if (stack != null && stack.getItem() == Items.DYE)
            {
                return EnumDyeColor.byDyeDamage(stack.getMetadata()).getMapColor().colorValue;
            }
        }

        return 0xFFFFFF;
    }

    public String getActiveName()
    {
        if (this.activeTarget >= 0 && this.activeTarget < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(this.activeTarget + 8);

            if (stack != null && stack.hasDisplayName())
            {
                return stack.getDisplayName();
            }
        }

        return "FOO-BAR";
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.activeTarget = nbt.getByte("ActiveTarget");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("ActiveTarget", this.activeTarget);
        nbt.setBoolean("Active", this.active);
    }

    private boolean tryActivatePortal()
    {
        return ! this.active;
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        super.inventoryChanged(inventoryId, slot);

        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
    }

    private class ItemHandlerWrapper extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapper(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return false;
            }

            if (slot < 8)
            {
                return stack.getItem() == EnderUtilitiesItems.linkCrystal &&
                        ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
            }

            return stack.getItem() == Items.DYE;
        }
    }

    @Override
    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return new ContainerPortalPanel(player, this);
    }

    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiPortalPanel(this.getContainer(player), this);
    }
}
