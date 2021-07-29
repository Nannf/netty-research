package io.netty.example.study.client.dispatcher;

import io.netty.example.study.common.OperationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * @date 2021/7/29 9:31
 */
public class RequestPendingCenter {

    private Map<Long, OperationResultFuture> map = new ConcurrentHashMap<>();

    // 发送时需要放到请求体里
    public void add(Long streamId, OperationResultFuture operationResultFuture) {
        this.map.put(streamId,operationResultFuture);
    }

    public void set(Long streamId, OperationResult operationResult) {
        if (map.containsKey(streamId)) {
            map.get(streamId).setSuccess(operationResult);
            // 防止map无限膨胀
            map.remove(streamId);
        }
    }
}
