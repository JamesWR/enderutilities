package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityEnderUtilities extends TileEntity
{
    protected String tileEntityName;
    protected EnumFacing facing;
    protected OwnerData ownerData;
    protected String ownerName;

    public TileEntityEnderUtilities(String name)
    {
        this.facing = BlockEnderUtilities.DEFAULT_FACING;
        this.ownerData = null;
        this.ownerName = null;
        this.tileEntityName = name;
    }

    public String getTEName()
    {
        return this.tileEntityName;
    }

    public void setFacing(EnumFacing facing)
    {
        this.facing = facing;
    }

    public EnumFacing getFacing()
    {
        return this.facing;
    }

    @Override
    public void func_189668_a(Mirror mirrorIn)
    {
        this.func_189667_a(mirrorIn.toRotation(this.facing));
    }

    @Override
    public void func_189667_a(Rotation rotationIn)
    {
        this.facing = rotationIn.rotate(this.facing);
    }

    public void setOwner(EntityPlayer player)
    {
        this.ownerData = player != null ? new OwnerData(player) : null;
    }

    public String getOwnerName()
    {
        return this.ownerName;
    }

    public UUID getOwnerUUID()
    {
        return this.ownerData != null ? this.ownerData.getOwnerUUID() : null;
    }

    public void onLeftClickBlock(EntityPlayer player) { }

    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Rotation", Constants.NBT.TAG_BYTE))
        {
            this.facing = EnumFacing.getFront(nbt.getByte("Rotation"));
        }

        this.ownerData = OwnerData.getOwnerDataFromNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setString("Version", Reference.MOD_VERSION);
        nbt.setByte("Rotation", (byte)this.facing.getIndex());

        if (this.ownerData != null)
        {
            this.ownerData.writeToNBT(nbt);
        }

        return nbt;
    }

    /**
     * Get the data used for syncing the TileEntity to the client.
     * The data returned from this method doesn't have the position,
     * the position will be added in getUpdateTag() which calls this method.
     */
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt.setByte("r", (byte)(this.facing.getIndex() & 0x07));

        if (this.ownerData != null)
        {
            nbt.setString("o", this.ownerData.getOwnerName());
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        // The tag from this method is used for the initial chunk packet,
        // and it needs to have the TE position!
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", this.getPos().getX());
        nbt.setInteger("y", this.getPos().getY());
        nbt.setInteger("z", this.getPos().getZ());

        // Add the per-block data to the tag
        return this.getUpdatePacketTag(nbt);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        if (this.worldObj != null)
        {
            return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdatePacketTag(new NBTTagCompound()));
        }

        return null;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        if (tag.hasKey("r") == true)
        {
            this.setFacing(EnumFacing.getFront((byte)(tag.getByte("r") & 0x07)));
        }
        if (tag.hasKey("o", Constants.NBT.TAG_STRING) == true)
        {
            this.ownerName = tag.getString("o");
        }

        IBlockState state = this.worldObj.getBlockState(this.getPos());
        this.worldObj.notifyBlockUpdate(this.getPos(), state, state, 3);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        this.handleUpdateTag(packet.getNbtCompound());
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "(" + this.getPos() + ")@" + System.identityHashCode(this);
    }
}
