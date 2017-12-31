/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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

import com.sk89q.worldedit.EditSession;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

/**
 *
 * @author SBPrime
 */
public abstract class AsyncCommand implements IAsyncCommand {

    /**
     * The player entry
     */
    private final IPlayerEntry m_playerEntry;

    /**
     * The player entry
     *
     * @return
     */
    @Override
    public IPlayerEntry getPlayer() {
        return m_playerEntry;
    }


    /**
     * Create new instance of the class
     * @param player 
     */
    protected AsyncCommand(IPlayerEntry player) {
        m_playerEntry = player;
    }
    
    /**
     * The task
     *
     * @param editSesstion
     * @return
     * @throws MaxChangedBlocksException
     */
    @Override
    public abstract Integer task(IAweEditSession editSesstion) throws WorldEditException;

    @Override
    public Integer execute(ICancelabeEditSession param) throws MaxChangedBlocksException {
        try {
            return task(param);
        } catch (WorldEditException ex) {
            if (ex instanceof MaxChangedBlocksException) {
                throw (MaxChangedBlocksException) ex;
            }

            ExceptionHelper.printException(ex, "Unable to execute AsyncCommand");
            return 0;
        }
    }

    /**
     * Run the command
     *
     * @param command
     * @param blockPLacer
     * @param player
     * @param editSession
     * @throws MaxChangedBlocksException
     */
    public static void run(IAsyncCommand command, IPlayerEntry player,
            IBlockPlacer blockPLacer, EditSession editSession) throws MaxChangedBlocksException {
        if (command == null) {
            return;
        }
        
        final IThreadSafeEditSession tsEditSesstion = (editSession instanceof IThreadSafeEditSession)
                ? (IThreadSafeEditSession) editSession : null;

        if (tsEditSesstion == null) {            
            log("-----------------------------------------------------------------------");
            log(String.format("Warning: Unable to perform AsyncCommand %1$s", command.getName()));
            log(String.format("Expected editSession: %1$s", IThreadSafeEditSession.class.getName()));
            log(String.format("Provided editSession: %1$s", editSession.getClass().getName()));
            log("Send this message to the author of the plugin!");
            log("-----------------------------------------------------------------------");
            return;
        }

        final String name = command.getName();
        final boolean isAsync = tsEditSesstion.checkAsync(name);

        if (!isAsync) {
            try {
                int affected = command.task(tsEditSesstion);
                player.say(MessageType.EX_CMD_BLOCKS_CHANGED.format(affected));
            } catch (WorldEditException ex) {
                if (ex instanceof MaxChangedBlocksException) {
                    throw (MaxChangedBlocksException) ex;
                }
                ExceptionHelper.printException(ex, "Unable to execute AsyncCommand");
            }
            return;
        }

        blockPLacer.performAsAsyncJob(tsEditSesstion, player, name, command);
    }
}
