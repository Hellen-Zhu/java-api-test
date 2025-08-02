# API Auto Test

一个基于 Spring Boot 的 API 自动化测试平台，提供 RESTful API 接口用于管理和执行自动化测试用例。

## 🚀 项目简介

API Auto Test 是一个现代化的 API 测试管理平台，使用 Spring Boot 3.5.3 构建，支持 PostgreSQL 数据库，提供完整的测试用例管理功能。

## 🛠️ 技术栈

- **后端框架**: Spring Boot 3.5.3
- **数据库**: PostgreSQL
- **ORM框架**: MyBatis 3.0.3
- **API文档**: SpringDoc OpenAPI 3 (Swagger)
- **JSON处理**: FastJSON2
- **构建工具**: Gradle
- **Java版本**: JDK 17
- **开发工具**: Lombok

## 📋 功能特性

- ✅ RESTful API 接口
- ✅ 自动化测试用例管理
- ✅ PostgreSQL 数据库支持
- ✅ Swagger API 文档
- ✅ 异步处理支持
- ✅ 驼峰命名转换
- ✅ 完整的错误处理

## 🏗️ 项目结构

```
src/main/java/com/api/
├── ApiAutoTestApplication.java    # 主启动类
├── controller/                    # 控制器层
│   ├── AutoCaseController.java   # 测试用例控制器
│   └── DemoController.java       # 演示控制器
├── service/                      # 服务层
│   └── impl/
│       └── AutoCaseServiceImpl.java
├── mapper/                       # 数据访问层
│   └── AutoCaseMapper.java
├── model/                        # 数据模型
│   └── TestAPIParameter.java
├── entities/                     # 实体类
├── utils/                        # 工具类
└── config/                       # 配置类

src/main/resources/
├── application.properties        # 应用配置文件
├── mybatis.xml                   # MyBatis 配置文件
└── mappers/                      # MyBatis 映射文件
    └── insert.xml
```

## 🚀 快速开始

### 环境要求

- JDK 17 或更高版本
- PostgreSQL 数据库
- Gradle 7.0 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd api-auto-test
   ```

2. **配置数据库**
   
   在 `src/main/resources/application.properties` 中配置数据库连接：
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/autotest
   spring.datasource.username=admin
   spring.datasource.password=password
   spring.datasource.driver-class-name=org.postgresql.Driver
   ```

3. **创建数据库**
   ```sql
   CREATE DATABASE autotest;
   ```

4. **运行应用**
   ```bash
   # 使用 Gradle
   ./gradlew bootRun
   
   # 或者构建后运行
   ./gradlew build
   java -jar build/libs/api-auto-test-0.0.1-SNAPSHOT.jar
   ```

5. **访问应用**
   - 应用地址: http://localhost:8081
   - API文档: http://localhost:8081/swagger-ui.html

## 📚 API 文档

### 测试用例管理

#### 保存测试用例
```http
POST /saveAutoCase
Content-Type: application/json

{
  "name": "测试用例名称",
  "description": "测试用例描述",
  "parameters": {
    "param1": "value1",
    "param2": "value2"
  }
}
```

### 演示接口

#### 问候接口
```http
GET /hello?name=World
```

#### 获取列表
```http
GET /getList?start=1&end=10
```

#### 路径参数示例
```http
GET /myGetList/param/1/10
```

## 🔧 配置说明

### 应用配置 (application.properties)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.application.name` | 应用名称 | api-auto-test |
| `server.port` | 服务端口 | 8081 |
| `spring.datasource.url` | 数据库连接URL | jdbc:postgresql://localhost:5432/autotest |
| `spring.datasource.username` | 数据库用户名 | admin |
| `spring.datasource.password` | 数据库密码 | password |
| `mybatis.mapper-locations` | MyBatis映射文件位置 | classpath*:mappers/**/*.xml |
| `mybatis.type-aliases-package` | 类型别名包 | com.api.model |

### MyBatis 配置

项目使用 `mybatis.xml` 作为主要配置文件，包含：
- 驼峰命名转换设置
- 类型别名配置
- 映射器配置

## 🧪 测试

运行测试：
```bash
./gradlew test
```

## 📦 构建

构建项目：
```bash
./gradlew build
```

构建产物位于 `build/libs/` 目录。

## 🔍 开发指南

### 添加新的 API 接口

1. 在 `controller` 包下创建新的控制器类
2. 使用 `@RestController` 注解标记控制器
3. 使用 `@Operation` 注解添加 Swagger 文档
4. 在 `service` 包下实现业务逻辑
5. 在 `mapper` 包下添加数据访问方法

### 数据库操作

1. 在 `model` 包下定义数据模型
2. 在 `mapper` 包下创建 Mapper 接口
3. 在 `src/main/resources/mappers/` 下创建 XML 映射文件
4. 在 `mybatis.xml` 中注册映射器

## 🐛 常见问题

### 1. 数据库连接失败
- 检查 PostgreSQL 服务是否启动
- 验证数据库连接配置是否正确
- 确认数据库用户权限

### 2. 端口被占用
- 修改 `application.properties` 中的 `server.port` 配置
- 或者停止占用端口的其他服务

### 3. MyBatis 映射文件找不到
- 检查 `mybatis.mapper-locations` 配置
- 确认 XML 文件路径正确
- 验证 `mybatis.xml` 中的映射器配置

## 📄 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

如有问题，请通过以下方式联系：
- 提交 Issue
- 发送邮件

---

**注意**: 这是一个开发中的项目，API 接口可能会发生变化。 