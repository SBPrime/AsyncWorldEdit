/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.configuration;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * All operations that can by done in async mode
 * @author SBPrime
 */
public class WorldeditOperations 
{
    private static final Map<String, WorldeditOperations> s_allOperations = new ConcurrentHashMap<>();
    
    public static final WorldeditOperations undo = create("undo");
    public static final WorldeditOperations redo = create("redo");
    public static final WorldeditOperations fillXZ = create("fillXZ");
    public static final WorldeditOperations fillXY = create("fillXY");
    public static final WorldeditOperations fillZY = create("fillZY");
    public static final WorldeditOperations fill3d = create("fill3d");
    public static final WorldeditOperations removeAbove = create("removeAbove");
    public static final WorldeditOperations removeBelow = create("removeBelow");
    public static final WorldeditOperations removeNear = create("removeNear");
    public static final WorldeditOperations setBlocks = create("setBlocks");
    public static final WorldeditOperations replaceBlocks = create("replaceBlocks");
    public static final WorldeditOperations makeCuboidFaces = create("makeCuboidFaces");
    public static final WorldeditOperations makeCuboidWalls = create("makeCuboidWalls");
    public static final WorldeditOperations overlayCuboidBlocks = create("overlayCuboidBlocks");
    public static final WorldeditOperations naturalizeCuboidBlocks = create("naturalizeCuboidBlocks");
    public static final WorldeditOperations stackCuboidRegion = create("stackCuboidRegion");
    public static final WorldeditOperations moveCuboidRegion = create("moveCuboidRegion");
    public static final WorldeditOperations drainArea = create("drainArea");
    public static final WorldeditOperations fixLiquid = create("fixLiquid");
    public static final WorldeditOperations makeCylinder = create("makeCylinder");
    public static final WorldeditOperations makeSphere = create("makeSphere");
    public static final WorldeditOperations makePyramid = create("makePyramid");
    public static final WorldeditOperations thaw = create("thaw");
    public static final WorldeditOperations simulateSnow = create("simulateSnow");
    public static final WorldeditOperations green = create("green");
    public static final WorldeditOperations makePumpkinPatches = create("makePumpkinPatches");
    public static final WorldeditOperations makeForest = create("makeForest");
    public static final WorldeditOperations makeShape = create("makeShape");
    public static final WorldeditOperations deformRegion = create("deformRegion");
    public static final WorldeditOperations hollowOutRegion = create("hollowOutRegion");
    public static final WorldeditOperations paste = create("paste");
    public static final WorldeditOperations copy = create("copy");
    public static final WorldeditOperations cut = create("cut");
    public static final WorldeditOperations regenerate = create("regenerate");
    public static final WorldeditOperations generate = create("generate");
    public static final WorldeditOperations center = create("center");
    public static final WorldeditOperations drawLine = create("drawLine");
    public static final WorldeditOperations drawSpline = create("drawSpline");
    public static final WorldeditOperations makeBiomeShape = create("makeBiomeShape");
    public static final WorldeditOperations forest = create("forest");
    public static final WorldeditOperations flora = create("flora");
    public static final WorldeditOperations setBiome = create("setBiome");
    public static final WorldeditOperations loadSchematic = create("loadSchematic");
    public static final WorldeditOperations saveSchematic = create("saveSchematic");
    public static final WorldeditOperations craftScript = create("craftScript");
    public static final WorldeditOperations makeFaces = create("makeFaces");
    public static final WorldeditOperations makeWalls = create("makeWalls");
    public static final WorldeditOperations overlayBlocks = create("overlayBlocks");
    public static final WorldeditOperations naturalizeBlocks = create("naturalizeBlocks");
    public static final WorldeditOperations stackRegion = create("stackRegion");
    public static final WorldeditOperations moveRegion = create("moveRegion");
    public static final WorldeditOperations smooth = create("smooth");
    public static final WorldeditOperations restore = create("restore");
    public static final WorldeditOperations execute = create("execute");
    public static final WorldeditOperations executeLast = create("executeLast");
    //Schematic commands
    public static final WorldeditOperations schematicInfo = create("schematicInfo");
    public static final WorldeditOperations placeSchematic = create("placeSchematic");
    public static final WorldeditOperations load = create("load");
    public static final WorldeditOperations save = create("save");
    //Chunk commands
    public static final WorldeditOperations chunkSet = create("chunkSet");
    public static final WorldeditOperations chunkSetBiome = create("chunkSetBiome");
    public static final WorldeditOperations chunkReplace = create("chunkReplace");
    public static final WorldeditOperations chunkClear = create("chunkClear");
    public static final WorldeditOperations chunkFill = create("chunkFill");
    public static final WorldeditOperations chunkClone = create("chunkClone");
    public static final WorldeditOperations chunkPaste = create("chunkPaste");
    public static final WorldeditOperations chunkCopy = create("chunkCopy");
    public static final WorldeditOperations chunkRelight = create("chunkRelight");
    
    public static final WorldeditOperations brush = create("brush");
    public static final WorldeditOperations tool = create("tool");    
    
    // Tools
    public static final WorldeditOperations navigationWand = WorldeditOperations.create("navigationWand");
    public static final WorldeditOperations tree = create("tree");
    public static final WorldeditOperations pickaxe = create("pickaxe"); 

    public static WorldeditOperations valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name", new NullPointerException());
        }
        
        return s_allOperations.get(name.toLowerCase());
    }
    
    public static WorldeditOperations create(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name", new NullPointerException());
        }
        
        return s_allOperations.computeIfAbsent(name.toLowerCase(), WorldeditOperations::new);
    }

    public static Stream<WorldeditOperations> values() {
        return Stream.of(WorldeditOperations.class.getFields())
                .filter(i -> i.getModifiers() == (Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC))
                .filter(i -> WorldeditOperations.class.isAssignableFrom(i.getType()))
                .map(i -> {
            try {
                return (WorldeditOperations)i.get(null);
            } catch (Exception ex) {
                return null;
            }
        }).filter(i -> i != null);
    }
    
    private final String m_name;
    
    private WorldeditOperations(String name) {
        m_name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WorldeditOperations)) {
            return false;
        }
        
        return m_name.equals(((WorldeditOperations)o).m_name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.m_name);
        return hash;
    }

    @Override
    public String toString() {
        return m_name;
    }
}
