/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.progressDisplay;

import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplayManager;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import static org.primesoft.asyncworldedit.progressDisplay.ActionBarAPIBackend.log;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;


/**
 *
 * @author Weby
 */
public class ActionBarAPIIntegrator implements IProgressDisplay
{
    private final Server m_server;
    private final boolean m_isInitialized;
    private final IProgressDisplayManager m_progressManager;
    private static final String light = "░";
    private static final String dark = "█";
    private static int barAmount = 20;

    /**
     * Get instance of the core blocks hub plugin
     *
     * @param plugin
     * @return
     */
    private static ActionBarAPI getABAPI(JavaPlugin plugin) {
        try {
            Plugin cPlugin = plugin.getServer().getPluginManager().getPlugin("ActionBarAPI");

            if ((cPlugin == null) || (!(cPlugin instanceof ActionBarAPI))) {
                log("ActionBarAPI not found.");
                return null;
            }

            return (ActionBarAPI) cPlugin;
        } catch (NoClassDefFoundError ex) {
            ExceptionHelper.printException(ex, "Error initializing BarAPI.");
            return null;
        }
    }

    public ActionBarAPIIntegrator(JavaPlugin plugin, IProgressDisplayManager progressManager) {
        ActionBarAPI ba = getABAPI(plugin);
        m_isInitialized = ba != null;
        
        m_progressManager = progressManager;
        m_server = plugin.getServer();
    }

    @Override
    public void setMessage(IPlayerEntry player, int jobsCount, 
            int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
        if (!m_isInitialized || player == null) {
            return;
        }
        
        Player bPlayer = m_server.getPlayer(player.getUUID());
        if (bPlayer == null) {
            return;
        }

        String message = m_progressManager.formatMessage(jobsCount, placingSpeed, timeLeft);
        if (percentage < 0) {
            percentage = 0;
        } else if (percentage > 100) {
            percentage = 100;
        }

        int increment = 100/barAmount;
        int darkAmount = (int) percentage/increment;
        int lightAmount = barAmount-darkAmount;

        String bars = "";
        for(int i = 0; i < darkAmount; i++)
            bars+=dark;
        for(int i = 0; i < lightAmount; i++)
            bars+=light;

        message += " : "+bars+" "+(int) percentage+"%";
        ActionBarAPI.sendActionBar(bPlayer, message);
    }

    @Override
    public void disableMessage(IPlayerEntry player) {
        if (!m_isInitialized || player == null ) {
            return;
        }

        Player bPlayer = m_server.getPlayer(player.getUUID());
        if (bPlayer == null) {
            return;
        }
        
        ActionBarAPI.sendActionBar(bPlayer, "");
    }

    @Override
    public String getName() {
        return "Action BAR";
    }
}