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
package org.primesoft.asyncworldedit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.commands.Commands;

/**
 *
 * @author SBPrime
 */
public final class Help {

    private final static String[] HelpGlobal = new String[]{
        ChatColor.YELLOW + "Async WorldEdit help:\n",
        ChatColor.BLUE + "Help" + ChatColor.WHITE + " - diaplay help screen",
        ChatColor.BLUE + "Jobs" + ChatColor.WHITE + " - display queued block operations",
        ChatColor.BLUE + "Purge" + ChatColor.WHITE + " - remove all queued block operations",
        ChatColor.BLUE + "Toggle" + ChatColor.WHITE + " - toggle AsyncWorldEdit on/off",
        ChatColor.BLUE + "Reload" + ChatColor.WHITE + " - reload configuration",
        ChatColor.YELLOW + "To display help on command use: " + ChatColor.BLUE + "/Help <command>"
    };
    private final static String[] HelpToggle = new String[]{
        ChatColor.YELLOW + "Toggle " + ChatColor.WHITE + " - toggle AsyncWorldEdit on/off",
        ChatColor.BLUE + " Toggle" + ChatColor.WHITE + " - toggle AsyncWorldEdit",
        ChatColor.BLUE + " Toggle on" + ChatColor.WHITE + " - toggle AsyncWorldEdit on",
        ChatColor.BLUE + " Toggle off" + ChatColor.WHITE + " - toggle AsyncWorldEdit off",
        ChatColor.BLUE + " Toggle <u:playerName> " + ChatColor.WHITE + " - toggle AsyncWorldEdit",
        ChatColor.BLUE + " Toggle <u:playerName> on" + ChatColor.WHITE + " - toggle AsyncWorldEdit on",
        ChatColor.BLUE + " Toggle <u:playerName> off" + ChatColor.WHITE + " - toggle AsyncWorldEdit off",};
    private final static String[] HelpPurge = new String[]{
        ChatColor.YELLOW + "Purge " + ChatColor.WHITE + " - remove all queued block operations",
        ChatColor.BLUE + " Purge" + ChatColor.WHITE + " - purges your operations",
        ChatColor.BLUE + " Purge <u:playerName>" + ChatColor.WHITE + " - purges other player operations",
        ChatColor.BLUE + " Purge all" + ChatColor.WHITE + " - purges all operations",};
    private final static String[] HelpJobs = new String[]{
        ChatColor.YELLOW + "Jobs " + ChatColor.WHITE + " - display queued block operations",
        ChatColor.BLUE + " Jobs" + ChatColor.WHITE + " - displays your operations",
        ChatColor.BLUE + " Jobs <u:playerName>" + ChatColor.WHITE + " - displays other player operations",
        ChatColor.BLUE + " Jobs all" + ChatColor.WHITE + " - displays all queued operations",};

    public static boolean ShowHelp(Player player, String command) {
        String[] help = HelpGlobal;

        if (command != null) {
            if (command.equalsIgnoreCase(Commands.COMMAND_PURGE)) {
                help = HelpPurge;
            } else if (command.equalsIgnoreCase(Commands.COMMAND_JOBS)) {
                help = HelpJobs;
            } else if (command.equalsIgnoreCase(Commands.COMMAND_TOGGLE)) {
                help = HelpToggle;
            }
        }

        for (String string : help) {
            PluginMain.Say(player, string);
        }

        return true;
    }
}