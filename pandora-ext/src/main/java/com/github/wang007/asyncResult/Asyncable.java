package com.github.wang007.asyncResult;

import java.util.concurrent.CompletionStage;

/**
 * 代表一个可异步化的对象，且该对象必须有完成的动作
 * 并通知给{@link #toFuture(),#toCompletionStage(),#toCompletionStageResult()}
 *
 * @see Future
 * @see AsyncStageResult
 *
 * created by wang007 on 2019/12/2
 */
public interface Asyncable<T> {

    /**
     * 代表当前future是否已完成
     *
     * @return true: 当前future已完成，false: 未完成
     */
    boolean isCompleted();

    /**
     * 与之关联的future，当Promise设置结果时，通知到{@link Future}。
     *
     * @return future
     */
    Future<T> toFuture();

    /**
     * 与之关联的Future，当Promise设置结果时，通知到{@link CompletionStage}
     * 与java标准库api关联
     *
     * @return CompletionStage
     */
    CompletionStage<T> toCompletionStage();

    /**
     * 与之关联的Future，当Promise设置结果时，通知到{@link CompletionStage}
     * 与java标准库api关联
     *
     * @return CompletionStage
     */
    AsyncStageResult<T> toAsyncStageResult();

}
