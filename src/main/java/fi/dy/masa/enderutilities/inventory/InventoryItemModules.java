package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModule;

public class InventoryItemModules extends InventoryItem
{
    protected InventoryItemModular inventoryItemModular;

    public InventoryItemModules(InventoryItemModular modularInventory, ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        super(containerStack, invSize, isRemote, player);
        this.inventoryItemModular = modularInventory;
        this.stackLimit = 1;
    }

    /*@Override
    public ItemStack getContainerItemStack()
    {
        return this.inventoryItemModular.getModularItemStack();
    }*/

    /*@Override
    public void writeToContainerItemStack()
    {
        super.writeToContainerItemStack();

        if (this.isRemote == false)
        {
            this.inventoryItemModular.readFromContainerItemStack();
        }
    }*/

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (this.inventoryItemModular.getContainerItemStack() == null)
        {
            return false;
        }

        return stack == null || stack.getItem() instanceof IModule;
    }
}
