package fi.dy.masa.enderutilities.event;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiHandyBag;
import fi.dy.masa.enderutilities.gui.client.GuiScreenBuilderWandTemplate;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.item.block.ItemBlockPlacementProperty;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.Keybindings;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

public class InputEventHandler
{
    private static final Int2LongOpenHashMap KEY_PRESS_TIMES = new Int2LongOpenHashMap();
    private final Minecraft mc;
    /** Has the active mouse scroll modifier mask, if any */
    private static int scrollingMask = 0;
    /** Has the currently active/pressed mask of supported modifier keys */
    private static int modifierMask = 0;
    public static int doubleTapLimit = 500;

    public InputEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    /**
     * Reset the modifiers externally. This is to fix the stuck modifier keys
     * if a GUI is opened while the modifiers are active.
     * FIXME Apparently there are key input events for GUI screens in 1.8,
     * so this probably can be removed then.
     */
    public static void resetModifiers()
    {
        scrollingMask = 0;
        modifierMask = 0;
    }

    public static boolean isHoldingKeyboundItem(EntityPlayer player)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(player, IKeyBound.class);

        return stack.isEmpty() == false && ((stack.getItem() instanceof ItemBlockPlacementProperty) == false || 
                ((ItemBlockPlacementProperty) stack.getItem()).hasPlacementProperty(stack));
    }

    public static boolean hasKeyBoundUnselectedItem(EntityPlayer player)
    {
        return InventoryUtils.getFirstItemOfType(player, IKeyBoundUnselected.class).isEmpty() == false;
    }

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event)
    {
        int keyCode = Keyboard.getEventKey();
        boolean keyState = Keyboard.getEventKeyState();

        this.onInputEvent(keyCode, keyState);
    }

    @SubscribeEvent
    public void onGuiKeyInputEventPre(GuiScreenEvent.KeyboardInputEvent.Pre event)
    {
        if (event.getGui() instanceof GuiHandyBag)
        {
            int key = Keyboard.getEventKey();

            // Double-tap shift
            if (Keyboard.getEventKeyState() && (key == Keyboard.KEY_LSHIFT || key == Keyboard.KEY_RSHIFT) && this.checkForDoubleTap(key))
            {
                PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                    ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_SHIFTCLICK_DOUBLETAP, 0));
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event)
    {
        int dWheel = event.getDwheel();

        if (dWheel != 0)
        {
            dWheel /= 120;

            // If the player pressed down a modifier key while holding an IKeyBound item
            // (note: this means it specifically WON'T work if the player started pressing a modifier
            // key while holding something else, for example when scrolling through the hotbar!!),
            // then we allow for easily scrolling through the changeable stuff using the mouse wheel.
            if (scrollingMask != 0)
            {
                EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();

                if (isHoldingKeyboundItem(player))
                {
                    int key = HotKeys.KEYCODE_SCROLL | scrollingMask;

                    // Scrolling up, reverse the direction.
                    if (dWheel > 0)
                    {
                        key |= HotKeys.SCROLL_MODIFIER_REVERSE;
                    }

                    if (event.isCancelable())
                    {
                        event.setCanceled(true);
                    }

                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
            }
        }
        else if (this.onInputEvent(Mouse.getEventButton() - 100, Mouse.getEventButtonState()))
        {
            event.setCanceled(true);
        }
    }

    private boolean onInputEvent(int keyCode, boolean keyState)
    {
        EntityPlayer player = this.mc.player;

        if (player == null)
        {
            return false;
        }

        // One of our supported modifier keys was pressed or released
        if (HotKeys.isModifierKey(keyCode))
        {
            int mask = HotKeys.getModifierMask(keyCode);

            // Key was pressed
            if (keyState)
            {
                modifierMask |= mask;

                // Only add scrolling mode mask if the currently selected item is one of our IKeyBound items
                if (isHoldingKeyboundItem(player))
                {
                    scrollingMask |= mask;
                }
            }
            // Key was released
            else
            {
                modifierMask &= ~mask;
                scrollingMask &= ~mask;
            }
        }

        // In-game (no GUI open)
        if (this.mc.inGameHasFocus)
        {
            if (keyState && Keybindings.keyToggleMode.isActiveAndMatches(keyCode))
            {
                if (this.buildersWandClientSideHandling())
                {
                    return false;
                }

                if (isHoldingKeyboundItem(player))
                {
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(HotKeys.KEYBIND_ID_TOGGLE_MODE | modifierMask));
                    return false;
                }
            }

            if (keyState && Keybindings.keyActivateUnselected.isActiveAndMatches(keyCode) && hasKeyBoundUnselectedItem(player))
            {
                PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(HotKeys.KEYBIND_ID_TOGGLE_MODE | modifierMask));
                return false;
            }
            // Track the event of opening and closing the player's inventory.
            // This is intended to have the Handy Bag either open or not open ie. do the same thing for the duration
            // that the inventory is open at once. This is intended to get rid of the previous unintended behavior where
            // if you sneak + open the inventory to open just the regular player inventory, if you then look at recipes in JEI
            // or something similar where the GuiScreen changes, then the bag would suddenly open instead of the player inventory
            // when closing the recipe screen and returning to the inventory.

            // Based on a quick test, the inventory key fires as state when opening the inventory (before the gui opens)
            // and as state == false when closing the inventory (after the gui has closed).
            else if (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
            {
                boolean shouldOpen = keyState && player.isSneaking() == Configs.handyBagOpenRequiresSneak;
                GuiEventHandler.instance().setHandyBagShouldOpen(shouldOpen);
            }
            else if (keyCode == Keyboard.KEY_ESCAPE)
            {
                GuiEventHandler.instance().setHandyBagShouldOpen(false);
            }
            else if (keyState && this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode))
            {
                World world = player.getEntityWorld();
                RayTraceResult trace = EntityUtils.getRayTraceFromPlayer(world, player, true);

                if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
                {
                    int key = HotKeys.KEYCODE_MIDDLE_CLICK | HotKeys.getActiveModifierMask();
                    BlockPos pos = trace.getBlockPos();
                    TileEntityEnderUtilities te = BlockEnderUtilities.getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

                    if (te != null && te.onInputAction(key, player, trace, world, pos))
                    {
                        PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                        return true;
                    }
                }
            }
            // Jump or sneak above an Ender Elevator - activate it
            else if (keyState && (keyCode == this.mc.gameSettings.keyBindJump.getKeyCode() ||
                                  keyCode == this.mc.gameSettings.keyBindSneak.getKeyCode()))
            {
                BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
                World world = player.getEntityWorld();

                // Check the player's feet position in case they are standing inside a slab or layer elevator
                if (world.getBlockState(pos       ).getBlock() instanceof BlockElevator ||
                    world.getBlockState(pos.down()).getBlock() instanceof BlockElevator)
                {
                    int key = keyCode == this.mc.gameSettings.keyBindJump.getKeyCode() ? HotKeys.KEYCODE_JUMP : HotKeys.KEYCODE_SNEAK;
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
            }
        }

        return false;
    }

    private boolean checkForDoubleTap(int key)
    {
        long currentTime = System.currentTimeMillis();
        boolean ret = KEY_PRESS_TIMES.containsKey(key) && (currentTime - KEY_PRESS_TIMES.get(key)) <= doubleTapLimit;

        if (ret == false)
        {
            KEY_PRESS_TIMES.put(key, currentTime);
        }
        else
        {
            KEY_PRESS_TIMES.remove(key);
        }

        return ret;
    }

    private boolean buildersWandClientSideHandling()
    {
        if (GuiScreen.isShiftKeyDown() || GuiScreen.isCtrlKeyDown() || GuiScreen.isAltKeyDown())
        {
            return false;
        }

        ItemStack stack = this.mc.player.getHeldItemMainhand();

        if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.BUILDERS_WAND &&
            ItemBuildersWand.Mode.getMode(stack) == Mode.COPY)
        {
            this.mc.displayGuiScreen(new GuiScreenBuilderWandTemplate());
            return true;
        }

        return false;
    }
}
