/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Vector;
import java.lang.reflect.Field;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntityEntry;

/**
 * This class is a wrapper to better handle entity paste Note: Do not use any
 * operations from this class, always use th parrent!
 *
 * @author SBPrime
 */
public class CuboidClipboardWrapper extends ProxyCuboidClipboard {
    /**
     * The job id
     */
    private final int m_jobId;
    
    /**
     * The blocks placer
     */
    private final BlockPlacer m_blocksPlacer;
    /**
     * Player
     */
    private final String m_player;

    /**
     * Inject entities to CuboidClipboard
     *
     * @param cc
     * @param value
     */
    public static void setEntities(CuboidClipboard cc, Object value) {
        try {
            Field field = cc.getClass().getDeclaredField("entities");
            field.setAccessible(true);
            field.set(cc, value);
        } catch (IllegalArgumentException ex) {
            PluginMain.log("Unable to set entities: unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            PluginMain.log("Unable to set entities: security exception.");
        } catch (NoSuchFieldException ex) {
            PluginMain.log("Unable to set entities: unsupported WorldEdit version.");
        } catch (SecurityException ex) {
            PluginMain.log("Unable to set entities: security exception.");
        }
    }

    /**
     * Get entities from CuboidClipboard
     *
     * @param cc
     * @param value
     */
    public static Object getEntities(CuboidClipboard cc) {
        try {
            Field field = cc.getClass().getDeclaredField("entities");
            field.setAccessible(true);
            return field.get(cc);
        } catch (IllegalArgumentException ex) {
            PluginMain.log("Unable to set entities: unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            PluginMain.log("Unable to set entities: security exception.");
        } catch (NoSuchFieldException ex) {
            PluginMain.log("Unable to set entities: unsupported WorldEdit version.");
        } catch (SecurityException ex) {
            PluginMain.log("Unable to set entities: security exception.");
        }

        return null;
    }
    
    public CuboidClipboardWrapper(String player, CuboidClipboard parrent) {
        this(player, parrent, -1);
    }

    public CuboidClipboardWrapper(String player, CuboidClipboard parrent, int jobId) {
        super(parrent);

        m_jobId = jobId;
        m_blocksPlacer = PluginMain.getInstance().getBlockPlacer();
        m_player = player;
    }

    @Override
    public LocalEntity[] pasteEntities(Vector pos) {
        synchronized (m_parrent) {
            final Object entities = getEntities(m_parrent);
            final int jobId = m_jobId < 0 ? m_blocksPlacer.getJobId(m_player) : m_jobId;
            final BlockPlacerEntityEntry entry =
                    new BlockPlacerEntityEntry(null, jobId, entities, pos, m_parrent);

            m_blocksPlacer.addTasks(m_player, entry);
        }
        return new LocalEntity[0];
    }
}
