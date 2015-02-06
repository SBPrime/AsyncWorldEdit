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
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IServerBridge;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
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

    private Object m_plotWorldEdit;
    private Method m_setMaskMethod;
    private Method m_removeMaskMethod;

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

        Exception e;
        
        try {
            if (m_manager.isPlayerIgnoringWELimit(uuid)) {
                m_setMaskMethod.invoke(m_plotWorldEdit, iPlayer);
            } else if (m_removeMaskMethod != null) {
                m_removeMaskMethod.invoke(m_plotWorldEdit, iPlayer);
            }
            
            return;
        } catch (IllegalAccessException ex) {
            e = ex;
        } catch (IllegalArgumentException ex) {
            e = ex;
        } catch (InvocationTargetException ex) {
            e = ex;
        }
        
        ExceptionHelper.printException(e, "Unable to update mask.");
    }

    @Override
    public boolean initialize(Plugin instance) {
        m_isEnabled = false;
        try {
            m_plotMeCore = (PlotMe_CorePlugin) instance;
            m_core = m_plotMeCore.getAPI();
            m_bridge = m_core.getServerBridge();
            m_manager = m_core.getPlotMeCoreManager();

            m_plotWorldEdit = getPlotWorldEdit(m_bridge);

            if (m_plotWorldEdit == null) {
                AsyncWorldEditMain.log("WorldEdit method not found in IServerBridge.");
                return false;
            }

            Class<?> plotWorldEditClass = m_plotWorldEdit.getClass();

            m_setMaskMethod = plotWorldEditClass.getMethod("setMask", IPlayer.class);
            m_removeMaskMethod = plotWorldEditClass.getMethod("removeMask", IPlayer.class);

            m_isEnabled = true;
        } catch (Throwable ex) {
            ExceptionHelper.printException(ex, "Error initializing PlotMe-Core integrator");
        }

        return m_isEnabled;
    }

    /**
     * Try to find the plotworldedit field
     *
     * @return
     */
    private Object getPlotWorldEdit(IServerBridge bridge) throws Exception {
        Class<?> bridgeClass = bridge.getClass();
        Method method = bridgeClass.getMethod("getPlotWorldEdit");

        if (method == null) {
            return null;
        }                

        return method.invoke(bridge, null);
    }
}
