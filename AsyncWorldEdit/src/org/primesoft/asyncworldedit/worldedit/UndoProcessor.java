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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.change.EntityCreate;
import com.sk89q.worldedit.history.change.EntityRemove;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.primesoft.asyncworldedit.utils.InjectionException;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class UndoProcessor implements Operation {

    public static void processUndo(ThreadSafeEditSession parent,
            EditSession sender,
            EditSession session) {

        Iterator<Change> changes = parent.doUndo();
        Mask oldMask = session.getMask();
        session.setMask(sender.getMask());

        final HashMap<Double, HashMap<Double, HashMap<Double, List<BaseBlock>>>> placedBlocks
                = new HashMap<Double, HashMap<Double, HashMap<Double, List<BaseBlock>>>>();
        final Stack<Vector> posStack = new Stack<Vector>();
        final Stack<Change> initialChanges = new Stack<Change>();
        final Stack<Change> finalChanges = new Stack<Change>(); //This handles mostly entities

        while (changes.hasNext()) {
            Change change = changes.next();

            if (change instanceof EntityCreate) {
                initialChanges.add(change);
            } else if (change instanceof EntityRemove) {
                finalChanges.add(change);
            } else if (change instanceof BlockChange) {
                BlockChange bChange = (BlockChange) change;
                Vector pos = bChange.getPosition();
                BaseBlock block = bChange.getPrevious();
                Double x = pos.getX();
                Double y = pos.getY();
                Double z = pos.getZ();

                HashMap<Double, HashMap<Double, List<BaseBlock>>> mapX = placedBlocks.get(x);
                if (mapX == null) {
                    mapX = new HashMap<Double, HashMap<Double, List<BaseBlock>>>();
                    placedBlocks.put(x, mapX);
                }

                HashMap<Double, List<BaseBlock>> mapY = mapX.get(y);
                if (mapY == null) {
                    mapY = new HashMap<Double, List<BaseBlock>>();
                    mapX.put(y, mapY);
                }

                List<BaseBlock> list = mapY.get(z);
                if (list == null) {
                    list = new ArrayList<BaseBlock>();
                    mapY.put(z, list);
                }

                posStack.push(pos);
                list.add(block);
            } else {
                finalChanges.add(change);
            }
        }
        try {
            Operations.completeBlindly(new UndoProcessor(session, initialChanges, finalChanges, posStack, placedBlocks));

        } finally {
            session.flushQueue();
            session.setMask(oldMask);
        }
    }

    private final EditSession m_session;
    private final Stack<Change> m_initialChanges;
    private final Stack<Change> m_finalChanges;
    private final Stack<Vector> m_posStack;
    private final HashMap<Double, HashMap<Double, HashMap<Double, List<BaseBlock>>>> m_placedBlocks;

    private UndoProcessor(EditSession session,
            Stack<Change> initialChanges, Stack<Change> finalChanges,
            Stack<Vector> posStack,
            HashMap<Double, HashMap<Double, HashMap<Double, List<BaseBlock>>>> placedBlocks) {
        m_session = session;
        m_initialChanges = initialChanges;
        m_finalChanges = finalChanges;
        m_posStack = posStack;
        m_placedBlocks = placedBlocks;
    }

    @Override
    public Operation resume(RunContext rc) throws WorldEditException {
        UndoContext uc = new UndoContext();
        Extent bypassHistory = Reflection.get(EditSession.class, Extent.class, m_session, "bypassHistory",
                "Unable to get history");

        if (bypassHistory == null) {
            throw new InjectionException("Unable to perform undo operation. Unable to get bypassHistory field");
        }
        uc.setExtent(bypassHistory);

        for (Change change : m_initialChanges) {
            change.undo(uc);
        }
        while (!m_posStack.empty()) {
            Vector pos = m_posStack.pop();
            Double x = pos.getX();
            Double y = pos.getY();
            Double z = pos.getZ();

            HashMap<Double, HashMap<Double, List<BaseBlock>>> mapX = m_placedBlocks.get(x);
            if (mapX == null) {
                continue;
            }
            HashMap<Double, List<BaseBlock>> mapY = mapX.get(y);
            if (mapY == null) {
                continue;
            }
            List<BaseBlock> list = mapY.get(z);
            if (list == null) {
                continue;
            }

            for (BaseBlock block : list) {
                m_session.smartSetBlock(pos, block);
            }
            list.clear();
        }
        for (Change change : m_finalChanges) {
            change.undo(uc);
        }

        return null;
    }

    @Override
    public void cancel() {
    }
}
