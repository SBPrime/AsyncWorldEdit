/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.platform.bukkit;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.primesoft.asyncworldedit.LoggerProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public final class TileEntityUtils {

    private final static Map<Material, Boolean> m_isTileEntity;

    private TileEntityUtils() {
    }

    static {
        m_isTileEntity = cacheMaterials();
    }

    static boolean isTileEntity(Material m) {
        if (m == null) {
            return false;
        }
        
        return m_isTileEntity.getOrDefault(m, false);
    }

    private static Map<Material, Boolean> cacheMaterials() {
        Method blockData_getState = null;
        Method iBlockData_getBlock = null;
        Method block_isTileEntity = null;

        final Map<Material, Boolean> result = new HashMap<>();
        for (Material m : Material.values()) {
            boolean isTilleEntity = false;
            
            if (m.isBlock()) {
                BlockData bd;
                try {
                    bd = m.createBlockData();                    
                } catch (Exception ex) {
                    ExceptionHelper.printException(ex, "Unable to create block data for '" + m + "'.");
                    bd = null;
                }

                Object blockDataState = null;
                if (bd != null) {
                    if (blockData_getState == null) {
                        blockData_getState = findMethod(bd.getClass(), "getState");
                    }
                    
                    blockDataState = blockData_getState != null ? Reflection.invoke(bd, Object.class, blockData_getState, "Unable to get block state") : null;
                }
                
                Object nmsBlock = null;
                if (blockDataState != null) {
                    if (iBlockData_getBlock == null) {
                        iBlockData_getBlock = findMethod(blockDataState.getClass(), "getBlock");
                    }
                    
                    nmsBlock = iBlockData_getBlock != null ? Reflection.invoke(blockDataState, Object.class, iBlockData_getBlock, "Unable to get NMS block") : null;
                }
                
                if (nmsBlock != null) {
                    if (block_isTileEntity == null) {
                        block_isTileEntity = findMethod(nmsBlock.getClass(), "isTileEntity");
                    }
                    
                    if (block_isTileEntity != null) {
                        isTilleEntity = Reflection.invoke(nmsBlock, Boolean.class, block_isTileEntity, "Unable to check if block is tile entity");
                    }
                }
            }
            
            result.put(m, isTilleEntity);
        }

        return Collections.unmodifiableMap(result);
    }
       
    private static Method findMethod(Class<?> cls, String name) {
        Method result = null;
        while (cls != null && !Object.class.equals(cls) && result == null) {
            Optional<Method> method = Stream.of(cls.getMethods())
                    .filter(i -> i.getName().equals(name))
                    .filter(i -> i.getParameterCount() == 0)
                    .findFirst();
                        
            cls = cls.getSuperclass();
            
            if (method.isPresent()) {
                result = method.get();
            }
        }
        
        if (result == null) {
            LoggerProvider.log("Unable to find '" + name + "' in '" + (cls == null ? "null" : cls.getName()) + "'");
        }
        return result;
    }

}
