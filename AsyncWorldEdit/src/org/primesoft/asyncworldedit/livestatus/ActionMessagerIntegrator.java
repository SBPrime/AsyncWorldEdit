package org.primesoft.asyncworldedit.livestatus;

import net.sneling.actionmessager.ActionMessager;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;

import javax.annotation.Nonnull;

/**
 * @author Weby@we-bb.com <Nicolas Glassey>
 * @version 1.0.0
 * @since 30/04/15
 */
public class ActionMessagerIntegrator implements LiveStatus
{
    final ActionMessager am = (ActionMessager) AsyncWorldEditMain.getInstance().getServer().getPluginManager().getPlugin("ActionMessager");

    @Override
    public void disableMessage(@Nonnull PlayerEntry player)
    {
        if(am!=null)
            am.sendMessage(player.getPlayer(), "");
    }

    @Override
    public void setMessage(@Nonnull PlayerEntry player, int blocks, int maxBlocks, int jobs, double speed, double time, double percentage)
    {
        if(am!=null)
        {
            String block_full = "█";
            String block_half = "░";
            int barAmount = 20;

            String message = MessageType.CMD_JOBS_PROGRESS_BAR.format(jobs, speed, time);

            if (message == null)
                message = "";
            if (percentage < 0) percentage = 0;
            if (percentage > 100) percentage = 100;

            int increment = 100 / barAmount;
            int darkAmount = (int) percentage / increment;
            int lightAmount = barAmount - darkAmount;

            String bars = "";
            for (int i = 0; i < darkAmount; i++)
                bars += block_full;
            for (int i = 0; i < lightAmount; i++)
                bars += block_half;

            message += " : " + bars + " " + (int) percentage + "%";

            am.sendMessage(player.getPlayer(), message);
        }
    }
}
