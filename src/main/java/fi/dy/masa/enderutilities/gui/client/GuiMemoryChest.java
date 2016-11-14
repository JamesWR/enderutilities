package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.ContainerMemoryChest;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;

public class GuiMemoryChest extends GuiEnderUtilities implements IButtonCallback
{
    public static final int BTN_ID_TOGGLE_LOCK = 1;

    private final TileEntityMemoryChest temc;
    private final IItemHandler inventory;
    private final int chestTier;

    public GuiMemoryChest(ContainerMemoryChest container, TileEntityMemoryChest te)
    {
        super(container, 176, 176, "gui.container." + te.getTEName() + "." + (te.getStorageTier() < 3 ? te.getStorageTier() : 0));

        this.infoArea = new InfoArea(151, 5, 17, 17, "enderutilities.gui.label.memorychest.info");
        this.temc = te;
        this.inventory = this.container.inventory;
        this.chestTier = te.getStorageTier();
    }

    @Override
    public void initGui()
    {
        this.setGuiYSize();

        super.initGui();
        this.createButtons();
    }

    protected void setGuiYSize()
    {
        switch(this.chestTier)
        {
            case 0: this.ySize = 140; break;
            case 1: this.ySize = 176; break;
            case 2: this.ySize = 234; break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int y = 47;

        switch(this.chestTier)
        {
            case 0: y =  47; break;
            case 1: y =  83; break;
            case 2: y = 137; break;
            default:
        }

        this.fontRendererObj.drawString(I18n.format("enderutilities.container.memorychest", new Object[0]), 8, 15, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, y, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int invSize = this.inventory.getSlots();

        // Draw the colored background icon for locked/"templated" slots
        long mask = this.temc.getTemplateMask();
        long bit = 0x1;

        for (int i = 0; i < invSize; i++, bit <<= 1)
        {
            Slot slot = this.inventorySlots.getSlot(i);
            if ((mask & bit) != 0)
            {
                int x = this.guiLeft + slot.xDisplayPosition;
                int y = this.guiTop + slot.yDisplayPosition;
                int v = 18;

                // Empty locked slots are in a different color
                if (this.inventory.getStackInSlot(i) == null)
                {
                    v = 36;
                }

                this.drawTexturedModalRect(x - 1, y - 1, 102, v, 18, 18);
            }
        }

        // Draw a faint version of the template item for empty locked slots
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        bit = 0x1;
        for (int i = 0; i < invSize; i++, bit <<= 1)
        {
            Slot slot = this.inventorySlots.getSlot(i);
            if ((mask & bit) != 0)
            {
                if (this.inventory.getStackInSlot(i) == null)
                {
                    ItemStack stack = this.temc.getTemplateStack(i);
                    if (stack != null)
                    {
                        int x = this.guiLeft + slot.xDisplayPosition;
                        int y = this.guiTop + slot.yDisplayPosition;
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        GlStateManager.enableBlend();
                        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
                    }
                }
            }
        }

        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        String str = I18n.format("enderutilities.gui.label.publicprivate") + " (" +
                I18n.format("enderutilities.tooltip.item.owner") + ": " + this.temc.getOwnerName() + ")";

        this.buttonList.add(new GuiButtonCallback(BTN_ID_TOGGLE_LOCK, x + 138, y + 15, 8, 8, 0, 0,
                this.guiTextureWidgets, 8, 0, this, str));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == BTN_ID_TOGGLE_LOCK)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.temc.getWorld().provider.getDimension(), this.temc.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityMemoryChest.GUI_ACTION_TOGGLE_LOCKED, 0));
        }
    }

    @Override
    public int getButtonU(int callbackId, int defaultU)
    {
        return defaultU;
    }

    @Override
    public int getButtonV(int callbackId, int defaultV)
    {
        // Locked mode
        if (callbackId == BTN_ID_TOGGLE_LOCK)
        {
            return this.temc.isPublic() ? 0 : 48;
        }

        return defaultV;
    }
}
