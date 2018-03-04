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
package org.primesoft.asyncworldedit.utils;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import java.lang.reflect.Field;
import java.util.List;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.IClassScanner;
import org.primesoft.asyncworldedit.api.inner.IClassScannerResult;
import org.primesoft.asyncworldedit.injector.scanner.ClassScannerResult;
import org.primesoft.asyncworldedit.worldedit.extent.MultiThreadExtent;

/**
 *
 * @author SBPrime
 */
public class MaskUtils {

    /**
     * Inject new extent to mask
     *
     * @param classScanner
     * @param source
     * @param extent
     * @return 
     */
    public static Extent injectExtent(IClassScanner classScanner, Mask source, Extent extent) {
        if (source == null || extent == null) {
            return null;
        }

        List<IClassScannerResult> result = classScanner.scan(new Class<?>[]{Extent.class}, source);
        
        
        for (IClassScannerResult r : result) {
            Object o = r.getValue();
            if (o instanceof MultiThreadExtent) {
                return (MultiThreadExtent)o;
            }
        }
        
        if (!validate(result)) {
            log("-----------------------------------------------------------------------");
            log("Warning: unable to inject extents to mask.");
            log(String.format("Mask type: %1$s", source));
            log("Send this message to the author of the plugin!");
            log("-----------------------------------------------------------------------");
            return null;
        }
        
        final Extent maskExtent = result.size() > 0 ? (Extent)result.get(0).getValue() : null;

        for (IClassScannerResult entry : result) {            
            Field field = entry.getField();
            Object parent = entry.getOwner();

            if (field == null || parent == null) {
                continue;
            }

            Reflection.set(parent, field, extent, "extent");
        }
        
        return maskExtent;
    }
    
    /**
     * Inject new extent to mask
     *
     * @param classScanner
     * @param source
     * @param extent
     * @return 
     */
    public static Extent injectExtent(IClassScanner classScanner, Mask2D source, Extent extent) {
        if (source == null || extent == null) {
            return null;
        }

        List<IClassScannerResult> result = classScanner.scan(new Class<?>[]{Extent.class}, source);
        
        
        for (IClassScannerResult r : result) {
            Object o = r.getValue();
            if (o instanceof MultiThreadExtent) {
                return (MultiThreadExtent)o;
            }
        }
        
        if (!validate(result)) {
            log("-----------------------------------------------------------------------");
            log("Warning: unable to inject extents to mask.");
            log(String.format("Mask type: %1$s", source));
            log("Send this message to the author of the plugin!");
            log("-----------------------------------------------------------------------");
            return null;
        }
        
        final Extent maskExtent = result.size() > 0 ? (Extent)result.get(0).getValue() : null;

        for (IClassScannerResult entry : result) {            
            Field field = entry.getField();
            Object parent = entry.getOwner();

            if (field == null || parent == null) {
                continue;
            }

            Reflection.set(parent, field, extent, "extent");
        }
        
        return maskExtent;
    }

    
    /**
     * Validate the scann result
     *
     * @param results
     * @return
     */
    private static boolean validate(List<IClassScannerResult> results) {
        Extent extent = null;

        if (results.isEmpty()) {
            return true;
        }

        for (IClassScannerResult entry : results) {
            Extent v = (Extent) entry.getValue();

            if (extent == null) {
                extent = v;
            } else if (extent != v) {
                //We support only single extent at this moment
                return false;
            }
        }

        return true;
    }
}
