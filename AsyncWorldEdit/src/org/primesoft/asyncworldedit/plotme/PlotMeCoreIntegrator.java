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
package org.primesoft.asyncworldedit.plotme;

import com.worldcretornica.plotme_core.PlotMeCoreManager;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.PlotWorldEdit;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IServerBridge;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import java.util.UUID;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class PlotMeCoreIntegrator implements IPlotMeIntegrator {
    
    private boolean m_isEnabled;
    private PlotMe_CorePlugin m_plotMeCore;
    private PlotMeCoreManager m_manager;
    private PlotMe_Core m_core;
    private IServerBridge m_bridge;
    private PlotWorldEdit m_worldEdit;
    
    @Override
    public void updateMask(Player player) {
        if (!m_isEnabled || player == null) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        IPlayer iPlayer = m_bridge.getPlayer(uuid);
        if (iPlayer == null) {
            return;
        }
        
        
        if (!m_manager.isPlotWorld(iPlayer)) {
            return;
        }
        
        if (m_manager.isPlayerIgnoringWELimit(uuid)) {
            m_worldEdit.removeMask(iPlayer);
        } else {
            m_worldEdit.setMask(iPlayer);
        }
    }
    
    @Override
    public boolean initialize(Plugin instance) {
        m_isEnabled = false;
        try {
            PluginManager pm = getServer().getPluginManager();
            if (pm.getPlugin("PlotMe") != null) {
                m_plotMeCore = (PlotMe_CorePlugin) pm.getPlugin("PlotMe");
                
                m_core = m_plotMeCore.getAPI();
                m_bridge = m_core.getServerBridge();
                m_worldEdit = m_bridge.getPlotWorldEdit();
                m_manager = m_core.getPlotMeCoreManager();

                m_isEnabled = true;
            }
        } catch (Throwable ex) {
            ExceptionHelper.printException(ex, "Error initializing PlotMe-Core integrator");
        }
        
        return m_isEnabled;
    }
}
