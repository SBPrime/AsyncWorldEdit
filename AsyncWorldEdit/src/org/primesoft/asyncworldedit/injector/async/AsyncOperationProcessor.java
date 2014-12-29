/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector.async;

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.injector.validators.OperationValidator;
import org.primesoft.asyncworldedit.injector.validators.StackValidator;
import com.sk89q.worldedit.function.operation.Operation;
import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.PlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.injector.classfactory.IOperationProcessor;
import org.primesoft.asyncworldedit.injector.scanner.ClassScanner;
import org.primesoft.asyncworldedit.injector.scanner.ClassScannerResult;
import org.primesoft.asyncworldedit.injector.utils.ExceptionOperationAction;
import org.primesoft.asyncworldedit.injector.utils.OperationAction;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.utils.WaitFor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.AsyncTask;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author SBPrime
 */
public class AsyncOperationProcessor implements IOperationProcessor {

    /**
     * Bukkit schedule
     */
    private final BukkitScheduler m_schedule;

    /**
     * The parent plugin
     */
    private final AsyncWorldEditMain m_plugin;

    /**
     * Async block placer
     */
    protected final BlockPlacer m_blockPlacer;

    public AsyncOperationProcessor(AsyncWorldEditMain plugin) {
        m_plugin = plugin;
        m_schedule = m_plugin.getServer().getScheduler();
        m_blockPlacer = m_plugin.getBlockPlacer();
    }

    @Override
    public <TException extends Exception> void process(final Operation op,
            final ExceptionOperationAction<TException> action) throws TException {
        InOutParam<String> operationName = InOutParam.Out();
        
        if (!StackValidator.isVaild(operationName) || !OperationValidator.isValid(op)) {
            action.Execute(op);
            return;
        }

        List<ClassScannerResult<AsyncEditSession>> sessions = ClassScanner.scan(AsyncEditSession.class, op);
        if (!validate(sessions)) {
            action.Execute(op);
            return;
        }

        final AsyncEditSession asyncSession = sessions.get(0).getValue();
        final String name = operationName.getValue();
        if (!asyncSession.checkAsync(name))
        {
            action.Execute(op);
            return;
        }
        
        final WaitFor wait = asyncSession.getWait();
        final PlayerEntry playerEntry = asyncSession.getPlayer();
        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerEntry, cancelableSession, jobId, name);

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerEntry, job);
        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(cancelableSession, playerEntry, 
                name, m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        try {
                            wait.checkAndWait(null);
                            action.Execute(op);

                            return cancelableSession.getChangeSet().size();
                        } catch (Exception ex) {
                            if (ex instanceof MaxChangedBlocksException) {
                                throw (MaxChangedBlocksException) ex;
                            }

                            ExceptionHelper.printException(ex, "Error while processing async operation " + name);
                            //Silently discard other errors :(
                            return 0;
                        }
                    }
                });
    }

    @Override
    public void process(final Operation op, final OperationAction action) {
        InOutParam<String> operationName = InOutParam.Out();
        
        if (!StackValidator.isVaild(operationName) || !OperationValidator.isValid(op)) {
            action.Execute(op);
            return;
        }

        List<ClassScannerResult<AsyncEditSession>> sessions = ClassScanner.scan(AsyncEditSession.class, op);
        if (!validate(sessions)) {
            action.Execute(op);
            return;
        }

        final AsyncEditSession asyncSession = sessions.get(0).getValue();
        final String name = operationName.getValue();
        
        if (!asyncSession.checkAsync(name))
        {
            action.Execute(op);
            return;
        }
        
        final WaitFor wait = asyncSession.getWait();
        final PlayerEntry playerEntry = asyncSession.getPlayer();
        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerEntry, cancelableSession, jobId, name);

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerEntry, job);
        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(cancelableSession, playerEntry, 
                name, m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        wait.checkAndWait(null);
                        action.Execute(op);

                        return cancelableSession.getChangeSet().size();
                    }
                });
    }

    /**
     * Validate the edit sessions
     *
     * @param sessions
     * @return
     */
    private boolean validate(List<ClassScannerResult<AsyncEditSession>> sessions) {
        AsyncEditSession session = null;

        if (sessions.isEmpty()) {
            return false;
        }

        for (ClassScannerResult<AsyncEditSession> entry : sessions) {
            AsyncEditSession s = entry.getValue();
            if (session == null) {
                session = s;
            } else if (session != s) {
                //We support only single edit session at this moment
                return false;
            }
        }

        return session != null;
    }

    /**
     * Inject edit session to operation
     *
     * @param sessions
     * @param value
     */
    private void injectEditSession(List<ClassScannerResult<AsyncEditSession>> sessions, Object value) {
        for (ClassScannerResult<AsyncEditSession> entry : sessions) {
            Field field = entry.getField();
            Object parent = entry.getOwner();

            if (field == null || parent == null) {
                continue;
            }

            Reflection.set(parent, field, value, "edit session");
        }
    }
}
