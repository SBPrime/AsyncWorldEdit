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

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.*;
import org.primesoft.asyncworldedit.Permission;
import org.primesoft.asyncworldedit.PermissionManager;

/**
 *
 * @author SBPrime
 */
public class ToggleCommand {
    public static void Execte(PluginMain sender, Player player, String[] args) {
        if (args.length < 1 || args.length > 3) {
            Help.ShowHelp(player, Commands.COMMAND_TOGGLE);
            return;
        }

        PlayerManager manager = sender.getPlayerManager();
        PlayerWrapper wrapper = null;
        boolean mode;

        if (args.length == 1) {
            if (player == null) {
                PluginMain.say(player, ChatColor.RED + "Command available ingame.");
                return;
            }
            if (!PermissionManager.isAllowed(player, Permission.Mode_Change)) {
                PluginMain.say(player, ChatColor.RED + "You have no permissions to do that.");
                return;
            }

            wrapper = manager.getPlayer(player.getUniqueId());
            if (wrapper == null) {
                return;
            }
            mode = !wrapper.getMode();
        } else {
            String arg = args[1];
            if (arg.startsWith("u:")) {
                if (!PermissionManager.isAllowed(player, Permission.Mode_Change_Other)) {
                    PluginMain.say(player, ChatColor.RED + "You have no permissions to do that.");
                    return;
                }

                String name = arg.substring(2);
                UUID uuid = sender.getPlayerManager().getPlayerUUID(name);
                if (uuid.equals(ConfigProvider.DEFAULT_USER)) {
                    PluginMain.say(player, ChatColor.RED + "Player not found.");
                    return;
                }
                wrapper = manager.getPlayer(uuid);
                if (wrapper == null) {
                    PluginMain.say(player, ChatColor.RED + "Player " + ChatColor.WHITE + name + ChatColor.RED + " not found.");
                    return;
                }

                if (args.length == 3) {
                    arg = args[2];
                    if (arg.equalsIgnoreCase("on")) {
                        mode = true;
                    } else if (arg.equalsIgnoreCase("off")) {
                        mode = false;
                    } else {
                        Help.ShowHelp(player, Commands.COMMAND_TOGGLE);
                        return;
                    }
                } else {
                    mode = !wrapper.getMode();
                }
            } else {
                if (player == null) {
                    PluginMain.say(player, ChatColor.RED + "Command available ingame.");
                    return;
                }
                if (!PermissionManager.isAllowed(player, Permission.Mode_Change)) {
                    PluginMain.say(player, ChatColor.RED + "You have no permissions to do that.");
                    return;
                }
                if (arg.equalsIgnoreCase("on")) {
                    mode = true;
                } else if (arg.equalsIgnoreCase("off")) {
                    mode = false;
                } else {
                    Help.ShowHelp(player, Commands.COMMAND_TOGGLE);
                    return;
                }
                wrapper = manager.getPlayer(player.getUniqueId());
            }
        }

        wrapper.setMode(mode);
        PluginMain.say(player, "AsyncWorldEdit: " + (wrapper.getMode() ? "on" : "off"));
    }
}
