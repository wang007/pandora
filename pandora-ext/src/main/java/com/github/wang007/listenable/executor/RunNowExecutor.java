package com.github.wang007.listenable.executor;

import java.util.concurrent.Executor;

/**
 * 立即执行{@link Runnable}
 *
 * created by wang007 on 2019/12/3
 */
public class RunNowExecutor implements Executor {

    public final static RunNowExecutor Executor = new RunNowExecutor();

    private RunNowExecutor(){}

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
