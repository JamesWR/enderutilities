package fi.dy.masa.enderutilities.util.nbt;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.base.IModular;

public class UtilItemModular
{
	public enum ModuleType
	{
		// Note, the ordinal value of these modules is also used as the texture offset
		// when rendering the slot backgrounds from the GUI texture!! (checked to be in the range 0..n)
		// It is also used to store the selected module to the tool's NBT.
		// So don't go changing them after the first release to the public!
		TYPE_ENDERCORE_ACTIVE	(0, 0x01),
		TYPE_ENDERCAPACITOR		(1, 0x02),
		TYPE_LINKCRYSTAL		(2, 0x04),
		TYPE_MOBPERSISTANCE		(3, 0x08),
		TYPE_ANY				(-1, 0x00),
		TYPE_INVALID			(-10, 0x00);

		private int index;
		private int bitmask;

		ModuleType(int index, int bitmask)
		{
			this.index = index;
			this.bitmask = bitmask;
		}

		public int getOrdinal()
		{
			return this.index;
		}

		public int getModuleBitmask()
		{
			return this.bitmask;
		}

		public boolean equals(ModuleType val)
		{
			return val.getOrdinal() == this.index;
		}

		public boolean equals(int val)
		{
			return val == this.index;
		}
	}

	/* Returns the number of installed modules of the given type. */
	public static int getModuleCount(ItemStack stack, ModuleType moduleType)
	{
		if (stack == null || (stack.getItem() instanceof IModular) == false) { return 0; }

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
		{
			return 0;
		}

		NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		int count = 0;
		int listNumStacks = nbtTagList.tagCount();

		// Read all the module ItemStacks from the tool
		for (int i = 0; i < listNumStacks; ++i)
		{
			if (UtilItemModular.getModuleType(ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i))).equals(moduleType) == true)
			{
				count++;
			}
		}

		return count;
	}

	/* Returns a bitmask of the installed module types. Used for quicker checking of what is installed. */
	public static int getInstalledModulesMask(ItemStack stack)
	{
		if (stack == null) { return 0; }

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
		{
			return 0;
		}

		NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		int mask = 0;
		int listNumStacks = nbtTagList.tagCount();

		// Read all the module ItemStacks from the tool
		for (int i = 0; i < listNumStacks; ++i)
		{
			mask |= UtilItemModular.getModuleType(ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i))).getModuleBitmask();
		}

		return mask;
	}

	/* Returns the offset of the lowest tier module of the given type.
	 * This is to get all the module tiers to be in the range of 1..n, regardless of
	 * how the underlying item stack's damage values are allocated.
	 */
	public static int getTierOffset(ModuleType moduleType)
	{
		if (moduleType.equals(ModuleType.TYPE_ENDERCORE_ACTIVE) == true)
		{
			return -14;
		}

		return 1;
	}

	/* Returns the (max, if multiple) tier of the installed module.
	 * NOTE: This is based on the item damage, and assumes that higher damage is higher tier. */
	public static int getModuleTier(ItemStack stack, ModuleType moduleType)
	{
		if (stack == null) { return -1; }

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
		{
			return -1;
		}

		NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		int tier = -1;
		int listNumStacks = nbtTagList.tagCount();
		ItemStack moduleStack;

		// Read all the module ItemStacks from the tool
		for (int i = 0; i < listNumStacks; ++i)
		{
			moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
			if (UtilItemModular.getModuleType(moduleStack).equals(moduleType) == true && moduleStack.getItemDamage() > tier)
			{
				tier = moduleStack.getItemDamage();
			}
		}

		return tier + UtilItemModular.getTierOffset(moduleType);
	}

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	public static ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType)
	{
		if (stack == null) { return null; }

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
		{
			return null;
		}

		NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		int listNumStacks = nbtTagList.tagCount();
		int selected = 0;
		if (nbt.hasKey("Selected_" + moduleType.getOrdinal(), Constants.NBT.TAG_BYTE) == true)
		{
			selected = nbt.getByte("Selected_" + moduleType.getOrdinal());
			if (selected >= UtilItemModular.getModuleCount(stack, moduleType))
			{
				selected = 0;
			}
		}
		ItemStack moduleStack;

		// Get the selected-th module stack of the given type
		int num = -1;
		for (int i = 0; i < listNumStacks && num < selected; ++i)
		{
			moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
			if (UtilItemModular.getModuleType(moduleStack).equals(moduleType) == true)
			{
				num++;
			}
		}

		if (num >= 0)
		{
			moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(num));
			if (UtilItemModular.getModuleType(moduleStack).equals(moduleType) == true)
			{
				return moduleStack;
			}
		}

		return null;
	}

	/* Sets the selected modules' ItemStack of the given module type to the one provided. */
	public static ItemStack setSelectedModuleStack(ItemStack toolStack, UtilItemModular.ModuleType moduleType, ItemStack newModuleStack)
	{
		if (toolStack == null) { return null; }

		NBTTagCompound nbt = toolStack.getTagCompound();
		if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
		{
			return null;
		}

		NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		int listNumStacks = nbtTagList.tagCount();
		int selected = 0;
		if (nbt.hasKey("Selected_" + moduleType.getOrdinal(), Constants.NBT.TAG_BYTE) == true)
		{
			selected = nbt.getByte("Selected_" + moduleType.getOrdinal());
			if (selected >= UtilItemModular.getModuleCount(toolStack, moduleType))
			{
				selected = 0;
			}
		}
		ItemStack moduleStack;

		// Get the selected-th module stack of the given type
		int num = -1;
		for (int i = 0; i < listNumStacks && num < selected; ++i)
		{
			moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
			if (UtilItemModular.getModuleType(moduleStack).equals(moduleType) == true)
			{
				num++;
			}
		}

		if (num >= 0)
		{
			moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(num));
			if (UtilItemModular.getModuleType(moduleStack).equals(moduleType) == true)
			{
				// Write/replace the compound tag at position num with a tag created from the new module ItemStack
				nbtTagList.func_150304_a(num, newModuleStack.writeToNBT(new NBTTagCompound()));
			}
		}

		return toolStack;
	}

	/* Returns a list of all the installed modules. */
	public static List<NBTTagCompound> getAllModules(ItemStack stack)
	{
		if (stack == null) { return null; }
		return null;
	}

	/* Sets the modules to the ones provided in the list. */
	public static ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
	{
		if (stack == null) { return null; }
		return stack;
	}

	/* Sets the module indicated by the position to the one provided in the compound tag. */
	public static ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
	{
		if (stack == null) { return null; }
		return stack;
	}

	/* Returns the type of module the input stack contains. */
	public static ModuleType getModuleType(ItemStack moduleStack)
	{
		// Active Ender Core
		if (moduleStack.getItem() == EnderUtilitiesItems.enderPart && moduleStack.getItemDamage() >= 15 && moduleStack.getItemDamage() <= 17)
		{
			return UtilItemModular.ModuleType.TYPE_ENDERCORE_ACTIVE;
		}

		if (moduleStack.getItem() == EnderUtilitiesItems.enderCapacitor)
		{
			return UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR;
		}

		if (moduleStack.getItem() == EnderUtilitiesItems.linkCrystal)
		{
			return UtilItemModular.ModuleType.TYPE_LINKCRYSTAL;
		}

		return UtilItemModular.ModuleType.TYPE_INVALID;
	}

	/* Change the selected module to the next one, if any. */
	public static ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse)
	{
		if (stack == null)
		{
			return stack;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
		{
			return stack;
		}

		int moduleCount = UtilItemModular.getModuleCount(stack, moduleType);
		if (moduleCount == 0)
		{
			return stack;
		}

		int selected = nbt.getByte("Selected_" + moduleType.getOrdinal());
		if (reverse == true)
		{
			if (--selected < 0)
			{
				selected = moduleCount - 1;
			}
		}
		else
		{
			if (++selected >= moduleCount)
			{
				selected = 0;
			}
		}
		nbt.setByte("Selected_" + moduleType.getOrdinal(), (byte)selected);

		return stack;
	}
}