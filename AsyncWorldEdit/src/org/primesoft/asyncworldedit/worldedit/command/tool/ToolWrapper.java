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
package org.primesoft.asyncworldedit.worldedit.command.tool;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.AreaPickaxe;
import com.sk89q.worldedit.command.tool.BlockDataCyler;
import com.sk89q.worldedit.command.tool.BlockReplacer;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.DistanceWand;
import com.sk89q.worldedit.command.tool.FloatingTreeRemover;
import com.sk89q.worldedit.command.tool.FloodFillTool;
import com.sk89q.worldedit.command.tool.LongRangeBuildTool;
import com.sk89q.worldedit.command.tool.QueryTool;
import com.sk89q.worldedit.command.tool.RecursivePickaxe;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.TreePlanter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.Location;
import org.primesoft.asyncworldedit.core.AwePlatform;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.configuration.WorldeditOperations;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.utils.SchedulerUtils;
import org.primesoft.asyncworldedit.utils.WaitFor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.AsyncTask;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.FakeLocalSession;
import org.primesoft.asyncworldedit.worldedit.util.LocationWrapper;

/**
 *
 * @author SBPrime
 */
public class ToolWrapper {
    public static Tool wrapTool(Tool tool) {
        if (tool == null) {
            return null;
        }
        
        if (tool instanceof IAsyncTool ||
            tool instanceof DistanceWand || 
            tool instanceof QueryTool ||
            tool instanceof LongRangeBuildTool ||
            tool instanceof WrappedBrushTool) {
            return tool;
        }
        
        
        if (tool instanceof TreePlanter) {
            return AsyncTreePlanter.wrap((TreePlanter)tool);
        }
        if (tool instanceof BlockDataCyler)
        {
            return AsyncBlockDataCyler.wrap((BlockDataCyler)tool);
        }
        if (tool instanceof FloatingTreeRemover) {
            return AsyncFloatingTreeRemover.wrap((FloatingTreeRemover)tool);
        }
        if (tool instanceof FloodFillTool) {
            return AsyncFloodFillTool.wrap((FloodFillTool)tool);
        }
        if (tool instanceof BlockReplacer) {
            return AsyncBlockReplacer.wrap((BlockReplacer)tool);
        }
        if (tool instanceof BrushTool) {
            return new WrappedBrushTool((BrushTool)tool);
        }
        
        log(String.format("WARNING: The tool %1$s is not supported.", tool.getClass().getName()));
        return tool;
    }
    
    
    public static BlockTool wrapPickaxe(BlockTool tool) {
        if (tool == null) {
            return null;
        }
        
        if (tool instanceof IAsyncTool ||
            tool instanceof SinglePickaxe) {
            return tool;
        }
        
        
        if (tool instanceof RecursivePickaxe) {
            return AsyncRecursivePickaxe.wrap((RecursivePickaxe)tool);
        }
        if (tool instanceof AreaPickaxe) {
            return AsyncAreaPickaxe.wrap((AreaPickaxe)tool);
        }
        
        
        log(String.format("WARNING: The tool %1$s is not supported.", tool.getClass().getName()));
        return tool;
    }

    
    /**
     * Perform tool action as async job
     * @param server
     * @param config
     * @param player
     * @param session
     * @param clicked
     * @param toolAction
     * @param jobName
     * @param worldeditOperations 
     * @return  
     */
    public static boolean performAction(final Platform server, final LocalConfiguration config, final Player player, 
            final LocalSession session, Location clicked, 
            final ToolAction toolAction, String jobName, WorldeditOperations worldeditOperations) {
        
        if (toolAction == null) {
            return false;
        }
        
        EditSession editSession = session.createEditSession(player);
        if (!(editSession instanceof AsyncEditSession)) {
            return toolAction.execute(server, config, player, session, clicked);
        }

        final IAsyncWorldEditCore aweCore = AwePlatform.getInstance().getCore();
        final IBlockPlacer blockPlacer = aweCore.getBlockPlacer();
        final IPlayerManager playerManager = aweCore.getPlayerManager();
        final IScheduler scheduler = aweCore.getPlatform().getScheduler();
        
        
        final AsyncEditSession aEditSession = (AsyncEditSession) editSession;
        final WaitFor waitFor = aEditSession.getWait();
        final IPlayerEntry playerEntry = playerManager.getPlayer(player.getUniqueId());
        
        
        boolean isAsync = aEditSession.checkAsync(WorldeditOperations.tool) && aEditSession.checkAsync(worldeditOperations);

        if (!isAsync) {
            return toolAction.execute(server, config, player, session, LocationWrapper.wrap(clicked, -1, false, playerEntry));
        }

        final int jobId = blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cSession = new CancelabeEditSession(aEditSession, editSession.getMask(), jobId);

        final JobEntry job = new JobEntry(playerEntry, cSession, jobId, jobName);
        blockPlacer.addJob(playerEntry, job);

        final LocationWrapper clickedWrapped = LocationWrapper.wrap(clicked, jobId, isAsync, playerEntry);

        SchedulerUtils.runTaskAsynchronously(scheduler, new AsyncTask(cSession, playerEntry, jobName,
                blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession cSession)
                    throws MaxChangedBlocksException {
                        FakeLocalSession fakeSession = new FakeLocalSession(cSession, session);

                        waitFor.checkAndWait(null);
                        
                        toolAction.execute(server, config, player, fakeSession, clickedWrapped);
                        return 0;
                    }
                });

        session.remember(aEditSession);
        return true;
    }
}
