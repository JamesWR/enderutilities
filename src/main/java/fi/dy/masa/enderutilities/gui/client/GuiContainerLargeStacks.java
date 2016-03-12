package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;

public class GuiContainerLargeStacks extends GuiEnderUtilities
{
    protected final List<IItemHandler> scaledStackSizeTextTargetInventories;

    public GuiContainerLargeStacks(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container, xSize, ySize, textureName);

        this.scaledStackSizeTextTargetInventories = new ArrayList<IItemHandler>();
    }

    @Override
    public void drawSlot(Slot slotIn)
    {
        int slotPosX = slotIn.xDisplayPosition;
        int slotPosY = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick == false;
        ItemStack itemstack1 = this.mc.thePlayer.inventory.getItemStack();
        String str = null;

        if (slotIn == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick  == true && itemstack != null)
        {
            itemstack = itemstack.copy();
            itemstack.stackSize /= 2;
        }
        else if (this.dragSplitting == true && this.dragSplittingSlots.contains(slotIn) && itemstack1 != null)
        {
            if (this.dragSplittingSlots.size() == 1)
            {
                return;
            }

            if (Container.canAddItemToSlot(slotIn, itemstack1, true) == true && this.inventorySlots.canDragIntoSlot(slotIn) == true)
            {
                itemstack = itemstack1.copy();
                flag = true;
                Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);

                if (itemstack.stackSize > itemstack.getMaxStackSize())
                {
                    str = EnumChatFormatting.YELLOW + "" + itemstack.getMaxStackSize();
                    itemstack.stackSize = itemstack.getMaxStackSize();
                }

                if (itemstack.stackSize > slotIn.getItemStackLimit(itemstack))
                {
                    str = EnumChatFormatting.YELLOW + "" + slotIn.getItemStackLimit(itemstack);
                    itemstack.stackSize = slotIn.getItemStackLimit(itemstack);
                }
            }
            else
            {
                this.dragSplittingSlots.remove(slotIn);
                this.updateDragSplitting();
            }
        }

        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;

        if (itemstack == null)
        {
            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();

            if (textureatlassprite != null)
            {
                GlStateManager.disableLighting();
                this.mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
                this.drawTexturedModalRect(slotPosX, slotPosY, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        if (flag1 == false)
        {
            if (flag == true)
            {
                drawRect(slotPosX, slotPosY, slotPosX + 16, slotPosY + 16, -2130706433);
            }

            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(itemstack, slotPosX, slotPosY);

            // This slot belongs to a "large stacks" type inventory, render the stack size text scaled to 0.5x
            if (slotIn instanceof SlotItemHandler && this.scaledStackSizeTextTargetInventories.contains(((SlotItemHandler)slotIn).itemHandler) == true)
            {
                this.renderLargeStackItemOverlayIntoGUI(this.fontRendererObj, itemstack, slotPosX, slotPosY);
            }
            else
            {
                this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemstack, slotPosX, slotPosY, str);
            }
        }

        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    public void renderLargeStackItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack stack, int xPosition, int yPosition)
    {
        if (stack == null)
        {
            return;
        }

        if (stack.stackSize != 1)
        {
            String str = stack.stackSize < 1 ? EnumChatFormatting.RED + String.valueOf(stack.stackSize) : String.valueOf(stack.stackSize);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();

            GlStateManager.pushMatrix();
            GlStateManager.translate(xPosition, yPosition, 0.0d);
            GlStateManager.scale(0.5d, 0.5d, 0.5d);

            fontRenderer.drawStringWithShadow(str, (31 - fontRenderer.getStringWidth(str)), 23, 0xFFFFFF);

            GlStateManager.popMatrix();

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }

        if (stack.getItem().showDurabilityBar(stack))
        {
            double health = stack.getItem().getDurabilityForDisplay(stack);
            int j = (int)Math.round(13.0D - health * 13.0D);
            int i = (int)Math.round(255.0D - health * 255.0D);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            drawQuad(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
            drawQuad(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
            drawQuad(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);

            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    public void drawQuad(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        renderer.pos(x +     0, y +      0, 0.0d).color(red, green, blue, alpha).endVertex();
        renderer.pos(x +     0, y + height, 0.0d).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y + height, 0.0d).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y +      0, 0.0d).color(red, green, blue, alpha).endVertex();

        Tessellator.getInstance().draw();
    }
}