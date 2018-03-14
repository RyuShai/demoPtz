package com.example.tsuki.demoptz.socket;

/**
 * Created by Mahabali
 * on 11/16/15.
 */
public interface SocketListener {
    void onSocketConnected();
    void onSocketDisconnected();
    void onJoinFinish(String data);
    void onNewCamera(String data);
    void onGetNews(String data);
    void onvifController(String data);
    void onvifResultData(String data);
    void onvifConnect(String data);
    void onCameraChanger(String data);
}
