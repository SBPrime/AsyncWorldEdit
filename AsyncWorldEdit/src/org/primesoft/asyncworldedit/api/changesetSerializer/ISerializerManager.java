/*
 * AsyncWorldEdit API
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit API contributors
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
package org.primesoft.asyncworldedit.api.changesetSerializer;

import com.sk89q.worldedit.history.change.Change;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;

/**
 *
 * @author SBPrime
 */
public interface ISerializerManager {

    /**
     * Register new serializer
     *
     * @param serializer
     */
    void addSerializer(IChangesetSerializer serializer);

    /**
     * Unregister serializer
     *
     * @param serializer
     */
    void removeSerializer(IChangesetSerializer serializer);

    /**
     * Initialize the undo storage file 
     * (do not add the random seed)
     *
     * @param player
     * @param id
     * @return
     */
    File open(IPlayerEntry player, int id);
    
    /**
     * Initialize the undo storage file
     *
     * @param player The Player
     * @param id The undo ID (starting from 0)
     * @param addRandomSeed should a random seed be added to the file name
     * @return
     */
    File open(IPlayerEntry player, int id, boolean addRandomSeed);
    

    /**
     * Close undo storage file
     *
     * @param storageFile
     */
    void close(File storageFile);
        
    /**
     * Get memory storage for file
     * @param storageFile
     * @return 
     */
    IMemoryStorage getMemoryStorage(File storageFile);

    /**
     * Save changes to file
     *
     * @param storageFile
     * @param data
     */
    void save(File storageFile, List<Change> data);

    /**
     * Load changes from file
     *
     * @param storageFile
     * @param entries Number of entries to load
     * @param player
     * @param cancelable
     * @return
     */
    List<Change> load(File storageFile, int entries, IPlayerEntry player, ICancelabeEditSession cancelable);

    /**
     * Deserialize the undo entry
     *
     * @param entry
     * @param storage
     * @return
     */
    Change deserialize(IUndoEntry entry, IMemoryStorage storage);

    /**
     * Serialize the change to UndoEntry
     *
     * @param change
     * @param storage
     * @return
     */
    IUndoEntry serialize(Change change, IMemoryStorage storage);

    /**
     * Load the undo data from stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    IUndoEntry load(RandomAccessFile stream) throws IOException;

    /**
     * Load the undo data from stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    IUndoEntry load(DataInputStream stream) throws IOException;
    
    
    /**
     * Save the undo data to stream
     *
     * @param stream
     * @param undoEntry
     * @throws IOException
     * @return number of written bytes
     */
    int save(RandomAccessFile stream, IUndoEntry undoEntry) throws IOException;
    
    /**
     * Save the undo data to stream
     *
     * @param stream
     * @param undoEntry
     * @throws IOException
     * @return number of written bytes
     */
    int save(DataOutputStream stream, IUndoEntry undoEntry) throws IOException;
}
