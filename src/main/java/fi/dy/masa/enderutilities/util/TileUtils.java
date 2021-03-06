package fi.dy.masa.enderutilities.util;

import javax.annotation.Nonnull;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileUtils
{
    public static ItemStack storeTileEntityInStack(ItemStack stack, TileEntity te, boolean addNBTLore)
    {
        return storeTileEntityNBTInStack(stack, te.writeToNBT(new NBTTagCompound()), addNBTLore);
    }

    public static ItemStack storeTileEntityNBTInStack(ItemStack stack, NBTTagCompound nbt, boolean addNBTLore)
    {
        nbt.removeTag("x");
        nbt.removeTag("y");
        nbt.removeTag("z");

        if (stack.getItem() == Items.SKULL && nbt.hasKey("Owner"))
        {
            NBTTagCompound tagOwner = nbt.getCompoundTag("Owner");
            NBTTagCompound nbtOwner2 = new NBTTagCompound();
            nbtOwner2.setTag("SkullOwner", tagOwner);
            stack.setTagCompound(nbtOwner2);
        }
        else
        {
            stack.setTagInfo("BlockEntityTag", nbt);

            if (addNBTLore)
            {
                NBTTagCompound tagDisplay = new NBTTagCompound();
                NBTTagList tagLore = new NBTTagList();
                tagLore.appendTag(new NBTTagString("(+NBT)"));
                tagDisplay.setTag("Lore", tagLore);
                stack.setTagInfo("display", tagDisplay);
            }
        }

        return stack;
    }

    public static ItemStack storeTileEntityInStackWithCachedInventory(ItemStack stack, TileEntity te, boolean addNBTLore, int maxStackEntries)
    {
        storeTileEntityInStack(stack, te, addNBTLore);

        if (te instanceof TileEntityEnderUtilitiesInventory)
        {
            NBTTagCompound nbt = NBTUtils.getRootCompoundTag(stack, true);
            ((TileEntityEnderUtilitiesInventory) te).getCachedInventory(nbt, maxStackEntries);
        }

        return stack;
    }

    /**
     * Creates a new TileEntity from the NBT provided, adds that TileEntity to the world, and marks it dirty
     * @param world
     * @param pos
     * @param nbt
     * @return true if creating and setting the TileEntity succeeded
     */
    public static boolean createAndAddTileEntity(World world, BlockPos pos, @Nonnull NBTTagCompound nbt)
    {
        return createAndAddTileEntity(world, pos, nbt, Rotation.NONE, Mirror.NONE);
    }

    /**
     * Creates a new TileEntity from the NBT provided, adds that TileEntity to the world,
     * then mirrors and rotates it (if they are not NONE), and then finally marks it dirty.
     * @param world
     * @param pos
     * @param tag
     * @param rotation
     * @param mirror
     * @return true if creating and setting the TileEntity succeeded
     */
    public static boolean createAndAddTileEntity(World world, BlockPos pos, @Nonnull NBTTagCompound nbt, Rotation rotation, Mirror mirror)
    {
        BlockPos posOrig = null;

        // Just store and later restore the original position instead of copying the entire (possibly large) NBT tag
        if (nbt.hasKey("x", Constants.NBT.TAG_INT) && nbt.hasKey("y", Constants.NBT.TAG_INT) && nbt.hasKey("z", Constants.NBT.TAG_INT))
        {
            posOrig = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        }

        // Set the correct position in the NBT prior to creating the TileEntity.
        // This can improve compatibility/reduce glitches with some tiles.
        NBTUtils.setPositionInTileEntityNBT(nbt, pos);

        // Creating the TileEntity from NBT and then calling World#setTileEntity() causes
        // TileEntity#validate() and TileEntity#onLoad() to get called for the TE
        // from Chunk#addTileEntity(), which should hopefully be more mod
        // friendly than just doing world.getTileEntity(pos).readFromNBT(tag)
        TileEntity te = TileEntity.create(world, nbt);

        if (posOrig != null)
        {
            // Restore the original position, in case the tag ends up written back to somewhere.
            // (Although when does the original position matter anyway?)
            NBTUtils.setPositionInTileEntityNBT(nbt, posOrig);
        }

        if (te != null)
        {
            //te.setPos(pos); // this happens in World#setTileEntity() and in Chunk#addTileEntity()
            world.setTileEntity(pos, te);

            if (mirror != Mirror.NONE)
            {
                te.mirror(mirror);
            }

            if (rotation != Rotation.NONE)
            {
                te.rotate(rotation);
            }

            te.markDirty();

            return true;
        }

        return false;
    }
}
