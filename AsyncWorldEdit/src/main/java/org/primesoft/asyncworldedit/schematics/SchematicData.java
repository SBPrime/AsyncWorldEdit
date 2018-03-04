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
package org.primesoft.asyncworldedit.schematics;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import java.util.List;

/**
 *
 * @author SBPrime
 */
public class SchematicData {

    private final String m_material;
    private final Short m_w;
    private final Short m_h;
    private final Short m_l;
    private final BlockVector m_origin;
    private byte[] m_blocks;
    private byte[] m_blocksAdd;
    private byte[] m_data;
    private Tag[] m_tileEntities;
    private Tag[] m_entities;

    public String getMaterial() {
        return m_material;
    }

    public Short getW() {
        return m_w;
    }

    public Short getH() {
        return m_h;
    }

    public Short getL() {
        return m_l;
    }

    public byte[] getBlocks() {
        return m_blocks;
    }

    public byte[] getBlocksAdd() {
        return m_blocksAdd;
    }

    public byte[] getData() {
        return m_data;
    }

    public Tag[] getTileEntities() {
        return m_tileEntities;
    }

    public Tag[] getEntities() {
        return m_entities;
    }

    public BlockVector getOrigin() {
        return m_origin;
    }

    /**
     * Is the schematic data valid
     *
     * @return
     */
    public boolean isValid() {
        return hasBlocks() && m_w > 0 && m_h > 0 && m_l > 0
                && m_material != null && m_material.equals("Alpha");
    }

    public void setBlocks(byte[] blocks) {
        if (blocks == null) {
            blocks = new byte[0];
        }

        m_blocks = blocks;
    }

    public void setBlocksEx(byte[] blocksAdd) {
        if (blocksAdd == null) {
            blocksAdd = new byte[0];
        }
        m_blocksAdd = blocksAdd;
    }

    public void setData(byte[] data) {
        if (data == null) {
            data = new byte[0];
        }
        m_data = data;
    }

    public void setTileEntities(List<Tag> tileEntities) {
        if (tileEntities == null) {
            m_tileEntities = new Tag[0];
            return;
        }
        m_tileEntities = tileEntities.toArray(new Tag[0]);
    }

    public void setEntities(List<Tag> entities) {
        if (entities == null) {
            m_entities = new Tag[0];
            return;
        }
        m_entities = entities.toArray(new Tag[0]);
    }

    public boolean hasBlocks() {
        return m_blocks != null && m_blocks.length > 0;
    }

    public boolean hasBlocksAdd() {
        return m_blocksAdd != null && m_blocksAdd.length > 0;
    }

    public boolean hasData() {
        return m_data != null && m_data.length > 0;
    }

    public boolean hasTileEntities() {
        return m_tileEntities != null && m_tileEntities.length > 0;
    }

    public boolean hasEntities() {
        return m_entities != null && m_entities.length > 0;
    }

    public boolean hasOrigin() {
        return m_origin != null;
    }

    public SchematicData(String material,
            Short w, Short h, Short l,
            BlockVector origin) {
        m_material = material;
        m_w = w;
        m_h = h;
        m_l = l;
        m_origin = origin;
        m_blocks = new byte[0];
        m_blocksAdd = new byte[0];
        m_data = new byte[0];
        m_tileEntities = new Tag[0];
        m_entities = new Tag[0];
    }
}
