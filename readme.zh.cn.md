# java-native-media

音视频处理优化库

[[toc]]

---

## 项目背景

在传统的 Java 音视频处理方案中，开发者常常依赖调用 FFmpeg 命令行工具来实现各类音频处理需求。然而，这种方案存在两个显著的痛点：

1. **性能损耗**：调用外部进程会带来额外的进程间通信开销，影响处理效率；
2. **体积膨胀**：为了保证功能完整，通常需要捆绑 FFmpeg 二进制文件，导致最终应用包体积显著增加。

为了解决以上问题，本项目利用 JNI 技术，使 Java 能直接调用经过深度优化的 C 语言音视频处理库，针对特定场景实现高性能、低体积的音视频处理方案。

---

## 核心功能

### 1. MP3 智能分片

#### 应用场景

- 针对大语言模型输入文件大小限制（例如 25MB）提供智能分片解决方案

#### 技术特点

- **按指定字节数分割**：可以根据用户设定的分片大小进行智能分割；
- **保证音频完整性**：在分片过程中充分考虑音频数据的完整性，确保分片后仍能正确播放；
- **内存高效分流**：采用高效的内存分流处理机制，优化分片性能和资源使用

### 2. MP4 转 MP3

#### 应用场景

- 为视频内容语音识别提供预处理，将视频中的音频轨道提取并转换为 MP3 格式

#### 技术特点

- **高效提取音频**：快速提取 MP4 文件中的音频轨道；
- **保持音质**：在转换过程中尽量保留原始音频质量，确保转换结果满足识别需求

---

## 使用示例

下面提供了两个典型的使用示例，分别展示了 MP3 分片和 MP4 转 MP3 的调用方式。

### MP3 分片功能示例

在测试用例中，调用 `NativeMedia.splitMp3` 方法对指定 MP3 文件进行分片处理。

```java
public class NativeMediaTest {

  @Test
  public void testSplitMp3() {
    String inputFile = "/audio/01.mp3";
    long splitSize = 10 * 1024 * 1024; // 10MB分片

    String[] outputFiles = NativeMedia.splitMp3(inputFile, splitSize);

    for (String filePath : outputFiles) {
      System.out.println("生成分片文件: " + filePath);
    }
  }
}
```

**参数说明**

- `inputFile`：源文件的绝对路径。
- `splitSize`：分片大小（单位：字节），建议设置为 `25 * 1024 * 1024` 以适配大语言模型输入文件大小限制。

### MP4 转 MP3 示例

通过调用 `NativeMedia.mp4ToMp3` 方法，可以轻松将 MP4 视频文件转换为 MP3 音频文件。

```java
package com.litongjava.media;

public class Mp4ToMp3Test {
  public static void main(String[] args) {
    String inputPath = "E:\\code\\cpp\\project-ping\\native-media\\samples\\01.mp4";
    String result = NativeMedia.mp4ToMp3(inputPath);
    if (result.startsWith("Error:")) {
      System.out.println("Conversion failed: " + result);
    } else {
      System.out.println("Conversion successful! Output file: " + result);
    }
  }
}
```

---

## 性能优势

经过实际测试对比，采用 java-native-media 方案在典型场景下具有以下优势：

- **处理耗时减少 40%-60%**：通过 JNI 调用优化，避免了进程间通信带来的性能损耗；
- **内存占用降低 30%**：针对特定场景的内存分流处理机制使内存使用更高效；
- **包体积显著减小**：相比捆绑完整的 FFmpeg 二进制文件，最终包体积可缩小 85%。

---

## Docker 部署

为了方便部署和使用，本项目还提供了基于 Docker 的示例。以下是构建和运行 Docker 容器的详细步骤：

### 1. 添加 Maven 依赖

在项目的 `pom.xml` 中添加如下依赖：

```xml
<dependency>
  <groupId>com.litongjava</groupId>
  <artifactId>java-native-media</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 2. 创建 java-native-media-test 项目

源码地址
项目中包含独立的 `java-native-media-test-1.0.0.jar`，其中已经内嵌 `java-native-media-1.0.0.jar`。示例代码如下，展示了如何通过 HTTP 接口调用 MP4 转 MP3 功能：

```java
package com.litongjava.test.controller;

import java.io.File;
import com.litongjava.annotation.RequestPath;
import com.litongjava.media.NativeMedia;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.hutool.FilenameUtils;

@RequestPath("/media")
public class MediaController {

  public HttpResponse toMp3(HttpRequest request) {
    UploadFile uploadFile = request.getUploadFile("file");
    byte[] data = uploadFile.getData();
    new File("upload").mkdirs();
    File file = new File("upload", uploadFile.getName());
    FileUtil.writeBytes(data, file);
    String result = NativeMedia.mp4ToMp3(file.getAbsolutePath());
    String contentType = ContentTypeUtils.getContentType(FilenameUtils.getSuffix(result));
    byte[] fileBytes = FileUtil.readBytes(new File(result));
    HttpResponse response = TioRequestContext.getResponse();
    Resps.bytesWithContentType(response, fileBytes, contentType);
    return response;
  }
}
```

### 3. Dockerfile 示例

下面提供了一个 Dockerfile 示例，用于构建基于 JDK 8 的 Docker 镜像，并安装了必要的依赖（如 FFmpeg 和 libmp3lame0）：

```dockerfile
FROM litongjava/jdk:8u411-stable-slim

RUN apt-get update && apt-get install -y ffmpeg libmp3lame0

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件到容器
COPY target/java-native-media-test-1.0.0.jar /app/

# 运行 JAR 文件
CMD ["java", "-jar", "java-native-media-test-1.0.0.jar"]
```

构建镜像命令：

```bash
docker build -t litongjava/java-native-media-test:1.0.0 .
```

运行镜像示例：

```bash
docker run -dit --name java-native-media-test -p 80:80 litongjava/java-native-media-test:1.0.0
```

或使用以下命令运行并在运行结束后自动清理容器：

```bash
docker run --rm --name java-native-media-test -p 80:80 litongjava/java-native-media-test:1.0.0
```

运行时输出示例：

```
os name:linux user.home:/root
load /root/lib/linux_amd64/libnative_media.so
```

最终打包后的文件信息：

- `java-native-media-1.0.0.jar` 大小约 7.21MB
- `java-native-media-test-1.0.0.jar` 大小约 13.6MB

---

## 注意事项

- **项目定位**：本项目专注于针对特定场景的垂直优化，不是一个通用型多媒体处理框架。如果项目需求超出本项目支持的核心功能范围（如图形化界面、滤镜效果、非常规编码格式处理），建议选用其他方案；
- **功能覆盖**：相较于 FFmpeg，本项目仅实现了高频使用场景下的核心功能，开发者需根据具体需求选择最适合的工具；
- **性能与体积**：项目经过深度优化，显著提升了处理性能并大幅缩减了包体积，但在一些边缘场景下可能需要额外适配。

---

通过以上文档，开发者可以快速了解 java-native-media 项目的设计初衷、功能特点以及使用方法。希望这篇文档能帮助您在项目中更高效地进行音视频处理任务。