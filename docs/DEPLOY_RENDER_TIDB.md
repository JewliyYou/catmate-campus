# Render + TiDB Cloud 免费部署

该方案将 Vue 页面和 Spring Boot API 放在同一个 Render Web Service 中，使用同一个 HTTPS 域名；TiDB Cloud Starter 通过 MySQL 协议保存业务数据。Render 免费服务闲置 15 分钟后会休眠，下一次访问可能需要等待约一分钟。

## 1. 创建 TiDB Cloud Starter

1. 注册并登录 TiDB Cloud。
2. 创建 **Starter** 实例，将 Spending Limit 设为 `0`；地区优先选择离 Render 新加坡节点最近的可用区域。
3. 创建数据库用户并保存连接页面提供的主机、端口、用户名和密码。
4. 使用公共连接地址，并按 TiDB 控制台要求配置访问范围。
5. 在 SQL 控制台执行：

```sql
CREATE DATABASE IF NOT EXISTS catmate CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

不要把数据库密码写入源码、Dockerfile、`render.yaml` 或 Git 仓库。

## 2. 将项目上传到 GitHub

在项目根目录创建 Git 仓库并推送到自己的 GitHub 仓库。提交前确认 `.env`、数据库密码和其他真实凭据不在待提交文件中。

## 3. 在 Render 创建服务

1. 登录 Render，选择 **New > Blueprint**。
2. 连接 GitHub 仓库；Render 会读取根目录的 `render.yaml`。
3. 选择免费规格，并填写所有标记为需要手动输入的环境变量。

| 变量 | 填写内容 |
|---|---|
| `DB_URL` | TiDB JDBC 地址，格式见下方示例 |
| `DB_USERNAME` | TiDB 连接用户名 |
| `DB_PASSWORD` | TiDB 连接密码 |
| `INITIAL_ADMIN_USERNAME` | 3 至 20 位字母、数字或下划线组成的管理员账号 |
| `INITIAL_ADMIN_PASSWORD` | 至少 12 个字符的独立强密码 |

`DB_URL` 示例（必须替换主机和端口，不能照抄占位符）：

```text
jdbc:mysql://<TIDB_HOST>:<TIDB_PORT>/catmate?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
```

`SEED_DEMO_USERS=false` 已写入部署配置，云端不会创建 `user / 123456` 和 `admin / admin123`。管理员仅在数据库中不存在同名账号时创建；以后修改 Render 中的初始密码变量不会覆盖已有账号密码。

## 4. 验证部署

Render 构建成功后会提供 `https://...onrender.com` 地址。依次确认：

1. 根地址能打开“猫伴校园”。
2. 使用部署时设置的管理员账号登录。
3. 猫咪档案能加载图片和数据。
4. 新建一条测试记录，等待重新部署或服务休眠后再次确认数据仍存在。
5. Render 日志中没有数据库连接、内存不足或重复重启错误。

## 5. 免费规格注意事项

- Render 服务休眠后的第一次访问较慢，属于免费规格的正常现象。
- 不要使用定时请求绕过平台休眠限制。
- TiDB Starter 的消费上限保持为 `0`，避免意外进入付费用量。
- 定期从 TiDB 导出备份；免费服务不提供生产级可用性承诺。
- 对公网开放前检查猫咪精确窝点、电话、住址和证明材料等敏感信息。
