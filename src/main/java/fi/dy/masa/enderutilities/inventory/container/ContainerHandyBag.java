package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacks;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerArmor;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerCraftResult;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotModuleModularItem;
import fi.dy.masa.enderutilities.inventory.wrapper.InventoryCraftingEnderUtilities;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerCraftResult;
import fi.dy.masa.enderutilities.item.ItemHandyBag.ShiftMode;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ContainerHandyBag extends ContainerLargeStacks implements IContainerItem
{
    public static final EntityEquipmentSlot[] EQUIPMENT_SLOT_TYPES = new EntityEquipmentSlot[]
    {
        EntityEquipmentSlot.HEAD,
        EntityEquipmentSlot.CHEST,
        EntityEquipmentSlot.LEGS,
        EntityEquipmentSlot.FEET
    };
    public final InventoryItemModular inventoryItemModular;
    private final InventoryCraftingEnderUtilities craftMatrix;
    private final IItemHandler craftMatrixWrapper;
    private final ItemHandlerCraftResult craftResult = new ItemHandlerCraftResult();
    private ItemStack modularStackLast = ItemStack.EMPTY;
    private int craftingSlot = 0;

    public ContainerHandyBag(EntityPlayer player, ItemStack containerStack)
    {
        super(player, new InventoryItemModular(containerStack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        this.inventoryItemModular = (InventoryItemModular) this.inventory;
        this.inventoryItemModular.setHostInventory(this.playerInv);
        this.craftMatrix = new InventoryCraftingEnderUtilities(2, 2, new ItemStackHandlerBasic(4), this.craftResult, player, this);
        this.craftMatrixWrapper = new InvWrapper(this.craftMatrix);
        this.inventoryNonWrapped = (InventoryItemModular) this.inventory;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 174);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        if (this.getBagTier() == 1)
        {
            posX += 40;
        }

        super.addPlayerInventorySlots(posX, posY);

        int playerArmorStart = this.inventorySlots.size();

        // Player armor slots
        posY = 15;

        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerArmor(this, this.playerInv, i, 39 - i, posX, posY + i * 18));
        }

        this.playerArmorSlots = new MergeSlotRange(playerArmorStart, 4);

        this.addOffhandSlot(posX + 4 * 18, 51);

        // Player crafting slots
        posX += 90;
        posY = 15;
        this.craftingSlot = this.inventorySlots.size();
        this.addSlotToContainer(new SlotItemHandlerCraftResult(this.craftMatrix, this.craftResult, 0, posX + 54, posY + 10, this.player));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.craftMatrixWrapper, j + i * 2, posX + j * 18, posY + i * 18));
            }
        }
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int xOff = 8;
        int yOff = 102;

        if (this.getBagTier() == 1)
        {
            xOff += 40;
        }

        // The top/middle section of the bag inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, xOff + j * 18, yOff + i * 18));
            }
        }

        // Large Handy Bag
        if (this.getBagTier() == 1)
        {
            int xOffXtra = 8;
            yOff = 102;

            // Left side extra slots
            for(int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 27 + i * 2, xOffXtra +  0, yOff + i * 18));
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 28 + i * 2, xOffXtra + 18, yOff + i * 18));
            }

            xOffXtra = 214;

            // Right side extra slots
            for(int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 41 + i * 2, xOffXtra +  0, yOff + i * 18));
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 42 + i * 2, xOffXtra + 18, yOff + i * 18));
            }
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        xOff += 90;
        yOff = 69;
        int moduleSlots = this.inventoryItemModular.getModuleInventory().getSlots();
        // Add the Memory Card slots as a priority merge slot range
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), moduleSlots);

        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItemModular.getModuleInventory(), i, xOff + i * 18, yOff, ModuleType.TYPE_MEMORY_CARD_ITEMS, this));
        }
    }

    @Override
    public ItemStack getContainerItem()
    {
        return this.inventoryItemModular.getModularItemStack();
    }

    public void dropCraftingGridContents()
    {
        for (int i = 0; i < 4; ++i)
        {
            ItemStack stack = this.craftMatrix.removeStackFromSlot(i);

            if (stack.isEmpty() == false)
            {
                this.player.dropItem(stack, true);
            }
        }

        this.craftResult.setStackInSlot(0, ItemStack.EMPTY);
    }

    public int getBagTier()
    {
        if (this.inventoryItemModular.getModularItemStack().isEmpty() == false)
        {
            return this.inventoryItemModular.getModularItemStack().getMetadata() == 1 ? 1 : 0;
        }

        return 0;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        // Drop the items in the crafting grid
        this.dropCraftingGridContents();
    }

    @Override
    protected void shiftClickSlot(int slotNum, EntityPlayer player)
    {
        if (slotNum != this.craftingSlot)
        {
            super.shiftClickSlot(slotNum, player);
            return;
        }

        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stackOrig = slot.getStack().copy();
            int num = 64;

            while (num-- > 0)
            {
                // Could not transfer the items, or ran out of some of the items, so the crafting result changed, bail out now
                if (this.transferStackFromSlot(player, slotNum) == false ||
                    InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
                {
                    break;
                }
            }
        }
    }

    @Override
    protected void rightClickSlot(int slotNum, EntityPlayer player)
    {
        // Not a crafting output slot
        if (slotNum != this.craftingSlot)
        {
            super.rightClickSlot(slotNum, player);
            return;
        }

        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stackOrig = slot.getStack().copy();
            int num = stackOrig.getMaxStackSize() / stackOrig.getCount();

            while (num-- > 0)
            {
                super.rightClickSlot(slotNum, player);

                // Ran out of some of the ingredients, so the crafting result changed, stop here
                if (InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
                {
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        super.slotClick(slotNum, dragType, clickType, player);

        if (this.isClient == false && slotNum == this.craftingSlot)
        {
            this.syncSlotToClient(this.craftingSlot);
            this.syncCursorStackToClient();
        }

        return ItemStack.EMPTY;
    }

    @Override
    protected boolean transferStackFromPlayerMainInventory(EntityPlayer player, int slotNum)
    {
        ItemStack modularStack = this.inventoryItemModular.getModularItemStack();

        if (modularStack.isEmpty() == false && ShiftMode.getEffectiveMode(modularStack) == ShiftMode.INV_HOTBAR)
        {
            if (this.playerHotbarSlots.contains(slotNum))
            {
                return this.transferStackToSlotRange(player, slotNum, this.playerMainSlots, false);
            }
            else if (this.playerMainSlots.contains(slotNum))
            {
                return this.transferStackToSlotRange(player, slotNum, this.playerHotbarSlots, false);
            }
        }

        return super.transferStackFromPlayerMainInventory(player, slotNum);
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.player.getEntityWorld().isRemote == false)
        {
            ItemStack modularStack = this.inventoryItemModular.getModularItemStack();

            // The Bag's stack has changed (ie. to/from empty, or different instance), re-read the inventory contents.
            if (modularStack != this.modularStackLast)
            {
                this.inventoryItemModular.readFromContainerItemStack();
                this.modularStackLast = modularStack;
            }
        }

        super.detectAndSendChanges();
    }
}
