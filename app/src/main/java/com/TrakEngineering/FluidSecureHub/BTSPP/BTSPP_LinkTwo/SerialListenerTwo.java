package com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkTwo;

public interface SerialListenerTwo {
    void onSerialConnectTwo();
    void onSerialConnectErrorTwo(Exception e);
    void onSerialReadTwo(byte[] data);
    void onSerialIoErrorTwo(Exception e);
}
