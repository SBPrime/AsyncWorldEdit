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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.command.tool.brush.GravityBrush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import org.primesoft.asyncworldedit.injector.injected.commands.tool.IBrushToolAccessor;
import org.primesoft.asyncworldedit.worldedit.command.tool.brush.AsyncGravityBrush;

/**
 *
 * @author SBPrime
 */
public final class WrappedBrushTool extends BrushTool {
    /**
     * The parent brush
     */
    private final BrushTool m_parent;
            
    WrappedBrushTool(BrushTool brushTool) {
        super("");
        
        m_parent = brushTool;
        
        String permissions = ((IBrushToolAccessor)m_parent).getPermission();

        if (permissions == null) {
            permissions = "worldedit.brush.sphere";
        }
        
        
        //Set brush to current brush to wrap the current brush if needed.
        setBrush(m_parent.getBrush(), permissions);
    }

    @Override
    public boolean canUse(Actor player) {
        return m_parent.canUse(player);
    }

    @Override
    public Mask getMask() {
        return m_parent.getMask();
    }

    @Override
    public void setMask(Mask filter) {
        m_parent.setMask(filter);
    }

    @Override
    public void setBrush(Brush brush, String permission) {
        if (brush instanceof GravityBrush) {
            brush = AsyncGravityBrush.wrap((GravityBrush)brush);
        }
        
        m_parent.setBrush(brush, permission);
    }

    @Override
    public Brush getBrush() {
        return m_parent.getBrush();
    }

    @Override
    public void setFill(Pattern material) {
        m_parent.setFill(material);
    }

    @Override
    public Pattern getMaterial() {
        return m_parent.getMaterial();
    }

    @Override
    public double getSize() {
        return m_parent.getSize();
    }

    @Override
    public void setSize(double radius) {
        m_parent.setSize(radius);
    }

    @Override
    public int getRange() {
        return m_parent.getRange();
    }

    
    @Override
    public void setRange(int range) {
        m_parent.setRange(range);
    }
    
    
    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
        return m_parent.actPrimary(server, config, player, session);
    }

    @Override
    public void setTraceMask(Mask traceMask) {
        m_parent.setTraceMask(traceMask);
    }        
}
