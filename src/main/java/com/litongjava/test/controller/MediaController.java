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
