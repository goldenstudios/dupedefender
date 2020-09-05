package com.tfaluc.dupedefender;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

import static com.tfaluc.dupedefender.DupeDefender.*;

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
                watchList.clear();
                for (World world : server.worlds) {
                    DupeManager.processWorld(world);
                }
                break;
            case "remove":
                if (args.length > 1) {
                    if (toWatchList.contains(args[1])) {
                        toWatchList.remove(args[1]);
                        sender.sendMessage(new TextComponentString("Removed '" + args[1] + "' from toWatchList"));
                    } else sender.sendMessage(new TextComponentString("Specified item does not exist in toWatchList"));
                }
                break;
            case "add":
                if (args.length > 1) {
                    if (toWatchList.contains(args[1])) {
                        sender.sendMessage(new TextComponentString("Specified item already exists in toWatchList"));
                    } else {
                        toWatchList.add(args[1]);
                        sender.sendMessage(new TextComponentString("Added '" + args[1] + "' to toWatchList"));
                    }
                }
                break;
            case "watch":
                if (sender.getCommandSenderEntity() instanceof EntityPlayer) {
                    UUID sUUID = getCommandSenderAsPlayer(sender).getUniqueID();
                    if (watchers.contains(sUUID)) {
                        sender.sendMessage(new TextComponentString("You are already a watcher!"));
                        return;
                    }
                    watchers.add(sUUID);
                    sender.sendMessage(new TextComponentString("You are now a watcher!"));
                } else {
                    logger.warn("Attempt to call 'watch' on a non player entity, command block?");
                }
                break;
            default:
                sender.sendMessage(new TextComponentString("Unknown argument."));
                break;
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
