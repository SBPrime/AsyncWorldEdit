/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2017, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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
package org.primesoft.asyncworldedit.excommands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.ALL;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import java.io.File;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.injector.classfactory.IJob;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;

/**
 *
 * @author SBPrime
 */
public class ScriptingCommands {
    private final WorldEdit m_worldEdit;
    
    public ScriptingCommands(WorldEdit worldEdit) {
        m_worldEdit = worldEdit;
    }
    
    @Command(
        aliases = { "cs" },
        usage = "<filename> [args...]",
        desc = "Execute a CraftScript",
        min = 1,
        max = -1
    )
    @CommandPermissions("worldedit.scripting.execute")
    @Logging(ALL)
    public void execute(final Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        final String[] scriptArgs = args.getSlice(1);
        String name = args.getString(0);

        if (!player.hasPermission("worldedit.scripting.execute." + name)) {
            player.printError("You don't have permission to use that script.");
            return;
        }

        session.setLastScript(name);

        File dir = m_worldEdit.getWorkingDirectoryFile(m_worldEdit.getConfiguration().scriptsDir);
        final File f = m_worldEdit.getSafeOpenFile(player, dir, name, "js", "js");

        InjectorCore.getInstance().getClassFactory().getJobProcessor().executeJob(player, new IJob() {
            @Override
            public String getName() {
                return "craftScript";
            }

            @Override
            public void execute() {
                try {
                    m_worldEdit.runScript(player, f, scriptArgs);
                } catch (WorldEditException ex) {
                    player.printError("Error while executing CraftScript.");
                }
            }
        });
    }
    
    @Command(
        aliases = { ".s" },
        usage = "[args...]",
        desc = "Execute last CraftScript",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.scripting.execute")
    @Logging(ALL)
    public void executeLast(final Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        
        String lastScript = session.getLastScript();

        if (!player.hasPermission("worldedit.scripting.execute." + lastScript)) {
            player.printError("You don't have permission to use that script.");
            return;
        }

        if (lastScript == null) {
            player.printError("Use /cs with a script name first.");
            return;
        }

        final String[] scriptArgs = args.getSlice(0);

        File dir = m_worldEdit.getWorkingDirectoryFile(m_worldEdit.getConfiguration().scriptsDir);
        final File f = m_worldEdit.getSafeOpenFile(player, dir, lastScript, "js", "js");

        InjectorCore.getInstance().getClassFactory().getJobProcessor().executeJob(player, new IJob() {
            @Override
            public String getName() {
                return "craftScript";
            }

            @Override
            public void execute() {
                try {
                    m_worldEdit.runScript(player, f, scriptArgs);
                } catch (WorldEditException ex) {
                    player.printError("Error while executing CraftScript.");
                }
            }
        });
    }
}
