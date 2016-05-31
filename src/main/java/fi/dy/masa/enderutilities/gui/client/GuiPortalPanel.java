package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class GuiPortalPanel extends GuiEnderUtilities
{
    private final TileEntityPortalPanel tepp;

    public GuiPortalPanel(ContainerEnderUtilities container, TileEntityPortalPanel te)
    {
        super(container, 176, 203, "gui.container." + te.getTEName());
        this.tepp = te;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.tepp.hasCustomName() ? this.tepp.getName() : I18n.format(this.tepp.getName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 110, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int u = 240;
        int v = 32;
        int active = this.tepp.getActiveTarget();

        for (int i = 0; i < 16; i++)
        {
            Slot slot = this.container.getSlot(i);

            if (i < 8 && active == i)
            {
                //System.out.println("active: " + i);
                this.drawTexturedModalRect(x + slot.xDisplayPosition - 1, y + slot.yDisplayPosition - 1,      102, 54, 18, 18);
                this.drawTexturedModalRect(x + slot.xDisplayPosition - 1, y + slot.yDisplayPosition - 1 + 18, 102, 54, 18, 18);
            }

            if (slot.getHasStack() == false)
            {
                this.drawTexturedModalRect(x + slot.xDisplayPosition, y + slot.yDisplayPosition, u, v, 16, 16);
            }

            if (i == 7)
            {
                u = 0;
                v = 240;
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        Slot slot = this.getSlotUnderMouse();
        // Hovering over an empty dye slot
        if (slot != null && slot.getHasStack() == false && slot.slotNumber >= 8 && slot.slotNumber <= 15)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.dyeslot", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int xOff = 57;
        int yOff = 56;

        for (int i = 0; i < 8; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + xOff, y + yOff, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
            xOff += 18;

            if (i == 3)
            {
                xOff = 57;
                yOff = 106;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id < 8)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, this.tepp.getPos(),
                    ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, 0, button.id));
        }
    }
}
