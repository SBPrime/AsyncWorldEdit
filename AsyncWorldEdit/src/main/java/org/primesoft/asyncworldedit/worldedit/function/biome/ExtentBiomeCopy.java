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
package org.primesoft.asyncworldedit.worldedit.function.biome;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author SBPrime
 */
public class ExtentBiomeCopy implements RegionFunction {

    private final Extent m_source;
    private final Vector m_from;

    private final Extent m_destination;
    private final Vector m_to;

    private final Transform m_transform;

    private final HashMap<Integer, HashMap<Integer, Integer>> m_biomeCache;

    /**
     * Make a new biome copy.
     *
     * @param source
     * @param from
     * @param destination
     * @param to
     * @param transform
     * @param singleSet
     */
    public ExtentBiomeCopy(Extent source, Vector from,
            Extent destination, Vector to,
            Transform transform, boolean singleSet) {

        m_source = source;
        m_from = from;

        m_destination = destination;
        m_to = to;

        m_transform = transform;

        m_biomeCache = singleSet ? new LinkedHashMap<Integer, HashMap<Integer, Integer>>() : null;
    }

    @Override
    public boolean apply(Vector position) throws WorldEditException {
        BaseBiome biome = m_source.getBiome(position.toVector2D());
        Vector orig = position.subtract(m_from);
        Vector transformed = m_transform.apply(orig);

        BlockVector2D biomePosition = transformed.add(m_to).toVector2D().toBlockVector2D();        
        if (m_biomeCache == null) {
            return m_destination.setBiome(biomePosition, biome);
        }

        Integer x = biomePosition.getBlockX();
        Integer z = biomePosition.getBlockZ();

        HashMap<Integer, Integer> entry = m_biomeCache.get(x);
        if (entry == null) {
            entry = new LinkedHashMap<Integer, Integer>();
            m_biomeCache.put(x, entry);
        }

        Integer oldBiomeId = entry.get(z);
        int newBiomeId = biome.getId();
        if (oldBiomeId != null && oldBiomeId == newBiomeId) {
            return false;
        }

        if (!m_destination.setBiome(biomePosition, biome)) {
            return false;
        }

        entry.put(z, newBiomeId);
        return true;
    }
}
