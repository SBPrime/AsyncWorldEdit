/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.Help;
import org.primesoft.asyncworldedit.PlayerManager;
import org.primesoft.asyncworldedit.PlayerWrapper;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.blockPlacer.PlayerEntry;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.permissions.PermissionManager;

/**
 *
 * @author SBPrime
 */
public class JobsCommand {

    private final static int MAX_LINES = 6;

    public static void Execte(AsyncWorldEditMain sender, Player player, String[] args) {
        final List<String> lines = new ArrayList<String>();
        if (args.length < 1 || args.length > 3) {
            Help.ShowHelp(player, Commands.COMMAND_JOBS);
            return;
        }

        BlockPlacer bPlacer = sender.getBlockPlacer();
        String playerName = null;
        Permission perm = Permission.JOBS_ALL;
        boolean onlyInGame = false;
        boolean all = false;
        int page = -1;
        int len = args.length;

        switch (len) {
            case 1: {
                playerName = player != null ? player.getName() : null;
                perm = Permission.JOBS_SELF;
                onlyInGame = true;
                all = false;
                page = -1;
                break;
            }
            case 2: {
                if (args[1].startsWith("u:")) {
                    playerName = args[1].substring(2);
                    perm = Permission.JOBS_OTHER;
                    onlyInGame = false;
                    all = false;
                    page = -1;
                } else if (args[1].equalsIgnoreCase("all")) {
                    playerName = null;
                    perm = Permission.JOBS_ALL;
                    onlyInGame = false;
                    all = true;
                    page = -1;
                } else {
                    try {
                        playerName = player != null ? player.getName() : null;
                        perm = Permission.JOBS_SELF;
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
                    perm = Permission.JOBS_OTHER;
                    onlyInGame = false;
                    all = false;
                } else if (args[1].equalsIgnoreCase("all")) {
                    playerName = null;
                    perm = Permission.JOBS_ALL;
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
            AsyncWorldEditMain.say(player, ChatColor.RED + "Command available ingame.");
            return;
        }
        if (!PermissionManager.isAllowed(player, perm)) {
            AsyncWorldEditMain.say(player, ChatColor.RED + "You have no permissions to do that.");
            return;
        }
        
        final PlayerManager pm = sender.getPlayerManager();
        if (!all) {            
            UUID playerUuid = pm.getPlayerUUID(playerName);
            
            switch (perm) {
                case JOBS_SELF:
                    lines.add(ChatColor.YELLOW + "You have " + bPlacer.getPlayerMessage(playerUuid));
                    break;
                case JOBS_OTHER:
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
            AsyncWorldEditMain.say(player, "page " + page + " of " + maxPages);
        }
    }

    private static void say(Player player, String[] lines, int from, int to) {
        from = Math.max(from, 0);
        to = Math.min(to, lines.length);
        for (int i = from; i < to; i++) {
            AsyncWorldEditMain.say(player, lines[i]);
        }
    }
}
