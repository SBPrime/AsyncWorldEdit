/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.adapter.map;

import com.sk89q.worldedit.BlockVector2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.map.IMapUtils;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.IntUtils;

/**
 *
 * @author SBPrime
 */
public abstract class MapUtils implements IMapUtils {

    /**
     * The region file pattern
     */
    private final static Pattern s_regionFilePattern = Pattern.compile("r\\.([0-9-]+)\\.([0-9-]+)\\.mca");

    /**
     * The file filter
     */
    private final static FilenameFilter s_fileFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return s_regionFilePattern.matcher(name).matches();
        }
    };
    
    /**
     * Get teh map folder
     *
     * @param w
     * @return
     */
    @Override
    public abstract File getMapFolder(IWorld w);

    /**
     * Get the map region folder
     *
     * @param w
     * @return
     */
    @Override
    public abstract File getMapRegion(IWorld w);

    /**
     * Get the map region files
     *
     * @param w
     * @return
     */
    @Override
    public File[] getMapFiles(IWorld w) {
        File regionFolder = getMapRegion(w);
        if (w == null || regionFolder == null) {
            return null;
        }

        if (!regionFolder.exists() || !regionFolder.canRead()) {
            return null;
        }

        return regionFolder.listFiles(s_fileFilter);

    }

    /**
     * Get the available chunks
     *
     * @param w
     * @return
     */
    @Override
    public BlockVector2D[] getAllWorldChunks(IWorld w) {
        File[] regionFiles = getMapFiles(w);

        if (regionFiles == null) {
            return null;
        }

        final List<BlockVector2D> result = new ArrayList<BlockVector2D>();

        for (File f : regionFiles) {
            if (!f.canRead() || !f.exists()) {
                continue;
            }

            Matcher filename = s_regionFilePattern.matcher(f.getName());
            if (!filename.matches()) {
                //This should not happen but better be safe then sorry...
                continue;
            }

            InOutParam<Integer> rx = InOutParam.Out();
            InOutParam<Integer> rz = InOutParam.Out();

            if (!IntUtils.TryParseInteger(filename.group(1), rx)
                    || !IntUtils.TryParseInteger(filename.group(2), rz)) {
                log(String.format("Unable to get region coords from file %1$s.", f.getName()));
                continue;
            }

            Collection<BlockVector2D> chunks = getRegionChunks(f, rx.getValue() << 5, rz.getValue() << 5);
            if (chunks != null) {
                result.addAll(chunks);
            }

        }

        return result.toArray(new BlockVector2D[0]);
    }

    /**
     * Get all chunks from region file
     *
     * @param regionFile
     * @param cx
     * @param cz
     * @return
     * @throws IOException
     */
    private Collection<BlockVector2D> getRegionChunks(File regionFile, int cx, int cz) {
        final byte[] chunkData = new byte[0x1000];
        final int read;

        FileInputStream in = null;
        try {
            in = new FileInputStream(regionFile);
            read = in.read(chunkData, 0, chunkData.length);
        } catch (Exception ex) {
            ExceptionHelper.printException(ex, String.format("Unable to load available chunks from %1$s", regionFile.getPath()));

            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    //Ignore error when closing
                }
            }
        }

        if (read < chunkData.length) {
            log(String.format("Unable to load available chunks from %1$s, loaded: %2$d expected: %3$d bytes.",
                    regionFile.getPath(), read, chunkData.length));
            return null;
        }

        List<BlockVector2D> result = new ArrayList<BlockVector2D>();

        int idx = 0;
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                boolean hasData = chunkData[idx + 0] != 0 || chunkData[idx + 1] != 0 || 
                        chunkData[idx + 2] != 0 || chunkData[idx + 3] != 0;
                
                idx+=4;
                
                if (hasData) {
                    result.add(new BlockVector2D(cx + x, cz + z));
                }
            }
        }
        
        return result;
    }
}
