
## 在K8S集群中启动TIS方法

详细请查看在线文档：https://tis.pub/docs/install/tis/kubernetes/

## 测试数据库启动方法

如果您的测试环境中尚未有合适的测试数据库，可以利用TIS提供的`tis-test-mysql.yaml`脚本在K8S环境中创建MySQL服务，执行：
```shell
 kubectl apply -f tis-test-mysql.yaml
```
创建成功后，在集群内可使用`mysql-0.mysql-svc`作为 MySQL的连接地址，集群外可使用 MySQL 服务的 external-ip作为MySQL的连接地址使用。