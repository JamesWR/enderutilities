package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiInventorySwapper extends GuiEnderUtilities
{
    public static final int BTN_ID_FIRST_SELECT_MODULE  = 0;
    public static final int BTN_ID_FIRST_SELECT_PRESET  = 4;
    public static final int BTN_ID_FIRST_TOGGLE_ROWS    = 8;
    public static final int BTN_ID_FIRST_TOGGLE_COLUMNS = 12;

    public static final int selSlotHilightU = 204;
    public static final int selSlotHilightV = 18;
    public static final int disabledSlotU = 204;
    public static final int disabledSlotV = 0;
    public static final int toggleButtonU = 204;
    public static final int toggleButtonRowV = 52;
    public static final int toggleButtonColDownV = 68;
    public static final int toggleButtonColUpV = 84;
    public static final int selectionButtonU = 222;
    public static final int selectionButtonVMC = 0;
    public static final int selectionButtonVPreset = 8;
    public static final int selectionButtonHilightU = 246;
    public static final int selectionButtonHilightV = 10;
    public ContainerInventorySwapper container;
    public InventoryItemModular inventory;
    public EntityPlayer player;
    public int invSize;
    public int numModuleSlots;
    public int firstModuleSlotX;
    public int firstModuleSlotY;
    public int firstInvSlotX;
    public int firstInvSlotY;
    public int firstArmorSlotX;
    public int firstArmorSlotY;

    public GuiInventorySwapper(ContainerInventorySwapper container)
    {
        super(container, 204, 245, "gui.container.inventoryswapper");
        this.player = container.player;
        this.container = container;
        this.inventory = container.inventoryItemModular;
        this.invSize = this.inventory.getSizeInventory();
        this.numModuleSlots = this.inventory.getModuleInventory().getSizeInventory();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.firstModuleSlotX  = this.guiLeft + this.container.getSlot(40).xDisplayPosition;
        this.firstModuleSlotY  = this.guiTop  + this.container.getSlot(40).yDisplayPosition;
        this.firstInvSlotX   = this.guiLeft + this.container.getSlot(44).xDisplayPosition;
        this.firstInvSlotY   = this.guiTop  + this.container.getSlot(44).yDisplayPosition;
        this.firstArmorSlotX   = this.guiLeft + this.container.getSlot(44 + 36).xDisplayPosition;
        this.firstArmorSlotY   = this.guiTop  + this.container.getSlot(44 + 36).yDisplayPosition;

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper", new Object[0]), 12, 6, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper.storagemodules", new Object[0]), 125, 6, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper.presets", new Object[0]) + ":", 60, 135, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTexture);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.inventory.isUseableByPlayer(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.container.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, disabledSlotU, disabledSlotV, 18, 18);
            }
        }

        // Memory Card slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventory.getModuleInventory().isUseableByPlayer(this.player) == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX - 1 + i * 18, this.firstModuleSlotY - 1, disabledSlotU, disabledSlotV, 18, 18);
            }
        }

        // Draw the colored background for the selected module slot
        int index = this.inventory.getSelectedModuleIndex();
        if (index >= 0)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX - 1 + index * 18, this.firstModuleSlotY - 1, selSlotHilightU, selSlotHilightV, 18, 18);
            // Draw the selection border around the selected memory card module's selection button
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + index * 18, this.firstModuleSlotY + 18, selectionButtonHilightU, selectionButtonHilightV, 10, 10);
        }

        ItemStack stack = this.container.getModularItem();
        if (stack != null)
        {
            // Draw the selection border around the selected preset's button
            byte sel = NBTUtils.getByte(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(this.firstInvSlotX + 93 + sel * 18, this.firstInvSlotY - 29, selectionButtonHilightU, selectionButtonHilightV, 10, 10);

            // Draw the colored background for the selected/enabled inventory slots
            final long mask = ItemInventorySwapper.getEnabledSlotsMask(stack);
            long bit = 0x1;
            // Hotbar
            for (int c = 0; c < 9; c++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + 58, selSlotHilightU, selSlotHilightV, 18, 18);
                }
                bit <<= 1;
            }

            // Inventory
            for (int r = 0; r < 3; r++)
            {
                for (int c = 0; c < 9; c++)
                {
                    if ((mask & bit) != 0)
                    {
                        this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + r * 18, selSlotHilightU, selSlotHilightV, 18, 18);
                    }
                    bit <<= 1;
                }
            }

            // Armor slots
            for (int r = 0; r < 4; r++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstArmorSlotX - 1, this.firstArmorSlotY - 1 + (3 - r) * 18, selSlotHilightU, selSlotHilightV, 18, 18);
                }
                bit <<= 1;
            }
        }

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        this.bindTexture(TextureMap.locationItemsTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        IIcon icon = EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
        for (int i = 0; icon != null && i < this.numModuleSlots; i++)
        {
            if (this.inventory.getModuleInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModelRectFromIcon(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, icon, 16, 16);
            }
        }

        // Draw the background icon for empty player armor slots
        for (int i = 0; i < 4; i++)
        {
            if (this.player.inventory.getStackInSlot(39 - i) == null)
            {
                icon = ItemArmor.func_94602_b(i);
                this.drawTexturedModelRectFromIcon(this.firstArmorSlotX, this.firstArmorSlotY + i * 18, icon, 16, 16);
            }
        }

        // Draw the background icon for empty armor slots in the swapper's inventory
        for (int i = 0; i < 4; i++)
        {
            if (this.inventory.getStackInSlot(39 - i) == null)
            {
                //System.out.println("plop");
                icon = ItemArmor.func_94602_b(i);
                this.drawTexturedModelRectFromIcon(this.firstArmorSlotX + 23 + i * 18, this.firstArmorSlotY - 20, icon, 16, 16);
            }
        }

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8*/
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Hovering over the info icon
        if (mouseX >= x + 6 && mouseX <= x + 23 && mouseY >= y + 20 && mouseY <= y + 37)
        {
            List<String> list = new ArrayList<String>();
            ItemEnderUtilities.addTooltips("enderutilities.gui.label.inventoryswapper.info", list, false);
            //list.add(I18n.format("enderutilities.gui.label.inventoryswapper.info", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 4 + i * 18, this.firstModuleSlotY + 19, 8, 8, selectionButtonU, selectionButtonVMC, this.guiTexture, 8, 0));
        }

        // Add the preset selection buttons
        for (int i = 0; i < ItemInventorySwapper.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_PRESET + i, this.firstInvSlotX + 94 + i * 18, this.firstInvSlotY - 28, 8, 8, selectionButtonU, selectionButtonVPreset + i * 8, this.guiTexture, 8, 0));
        }

        // Add the Toggle Row buttons
        // Hotbar is first
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS, this.firstInvSlotX - 18, this.firstInvSlotY + 58, 16, 16, toggleButtonU, toggleButtonRowV, this.guiTexture, 16, 0));

        for (int i = 0; i < 3; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS + i + 1, this.firstInvSlotX - 18, this.firstInvSlotY + i * 18, 16, 16, toggleButtonU, toggleButtonRowV, this.guiTexture, 16, 0));
        }

        // Add the Toggle Column buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + i, this.firstInvSlotX + i * 18, this.firstInvSlotY - 18, 16, 16, toggleButtonU, toggleButtonColDownV, this.guiTexture, 16, 0));
        }

        // Toggle button for armor
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + 9, this.firstArmorSlotX, this.firstArmorSlotY + 72, 16, 16, toggleButtonU, toggleButtonColUpV, this.guiTexture, 16, 0));
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_MODULE && button.id < (BTN_ID_FIRST_SELECT_MODULE + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_SELECT_MODULE, button.id - BTN_ID_FIRST_SELECT_MODULE));
        }
        else if (button.id >= BTN_ID_FIRST_SELECT_PRESET && button.id < (BTN_ID_FIRST_SELECT_PRESET + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_CHANGE_PRESET, button.id - BTN_ID_FIRST_SELECT_PRESET));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_ROWS && button.id < (BTN_ID_FIRST_TOGGLE_ROWS + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_ROWS, button.id - BTN_ID_FIRST_TOGGLE_ROWS));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_COLUMNS && button.id < (BTN_ID_FIRST_TOGGLE_COLUMNS + 10))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_COLUMNS, button.id - BTN_ID_FIRST_TOGGLE_COLUMNS));
        }
    }
}