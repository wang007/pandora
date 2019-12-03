package com.github.wang007.asyncResult;

import java.util.concurrent.CompletionStage;

/**
 * 代表一个可异步化的对象，且该对象必须有完成的动作
 * 并通知给{@link #toFuture(),#toCompletionStage(),#toCompletionStageResult()}
 *
 * 主要是{@link CompletionStage}api太垃圾了，应该要适当隔离。通过toXXX方法隔离.
 * 喜欢{@link CompletionStage}api的话， 可以{@link #toCompletionStage()}或{@link #toCompletableResult()} 都有
 *
 *
 * @see Future
 * @see CompletableResult
 *
 * created by wang007 on 2019/12/2
 */
public interface Asyncable<T> {

    /**
     * 代表当前异步化的对象是是否已完成
     *
     * @return true: 当前future已完成，false: 未完成
     */
    default boolean isCompleted() {
        return toFuture().isCompleted();
    }

    /**
     * 与之关联的future，当Promise设置结果时，通知到{@link Future}。
     *
     * @return future
     */
     default Future<T> toFuture() {
         return toCompletableResult();
     }

    /**
     * 与之关联的CompletionStage，当Promise设置结果时，通知到{@link CompletionStage}
     * 与java标准库api关联
     *
     * @return CompletionStage
     */
    default CompletionStage<T> toCompletionStage() {
        return toCompletableResult();
    }

    /**
     * 与之关联的AsyncStageResult，当Promise设置结果时，通知到{@link CompletionStage}
     * 与java标准库api关联
     *
     * @return CompletionStage
     */
    CompletableResult<T> toCompletableResult();

}
