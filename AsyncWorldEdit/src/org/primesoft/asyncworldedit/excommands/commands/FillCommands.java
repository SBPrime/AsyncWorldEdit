/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.excommands.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Patterns;
import com.sk89q.worldedit.patterns.Pattern;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.excommands.AsyncCommand;
import org.primesoft.asyncworldedit.excommands.FillCommand;

/**
 * The AsyncWorldEdit additional fill commands
 * @author SBPrime
 */
public class FillCommands {
    
    /**
     * Instance of WorldEdit
     */
    private final WorldEdit m_worldEdit;

    /**
     * The AsyncWorldEdit
     */
    private final IAsyncWorldEdit m_asyncWorldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     * @param awe the AsyncWorldEdit
     */
    public FillCommands(WorldEdit worldEdit, IAsyncWorldEdit awe) {
        if (worldEdit == null) {
            throw new NullPointerException("worldEdit");
        }

        m_asyncWorldEdit = awe;
        m_worldEdit = worldEdit;
    }
    
    /**
     *
     * @param player
     * @param session
     * @param editSession
     * @param args
     * @throws WorldEditException
     */
    @Command(
            aliases = {"/fillxz", "/fillzx"},
            usage = "<block> <radius> [width]",
        desc = "Fill a hole",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.fill")
    public void fillxz(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        fill(player, session, editSession, args, true, false, true);
    }
    
    /**
     *
     * @param player
     * @param session
     * @param editSession
     * @param args
     * @throws WorldEditException
     */
    @Command(
            aliases = {"/fillxy", "/fillyx"},
            usage = "<block> <radius> [width]",
        desc = "Fill a hole",
        min = 2,
        max = 3
    )
    @CommandPermissions("awe.excommands.fill.fillxy")
    public void fillxy(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        fill(player, session, editSession, args, true, true, false);
    }
    
    /**
     *
     * @param player
     * @param session
     * @param editSession
     * @param args
     * @throws WorldEditException
     */
    @Command(
            aliases = {"/fillzy", "/fillyz"},
            usage = "<block> <radius> [width]",
        desc = "Fill a hole",
        min = 2,
        max = 3
    )
    @CommandPermissions("awe.excommands.fill.fillyz")
    public void fillyz(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        fill(player, session, editSession, args, false, true, true);
    }
    
        /**
     *
     * @param player
     * @param session
     * @param editSession
     * @param args
     * @throws WorldEditException
     */
    @Command(
            aliases = {"/fill3d", "/fillxyz", "/fillxzy", "/fillyxz", "/fillyzx", "/fillzxy", "/fillzyx"},
            usage = "<block> <radius> [width]",
        desc = "Fill a hole",
        min = 2,
        max = 3
    )
    @CommandPermissions("awe.excommands.fill.fillyz")
    public void fill3d(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        fill(player, session, editSession, args, true, true, true);
    }

    /**
     * 
     * @param player
     * @param session
     * @param editSession
     * @param args
     * @param axisX
     * @param axisY
     * @param axisZ 
     */
    private void fill(Player player, LocalSession session, EditSession editSession, CommandContext args, 
            boolean axisX, boolean axisY, boolean axisZ) throws WorldEditException {
        Pattern pattern = m_worldEdit.getBlockPattern(player, args.getString(0));
        double radius = Math.max(1, args.getDouble(1));
        m_worldEdit.checkMaxRadius(radius);
        int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : 1;

        Vector pos = session.getPlacementPosition(player);

        IBlockPlacer bp = m_asyncWorldEdit.getBlockPlacer();
        IPlayerManager pm = m_asyncWorldEdit.getPlayerManager();        
        IPlayerEntry playerEntry = pm.getPlayer(player.getUniqueId());
        
        AsyncCommand.run(new FillCommand(playerEntry, pos, Patterns.wrap(pattern), radius, depth,
                axisX, axisY, axisZ), playerEntry, bp, editSession);
    }
}
