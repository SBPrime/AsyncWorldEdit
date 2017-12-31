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
package org.primesoft.asyncworldedit.changesetSerializer;

import org.primesoft.asyncworldedit.changesetSerializer.serializers.SerializerSetChangesetChunkChange;
import org.primesoft.asyncworldedit.changesetSerializer.serializers.SerializerRelightChange;
import org.primesoft.asyncworldedit.changesetSerializer.serializers.SerializerChunkFlushChange;
import org.primesoft.asyncworldedit.changesetSerializer.serializers.SerializerBlockPlacerChange;
import org.primesoft.asyncworldedit.changesetSerializer.serializers.SerializerBlockChange;
import com.sk89q.worldedit.history.change.Change;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.changesetSerializer.IChangesetSerializer;
import org.primesoft.asyncworldedit.api.changesetSerializer.IMemoryStorage;
import org.primesoft.asyncworldedit.api.changesetSerializer.IUndoEntry;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IChunkCacheStream;
import org.primesoft.asyncworldedit.api.inner.IInnerSerializerManager;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.changesetSerializer.serializers.SerializerBiomeChange;
import org.primesoft.asyncworldedit.configuration.ConfigMemory;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.UndoBehaviour;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.io.ChunkCacheStream;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.GCUtils;
import org.primesoft.asyncworldedit.utils.io.VarInt;

/**
 *
 * @author SBPrime
 */
public final class SerializerManager implements IInnerSerializerManager {

    /**
     * The Zero UUID
     */
    private final UUID UUID_ZERO = new UUID(0, 0);

    private final String MEMORY_ENTRY = "++MEMORY++";

    /**
     * Current memory storage seed
     */
    private final UUID m_seed = UUID.randomUUID();

    /**
     * List of all undo descriptors
     */
    private final Map<File, UndoDescriptor> m_undoDescriptors = new LinkedHashMap<File, UndoDescriptor>();

    /**
     * List of all known serializers
     */
    private final List<IChangesetSerializer> m_serializers = new LinkedList<IChangesetSerializer>();

    /**
     * The AWE
     */
    private final IAsyncWorldEditCore m_awe;

    @Override
    public void addSerializer(IChangesetSerializer serializer) {
        if (serializer == null) {
            return;
        }

        synchronized (m_serializers) {
            m_serializers.add(serializer);
        }
    }

    @Override
    public void removeSerializer(IChangesetSerializer serializer) {
        if (serializer == null) {
            return;
        }

        synchronized (m_serializers) {
            m_serializers.remove(serializer);
        }
    }

    /**
     * Get serializer based on the class name
     *
     * @param className
     * @return
     */
    private IChangesetSerializer getSerializer(String className) {
        synchronized (m_serializers) {
            for (IChangesetSerializer serializer : m_serializers) {
                if (serializer.canSerialize(className)) {
                    return serializer;
                }
            }
        }
        return null;
    }

    public SerializerManager(IAsyncWorldEditCore awe) {
        m_awe = awe;
        initialize();
    }

    private void initialize() {
        addSerializer(new SerializerBlockChange());
        addSerializer(new SerializerBiomeChange());
        addSerializer(new SerializerBlockPlacerChange(this, m_awe.getBlockPlacer()));
        addSerializer(new SerializerChunkFlushChange(m_awe));
        addSerializer(new SerializerRelightChange(m_awe));
        addSerializer(new SerializerSetChangesetChunkChange(m_awe));
    }

    @Override
    public File open(IPlayerEntry player, int id) {
        return open(player, id, false);
    }

    @Override
    public File open(IPlayerEntry player, int id, boolean addRandomSeed) {
        File undoFolder = ConfigProvider.getUndoFolder();
        File playerFolder = new File(undoFolder, player.getUUID().toString());
        UUID uuid = addRandomSeed ? UUID.randomUUID() : UUID_ZERO;

        File undoFile = new File(playerFolder, String.format("%1$s.%2$s", uuid.toString(), id));

        if (!playerFolder.exists()) {
            playerFolder.mkdirs();
        }
        if (undoFile.exists()) {
            log(String.format("Undo file %1$s already exists, removing", undoFile));
            if (!undoFile.delete()) {
                log("Unable to open undo file. Undo disabled");
            }
        }

        try {
            UndoDescriptor ud = new UndoDescriptor(undoFile);

            synchronized (m_undoDescriptors) {
                m_undoDescriptors.put(undoFile, ud);
            }

            return undoFile;
        } catch (IOException ioe) {
            ExceptionHelper.printException(ioe, "Unable to open undo file. Undo disabled");

            return null;
        }
    }

    @Override
    public void close(File storageFile) {
        if (storageFile == null) {
            return;
        }

        UndoDescriptor ud;
        synchronized (m_undoDescriptors) {
            ud = m_undoDescriptors.get(storageFile);
            if (ud == null) {
                return;
            }
            m_undoDescriptors.remove(storageFile);
        }

        try {
            ud.close();
        } catch (IOException ioe) {
            ExceptionHelper.printException(ioe, "Unable to close undo file.");
        }
    }

    @Override
    public IMemoryStorage getMemoryStorage(File storageFile) {
        if (storageFile == null) {
            return null;
        }
        
        synchronized (m_undoDescriptors) {
            return m_undoDescriptors.get(storageFile);
        }
    }
    
    

    @Override
    public void save(File storageFile, List<Change> data) {
        if (storageFile == null) {
            return;
        }

        UndoDescriptor ud;
        synchronized (m_undoDescriptors) {
            ud = m_undoDescriptors.get(storageFile);
            if (ud == null) {
                return;
            }
        }

        final StreamProvider sp = StreamProvider.getInstance();
        final File fileName = ud.getFile();
        final File fileNameIdx = new File(ud.getFile().getPath()+".idx");

        sp.reserve();
        sp.reserve();
        
        sp.addReference(fileName);
        sp.addReference(fileNameIdx);
        
        synchronized (ud.getMutex()) {
            FileOutputStream stream = null;
            FileOutputStream streamIdx = null;                        
            
            try {
                stream = new FileOutputStream(fileName, true);
                streamIdx = new FileOutputStream(fileNameIdx, true);
                
                ByteArrayOutputStream memoryOut = new ByteArrayOutputStream();
                ByteArrayOutputStream memoryOutIdx = new ByteArrayOutputStream();
                
                DataOutputStream bufferStream = new DataOutputStream(memoryOut);
                DataOutputStream bufferStreamIdx = new DataOutputStream(memoryOutIdx);

                for (Change change : data) {
                    if (bufferStreamIdx.size() > 1000000) {
                        bufferStreamIdx.flush();
                        bufferStreamIdx.close();
                        streamIdx.write(memoryOutIdx.toByteArray());                                                

                        memoryOutIdx = new ByteArrayOutputStream();
                        bufferStreamIdx = new DataOutputStream(memoryOutIdx);
                    }
                    if (bufferStream.size() > 1000000) {
                        bufferStream.flush();
                        bufferStream.close();
                        stream.write(memoryOut.toByteArray());                                                

                        memoryOut = new ByteArrayOutputStream();
                        bufferStream = new DataOutputStream(memoryOut);
                    }
                    
                    long size = save(ud, bufferStream, change);
                    VarInt.writeLong(bufferStreamIdx, size);

                    if (ud.isClosed()) {
                        break;
                    }
                }

                bufferStream.flush();                
                bufferStream.close();
                
                bufferStreamIdx.flush();                
                bufferStreamIdx.close();
                
                memoryOut.flush();
                memoryOut.close();
                
                memoryOutIdx.flush();
                memoryOutIdx.close();

                stream.write(memoryOut.toByteArray());
                stream.flush();
                stream.close();
                
                streamIdx.write(memoryOutIdx.toByteArray());
                streamIdx.flush();
                streamIdx.close();
            } catch (IOException ioe) {
                ExceptionHelper.printException(ioe, String.format("Unable to save undo data. Data might be corrupted"));

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                    }
                }
                
                if (streamIdx != null) {
                    try {
                        streamIdx.close();
                    } catch (IOException ex) {
                    }
                }
            } finally {
                sp.release();
                sp.release();
                
                sp.removeReference(fileName);
                sp.removeReference(fileNameIdx);
            }
        }
    }

    @Override
    public List<Change> load(File storageFile, int entries,
            IPlayerEntry player, ICancelabeEditSession editSession) {
        final ArrayList<Change> result = new ArrayList<Change>();
        if (storageFile == null || entries == 0) {
            return result;
        }

        boolean isMain = m_awe.getTaskDispatcher().isMainTask();
        UndoBehaviour behaviour = ConfigProvider.undo().getStorageBehaviour();
        ConfigMemory mConfig = ConfigProvider.memory();
        long minMemoryHard = mConfig.getMinMemoryHard() * 1000;
        long minMemorySoft = mConfig.getMinMemorySoft() * 1000;

        UndoDescriptor ud;
        synchronized (m_undoDescriptors) {
            ud = m_undoDescriptors.get(storageFile);
            if (ud == null) {
                return result;
            }
        }

        final Object mutex = ud.getMutex();
        final StreamProvider sp = StreamProvider.getInstance();
        final File fileName = ud.getFile();

        sp.reserve();
        sp.addReference(fileName);
                
        synchronized (mutex) {
            RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(fileName, "r");
                UndoEntry uEntry;
                boolean breakLoop = false;

                IChunkCacheStream dataStream = new ChunkCacheStream(stream);

                do {
                    uEntry = UndoEntry.load(dataStream);
                    Change change = deserialize(uEntry, ud);

                    if (change != null) {
                        result.add(change);
                    }

                    if (behaviour != UndoBehaviour.Off) {
                        long memAvailable = GCUtils.getTotalAvailableMemory();
                        boolean memLow = minMemoryHard > 0 && memAvailable < minMemoryHard;
                        if (memLow) {
                            player.say(MessageType.BLOCK_PLACER_MEMORY_LOW.format());

                            if (behaviour == UndoBehaviour.Drop) {
                                breakLoop = true;
                            } else if (behaviour == UndoBehaviour.Wait && editSession != null) {
                                do {
                                    //TODO: This needs refactoring
                                    try {
                                        mutex.wait(1000);
                                    } catch (InterruptedException ex) {
                                        breakLoop = true;
                                        break;

                                    }

                                    GCUtils.GC();
                                } while (GCUtils.getTotalAvailableMemory() < minMemorySoft && !editSession.isCanceled());

                                breakLoop |= editSession.isCanceled();
                            } else if (editSession == null) {
                                breakLoop = true;
                            }
                        }
                    }
                } while (uEntry != null && result.size() < entries && !breakLoop);

                stream.close();
            } catch (IOException ioe) {
                ExceptionHelper.printException(ioe, "Unable to load undo data. Data might be corrupted.");

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                    }
                }
            } finally {
                sp.release();
                sp.removeReference(fileName);
            }
        }

        return result;
    }

    /**
     * Save change to disk
     *
     * @param descriptor
     * @param change
     */
    private long save(UndoDescriptor descriptor, DataOutputStream stream, Change change) throws IOException {
        if (descriptor.isClosed()) {
            return 0;
        }

        long idx = descriptor.getAndIncrementIdx();
        UndoEntry data = serialize(change, descriptor);
        if (data == null) {
            return 0;
        }

        data.setId(idx);
        return save(stream, data);
    }

    /**
     * Serialize the change to UndoEntry
     *
     * @param change
     * @param storage
     * @return
     */
    @Override
    public UndoEntry serialize(Change change, IMemoryStorage storage) {
        if (change == null) {
            return null;
        }

        final String className = change.getClass().getName();
        IChangesetSerializer serializer = getSerializer(className);
        UndoEntry data;
        if (serializer == null) {
            data = serializeToMemory(storage, change);
        } else {
            byte[] buf = serializer.serialize(change, storage);
            if (buf == null) {
                data = serializeToMemory(storage, change);
            } else {
                data = new UndoEntry(className, buf, -1);
            }
        }
        return data;
    }

    /**
     * Serialize change to memory
     *
     * @param storage
     * @param change
     * @return
     */
    private UndoEntry serializeToMemory(IMemoryStorage storage, Change change) {
        try {
            if (storage == null || change == null) {
                return null;
            }

            UUID uuid = storage.storeInMemory(change);
            if (uuid == null) {
                return null;
            }

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);

            stream.writeLong(uuid.getLeastSignificantBits());
            stream.writeLong(uuid.getMostSignificantBits());
            stream.writeLong(m_seed.getLeastSignificantBits());
            stream.writeLong(m_seed.getMostSignificantBits());

            stream.flush();

            return new UndoEntry(MEMORY_ENTRY, byteStream.toByteArray(), -1);
        } catch (IOException ex) {
            ExceptionHelper.printException(ex, "Unable to serialize changeset");
            return null;
        }
    }

    /**
     * Deserialize the undo entry
     *
     * @param entry
     * @return
     */
    @Override
    public Change deserialize(IUndoEntry entry, IMemoryStorage storage) {
        if (entry == null) {
            return null;
        }

        String type = entry.getType();
        if (type == null) {
            return null;
        }

        if (type.equals(MEMORY_ENTRY)) {
            return deserializeFromMemory(entry.getData(), storage);
        }

        IChangesetSerializer serializer = getSerializer(type);
        if (serializer == null) {
            log(String.format("ERROR: unable to deserialize %1$s, unknown type.", type));
            return null;
        }

        return serializer.deserialize(entry.getData(), storage);
    }

    /**
     * Deserialize data from memory
     *
     * @param bytes
     * @return
     */
    private Change deserializeFromMemory(byte[] bytes, IMemoryStorage storage) {
        if (bytes == null || bytes.length < 32 || storage == null) {
            return null;
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        DataInputStream stream = new DataInputStream(byteStream);

        long uuidL, uuidM, seedL, seedM;

        try {
            uuidL = stream.readLong();
            uuidM = stream.readLong();
            seedL = stream.readLong();
            seedM = stream.readLong();
        } catch (IOException ex) {
            ExceptionHelper.printException(ex, "Ups. This should never happen.");
            return null;
        }

        UUID uuid = new UUID(uuidM, uuidL);
        UUID seed = new UUID(seedM, seedL);

        if (!seed.equals(m_seed)) {
            return null;
        }

        return storage.getFromMemory(uuid);
    }

    @Override
    public IUndoEntry load(RandomAccessFile stream) throws IOException {
        return UndoEntry.load(stream);
    }

    @Override
    public IUndoEntry load(DataInputStream stream) throws IOException {
        return UndoEntry.load(stream);
    }
    
    @Override
    public IUndoEntry load(IChunkCacheStream stream) throws IOException {
        return UndoEntry.load(stream);
    }

    @Override
    public int save(RandomAccessFile stream, IUndoEntry undoEntry) throws IOException {
        return UndoEntry.save(stream, undoEntry);
    }

    @Override
    public int save(DataOutputStream stream, IUndoEntry undoEntry) throws IOException {
        return UndoEntry.save(stream, undoEntry);
    }
}
