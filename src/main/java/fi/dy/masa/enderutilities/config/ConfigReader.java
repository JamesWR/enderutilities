package fi.dy.masa.enderutilities.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.BlackLists;
import fi.dy.masa.enderutilities.util.WorldUtils;

public class ConfigReader
{
    private static final int CURRENT_CONFIG_VERSION = 6600;
    public static final String CATEGORY_BUILDERSWAND = "BuildersWand";
    public static final String CATEGORY_CLIENT = "Client";
    public static final String CATEGORY_GENERIC = "Generic";
    public static final String CATEGORY_LISTS = "Lists";
    //private static int confVersion = 0;
    private static File configurationFile;
    private static Configuration config;

    private static String currentCategory;
    private static boolean currentRequiresMcRestart;
    private static final Set<String> VALID_CATEGORIES = new HashSet<String>();
    private static final Set<String> VALID_CONFIGS = new HashSet<String>();

    public static void loadConfigsFromFile(File configFile)
    {
        configurationFile = configFile;
        config = new Configuration(configurationFile, null, true);
        config.load();
        reLoadAllConfigs(false);
    }

    public static Configuration loadConfigsFromFile()
    {
        //config.load();
        return config;
    }

    public static void reLoadAllConfigs(boolean reloadFromFile)
    {
        if (reloadFromFile)
        {
            config.load();
        }

        loadConfigGeneric(config);
        loadConfigItemControl(config);
        loadConfigLists(config);
        removeInvalidConfigs(config);

        if (config.hasChanged())
        {
            config.save();
        }
    }

    public static File getConfigFile()
    {
        return configurationFile;
    }

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.getModID()))
        {
            reLoadAllConfigs(false);
        }
    }

    private static void loadConfigGeneric(Configuration conf)
    {
        Property prop;
        currentCategory = CATEGORY_GENERIC;
        currentRequiresMcRestart = false;

        prop = getProp("barrelCapacityUpgradeStacksPer", 256);
        prop.setComment("How many stacks of storage each Barrel Capacity Upgrade adds");
        Configs.barrelCapacityUpgradeStacksPer = prop.getInt();

        prop = getProp("barrelInversedSneak", false);
        prop.setComment("Inverses the sneak behavior of taking out items from barrels.\nWhen inversed, you take out one item when NOT sneaking.");
        Configs.barrelInversedSneak = prop.getBoolean();

        prop = getProp("barrelMaxCapacityUpgrades", 64);
        prop.setComment("How many Barrel Capacity Upgrades can be applied to one barrel");
        Configs.barrelMaxCapacityUpgrades = prop.getInt();

        prop = getProp("barrelRenderFullnessBar", true);
        prop.setComment("Whether or not to render the \"fullness bar\" on Barrels");
        Configs.barrelRenderFullnessBar = prop.getBoolean();

        prop = getProp("enderBowAllowPlayers", true);
        prop.setComment("Is the Ender Bow allowed to teleport players (directly or in a 'stack' riding something)");
        Configs.enderBowAllowPlayers = prop.getBoolean();

        prop = getProp("enderBowAllowSelfTP", true);
        prop.setComment("Can the Ender Bow be used in the 'TP Self' mode");
        Configs.enderBowAllowSelfTP = prop.getBoolean();

        prop = getProp("enderBucketCapacity", 16000);
        prop.setComment("Maximum amount the Ender Bucket can hold, in millibuckets. Default: 16000 mB (= 16 buckets).");
        Configs.enderBucketCapacity = prop.getInt();

        prop = getProp("enderLassoAllowPlayers", true);
        prop.setComment("Is the Ender Lasso allowed to teleport players (directly or in a 'stack' riding something)");
        Configs.enderLassoAllowPlayers = prop.getBoolean();

        prop = getProp("fallingBlockDropsAsItemOnPlacementFail", false);
        prop.setComment("If true, then the block will drop as item when failing to place (like vanilla falling blocks).\n" +
                        "If false, then it will stay in entity form until it's able to place itself.");
        Configs.fallingBlockDropsAsItemOnPlacementFail = prop.getBoolean();

        prop = getProp("handyBagEnableItemUpdate", false);
        prop.setComment("Master config to enable calling the stored items' update method in the Handy Bag.\n" +
                        "WARNING: Due to how complex the bag's inventory stuff is (due to the bag storing Memory Cards,\n" +
                        "which then store the items), using this functionality\n" +
                        "might cause lots of NBT data changes in the bag and also considerable network bandwidth usage\n" +
                        "when updating the bag's NBT to the clients. So USE WITH CAUTION!");
        Configs.handyBagEnableItemUpdate = prop.getBoolean();

        prop = getProp(currentCategory, "harvestLevelEnderAlloyAdvanced", 3, false);
        prop.setComment("The harvest level of tools made from Advanced Ender Alloy (3 = vanilla diamond tool level).");
        Configs.harvestLevelEnderAlloyAdvanced = prop.getInt();

        prop = getProp("msuMaxItems", 1000000000);
        prop.setComment("The maximum amount of items (per slot) the MSU and MSB can store. Max is " + Integer.MAX_VALUE);
        Configs.msuMaxItems = prop.getInt();

        prop = getProp("portalAreaCheckLimit", 10000);
        prop.setComment("How many blocks to check at most when checking that one portal area is valid");
        Configs.portalAreaCheckLimit = prop.getInt();

        prop = getProp("portalFrameCheckLimit", 2000);
        prop.setComment("How many Portal Frame blocks to check at most");
        Configs.portalFrameCheckLimit = prop.getInt();

        prop = getProp("portalLoopCheckLimit", 2000);
        prop.setComment("How many blocks to check at most when checking portal enclosing loops");
        Configs.portalLoopCheckLimit = prop.getInt();

        prop = getProp("portalOnlyAllowsPortalTypeLinkCrystals", false);
        prop.setComment("If true, then the Portal Panel only accepts Portal type Link Crystals.\n" +
                        "This allows forcing the Portals to be used in pairs (or rather, that there\n" +
                        "must always also be _some_ Portal in the destination location.)");
        Configs.portalOnlyAllowsPortalTypeLinkCrystals = prop.getBoolean();

        prop = getProp("replaceEntityItemCollisionBoxHandling", false);
        prop.setComment("If enabled, then a custom event replaces fetching the block collision boxes for\n" +
                        "EntityItems and EntityXPOrbs when they are being pushed out of blocks.\n" +
                        "Without this, the Cracked Floor are really derpy and shoot the items and XP\n" +
                        "everywhere while they try to fall through the block.\n" +
                        "NOTE: This doesn't currently work in 1.11.2+ due to vanilla/Forge changes");
        Configs.replaceEntityItemCollisionBoxHandling = prop.getBoolean();

        prop = getProp("useEnderCharge", true);
        prop.setComment("Do items require Ender Charge to operate? (stored in Ender Capacitors)");
        Configs.useEnderCharge = prop.getBoolean();

        currentCategory = CATEGORY_BUILDERSWAND;
        conf.addCustomCategoryComment(currentCategory, "Configs for the Wand of the Lazy Builder");

        prop = getProp("buildersWandBlocksPerTick", 10);
        prop.setComment("The number of blocks the Lazy Builder's Wand will place each game tick\n"+
                        "in the \"build modes\". Default: 10 (= 200 blocks per second)");
        Configs.buildersWandBlocksPerTick = prop.getInt();

        prop = getProp("buildersWandEnableCopyMode", true);
        prop.setComment("Enables the Copy mode functionality in survival mode");
        Configs.buildersWandEnableCopyMode = prop.getBoolean();

        prop = getProp("buildersWandEnableMoveMode", true);
        prop.setComment("Enables the Move mode functionality in survival mode");
        Configs.buildersWandEnableMoveMode = prop.getBoolean();

        prop = getProp("buildersWandEnablePasteMode", true);
        prop.setComment("Enables the Paste mode functionality in survival mode");
        Configs.buildersWandEnablePasteMode = prop.getBoolean();

        prop = getProp("buildersWandEnableReplaceMode", true);
        prop.setComment("Enables the Replace mode functionality in survival mode");
        Configs.buildersWandEnableReplaceMode = prop.getBoolean();

        prop = getProp("buildersWandEnableReplace3DMode", true);
        prop.setComment("Enables the Replace 3D mode functionality in survival mode");
        Configs.buildersWandEnableReplace3DMode = prop.getBoolean();

        prop = getProp("buildersWandEnableStackMode", true);
        prop.setComment("Enables the \"Stack Area\" mode functionality in survival mode");
        Configs.buildersWandEnableStackMode = prop.getBoolean();

        prop = getProp("buildersWandGhostBlockAlpha", 0.7d);
        prop.setComment("The alpha value to use for the translucent ghost block rendering mode");
        Configs.buildersWandGhostBlockAlpha = (float) MathHelper.clamp(prop.getDouble(), 0, 1);

        prop = getProp("buildersWandMaxBlockHardness", 60d);
        prop.setComment("The maximum block hardness of the blocks the wand can break/move in survival mode");
        Configs.buildersWandMaxBlockHardness = (float) prop.getDouble();

        prop = getProp("buildersWandReplaceBlocksPerTick", 2);
        prop.setComment("The number of blocks to replace per game tick in the Replace mode, default: 2 (= 40 blocks per second)");
        Configs.buildersWandReplaceBlocksPerTick = prop.getInt();

        prop = getProp("buildersWandUseTranslucentGhostBlocks", true);
        prop.setComment("Use translucent ghost block rendering instead of opaque");
        Configs.buildersWandUseTranslucentGhostBlocks = prop.getBoolean();

        currentCategory = CATEGORY_CLIENT;
        conf.addCustomCategoryComment(currentCategory, "Client side configs");

        prop = getProp("announceLocationBindingInChat", false);
        prop.setComment("Prints a chat message when items are bound to a new location");
        Configs.announceLocationBindingInChat = prop.getBoolean();

        prop = getProp("buildersWandAndRulerRenderForOtherPlayers", true);
        prop.setComment("Render the Ruler and Builder's Wand areas/selections also for the items held by other players");
        Configs.buildersWandRenderForOtherPlayers = prop.getBoolean();

        prop = getProp("handyBagOpenRequiresSneak", false);
        prop.setComment("Reverse the sneak behaviour on opening the Handy Bag instead of the regular inventory");
        Configs.handyBagOpenRequiresSneak = prop.getBoolean();

        prop = getProp("useToolParticles", true);
        prop.setComment("Does the block drops teleporting by Ender tools cause particle effects");
        Configs.useToolParticles = prop.getBoolean();

        prop = getProp("useToolSounds", true);
        prop.setComment("Does the block drops teleporting by Ender tools play the sound effect");
        Configs.useToolSounds = prop.getBoolean();

        prop = getProp("Version", "configFileVersion", 6600, true);
        prop.setComment("Internal config file version tracking. DO NOT CHANGE!!");
        //confVersion = prop.getInt();

        // Update the version in the config to the current version
        prop.setValue(CURRENT_CONFIG_VERSION);
    }

    private static void loadConfigItemControl(Configuration conf)
    {
        Property prop;
        currentCategory = "DisableBlocks";
        currentRequiresMcRestart = true;

        conf.addCustomCategoryComment(currentCategory, "Completely disable blocks (don't register them to the game.)\n" +
                "Note that machines are grouped together and identified by the meta value. You can't disable just a specific meta value.");

        // Block disable
        Configs.disableBlockASU           = getProp("disableBlockAdjustableStorageUnit", false).getBoolean();
        Configs.disableBlockBarrel        = getProp("disableBlockBarrel", false).getBoolean();
        Configs.disableBlockDrawbridge    = getProp("disableBlockDrawbridge", false).getBoolean();
        Configs.disableBlockEnderElevator = getProp("disableBlockEnderElevator", false).getBoolean();
        Configs.disableBlockFloor         = getProp("disableBlockFloor", false).getBoolean();
        Configs.disableBlockInserter      = getProp("disableBlockInserter", false).getBoolean();
        Configs.disableBlockMolecularExciter = getProp("disableBlockMolecularExciter", false).getBoolean();
        Configs.disableBlockPhasing       = getProp("disableBlockPhasing", false).getBoolean();
        Configs.disableBlockPortal        = getProp("disableBlockPortal", false).getBoolean();
        Configs.disableBlockPortalFrame   = getProp("disableBlockPortalFrame", false).getBoolean();
        Configs.disableBlockPortalPanel   = getProp("disableBlockPortalPanel", false).getBoolean();
        Configs.disableBlockSoundBlock    = getProp("disableBlockSoundBlock", false).getBoolean();

        prop = getProp("disableBlockEnergyBridge", false);
        prop.setComment("Meta values: 0 = Energy Bridge Resonator; 1 = Energy Bridge Receiver; 2 = Energy Bridge Transmitter");
        Configs.disableBlockEnergyBridge = prop.getBoolean();

        prop = getProp("disableBlockMachine_0", false);
        prop.setComment("Info: Machine_0 meta values: 0 = Ender Furnace");
        Configs.disableBlockMachine_0 = prop.getBoolean();

        prop = getProp("disableBlockMachine_1", false);
        prop.setComment("Info: Machine_1 meta values: 0 = Ender Infuser; 1 = Tool Workstation, 2 = Creation Station");
        Configs.disableBlockMachine_1 = prop.getBoolean();

        prop = getProp("disableBlockStorage_0", false);
        prop.setComment("Meta values: 0..2 = Memory Chests, 3..6 = Handy Chests, 7 = Junk Storage Unit");
        Configs.disableBlockStorage_0 = prop.getBoolean();

        prop = getProp("disableBlockMassiveStorageUnit", false);
        prop.setComment("Meta values: 0 = Massive Storage Unit, 1 = Massive Storage Bundle");
        Configs.disableBlockMSU = prop.getBoolean();

        currentCategory = "DisableItems";
        conf.addCustomCategoryComment(currentCategory, "Completely disable items (don't register them to the game.)\n" +
                "Note that some items are grouped together using the damage value (and/or NBT data) to identify them.\n" +
                "You can't disable a specific damage value only (so that existing items would vanish).");

        // Item disable
        Configs.disableItemCraftingPart           = getProp("disableItemCraftingPart", false).getBoolean();
        Configs.disableItemEnderCapacitor         = getProp("disableItemEnderCapacitor", false).getBoolean();
        Configs.disableItemLinkCrystal            = getProp("disableItemLinkCrystal", false).getBoolean();

        Configs.disableItemBuildersWand           = getProp("disableItemBuildersWand", false).getBoolean();
        Configs.disableItemEnderArrow             = getProp("disableItemEnderArrow", false).getBoolean();
        Configs.disableItemEnderBag               = getProp("disableItemEnderBag", false).getBoolean();
        Configs.disableItemEnderBow               = getProp("disableItemEnderBow", false).getBoolean();
        Configs.disableItemEnderBucket            = getProp("disableItemEnderBucket", false).getBoolean();
        Configs.disableItemEnderLasso             = getProp("disableItemEnderLasso", false).getBoolean();
        Configs.disableItemEnderPearl             = getProp("disableItemEnderPearl", false).getBoolean();
        Configs.disableItemEnderPorter            = getProp("disableItemEnderPorter", false).getBoolean();
        Configs.disableItemEnderSword             = getProp("disableItemEnderSword", false).getBoolean();
        Configs.disableItemEnderTools             = getProp("disableItemEnderTools", false).getBoolean();
        Configs.disableItemHandyBag               = getProp("disableItemHandyBag", false).getBoolean();
        Configs.disableItemIceMelter              = getProp("disableItemIceMelter", false).getBoolean();
        Configs.disableItemInventorySwapper       = getProp("disableItemInventorySwapper", false).getBoolean();
        Configs.disableItemLivingManipulator      = getProp("disableItemLivingManipulator", false).getBoolean();
        Configs.disableItemMobHarness             = getProp("disableItemMobHarness", false).getBoolean();
        Configs.disableItemNullifier              = getProp("disableItemNullifier", false).getBoolean();
        Configs.disableItemPetContract            = getProp("disableItemPetContract", false).getBoolean();
        Configs.disableItemPickupManager          = getProp("disableItemPickupManager", false).getBoolean();
        Configs.disableItemQuickStacker           = getProp("disableItemQuickStacker", false).getBoolean();
        Configs.disableItemPortalScaler           = getProp("disableItemPortalScaler", false).getBoolean();
        Configs.disableItemRuler                  = getProp("disableItemRuler", false).getBoolean();
        Configs.disableItemSyringe                = getProp("disableItemSyringe", false).getBoolean();
        Configs.disableItemVoidPickaxe            = getProp("disableItemVoidPickaxe", false).getBoolean();

        // Recipe disable
        currentCategory = "DisableRecipes";
        conf.addCustomCategoryComment(currentCategory, "Disable item recipes");

        // Blocks
        Configs.disableRecipeEnderElevator        = getProp("disableRecipeEnderElevator", false).getBoolean();
    }

    private static void loadConfigLists(Configuration conf)
    {
        Property prop;

        currentRequiresMcRestart = false;
        currentCategory = CATEGORY_LISTS;

        prop = getProp("energyBridgeBedrockWhitelist", new String[] { "minecraft:bedrock" });
        prop.setComment("A list of blockstates that are valid for the Energy Bridge's Bedrock requirement.\n" +
                        "Use a blockstate string, for example: 'minecraft:gold_block' or 'minecraft:stone[variant=andesite]'.\n" +
                        "A block name without properties will match all states, for example: 'minecraft:stone'\n" +
                        "would match smooth stone and all the granite/andesite/diorite variants that are in the same block.");
        Configs.energyBridgeBedrockWhitelist = prop.getStringList();
        
        prop = getProp("endDimensions", new String[0]);
        prop.setComment("A list of dimension IDs that should be considered End dimensions, although they may otherwise not seem like it.\n" +
                        "This is mainly used for the Energy Bridges, to allow the Transmitters to work in custom End dimensions.");
        Configs.endDimensions = prop.getStringList();

        prop = getProp("enderBagListType", "whitelist");
        prop.setComment("Target control list type used for Ender Bag. Allowed values: blacklist, whitelist.");
        Configs.enderBagListTypeIsWhitelist = prop.getString().equalsIgnoreCase("whitelist");

        prop = getProp("enderBagBlackList", new String[0]);
        prop.setComment("Block types the Ender Bag is NOT allowed to (= doesn't properly) work with.");
        Configs.enderBagBlacklist = prop.getStringList();

        prop = getProp("enderBagWhiteList",
                new String[] {
                        "minecraft:chest",
                        "minecraft:dispenser",
                        "minecraft:dropper",
                        "minecraft:ender_chest",
                        "minecraft:furnace",
                        "minecraft:hopper",
                        "minecraft:trapped_chest"
                        });
        prop.setComment("Block types the Ender Bag is allowed to (= should properly) work with. **NOTE** Only some vanilla blocks work properly atm!!");
        Configs.enderBagWhitelist = prop.getStringList();

        prop = getProp("livingMatterManipulatorListType", "blacklist");
        prop.setComment("The list type used for the Living Matter Manipulator. Allowed values: blacklist, whitelist.");
        Configs.lmmListIsWhitelist = prop.getString().equalsIgnoreCase("whitelist");

        prop = getProp("livingMatterManipulatorBlackList",
                new String[] {
                        "minecraft:ender_dragon",
                        "minecraft:wither"
                        });
        prop.setComment("List of entity names the LMM is not allowed to store, if 'livingMatterManipulatorListType' is 'blacklist'.");
        Configs.lmmBlacklist = prop.getStringList();

        prop = getProp("livingMatterManipulatorWhiteList", new String[] {});
        prop.setComment("List of entity names the LMM is only allowed to store, if 'livingMatterManipulatorListType' is 'whitelist'.");
        Configs.lmmWhitelist = prop.getStringList();

        prop = getProp("teleportBlackList",
                new String[] {
                        "minecraft:ender_dragon",
                        "minecraft:ender_crystal",
                        "minecraft:wither"
                        });
        prop.setComment("Entities that are not allowed to be teleported using any methods");
        Configs.teleportBlacklist = prop.getStringList();

        BlackLists.registerEnergyBridgeBedrockWhitelist(Configs.energyBridgeBedrockWhitelist);
        BlackLists.registerEnderBagLists(Configs.enderBagBlacklist, Configs.enderBagWhitelist);
        BlackLists.registerTeleportBlacklist(Configs.teleportBlacklist);
        WorldUtils.setCustomEndDimensions(Configs.endDimensions);
    }

    private static Property getProp(String key, boolean defaultValue)
    {
        return getProp(currentCategory, key, defaultValue, currentRequiresMcRestart);
    }

    private static Property getProp(String category, String key, boolean defaultValue, boolean requiresMcRestart)
    {
        VALID_CATEGORIES.add(category);
        Property prop = config.get(category, key, defaultValue).setRequiresMcRestart(requiresMcRestart);
        VALID_CONFIGS.add(category + "_" + key);
        return prop;
    }

    private static Property getProp(String key, int defaultValue)
    {
        return getProp(currentCategory, key, defaultValue, currentRequiresMcRestart);
    }

    private static Property getProp(String category, String key, int defaultValue, boolean requiresMcRestart)
    {
        VALID_CATEGORIES.add(category);
        Property prop = config.get(category, key, defaultValue).setRequiresMcRestart(requiresMcRestart);
        VALID_CONFIGS.add(category + "_" + key);
        return prop;
    }

    private static Property getProp(String key, double defaultValue)
    {
        return getProp(currentCategory, key, defaultValue, currentRequiresMcRestart);
    }

    private static Property getProp(String category, String key, double defaultValue, boolean requiresMcRestart)
    {
        VALID_CATEGORIES.add(category);
        Property prop = config.get(category, key, defaultValue).setRequiresMcRestart(requiresMcRestart);
        VALID_CONFIGS.add(category + "_" + key);
        return prop;
    }

    private static Property getProp(String key, String defaultValue)
    {
        return getProp(currentCategory, key, defaultValue, currentRequiresMcRestart);
    }

    private static Property getProp(String category, String key, String defaultValue, boolean requiresMcRestart)
    {
        VALID_CATEGORIES.add(category);
        Property prop = config.get(category, key, defaultValue).setRequiresMcRestart(requiresMcRestart);
        VALID_CONFIGS.add(category + "_" + key);
        return prop;
    }

    private static Property getProp(String key, String[] defaultValue)
    {
        return getProp(currentCategory, key, defaultValue, currentRequiresMcRestart);
    }

    private static Property getProp(String category, String key, String[] defaultValue, boolean requiresMcRestart)
    {
        VALID_CATEGORIES.add(category);
        Property prop = config.get(category, key, defaultValue).setRequiresMcRestart(requiresMcRestart);
        VALID_CONFIGS.add(category + "_" + key);
        return prop;
    }

    private static void removeInvalidConfigs(Configuration conf)
    {
        List<String> categoriesToRemove = new ArrayList<String>();

        for (String category : conf.getCategoryNames())
        {
            if (VALID_CATEGORIES.contains(category) == false)
            {
                categoriesToRemove.add(category);
            }
            else
            {
                ConfigCategory cat = conf.getCategory(category);
                List<String> propsToRemove = new ArrayList<String>();

                for (String key : cat.keySet())
                {
                    if (VALID_CONFIGS.contains(category + "_" + key) == false)
                    {
                        propsToRemove.add(key);
                    }
                }

                for (String key : propsToRemove)
                {
                    cat.remove(key);
                }
            }
        }

        for (String category : categoriesToRemove)
        {
            conf.removeCategory(conf.getCategory(category));
        }
    }
}
