package com.tfaluc.dupedefender;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.tfaluc.dupedefender.DupeDefender.*;

@Mod.EventBusSubscriber(modid = DupeDefender.MODID)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DupeManager {
    public static final String NBTKey = "dupedefender_uuid";

    /**
     * When a player joins process their inventory and add watched items to the watchlist.
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        processPlayer(event.player);
    }

    /**
     * Handles PlayerEvent.ItemPickupEvent
     * <p>
     * If the itemstack is still in the player's inventory we process it and then replace it.
     */
    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        InventoryPlayer inven = event.player.inventory;
        int slot;
        if ((slot = inven.findSlotMatchingUnusedItem(event.getStack())) != -1) {
            inven.setInventorySlotContents(slot, processItem(inven.getStackInSlot(slot), event.player));
        }
    }

    /**
     * Handles ItemTossEvent
     * <p>
     * When the itemstack is tossed out of the inventory we remove it from the watchList
     * to prevent a false positive when/if it gets picked back up.
     */
    @SubscribeEvent
    @SuppressWarnings("ConstantConditions")
    public static void onItemThrown(ItemTossEvent event) {
        ItemStack item = event.getEntityItem().getItem();
        NBTTagCompound tag = item.getTagCompound();
        if (tag == null) return;
        try {
            if (tag.hasUniqueId(NBTKey)) {
                watchList.getOrDefault(item.getItem().getRegistryName().toString(), new HashSet<>())
                        .remove(tag.getUniqueId(NBTKey));
                logger.debug("Removed dropped item from watchList with the UUID of " + tag.getUniqueId(NBTKey));
            }
        } catch (NullPointerException e) {
            logger.warn("NullPointer exception when trying to process a thrown item, this shouldn't be possible", e);
        }
    }

    public static void processWorld(World world) {
        for (EntityPlayer player : world.playerEntities) {
            processPlayer(player);
        }
    }

    public static void processPlayer(EntityPlayer player) {
        InventoryPlayer inven = player.inventory;
        for (int slot = 0; slot < inven.getSizeInventory(); slot++) {
            inven.setInventorySlotContents(slot, processItem(inven.getStackInSlot(slot), player));
        }
    }

    public static UUID genUUID(String itemName) {
        // synchronization shouldn't be needed, but this is to prevent edge case scenarios
        synchronized (usedUUIDs) {
            UUID id;
            Set<UUID> existing = usedUUIDs.getOrDefault(itemName, new HashSet<>());
            do {
                id = UUID.randomUUID();
            } while (existing.contains(id));
            existing.add(id);
            usedUUIDs.put(itemName, existing);
            return id;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static ItemStack processItem(ItemStack itemStack, EntityPlayer itemStackOwner) {
        if (itemStack == ItemStack.EMPTY) return itemStack;
        String itemName;
        try {
            itemName = itemStack.getItem().getRegistryName().toString();
        } catch (NullPointerException e) {
            logger.warn("Attempted to call processItem on an item with no registry name", e);
            return itemStack;
        }
        logger.debug("Checking item: " + itemName);
        if (toWatchList.contains(itemName)) {
            logger.debug("  Item on toWatchList!");
            Set<UUID> uuidSet = watchList.getOrDefault(itemName, new HashSet<>());
            if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
            NBTTagCompound itemTag = itemStack.getTagCompound();
            if (!itemTag.hasUniqueId(NBTKey)) {
                logger.debug("  Item did not have UUID assigned, assigned new UUID.");
                UUID uuid = genUUID(itemName);
                itemTag.setUniqueId(NBTKey, uuid);
            }
            UUID uuid = itemTag.getUniqueId(NBTKey);
            if (uuidSet.contains(uuid)) {
                logger.debug("  Item already present on watch list. Uh oh.");
                // TODO: ALERT WATCHERS OF THE itemStack AND ALSO itemStackOwner
            } else {
                uuidSet.add(uuid);
                watchList.put(itemName, uuidSet);
                logger.debug("  Added item to watchlist with the UUID of " + uuid);
            }
        }
        return itemStack;
    }
}