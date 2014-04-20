/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.Help;
import org.primesoft.asyncworldedit.PermissionManager;
import org.primesoft.asyncworldedit.PlayerManager;
import org.primesoft.asyncworldedit.PlayerWrapper;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.PlayerEntry;

/**
 *
 * @author SBPrime
 */
public class JobsCommand {

    private final static int MAX_LINES = 6;

    public static void Execte(PluginMain sender, Player player, String[] args) {
        final List<String> lines = new ArrayList<String>();
        if (args.length < 1 || args.length > 3) {
            Help.ShowHelp(player, Commands.COMMAND_JOBS);
            return;
        }

        BlockPlacer bPlacer = sender.getBlockPlacer();
        String playerName = null;
        PermissionManager.Perms perm = PermissionManager.Perms.Jobs_All;
        boolean onlyInGame = false;
        boolean all = false;
        int page = -1;
        int len = args.length;

        switch (len) {
            case 1: {
                playerName = player != null ? player.getName() : null;
                perm = PermissionManager.Perms.Jobs_Self;
                onlyInGame = true;
                all = false;
                page = -1;
                break;
            }
            case 2: {
                if (args[1].startsWith("u:")) {
                    playerName = args[1].substring(2);
                    perm = PermissionManager.Perms.Jobs_Other;
                    onlyInGame = false;
                    all = false;
                    page = -1;
                } else if (args[1].equalsIgnoreCase("all")) {
                    playerName = null;
                    perm = PermissionManager.Perms.Jobs_All;
                    onlyInGame = false;
                    all = true;
                    page = -1;
                } else {
                    try {
                        playerName = player != null ? player.getName() : null;
                        perm = PermissionManager.Perms.Jobs_Self;
                        onlyInGame = true;
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        Help.ShowHelp(player, Commands.COMMAND_JOBS);
                        return;
                    }
                }
                break;
            }
            case 3: {
                if (args[1].startsWith("u:")) {
                    playerName = args[1].substring(2);
                    perm = PermissionManager.Perms.Jobs_Other;
                    onlyInGame = false;
                    all = false;
                } else if (args[1].equalsIgnoreCase("all")) {
                    playerName = null;
                    perm = PermissionManager.Perms.Jobs_All;
                    onlyInGame = false;
                    all = true;
                } else {
                    Help.ShowHelp(player, Commands.COMMAND_JOBS);
                    return;
                }

                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    Help.ShowHelp(player, Commands.COMMAND_JOBS);
                    return;
                }
                break;
            }
        }

        if (playerName == null && !all) {
            Help.ShowHelp(player, Commands.COMMAND_JOBS);
            return;
        }
        if (onlyInGame && player == null) {
            PluginMain.say(player, ChatColor.RED + "Command available ingame.");
            return;
        }
        if (!PermissionManager.isAllowed(player, perm)) {
            PluginMain.say(player, ChatColor.RED + "You have no permissions to do that.");
            return;
        }
        
        final PlayerManager pm = sender.getPlayerManager();
        if (!all) {            
            UUID playerUuid = pm.getPlayerUUID(playerName);
            
            switch (perm) {
                case Jobs_Self:
                    lines.add(ChatColor.YELLOW + "You have " + bPlacer.getPlayerMessage(playerUuid));
                    break;
                case Jobs_Other:
                    lines.add(ChatColor.YELLOW + "Player " + ChatColor.WHITE
                            + playerName + ChatColor.YELLOW + " has " + bPlacer.getPlayerMessage(playerUuid));
                    break;
            }
            PlayerEntry entry = bPlacer.getPlayerEvents(playerUuid);
            if (entry != null) {
                entry.printJobs(lines);
            }
        } else {
            UUID[] users = bPlacer.getAllPlayers();
            if (users.length == 0) {
                lines.add(ChatColor.YELLOW + "No operations queued.");
            } else {
                for (UUID user : users) {
                    PlayerEntry entry = bPlacer.getPlayerEvents(user);
                    int cnt = entry != null ? entry.getQueue().size() : 0;
                    PlayerWrapper pw = pm.getPlayer(user);
                    String name = pw != null ? pw.getName() : user.toString();
                    lines.add(ChatColor.YELLOW + "Player " + ChatColor.WHITE
                            + name + ChatColor.YELLOW + " has " + ChatColor.WHITE + cnt
                            + ChatColor.YELLOW + " block operations queued.");
                    if (entry != null) {
                        entry.printJobs(lines);
                    }
                }
            }
        }

        String[] l = lines.toArray(new String[0]);
        if (l.length <= MAX_LINES) {
            say(player, l, 0, l.length);
        } else {
            if (page < 1) {
                page = 1;
            }

            int maxPages = l.length / MAX_LINES + 1;
            say(player, l, (page - 1) * MAX_LINES, page * MAX_LINES);
            PluginMain.say(player, "page " + page + " of " + maxPages);
        }
    }

    private static void say(Player player, String[] lines, int from, int to) {
        from = Math.max(from, 0);
        to = Math.min(to, lines.length);
        for (int i = from; i < to; i++) {
            PluginMain.say(player, lines[i]);
        }
    }
}
