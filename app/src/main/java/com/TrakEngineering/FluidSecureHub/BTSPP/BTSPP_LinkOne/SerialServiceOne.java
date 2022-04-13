package com.TrakEngineering.FluidSecureHub.BTSPP.BTSPP_LinkOne;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.TrakEngineering.FluidSecureHub.BuildConfig;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import androidx.annotation.Nullable;

/**
 * create notification and queue serial data while activity is not in the foreground
 * use listener chain: SerialSocket -> SerialService -> UI fragment
 */
public class SerialServiceOne extends Service implements SerialListenerOne {

    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    public class SerialBinder extends Binder {
        public SerialServiceOne getService() {
            return SerialServiceOne.this;
        }
    }

    private enum QueueType {Connect, ConnectError, Read, IoError}

    private class QueueItem {
        QueueType type;
        byte[] data;
        Exception e;

        QueueItem(QueueType type, byte[] data, Exception e) {
            this.type = type;
            this.data = data;
            this.e = e;
        }
    }

    private final Handler mainLooper;
    private final IBinder binder;
    private final Queue<QueueItem> queue1, queue2;

    private SerialSocketOne socket;
    private SerialListenerOne listener;
    private boolean connected;

    /**
     * Lifecylce
     */
    public SerialServiceOne() {
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new SerialBinder();
        queue1 = new LinkedList<>();
        queue2 = new LinkedList<>();
    }

    @Override
    public void onDestroy() {
        cancelNotification();
        disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Api
     */
    public void connect(SerialSocketOne socket) throws IOException {
        socket.connect(this);
        this.socket = socket;
        connected = true;
    }

    public void disconnect() {
        connected = false;
        cancelNotification();
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    public void write(byte[] data) throws IOException {
        socket.write(data);
    }

    public void readPulse() throws IOException {
        socket.readPulse();
    }

    public void attach(SerialListenerOne listener) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        cancelNotification();
        // use synchronized() to prevent new items in queue2
        // new items will not be added to queue1 because mainLooper.post and attach() run in main thread
        synchronized (this) {
            this.listener = listener;
        }
        for (QueueItem item : queue1) {
            switch (item.type) {
                case Connect:
                    listener.onSerialConnectOne();
                    break;
                case ConnectError:
                    listener.onSerialConnectErrorOne(item.e);
                    break;
                case Read:
                    listener.onSerialReadOne(item.data);
                    break;
                case IoError:
                    listener.onSerialIoErrorOne(item.e);
                    break;
            }
        }
        for (QueueItem item : queue2) {
            switch (item.type) {
                case Connect:
                    listener.onSerialConnectOne();
                    break;
                case ConnectError:
                    listener.onSerialConnectErrorOne(item.e);
                    break;
                case Read:
                    listener.onSerialReadOne(item.data);
                    break;
                case IoError:
                    listener.onSerialIoErrorOne(item.e);
                    break;
            }
        }
        queue1.clear();
        queue2.clear();
    }

    public void detach() {
        if (connected)
            // items already in event queue (posted before detach() to mainLooper) will end up in queue1
            // items occurring later, will be moved directly to queue2
            // detach() and mainLooper.post run in the main thread, so all items are caught
            listener = null;
    }

    private void cancelNotification() {
        stopForeground(true);
    }

    /**
     * SerialListener
     */
    public void onSerialConnectOne() {
        if (connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialConnectOne();
                        } else {
                            queue1.add(new QueueItem(QueueType.Connect, null, null));
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.Connect, null, null));
                }
            }
        }
    }

    public void onSerialConnectErrorOne(Exception e) {
        if (connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialConnectErrorOne(e);
                        } else {
                            queue1.add(new QueueItem(QueueType.ConnectError, null, e));
                            cancelNotification();
                            disconnect();
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.ConnectError, null, e));
                    cancelNotification();
                    disconnect();
                }
            }
        }
    }

    public void onSerialReadOne(byte[] data) {
        if (connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialReadOne(data);
                        } else {
                            queue1.add(new QueueItem(QueueType.Read, data, null));
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.Read, data, null));
                }
            }
        }
    }

    public void onSerialIoErrorOne(Exception e) {
        if (connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialIoErrorOne(e);
                        } else {
                            queue1.add(new QueueItem(QueueType.IoError, null, e));
                            cancelNotification();
                            disconnect();
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.IoError, null, e));
                    cancelNotification();
                    disconnect();
                }
            }
        }
    }

}
