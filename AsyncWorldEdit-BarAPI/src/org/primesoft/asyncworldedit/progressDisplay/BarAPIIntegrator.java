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
package org.primesoft.asyncworldedit.progressDisplay;

import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import me.confuser.barapi.BarAPI;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class BarAPIIntegrator implements IProgressDisplay {

    private final boolean m_isInitialized;

    /**
     * Get instance of the core blocks hub plugin
     *
     * @param plugin
     * @return
     */
    public static BarAPI getBarAPI(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("BarAPI");

            if ((cPlugin == null) || (!(cPlugin instanceof BarAPI))) {
                AsyncWorldEditBukkit.log("BarAPI not found.");
                return null;
            }

            return (BarAPI) cPlugin;
        } catch (NoClassDefFoundError ex) {
            ExceptionHelper.printException(ex, "Error initializing BarAPI.");
            return null;
        }
    }

    public BarAPIIntegrator(JavaPlugin plugin) {
        BarAPI ba = getBarAPI(plugin);
        m_isInitialized = ba != null;
    }

    @Override
    public void setMessage(IPlayerEntry player, int jobsCount, 
            int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
        if (!(player instanceof PlayerEntry)) {
            return;
        }
        PlayerEntry pe = (PlayerEntry)player;
        
        if (!m_isInitialized || pe.getPlayer() == null) {
            return;
        }

        if (!player.getPermissionGroup().isBarApiProgressEnabled()) {
            return;
        }

        String message = MessageType.CMD_JOBS_PROGRESS_BAR.format(jobsCount, placingSpeed, timeLeft);
        if (percentage < 0) {
            percentage = 0;
        } else if (percentage > 100) {
            percentage = 100;
        }        
        
        BarAPI.setMessage(pe.getPlayer(), message, (float)percentage);
    }

    @Override
    public void disableMessage(IPlayerEntry player) {
        if (!(player instanceof PlayerEntry)) {
            return;
        }
        PlayerEntry pe = (PlayerEntry)player;
        
        if (!m_isInitialized || pe.getPlayer() == null) {
            return;
        }

        if (!player.getPermissionGroup().isBarApiProgressEnabled()) {
            return;
        }

        BarAPI.removeBar(pe.getPlayer());
    }

    @Override
    public String getName() {
        return "Bar API";
    }
}