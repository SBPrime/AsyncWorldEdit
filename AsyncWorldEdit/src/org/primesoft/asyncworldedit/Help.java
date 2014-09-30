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
        ChatColor.BLUE + "Cancel" + ChatColor.WHITE + " - cancel queued job",
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
        ChatColor.BLUE + " Jobs [page]" + ChatColor.WHITE + " - displays your operations",
        ChatColor.BLUE + " Jobs <u:playerName> [page]" + ChatColor.WHITE + " - displays other player operations",
        ChatColor.BLUE + " Jobs all [page]" + ChatColor.WHITE + " - displays all queued operations",};
    private final static String[] HelpCancel = new String[]{
        ChatColor.YELLOW + "Cancel " + ChatColor.WHITE + " - cancel queued job",        
        ChatColor.BLUE + " Cancel #id" + ChatColor.WHITE + " - cancel your job",
        ChatColor.BLUE + " Cancel <u:playerName> #id" + ChatColor.WHITE + " - cancel other player job",};
    private final static String[] HelpReload = new String[]{
        ChatColor.YELLOW + "Reload " + ChatColor.WHITE + " - Reload AWE configuration file",};

    public static boolean ShowHelp(Player player, String command) {
        String[] help = HelpGlobal;

        if (command != null) {
            if (command.equalsIgnoreCase(Commands.COMMAND_PURGE)) {
                help = HelpPurge;
            } else if (command.equalsIgnoreCase(Commands.COMMAND_JOBS)) {
                help = HelpJobs;
            } else if (command.equalsIgnoreCase(Commands.COMMAND_TOGGLE)) {
                help = HelpToggle;
            } else if (command.equalsIgnoreCase(Commands.COMMAND_RELOAD)) {
                help = HelpReload;
            } else if (command.equalsIgnoreCase(Commands.COMMAND_CANCEL)) {
                help = HelpCancel;
            }
        }

        for (String string : help) {
            AsyncWorldEditMain.say(player, string);
        }

        return true;
    }
}