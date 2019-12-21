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
package org.primesoft.asyncworldedit.playerManager;

import com.sk89q.worldedit.util.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.permissions.IPermission;
import org.primesoft.asyncworldedit.core.AwePlatform;

/**
 *
 * @author SBPrime
 */
public class PlayerEntryTest {
    @Before
    public void init() {
        IAsyncWorldEditCore core = Mockito.mock(IAsyncWorldEditCore.class);
        EventBus eb = new EventBus();
                
        AwePlatform.getInstance().initialize(core);
        
        Mockito.when(core.getEventBus()).thenReturn(eb);
        //Mockito.doNothing().when(eb).post(Mockito.anyObject());
    }
    
    @Test
    public void shouldChangeSpeedWithinAllowedRange() {
        // Given
        IPermissionGroup pg = Mockito.mock(IPermissionGroup.class);
        Mockito.when(pg.getRendererBlocks()).thenReturn(10);        
        PlayerEntry pe = new TestPlayerEntry(pg);
        
        // When
        pe.setRenderBlocks(5);
        
        // Then
        Assert.assertEquals("Render blocks", 5, pe.getRenderBlocks());
    }
    
    @Test
    public void shouldNotChangeSpeedLowerThen1() {
        // Given
        IPermissionGroup pg = Mockito.mock(IPermissionGroup.class);
        Mockito.when(pg.getRendererBlocks()).thenReturn(10);        
        PlayerEntry pe = new TestPlayerEntry(pg);
        
        // When
        pe.setRenderBlocks(0);
        int s1 = pe.getRenderBlocks();
        
        pe.setRenderBlocks(-1);
        int s2 = pe.getRenderBlocks();
        
        // Then
        Assert.assertEquals("Set 1: Render blocks", 10, s1);
        Assert.assertEquals("Set 2: Render blocks", 10, s2);
    }
    
    @Test
    public void shouldNotChangeSpeedHigherThenLimit() {
        // Given
        IPermissionGroup pg = Mockito.mock(IPermissionGroup.class);
        Mockito.when(pg.getRendererBlocks()).thenReturn(10);        
        PlayerEntry pe = new TestPlayerEntry(pg);
        
        // When
        pe.setRenderBlocks(100);
        
        // Then
        Assert.assertEquals("Render blocks", 10, pe.getRenderBlocks());
    }
    
    @Test
    public void shouldChangeSpeedWhenNoLimit() {
        // Given
        IPermissionGroup pg = Mockito.mock(IPermissionGroup.class);
        Mockito.when(pg.getRendererBlocks()).thenReturn(-1);
        PlayerEntry pe = new TestPlayerEntry(pg);
        
        // When
        pe.setRenderBlocks(100);
        
        // Then
        Assert.assertEquals("Render blocks", 100, pe.getRenderBlocks());
    }
    
    private static class TestPlayerEntry extends PlayerEntry {
        
        public TestPlayerEntry(IPermissionGroup pg) {
            super(null, null, pg);
        }

        @Override
        protected boolean sendRawMessage(String msg) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isAllowed(IPermission permission) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isPlayer() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isInGame() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void updatePermissionGroup() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isFake() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
