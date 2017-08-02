# 接入说明
## demo结构
### certs 存放证书目录
* jssecacerts : 海融易官网证书
* client1.p12 ：海融易颁发的客户端证书，需要时联系海融易方获取
### src
* MainApp： 接收参数，初始HryOpenApiClient 
* HryOpenApiClient 
  * 初始化双向认证httpclient
  * 签名生成
  * 调用接口方法，如：getDailyTaskList（实际接入，只需要实现该方法，其他代码可以复用）

## 接入流程
* 联系海融易 申请 测试 key和secret ， 和客户端证书client1.p12
* 将端证书client1.p12放入certs 目录
* 运行MainApp
   * 设置vm参数 ：  -Djavax.net.ssl.trustStore= certs/jssecacerts -Djavax.net.ssl.trustStorePassword=changeit
   * 设置program参数： -host zopenapi.hairongyi.com:443 -key {申请的key} -secret {申请的secret} -keystore certs/client1.p12 -keypass 123456 -keytype PKCS12
   *  run MainApp的main方法 
