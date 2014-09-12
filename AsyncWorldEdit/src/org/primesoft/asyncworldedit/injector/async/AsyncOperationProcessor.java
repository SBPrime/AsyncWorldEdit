/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.injector.async;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.injector.validators.OperationValidator;
import org.primesoft.asyncworldedit.injector.validators.StackValidator;
import com.sk89q.worldedit.function.operation.Operation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.injector.ExceptionOperationAction;
import org.primesoft.asyncworldedit.injector.IOperationProcessor;
import org.primesoft.asyncworldedit.injector.OperationAction;
import org.primesoft.asyncworldedit.injector.scanner.ClassScanner;
import org.primesoft.asyncworldedit.injector.scanner.ClassScannerResult;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.Reflection;
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
        final InOutParam<String> operationName = InOutParam.Out();
        
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
        final UUID playerUuid = asyncSession.getPlayer();
        final int jobId = m_blockPlacer.getJobId(playerUuid);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerUuid, cancelableSession, jobId, 
                operationName.getValue());

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerUuid, job);
        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(cancelableSession, playerUuid, 
                operationName.getValue(), m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        try {
                            //m_wait.checkAndWait(null);
                            action.Execute(op);

                            return cancelableSession.getChangeSet().size();
                        } catch (Exception ex) {
                            if (ex instanceof MaxChangedBlocksException) {
                                throw (MaxChangedBlocksException) ex;
                            }

                            //Silently discard other errors :(
                            return 0;
                        }
                    }
                });
    }

    @Override
    public void process(final Operation op, final OperationAction action) {
        final InOutParam<String> operationName = InOutParam.Out();
        
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
        final UUID playerUuid = asyncSession.getPlayer();
        final int jobId = m_blockPlacer.getJobId(playerUuid);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerUuid, cancelableSession, jobId, 
                operationName.getValue());

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerUuid, job);
        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(cancelableSession, playerUuid, 
                operationName.getValue(), m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        //m_wait.checkAndWait(null);
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
