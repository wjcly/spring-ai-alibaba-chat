# JDK 17 安装指南

## 为什么需要 JDK 17？

本项目使用 **Spring Boot 3.x** 和 **Spring AI**，它们都需要 Java 17 或更高版本。

Java 17 是 LTS（长期支持）版本，相比 Java 8 有很多改进：
- 更好的性能
- 更现代的语法特性（record、pattern matching、switch 表达式等）
- 长期支持到 2029 年

## Windows 安装 JDK 17

### 方式一：使用 Adoptium（推荐，免费）

1. **下载**
   - 访问：https://adoptium.net/temurin/releases/?version=17
   - 选择 Windows x64 MSI 安装包

2. **安装**
   - 运行下载的 `.msi` 文件
   - 按向导完成安装

3. **配置环境变量**
   ```
   JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x
   Path=%JAVA_HOME%\bin
   ```

4. **验证**
   ```cmd
   java -version
   ```
   应该显示：`openjdk version "17.x.x"`

### 方式二：使用 Oracle JDK

1. **下载**
   - 访问：https://www.oracle.com/java/technologies/downloads/#java17
   - 选择 Windows x64 Installer

2. **安装后配置**
   ```
   JAVA_HOME=C:\Program Files\Java\jdk-17
   Path=%JAVA_HOME%\bin
   ```

### 方式三：使用 winget（Windows 包管理器）

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

## Maven 配置

确保 Maven 使用 JDK 17：

```cmd
# 查看当前 Java 版本
java -version

# 编译项目
mvn clean compile -DskipTests

# 运行项目
mvn spring-boot:run
```

## 常见问题

### Q: 安装后 `java -version` 还是显示 1.8？
A: 需要更新环境变量或重启终端。

### Q: 如何同时保留 JDK 8 和 JDK 17？
A: 可以安装多个 JDK，通过修改 `JAVA_HOME` 切换：
```cmd
# 使用 JDK 8
set JAVA_HOME=C:\Program Files\Java\jdk1.8

# 使用 JDK 17
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
```

### Q: IDE 需要重新配置吗？
A: 是的，需要在 IDE 中配置 JDK 17：
- **IntelliJ IDEA**: File → Project Structure → SDKs → 添加 JDK 17
- **Eclipse**: Window → Preferences → Java → Installed JREs → 添加 JDK 17

## 项目启动步骤

1. 安装 JDK 17
2. 配置环境变量 `DASHSCOPE_API_KEY`
3. 编译项目：`mvn clean compile -DskipTests`
4. 运行项目：`mvn spring-boot:run`
5. 访问：http://localhost:8080/api

## 下载链接汇总

| 厂商 | 下载链接 | 费用 |
|------|----------|------|
| Adoptium | https://adoptium.net/ | 免费 |
| Oracle | https://www.oracle.com/java/technologies/downloads/ | 商用收费 |
| Amazon Corretto | https://aws.amazon.com/cn/corretto/ | 免费 |
| Microsoft Build | https://www.microsoft.com/openjdk | 免费 |

推荐使用 **Adoptium Temurin** 或 **Amazon Corretto**，完全免费且兼容性好。
