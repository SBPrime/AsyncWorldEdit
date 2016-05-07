package org.primesoft.asyncworldedit.playerManager;

import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Stub for the API to compile
 * @author SBPrime
 */
public class PlayerEntry {

    public final static UUID UUID_CONSOLE = UUID.randomUUID();
    public final static UUID UUID_UNKNOWN = UUID.randomUUID();

    public final static PlayerEntry CONSOLE = null;
    public final static PlayerEntry UNKNOWN = null;

    public void say(String msg) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public Player getPlayer() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public UUID getUUID() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean getMode() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public void setMode(boolean mode) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Is this player the console
     *
     * @return
     */
    public boolean isConsole() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean isUnknown() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean isPlayer() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    public boolean isInGame() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
}
