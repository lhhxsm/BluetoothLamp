package com.bluetooth.lamp.connect;

/**
 * 作者:lhh
 * 描述:处理网络协议，对数据进行封包或解包
 * 时间:2017/11/15.
 */
public interface ProtocolHandler<T> {
  byte[] encodePackage(T data);

  T decodePackage(byte[] data);
}
