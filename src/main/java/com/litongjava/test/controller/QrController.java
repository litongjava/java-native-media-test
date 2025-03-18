package com.litongjava.test.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.litongjava.annotation.AController;
import com.litongjava.annotation.RequestPath;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.qrcode.QrCodeUtils;

@AController
@RequestPath("/qr")
public class QrController {

  @RequestPath("/gen")
  public HttpResponse qr(String content) {
    HttpResponse response = TioRequestContext.getResponse();
    // 获取要生成的二维码内容
    if (StrUtil.isBlank(content)) {
      return Resps.json(response, RespBodyVo.fail("No content provided for QR code"));
    }

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      // 生成二维码图片，并写入到输出流
      QrCodeUtils.generateQRCode(content, 300, 300, outputStream);

      // 将输出流转换为字节数组
      byte[] qrCodeBytes = outputStream.toByteArray();

      // 使用 Resps 工具类创建一个包含二维码图片的响应
      return Resps.bytesWithContentType(response, qrCodeBytes, "image/png");

    } catch (IOException e) {
      e.printStackTrace();
      return Resps.json(response, RespBodyVo.fail("Error generating QR code"));
    }
  }
}