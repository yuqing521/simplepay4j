### 项目简介
simplepay4j是一个集成支付工具的java拓展包，目前只集成了微信支付基本功能

oschina地址：https://gitee.com/yaoguaiDa/simplepay4j  
github地址：https://github.com/YaoguaiDa/simplepay4j

* 为什么要重复造轮子？  
——年假在家闲呐！
### 项目特点
* 少依赖：除了yaml解析工具之外所有工具使用原生java开发，项目体积小
* 使用方便：隐藏了开发中繁琐的细节
* 易拓展：开发者可以根据需求灵活拓展自己的支付工具
* 容易理解：有完整的注释，以后也会有完善的使用文档和demo
* 模块化：可以按需引入需要的支付功能

### 使用方法
* 引入项目
    * 使用最新版：
        * 下载项目到本地
        * 将项目导入到ide
        * 在根目录运行命令: mvn clean install
        * 在自己的项目pom.xml文件<dependencies></dependencies>节点下加入
    ```xml
     <dependency>
        <groupId>com.applcn</groupId>
        <artifactId>simplePay4j-wechat</artifactId>
        <version>1.0.0-alpha</version>
     </dependency>
    ```
        * 刷新maven
    * maven仓库中的版本(包名调整之前的)：
        * 直接在自己的项目pom.xml文件<dependencies></dependencies>节点下加入
    ```xml
     <dependency>
        <groupId>com.applcn</groupId>
        <artifactId>wechat</artifactId>
        <version>1.0.0-alpha</version>
     </dependency>
    ```
        * 刷新maven
* 简单使用
    * jsapi/小程序支付：
    ```java
      WxAccountModel accountModel = new WxAccountModel("公众号appid/小程序appid","微信商户号", "商户秘钥");
      MethodProxy proxy = Wechat.orderMethod(accountModel);
      WxUnifiedOrderModel unifiedOrderModel = new WxUnifiedOrderModel("商品描述","商户订单号","商品价格，单位为分",
              "客户端ip", "回调地址", TradeTypeEnum.JSAPI);

      unifiedOrderModel.expand("发起支付的用户openid");
      WxUnifiedOrderResponse result = (WxUnifiedOrderResponse) proxy.unifiedOrder(unifiedOrderModel);
    ```
    返回的result直接转成json返回给前端，前端拿来调用支付api即可
    * 处理支付结果通知回调：
    ```java
      NotifyManageProxy proxy = Wechat.notifyManage(inputStream, "商户秘钥");
      WxOrderModel model = (WxOrderModel) proxy.manage();
      if(model != null){
        // TODO 处理用户自己的业务逻辑,如对比订单号，对比金额
      }else{
        // TODO 此处为签名错误返回信息构造可自行按照微信官方文档来写
        Map<String,String> error = new HashMap<>(2);
        error.put("return_code", "FAIL");
        error.put("return_msg", "签名错误");
        return XmlUtil.mapToXml(error, true);
      }
    ```
    inputStream: 即：java.io.InputStream，为前端出来的数据流
    * 订单查询：
        待完成
    * 关闭订单：
        待完成
    * 申请退款：
        待完成
    
### 其它
* 项目demo在example/test/java/下，使用的是springboottest，亲测
* 项目才开始还没写完，还需要优化，也还没经过详细测试，仅作为技术交流用，反正别拿去生产环境用！别拿去生产环境用！别拿去生产环境用！    