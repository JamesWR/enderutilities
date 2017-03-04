package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;

public class ContainerLargeStacks extends ContainerCustomSlotClick
{
    public ContainerLargeStacks(EntityPlayer player, IItemHandler inventory)
    {
        super(player, inventory);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our inventory
        if (slot instanceof SlotItemHandler && ((SlotItemHandler)slot).getItemHandler() == this.inventory)
        {
            return slot.getItemStackLimit(stack);
        }

        // Player inventory
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        if (this.listeners.contains(listener))
        {
            throw new IllegalArgumentException("Listener already listening");
        }
        else
        {
            this.listeners.add(listener);

            if (listener instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP) listener;
                player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
                this.syncAllSlots(player);
            }

            this.detectAndSendChanges();
        }
    }

    protected void syncAllSlots(EntityPlayerMP player)
    {
        for (int slot = 0; slot < this.inventorySlots.size(); slot++)
        {
            ItemStack stack = this.inventorySlots.get(slot).getStack();
            this.inventoryItemStacks.set(slot, ItemStack.copyItemStack(stack));
            PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, slot, stack), player);
        }
    }
}
