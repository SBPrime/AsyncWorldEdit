package org.primesoft.asyncworldedit.livestatus;

import me.confuser.barapi.BarAPI;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;

import javax.annotation.Nonnull;

/**
 * @author Weby@we-bb.com <Nicolas Glassey>
 * @version 1.0.0
 * @since 29/04/15
 */
public class BarAPIIntegrator implements LiveStatus
{

    @Override
    public void disableMessage(@Nonnull PlayerEntry player)
    {
        BarAPI.removeBar(player.getPlayer());
    }

    @Override
    public void setMessage(@Nonnull PlayerEntry player, int blocks, int maxBlocks, int jobs, double speed, double time, double percentage)
    {
        String message = MessageType.CMD_JOBS_PROGRESS_BAR.format(jobs, speed, time);

        if(message==null)
            message="";
        if(percentage<0) percentage=0;
        if(percentage>100) percentage=100;

        BarAPI.setMessage(player.getPlayer(), message, (float) percentage);
    }
}
