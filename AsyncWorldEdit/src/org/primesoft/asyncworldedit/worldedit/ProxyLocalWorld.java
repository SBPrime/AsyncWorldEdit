/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.Region;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PlayerWrapper;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.RegenerateEntry;

/**
 *
 * @author SBPrime
 */
public class ProxyLocalWorld extends BukkitWorld {

    /**
     * Player wraper
     */
    private final PlayerWrapper m_wrapper;

    /**
     * Bukkit schedule
     */
    private final BukkitScheduler m_schedule;

    /**
     * The plugin
     */
    private final PluginMain m_plugin;

    /**
     * The player
     */
    private final UUID m_player;

    /**
     * The blocks placer
     */
    private final BlockPlacer m_blockPlacer;
    
    public ProxyLocalWorld(UUID player, World world) {
        super(world);
        m_player = player;
        m_plugin = PluginMain.getInstance();
        m_blockPlacer = m_plugin.getBlockPlacer();
        m_schedule = m_plugin.getServer().getScheduler();
        m_wrapper = m_plugin.getPlayerManager().getPlayer(player);
    }
    
    @Override
    public boolean regenerate(final Region region, EditSession editSession) {
        boolean isAsync = checkAsync(WorldeditOperations.regenerate);
        if (!isAsync) {
            return super.regenerate(region, editSession);
        }
        
        final int jobId = getJobId();
        final EditSession session;
        final JobEntry job;
        
        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            session = new CancelabeEditSession(aSession, aSession.getAsyncMask(), jobId);
            job = new JobEntry(m_player, (CancelabeEditSession) session, jobId, "regenerate");
        } else {
            session = editSession;
            job = new JobEntry(m_player, jobId, "regenerate");
        }
        
        m_blockPlacer.addJob(m_player, job);
        
        final int maxY = getMaxY();
        m_schedule.runTaskAsynchronously(m_plugin, new WorldAsyncTask(getWorld(), session,
                m_player, "regenerate", m_blockPlacer, job) {
                    @Override
                    public void task(EditSession editSession, World world) throws MaxChangedBlocksException {
                        doRegen(editSession, region, maxY, world, jobId);
                    }
                    
                });
        
        return true;
    }

    /**
     * Perfrom the regen operation
     *
     * @param eSession
     * @param region
     * @param world
     */
    private void doRegen(EditSession eSession, Region region, int maxY, World world, int jobId) {
        BaseBlock[] history = new BaseBlock[16 * 16 * (maxY + 1)];
        
        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (maxY + 1); ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = eSession.getBlock(pt);
                    }
                }
            }
            
            m_blockPlacer.addTasks(m_player, new RegenerateEntry(jobId, world, chunk));

            // Then restore
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (maxY + 1); ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        // We have to restore the block if it was outside
                        if (!region.contains(pt)) {
                            eSession.smartSetBlock(pt, history[index]);
                        } else { // Otherwise fool with history
                            eSession.rememberChange(pt, history[index],
                                    eSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     */
    private boolean checkAsync(WorldeditOperations operation) {
        return ConfigProvider.isAsyncAllowed(operation) && (m_wrapper == null || m_wrapper.getMode());
    }

    /**
     * Get next job id for current player
     *
     * @return Job id
     */
    private int getJobId() {
        return m_blockPlacer.getJobId(m_player);
    }
}
