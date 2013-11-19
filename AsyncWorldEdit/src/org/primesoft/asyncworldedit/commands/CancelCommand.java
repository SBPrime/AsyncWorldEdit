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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.Help;
import org.primesoft.asyncworldedit.PermissionManager;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.PlayerEntry;

/**
 *
 * @author SBPrime
 */
public class CancelCommand {

    public static void Execte(PluginMain sender, Player player, String[] args) {
        if (args.length < 2 || args.length > 3) {
            Help.ShowHelp(player, Commands.COMMAND_CANCEL);
            return;
        }

        BlockPlacer bPlacer = sender.getBlockPlacer();
        int id;
        String name;
        if (args.length == 2) {
            if (!PermissionManager.isAllowed(player, PermissionManager.Perms.Cancel_Self)) {
                PluginMain.say(player, ChatColor.RED + "You have no permissions to do that.");
                return;
            }
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                PluginMain.say(player, ChatColor.RED + "Number expected.");
                return;
            }

            name = player.getName();            
        } else {
            String arg = args[1];
            if (arg.startsWith("u:")) {
                if (!PermissionManager.isAllowed(player, PermissionManager.Perms.Cancel_Other)) {
                    PluginMain.say(player, ChatColor.RED + "You have no permissions to do that.");
                    return;
                }

                name = arg.substring(2);
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    PluginMain.say(player, ChatColor.RED + "Number expected.");
                    return;
                }                                
            } else {
                Help.ShowHelp(player, Commands.COMMAND_JOBS);
                return;

            }
        }
        int size = sender.getBlockPlacer().cancelJob(name, id);
        PluginMain.say(player, "" + ChatColor.WHITE + size + ChatColor.YELLOW + " queue entries removed.");            
    }
}