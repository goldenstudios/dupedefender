package com.tfaluc.dupedefender;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DupeCommand extends CommandBase {
    @Override
    public String getName() {
        return "dupedefender";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "dupedefender [ run / add [item] / remove [item] ]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("You must provide at least one argument"));
            return;
        }
        switch (args[0]) {
            case "run":
                DupeDefender.watchList.clear();
                for (World world : server.worlds) {
                    DupeManager.processWorld(world);
                }
                break;
            case "remove":
                if (args.length > 1) {
                    if (DupeDefender.toWatchList.contains(args[1])) {
                        DupeDefender.toWatchList.remove(args[1]);
                        sender.sendMessage(new TextComponentString("Removed '" + args[1] + "' from toWatchList"));
                    } else sender.sendMessage(new TextComponentString("Specified item does not exist in toWatchList"));
                }
                break;
            case "add":
                if (args.length > 1) {
                    if (DupeDefender.toWatchList.contains(args[1])) {
                        sender.sendMessage(new TextComponentString("Specified item already exists in toWatchList"));
                    } else {
                        DupeDefender.toWatchList.add(args[1]);
                        sender.sendMessage(new TextComponentString("Added '" + args[1] + "' to toWatchList"));
                    }
                }
                break;
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
