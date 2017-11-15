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
   * 设置vm参数 ：  -Djavax.net.ssl.trustStore= certs/jssecacerts -Djavax.net.ssl.trustStorePassword=changeit 或者 把这文件 hry-openapi-client-demo/certs/jssecacerts 复制到 使用 jre/lib/security目录下
   * 设置program参数： -host zopenapi.hairongyi.com:443 -key {申请的key} -secret {申请的secret} -keystore certs/client1.p12 -keypass 123456 -keytype PKCS12
   *  run MainApp的main方法 
   
## 运行环境要求
* jdk1.7 以上
* jdk1.6 需要自行更改https的ssl初始化
   
## 环境说明
* 测试环境1 host c1openapi.hairongyi.com:443
* 测试环境2 host c2openapi.hairongyi.com:443
* 测试环境3 host c3openapi.hairongyi.com:443
* 准生产环境 host zopenapi.hairongyi.com:443
* 生产环境 host openapi.hairongyi.com:443   
   
## 注点意
*  请求参数的值是Object类型，Map<String, Object> params = new HashMap<>();

###  List<String> 的传入
```
     params.put("key",new String[]{"a","b"}
     or
     List<String> list = new ArrayList();
     list.add("a");
     list.add("b")；
     params.put("key",list)
```

###  List<Object> 是一个对象的list
```
   List<Map<String,Object>> list = new ArrayList();
   Map<String,Object> oneItem = new HashMap(String,Object);
   oneItem.put("key1",value1);
   oneItem.put("key2",value2);
   ...
   list.add(oneItem)
   ....
   params.put("key",list)
```

