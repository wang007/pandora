package com.github.wang007.listenable.executor;

import java.util.concurrent.Executor;

/**
 * created by wang007 on 2019/12/3
 */
public class RunNowExecutor implements Executor {

    public final static RunNowExecutor executor = new RunNowExecutor();

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
