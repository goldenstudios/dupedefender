package com.tfaluc.dupedefender;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.tfaluc.dupedefender.DupeDefender.*;

@Mod.EventBusSubscriber(modid = DupeDefender.MODID)
public class DupeManager {
    public static final String NBTKey = "dupedefender_uuid";

    public static void processWorld(World world) {
        for (EntityPlayer player : world.playerEntities) {
            processPlayer(player);
        }
    }

    public static void processPlayer(EntityPlayer player) {
        InventoryPlayer inven = player.inventory;
        for (int slot = 0; slot < inven.getSizeInventory(); slot++) {
            inven.setInventorySlotContents(slot, processItem(inven.getStackInSlot(slot)));
        }
    }

    public static UUID genUUID(String itemName) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (watchList.getOrDefault(itemName, new HashSet<>()).contains(id));
        return id;
    }

    @SuppressWarnings("ConstantConditions")
    public static ItemStack processItem(ItemStack itemStack) {
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
                NBTTagCompound dd = new NBTTagCompound();
                UUID uuid = genUUID(itemName);
                itemTag.setUniqueId(NBTKey, uuid);
            }
            UUID uuid = itemTag.getUniqueId(NBTKey);
            if (uuidSet.contains(uuid)) {
                logger.debug("  Item already present on watch list. Uh oh.");
                // TODO: ADD CODE TO ALERT STAFF
            } else {
                uuidSet.add(uuid);
                watchList.put(itemName, uuidSet);
                logger.debug("  Added item to watchlist with the UUID of " + uuid);
            }
        }
        return itemStack;
    }
}