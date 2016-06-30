/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.IPlotMeFix;

/**
 *
 * @author SBPrime
 */
public class PlotMeCoreFix_016 extends JavaPlugin implements IPlotMeFix {

    private AsyncWorldEditBukkit m_aweMain;
    private PlotMe_CorePlugin m_plotMeCore;
    private PlotMeCoreManager m_manager;
    private PlotMe_Core m_core;
    private IServerBridge m_bridge;
    private PlotWorldEdit m_worldEdit;

    @Override
    public void onEnable() {
        m_aweMain = getPlugin(AsyncWorldEditBukkit.class, "AsyncWorldEdit");
        m_plotMeCore = getPlugin(PlotMe_CorePlugin.class, "PlotMe");

        if (m_aweMain == null || m_plotMeCore == null) {
            return;
        }

        m_core = m_plotMeCore != null ? m_plotMeCore.getAPI() : null;
        m_manager = PlotMeCoreManager.getInstance();
        m_bridge = m_core != null ? m_core.getServerBridge() : null;

        m_worldEdit = m_bridge != null ? m_bridge.getPlotWorldEdit() : null;

        m_aweMain.setPlotMeFix(this);
    }

    @Override
    public void onDisable() {
        if (m_aweMain != null) {
            m_aweMain.setPlotMeFix(null);
        }
    }

    @Override
    public void setMask(UUID uuid) {
        IPlayer iPlayer = m_bridge.getPlayer(uuid);
        if (iPlayer == null) {
            return;
        }
        if (!m_manager.isPlotWorld(iPlayer)) {
            return;
        }
        if (m_manager.isPlayerIgnoringWELimit(iPlayer)) {
            m_worldEdit.removeMask(iPlayer);
        } else {
            m_worldEdit.setMask(iPlayer);
        }
    }

    /**
     * Get instance of a plugin
     *
     * @param <T>
     * @param pluginClass
     * @param pluginName
     * @return
     */
    public <T> T getPlugin(Class<T> pluginClass, String pluginName) {
        final Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);

        if (plugin == null) {
            return null;
        }

        if (!pluginClass.isAssignableFrom(plugin.getClass())) {
            return null;
        }

        return (T) plugin;
    }
}
