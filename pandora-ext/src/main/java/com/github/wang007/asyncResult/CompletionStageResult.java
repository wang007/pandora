package com.github.wang007.asyncResult;

import java.util.concurrent.CompletionStage;

/**
 * 异步结果的实现。与java8的CompletionStage异步api对接。
 *
 * 这里不继承标准库中的{@link java.util.concurrent.Future}，为了不提供阻塞的api，所有的异步结果必须使用回调处理。
 * 很遗憾的是{@link CompletionStage#toCompletableFuture()}接口api中提供了具体实现的api。fuck
 *
 * 尽管{@link CompletionStage}的api极其丑陋，但是没办法，它标准库api的实现。
 *
 * created by wang007 on 2019/12/2
 */
public interface CompletionStageResult<T> extends Future<T>, CompletionStage<T> {

}
