package com.tfaluc.dupedefender;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod(modid = DupeDefender.MODID, name = DupeDefender.NAME, version = DupeDefender.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class DupeDefender {
    public DupeDefender() {
    }

    public static final String MODID = "dupedefender";
    public static final String NAME = "Dupe Defender";
    public static final String VERSION = "1.0";

    public static Logger logger;
    private static Configuration config;
    public static List<String> toWatchList = new ArrayList<>();

    public static Map<String, Set<UUID>> watchList = new HashMap<>();

    public void loadWatchList() {
        ConfigCategory c = config.getCategory("general");
        if (c.containsKey("watchlist")) {
            toWatchList.addAll(Arrays.asList(c.get("watchlist").getString().split("\\|")));
        } else toWatchList = Collections.emptyList();
        while (toWatchList.contains(""))
            toWatchList.remove("");
        logger.info(String.format("Reloaded Watch List : %d Entries", toWatchList.size()));
    }

    public void saveWatchList() {
        ConfigCategory c = config.getCategory("general");
        StringBuilder prop = new StringBuilder();
        for (String s : toWatchList) {
            prop.append(s);
            prop.append("|");
        }
        Property p = new Property("watchlist", prop.toString(), Property.Type.STRING);
        c.put("watchlist", p);
        config.save();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
        loadWatchList();
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        saveWatchList();
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new DupeCommand());
    }
}
