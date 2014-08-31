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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionStub;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.util.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.extent.QueueExtent;

/**
 *
 * @author SBPrime
 */
public class UndoSession extends EditSessionStub {
    /**
     * NUll event
     */
    private final static EditSessionEvent NULL_EVENT = new EditSessionEvent(null, null, 0, Stage.BEFORE_REORDER);
    
    private final List<Entry<Vector, BaseBlock>> m_undoQueue;

    @SuppressWarnings("unchecked")
    public Entry<Vector, BaseBlock>[] getEntries() {
        return m_undoQueue.toArray(new Entry[0]);
    }

    public UndoSession(EventBus eventBus) {
        super(eventBus, null, 0, null, NULL_EVENT);

        m_undoQueue = new ArrayList<Entry<Vector, BaseBlock>>();
        Extent extent = new QueueExtent(m_undoQueue);

        Reflection.set(EditSession.class, this, "bypassHistory",
                new MultiStageReorder(extent, false),
                "Unable to inject history");
    }

    @Override
    public void flushQueue() {
    }
}