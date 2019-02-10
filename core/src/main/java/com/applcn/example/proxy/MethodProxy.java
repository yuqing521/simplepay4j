package com.applcn.example.proxy;

import com.applcn.example.model.*;
import com.applcn.example.result.*;

/**
 * 支付代理对象
 * @author dayaoguai
 */
public interface MethodProxy {

    /**
     * 初始化
     * @param accountModel
     */
    void init(AccountModel accountModel);

    /**
     * 统一下单
     * @return
     */
    UnifiedOrderResult unifiedOrder(UnifiedOrderModel unifiedOrderModel) throws Exception;

    /**
     * 订单查询
     * @param orderQueryModel
     * @return
     */
    QueryOrderResult orderQuery(OrderQueryModel orderQueryModel) throws Exception;

    /**
     * 关闭订单
     * @param closeOrderModel
     * @return
     */
    CloseOrderResult closeOrder(CloseOrderModel closeOrderModel) throws Exception;

    /**
     * 申请退款
     * @param refundModel
     * @return
     */
    RefundResult refund(RefundModel refundModel) throws Exception;

    /**
     * 查询退款
     * @param refundQueryModel
     * @return
     */
    RefundQueryResult refundquery(RefundQueryModel refundQueryModel);
}