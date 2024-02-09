package com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkSix;

public interface SerialListenerSix {
    void onSerialConnectSix();
    void onSerialConnectErrorSix(Exception e);
    void onSerialReadSix(byte[] data);
    void onSerialIoErrorSix(Exception e, Integer fromCode);
}
