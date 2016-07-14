/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.blockshub;

import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.blockshub.IBlocksHubApi;
import org.primesoft.blockshub.IBlocksHubApiProvider;

/**
 *
 * @author SBPrime
 */
class BlocksHubV2Factory implements IBlocksHubFactory {
    private static final String NAME = "BlocksHub v2.x";

    @Override
    public String getName() {
        return NAME;
    }
    
    public BlocksHubV2Factory() {
    }

    @Override
    public IBlocksHubIntegration create(Object blocksHub) {
                if (blocksHub == null) {
            return null;
        }
        
        if (!(blocksHub instanceof IBlocksHubApiProvider)) {
            log(String.format("%1$s: ...wrong plugin type", NAME));
            return null;
        }
        
        IBlocksHubApiProvider apiProvider = (IBlocksHubApiProvider)blocksHub;
        IBlocksHubApi api = apiProvider.getApi();
        
        if (api == null) {
            log(String.format("%1$s: ...API not available", NAME));
            return null;
        }
        
        double apiVersion = api.getVersion();
        if (apiVersion < 2 || apiVersion >= 3) {
            log(String.format("%1$s: ...unsupported API v%2$s, supported 2.x", NAME, apiVersion));
            return null;
        }
        
        return new BlocksHubIntegrationV2(api);
    }
    
}
