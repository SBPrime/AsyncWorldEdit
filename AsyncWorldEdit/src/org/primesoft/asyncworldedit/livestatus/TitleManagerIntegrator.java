package org.primesoft.asyncworldedit.livestatus;

import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;

import javax.annotation.Nonnull;

/**
 * @author Weby@we-bb.com <Nicolas Glassey>
 * @version 1.0.0
 * @since 29/04/15
 */
public class TitleManagerIntegrator implements LiveStatus
{
    @Override
    public void disableMessage(@Nonnull PlayerEntry player)
    {
        new ActionbarTitleObject("").send(player.getPlayer());
    }

    @Override
    public void setMessage(@Nonnull PlayerEntry player, int blocks, int maxBlocks, int jobs, double speed, double time, double percentage)
    {

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

        new ActionbarTitleObject(message).send(player.getPlayer());
    }
}
