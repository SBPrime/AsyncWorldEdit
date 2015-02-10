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

import org.bukkit.ChatColor;
import org.primesoft.asyncworldedit.Help;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 *
 * @author SBPrime
 */
public class PurgeCommand {

    public static void Execte(AsyncWorldEditMain sender, PlayerEntry player, String[] args) {
        if (args.length < 1 || args.length > 2) {
            Help.ShowHelp(player, Commands.COMMAND_PURGE);
            return;
        }

        if (args.length == 1) {
            if (!player.isInGame()) {
                player.say(MessageType.INGAME.format());
                return;
            }
            if (!player.isAllowed(Permission.PURGE_SELF)) {
                player.say(MessageType.NO_PERMS.format());
                return;
            }

            int size = sender.getBlockPlacer().purge(player);
            player.say(MessageType.CMD_PURGE_REMOVED.format(Integer.toString(size)));
        } else {
            String arg = args[1];
            if (arg.startsWith("u:")) {
                if (!player.isAllowed(Permission.PURGE_OTHER)) {
                    player.say(MessageType.NO_PERMS.format());
                    return;
                }

                
                String name = arg.substring(2);
                PlayerEntry playerEntry = sender.getPlayerManager().getPlayer(name);
                if (!playerEntry.isPlayer()) {
                    player.say(MessageType.PLAYER_NOT_FOUND.format());
                    return;
                }
                int size = sender.getBlockPlacer().purge(playerEntry);
                player.say(MessageType.CMD_PURGE_REMOVED.format(Integer.toString(size)));
            } else {
                if (!arg.equalsIgnoreCase("all")) {
                    Help.ShowHelp(player, Commands.COMMAND_PURGE);
                    return;
                }

                if (!player.isAllowed(Permission.PURGE_ALL)) {
                    player.say(MessageType.NO_PERMS.format());
                    return;
                }

                int size = sender.getBlockPlacer().purgeAll();
                player.say(MessageType.CMD_PURGE_REMOVED.format(Integer.toString(size)));
            }
        }
    }
}
