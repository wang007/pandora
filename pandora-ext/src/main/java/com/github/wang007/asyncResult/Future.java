package com.github.wang007.asyncResult;

/**
 * 当前future所有操作，都不会产生阻塞。
 *
 * {@link java.util.concurrent.Future}是阻塞
 *
 * created by wang007 on 2019/12/1
 */
public interface Future<T> extends AsyncResult<T>,Futureable<T> {

    /**
     * 代表当前future是否已完成
     *
     * @return true: 当前future已完成，false: 未完成
     */
    boolean isCompleted();

    /**
     * 添加异步回调处理器，可以添加多个。
     *
     * @param handler 异步回调处理器
     * @return this
     */
    Future<T> addHandler(Handler<AsyncResult<T>> handler);

    @Override
    default Future<T> toFuture() {
        return this;
    }
}
