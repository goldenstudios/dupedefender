package com.tfaluc.dupedefender;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Mod(modid = DupeDefender.MODID, name = DupeDefender.NAME, version = DupeDefender.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class DupeDefender {
    public DupeDefender() {
    }

    public static final String MODID = "dupedefender";
    public static final String NAME = "Dupe Defender";
    public static final String VERSION = "1.0";

    public static Logger logger;
    private static Configuration config;
    public static final Set<String> toWatchList = new HashSet<>();
    public static final Set<UUID> watchers = new HashSet<>();
    public static final Map<String, Set<UUID>> watchList = new HashMap<>();
    public static final Map<String, Set<UUID>> usedUUIDs = new HashMap<>();

    @SideOnly(Side.SERVER)
    public static MinecraftServer MCServer;

    public void loadConfig() {
        ConfigCategory c = config.getCategory("general");
        if (c.containsKey("watchers"))
            watchers.addAll(Arrays.stream(c.get("watchers").getString().split("\\|"))
                    .filter(s -> !s.equals("")).map(UUID::fromString).collect(Collectors.toSet()));
        c = config.getCategory("watchlist");
        if (c.containsKey("watch"))
            toWatchList.addAll(Arrays.stream(c.get("watch").getString().split("\\|"))
                    .filter(s -> !s.equals("")).collect(Collectors.toSet()));
        for (String name : c.keySet()) {
            if(name.equals("watch")) continue;
            Set<UUID> used = Arrays.stream(c.get(name).getString().split("\\|"))
                    .filter(s -> !s.equals("")).map(UUID::fromString).collect(Collectors.toSet());
            usedUUIDs.put(name, used);
        }
        logger.info(String.format("Reloaded watchList : %d Entries", toWatchList.size()));
        logger.info(String.format("Reloaded usedUUIDs : %d Entries", usedUUIDs.size()));
    }

    public void saveConfig() {
        ConfigCategory c = config.getCategory("watchlist");
        StringBuilder prop = new StringBuilder();
        for (String s : toWatchList) {
            prop.append(s);
            prop.append("|");
        }
        c.put("watch", new Property("watch", prop.toString(), Property.Type.STRING));
        for (Map.Entry<String, Set<UUID>> entry : usedUUIDs.entrySet()) {
            prop.setLength(0);
            for (UUID id : entry.getValue()) {
                prop.append(id.toString());
                prop.append("|");
            }
            c.put(entry.getKey(), new Property(entry.getKey(), prop.toString(), Property.Type.STRING));
        }
        c = config.getCategory("general");
        prop.setLength(0);
        for (UUID s : watchers) {
            prop.append(s.toString());
            prop.append("|");
        }
        c.put("watchers", new Property("watchers", prop.toString(), Property.Type.STRING));
        config.save();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        saveConfig();
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new DupeCommand());
        MCServer = event.getServer();
    }
}
