package com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkFour;

public interface SerialListenerFour {
    void onSerialConnectFour();
    void onSerialConnectErrorFour(Exception e);
    void onSerialReadFour(byte[] data);
    void onSerialIoErrorFour(Exception e);
}
