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

package org.primesoft.asyncworldedit.progressDisplay;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;

import javax.annotation.Nonnull;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;

/**
 * @author Weby@we-bb.com <Nicolas Glassey>
 * @version 1.0.0
 * @since 30/04/15
 */
public class BountifulAPIIntegrator implements IProgressDisplay
{
    @Override
    public void disableMessage(@Nonnull IPlayerEntry player)
    {
        if (!(player instanceof PlayerEntry)) {
            return;
        }
        BountifulAPI.sendActionBar(((PlayerEntry)player).getPlayer(), "");
    }

    @Override
    public void setMessage(IPlayerEntry player, int jobs, int blocks, int maxBlocks, double time, double speed, double percentage)
    {
        if (!(player instanceof PlayerEntry)) {
            return;
        }
        
        String block_full =  "█";
        String block_half =  "░";
        int barAmount = 20;

        String message = MessageType.CMD_JOBS_PROGRESS_BAR.format(jobs, speed, time);

        if(message==null)
            message="";
        if(percentage<0) percentage=0;
        if(percentage>100) percentage=100;

        int increment = 100/barAmount;
        int darkAmount = (int) percentage/increment;
        int lightAmount = barAmount-darkAmount;

        String bars = "";
        for(int i = 0; i < darkAmount; i++)
            bars+=block_full;
        for(int i = 0; i < lightAmount; i++)
            bars+=block_half;

        message += " : "+bars+" "+(int) percentage+"%";
        BountifulAPI.sendActionBar(((PlayerEntry)player).getPlayer(), message);
    }

    @Override
    public String getName() {
        return "Bountiful API";
    }
}
