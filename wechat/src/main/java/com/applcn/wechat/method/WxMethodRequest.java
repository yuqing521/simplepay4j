package com.applcn.wechat.method;

import com.applcn.example.model.*;
import com.applcn.wechat.config.UrlConfig;
import com.applcn.wechat.consts.WxCloseOrderRequestConsts;
import com.applcn.wechat.consts.WxOrderQueryRequestConsts;
import com.applcn.wechat.consts.WxRefundRequestConsts;
import com.applcn.wechat.consts.WxUnifiedOrderRquestConsts;
import com.applcn.example.proxy.MethodProxy;
import com.applcn.example.utils.StringUtil;
import com.applcn.example.utils.XmlUtil;
import com.applcn.wechat.enums.SignTypeEnum;
import com.applcn.wechat.enums.TradeTypeEnum;
import com.applcn.wechat.enums.WxReturnCodeEnum;
import com.applcn.wechat.exception.WxParamException;
import com.applcn.wechat.model.*;
import com.applcn.wechat.response.*;
import com.applcn.wechat.util.HttpUtil;
import com.applcn.wechat.util.SignUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * 微信支付
 * @author dayaoguai
 */
public class WxMethodRequest implements MethodProxy {

    private WxAccountModel accountModel;

    @Override
    public void init(AccountModel accountModel) {
       this.accountModel = (WxAccountModel) accountModel;
    }

    @Override
    public WxUnifiedOrderResponse unifiedOrder(UnifiedOrderModel unifiedOrderModel) throws Exception {
        WxUnifiedOrderModel unifiedOrder = (WxUnifiedOrderModel) unifiedOrderModel;

        Field[] fields = unifiedOrder.getClass().getDeclaredFields();
        Map<String, String> params = new HashMap<>(fields.length);
        TradeTypeEnum tradeType = unifiedOrder.getTradeType();
        SignTypeEnum signType = unifiedOrder.getSignType();

        params.put(WxUnifiedOrderRquestConsts.APP_ID, accountModel.getAppid());
        params.put(WxUnifiedOrderRquestConsts.MCH_ID, accountModel.getMchId());
        params.put(WxUnifiedOrderRquestConsts.NONCE_STR, UUID.randomUUID().toString().replace("-",""));
        params.put(WxUnifiedOrderRquestConsts.OUT_TRADE_NO, unifiedOrder.getOutTradeNo());
        params.put(WxUnifiedOrderRquestConsts.BODY, unifiedOrder.getBody());
        params.put(WxUnifiedOrderRquestConsts.TOTAL_FEE, unifiedOrder.getTotalFee()+"");
        params.put(WxUnifiedOrderRquestConsts.SPBILL_CREATE_IP, unifiedOrder.getSpbillCreateIp());
        params.put(WxUnifiedOrderRquestConsts.NOTIFY_URL, unifiedOrder.getNotifyUrl());
        params.put(WxUnifiedOrderRquestConsts.DEVICE_INFO,unifiedOrder.getDeviceInfo());
        params.put(WxUnifiedOrderRquestConsts.DETAIL,unifiedOrder.getDetail());
        params.put(WxUnifiedOrderRquestConsts.ATTACH,unifiedOrder.getAttach());
        params.put(WxUnifiedOrderRquestConsts.FEE_TYPE,unifiedOrder.getFeeType());
        params.put(WxUnifiedOrderRquestConsts.TIME_START,unifiedOrder.getTimeStart());
        params.put(WxUnifiedOrderRquestConsts.TIME_EXPIRE,unifiedOrder.getTimeExpire());
        params.put(WxUnifiedOrderRquestConsts.GOODS_TAG,unifiedOrder.getGoodsTag());
        params.put(WxUnifiedOrderRquestConsts.PRODUCT_ID,unifiedOrder.getProductId());
        params.put(WxUnifiedOrderRquestConsts.OPENID,unifiedOrder.getOpenid());
        params.put(WxUnifiedOrderRquestConsts.LIMIT_PAY,unifiedOrder.getLimitPay());
        params.put(WxUnifiedOrderRquestConsts.RECEIPT,unifiedOrder.getReceipt());
        params.put(WxUnifiedOrderRquestConsts.SCENE_INFO,unifiedOrder.getSceneInfo());

        if(tradeType != null){
            params.put(WxUnifiedOrderRquestConsts.TRADE_TYPE, tradeType.getValue());
            if(TradeTypeEnum.JSAPI == tradeType){
                if(StringUtil.isEmpty(unifiedOrder.getOpenid())){
                    throw new WxParamException("当trade_type=JSAPI时,用户标识不能为空");
                }
            }else if (TradeTypeEnum.NATIVE == tradeType){
                if(StringUtil.isEmpty(unifiedOrder.getProductId())){
                    throw new WxParamException("当trade_type=NATIVE时,商品id不能为空");
                }
            }

        }

        params.put(WxUnifiedOrderRquestConsts.SIGN, SignUtil.sign(params, accountModel.getKey(), signType));

        String xml = XmlUtil.mapToXml(params, true);

        String responseXml = HttpUtil.post(UrlConfig.getInstance().getUnifiedOrder(), xml);

        WxUnifiedOrderResponse response = XmlUtil.xmlToPojo(responseXml, WxUnifiedOrderResponse.class);

        if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getReturnCode())){
            if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getResultCode())){
                response.setKey(accountModel.getKey());
                response.setSignType(unifiedOrder.getSignType());
                return response;
            }else{
                throw new WxParamException(response.getErrCodeDes());
            }
        }else{
            throw new WxParamException(response.getReturnMsg());
        }
    }

    @Override
    public WxOrderQueryResponse orderQuery(OrderQueryModel orderQueryModel) throws Exception {
        WxOrderQueryModel orderQuery = (WxOrderQueryModel) orderQueryModel;

        Field[] fields = orderQuery.getClass().getDeclaredFields();
        Map<String, String> params = new HashMap<>(fields.length);
        SignTypeEnum signType = orderQuery.getSignType();
        params.put(WxOrderQueryRequestConsts.APP_ID, accountModel.getAppid());
        params.put(WxOrderQueryRequestConsts.MCH_ID, accountModel.getMchId());
        params.put(WxOrderQueryRequestConsts.NONCE_STR, UUID.randomUUID().toString().replace("-",""));
        if(StringUtil.isEmpty(orderQuery.getTransactionId()) && StringUtil.isEmpty(orderQuery.getOutTradeNo())){
            throw new WxParamException("微信订单号或者商户订单号不能为空");
        }

        if(!StringUtil.isEmpty(orderQuery.getTransactionId())){
            params.put(WxOrderQueryRequestConsts.TRANSACTION_ID, orderQuery.getTransactionId());
        }

        if(!StringUtil.isEmpty(orderQuery.getOutTradeNo())){
            params.put(WxOrderQueryRequestConsts.OUT_TRADE_NO, orderQuery.getOutTradeNo());
        }

        params.put(WxOrderQueryRequestConsts.SIGN, SignUtil.sign(params, accountModel.getKey(), signType));

        String xml = XmlUtil.mapToXml(params, true);

        String responseXml = HttpUtil.post(UrlConfig.getInstance().getOrderQuery(), xml);

        WxOrderQueryResponse response = XmlUtil.xmlToPojo(responseXml, WxOrderQueryResponse.class);

        if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getReturnCode())){
            if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getResultCode())){
                return response;
            }else{
                throw new WxParamException(response.getErrCodeDes());
            }
        }else{
            throw new WxParamException(response.getReturnMsg());
        }
    }

    @Override
    public WxCloseOrderResponse closeOrder(CloseOrderModel closeOrderModel) throws Exception {
        WxCloseOrderModel closeOrder = (WxCloseOrderModel) closeOrderModel;

        Field[] fields = closeOrder.getClass().getDeclaredFields();
        Map<String, String> params = new HashMap<>(fields.length);
        SignTypeEnum signType = closeOrder.getSignType();
        params.put(WxCloseOrderRequestConsts.APP_ID, accountModel.getAppid());
        params.put(WxCloseOrderRequestConsts.MCH_ID, accountModel.getMchId());
        params.put(WxCloseOrderRequestConsts.OUT_TRADE_NO, closeOrder.getOutTradeNo());
        params.put(WxCloseOrderRequestConsts.NONCE_STR, UUID.randomUUID().toString().replace("-", ""));

        params.put(WxOrderQueryRequestConsts.SIGN, SignUtil.sign(params, accountModel.getKey(), signType));

        String xml = XmlUtil.mapToXml(params, true);

        String resultXml = HttpUtil.post(UrlConfig.getInstance().getCloseOrder(), xml);

        WxCloseOrderResponse response = XmlUtil.xmlToPojo(resultXml, WxCloseOrderResponse.class);

        if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getReturnCode())){
            if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getResultCode())){
                return response;
            }else{
                throw new WxParamException(response.getResultCode());
            }
        }else{
            throw new WxParamException(response.getReturnMsg());
        }
    }

    @Override
    public WxRefundResponse refund(RefundModel refundModerl) throws Exception {
        WxRefundModel refundModel = (WxRefundModel) refundModerl;

        Field[] fields = refundModel.getClass().getDeclaredFields();
        Map<String, String> params = new HashMap<>(fields.length);
        SignTypeEnum signType = refundModel.getSignType();
        params.put(WxRefundRequestConsts.APP_ID, accountModel.getAppid());
        params.put(WxRefundRequestConsts.MCH_ID, accountModel.getMchId());
        params.put(WxRefundRequestConsts.NONCE_STR, UUID.randomUUID().toString().replace("-", ""));
        params.put(WxRefundRequestConsts.OUT_TRADE_NO, refundModel.getOutTradeNo());
        params.put(WxRefundRequestConsts.SIGN_TYPE, signType.getValue());
        params.put(WxRefundRequestConsts.TRANSACTION_ID, refundModel.getTransactionId());
        params.put(WxRefundRequestConsts.OUT_TRADE_NO, refundModel.getOutTradeNo());
        params.put(WxRefundRequestConsts.OUT_REFUND_NO, refundModel.getOutRefundNo());
        params.put(WxRefundRequestConsts.TOTAL_FEE, refundModel.getTotalFee().toString());
        params.put(WxRefundRequestConsts.REFUND_FEE, refundModel.getRefundFee().toString());
        params.put(WxRefundRequestConsts.REFUND_FEE_TYPE, refundModel.getRefundFeeType());
        params.put(WxRefundRequestConsts.REFUND_DESC, refundModel.getRefundDesc());
        params.put(WxRefundRequestConsts.REFUND_ACCOUNT, refundModel.getRefundAccount());
        params.put(WxRefundRequestConsts.NOTIFY_URL, refundModel.getNotifyUrl());

        params.put(WxRefundRequestConsts.SIGN, SignUtil.sign(params, accountModel.getKey(), signType));

        String xml = XmlUtil.mapToXml(params, true);

        String resultXml = HttpUtil.post(UrlConfig.getInstance().getCloseOrder(), xml);

        WxRefundResponse response = XmlUtil.xmlToPojo(resultXml, WxRefundResponse.class);
        if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getReturnCode())){
            if(WxReturnCodeEnum.SUCCESS.getValue().equals(response.getResultCode())){
                return response;
            }else{
                throw new WxParamException(response.getResultCode());
            }
        }else{
            throw new WxParamException(response.getReturnMsg());
        }
    }

    @Override
    public WxRefundQueryResponse refundquery(RefundQueryModel refundQueryModel) {

        return null;
    }
}