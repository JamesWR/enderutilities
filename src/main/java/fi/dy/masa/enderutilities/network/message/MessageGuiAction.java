package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.item.ItemPickupManager;
import fi.dy.masa.enderutilities.item.ItemQuickStacker;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import io.netty.buffer.ByteBuf;

public class MessageGuiAction implements IMessage
{
    private int guiId;
    private int action;
    private int elementId;
    private int dimension;
    private int posX;
    private int posY;
    private int posZ;

    public MessageGuiAction()
    {
    }

    public MessageGuiAction(int dim, BlockPos pos, int guiId, int action, int elementId)
    {
        this.dimension = dim;
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.guiId = guiId;
        this.action = action;
        this.elementId = elementId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.dimension = buf.readInt();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.guiId = buf.readInt();
        this.action = buf.readInt();
        this.elementId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.dimension);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeInt(this.guiId);
        buf.writeInt(this.action);
        buf.writeInt(this.elementId);
    }

    public static class Handler implements IMessageHandler<MessageGuiAction, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageGuiAction message, MessageContext ctx)
        {
            if (ctx.side != Side.SERVER)
            {
                EnderUtilities.logger.error("Wrong side in MessageGuiAction: " + ctx.side);
                return null;
            }

            final EntityPlayerMP sendingPlayer = ctx.getServerHandler().player;
            if (sendingPlayer == null)
            {
                EnderUtilities.logger.error("Sending player was null in MessageGuiAction");
                return null;
            }

            final WorldServer playerWorldServer = sendingPlayer.getServerWorld();
            if (playerWorldServer == null)
            {
                EnderUtilities.logger.error("World was null in MessageGuiAction");
                return null;
            }

            playerWorldServer.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message, sendingPlayer);
                }
            });

            return null;
        }

        protected void processMessage(final MessageGuiAction message, EntityPlayer player)
        {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension);

            if (world != null)
            {
                switch(message.guiId)
                {
                    case ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC:
                        BlockPos pos = new BlockPos(message.posX, message.posY, message.posZ);

                        if (world.isBlockLoaded(pos))
                        {
                            TileEntity te = world.getTileEntity(pos);
                            if (te != null && te instanceof TileEntityEnderUtilities)
                            {
                                ((TileEntityEnderUtilities) te).performGuiAction(player, message.action, message.elementId);
                            }
                        }
                        break;

                    case ReferenceGuiIds.GUI_ID_CONTAINER_GENERIC:
                        if (player.openContainer instanceof ContainerEnderUtilities)
                        {
                            ((ContainerEnderUtilities) player.openContainer).performGuiAction(player, message.action, message.elementId);
                        }
                        break;

                    case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                        ItemHandyBag.performGuiAction(player, message.action, message.elementId);
                        break;

                    case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                        ItemInventorySwapper.performGuiAction(player, message.action, message.elementId);
                        break;

                    case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                        ItemPickupManager.performGuiAction(player, message.action, message.elementId);
                        break;

                    case ReferenceGuiIds.GUI_ID_QUICK_STACKER:
                        ItemQuickStacker.performGuiAction(player, message.action, message.elementId);
                        break;

                    case ReferenceGuiIds.GUI_ID_NULLIFIER:
                        ItemNullifier.performGuiAction(player, message.action, message.elementId);
                        break;

                    default:
                }
            }
        }
    }
}
