package com.github.wang007.asyncResult;

import java.util.concurrent.CompletionStage;

/**
 *
 *
 * created by wang007 on 2019/12/2
 */
public interface Futureable<T> {

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
    CompletionStageResult<T> toCompletionStageResult();

}
