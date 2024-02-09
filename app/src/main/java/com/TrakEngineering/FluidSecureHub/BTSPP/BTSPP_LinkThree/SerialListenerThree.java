package com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkThree;

public interface SerialListenerThree {
    void onSerialConnectThree();
    void onSerialConnectErrorThree(Exception e);
    void onSerialReadThree(byte[] data);
    void onSerialIoErrorThree(Exception e, Integer fromCode);
}
