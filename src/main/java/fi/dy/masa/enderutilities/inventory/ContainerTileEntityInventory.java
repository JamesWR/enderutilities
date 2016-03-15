package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerTileEntityInventory extends ContainerEnderUtilities
{
    protected TileEntityEnderUtilitiesInventory te;

    public ContainerTileEntityInventory(EntityPlayer player, TileEntityEnderUtilitiesInventory te)
    {
        super(player, te.getWrappedInventoryForContainer());
        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return super.canInteractWith(player) && this.te.isInvalid() == false;
    }
}
