package com.example.tsuki.demoptz.socket;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.tsuki.demoptz.utils.Utils;

import org.json.JSONObject;

/**
 * Created by Mahabali
 * on 11/14/15.
 */
public class AppSocketListener implements SocketListener {
    private final String TAG = getClass().getName();
    private static AppSocketListener sharedInstance;
    private static SocketIOService mSocketIOService;
    private SocketListener mSocketListener;

    public void setmSocketListener(SocketListener mSocketListener) {
        this.mSocketListener = mSocketListener;
        if (mSocketIOService != null && mSocketIOService.isSocketConnected()) {
            onSocketConnected();
        }
    }

    public static AppSocketListener getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new AppSocketListener();
        }
        return sharedInstance;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected connected ");
            mSocketIOService = ((SocketIOService.LocalBinder) service).getService();
            mSocketIOService.setServiceBinded(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected connected");
            mSocketIOService.setServiceBinded(false);
            mSocketIOService = null;
            onSocketDisconnected();
        }
    };

    void initialize(String token, Context context) {
        Log.e(TAG, "initialize");
        Intent mIntent = new Intent(context, SocketIOService.class);
        mIntent.putExtra(SocketConstants.TOKEN, token);

        context.startService(mIntent);
        context.bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(context).
                registerReceiver(socketConnectionReceiver, new IntentFilter(SocketConstants.
                        CONNECT_SOCKET_SUCCESS));
        LocalBroadcastManager.getInstance(context).
                registerReceiver(connectionFailureReceiver, new IntentFilter(SocketConstants.
                        CONNECT_SOCKET_ERROR));
        LocalBroadcastManager.getInstance(context).
                registerReceiver(joinRoomReceiver, new IntentFilter(SocketConstants.
                        ONVIF_JOIN));
        LocalBroadcastManager.getInstance(context).
                registerReceiver(onvifResultData, new IntentFilter(SocketConstants.
                        ONVIF_RESULT));
        LocalBroadcastManager.getInstance(context).
                registerReceiver(onvifController, new IntentFilter(SocketConstants.
                        ONVIF_CONTROLLER));
        LocalBroadcastManager.getInstance(context).
                registerReceiver(onvifConnect, new IntentFilter(SocketConstants.
                        ONVIF_CONNECT));
    }

    private BroadcastReceiver socketConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.e(TAG, "Socket connected");
            onSocketConnected();
        }
    };

    private BroadcastReceiver joinRoomReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(SocketConstants.DATA);
            Log.e(TAG, "Join Socket finish ");
            onJoinFinish(data);
        }
    };

    private BroadcastReceiver onvifController = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(SocketConstants.DATA);
            Log.e(TAG, "Onvif Controller finish ");
            onvifController(data);
        }
    };

    private BroadcastReceiver onvifResultData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(SocketConstants.DATA);
            Log.e(TAG, "Onvif Result finish ");
            onvifResultData(data);
        }
    };

    private BroadcastReceiver onvifConnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(SocketConstants.DATA);
            Log.e(TAG, "Onvif Connect Camera finish ");
            onvifConnect(data);
        }
    };

    private BroadcastReceiver connectionFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSocketDisconnected();
        }
    };


    public void disconnect() {
        if (mSocketIOService != null) {
            mSocketIOService.removeSocketHandlers();
        }
    }


    @Override
    public void onSocketConnected() {
        Log.e(TAG, "onServiceConnected connected " + mSocketListener);
        if (mSocketListener != null) {
            mSocketListener.onSocketConnected();
        }
    }

    @Override
    public void onSocketDisconnected() {
        if (mSocketListener != null) {
            //Log.d(TAG, " on Socket DisConnected");
            mSocketListener.onSocketDisconnected();
        }
    }

    @Override
    public void onJoinFinish(String data) {
        Utils.mLogD("onJoinFinish: " + data);
        if (mSocketListener != null) {
            mSocketListener.onJoinFinish(data);
        }
    }

    @Override
    public void onNewCamera(String data) {
        if (mSocketListener != null) {
            //Log.d(TAG, " on Message");
            mSocketListener.onNewCamera(data);
        }
    }

    @Override
    public void onGetNews(String data) {
        if (mSocketListener != null) {
            mSocketListener.onGetNews(data);
        }
    }

    @Override
    public void onvifController(String data) {
        if (mSocketListener != null) {
            mSocketListener.onvifController(data);
        }
    }

    @Override
    public void onvifResultData(String data) {
        if (mSocketListener != null) {
            mSocketListener.onvifResultData(data);
        }
    }

    @Override
    public void onvifConnect(String data) {
        if (mSocketListener != null) {
            mSocketListener.onvifConnect(data);
        }
    }

    @Override
    public void onCameraChanger(String data) {
        if (mSocketListener != null) {
            mSocketListener.onCameraChanger(data);
        }
    }

    public void sendSocket(String event, String data) {
        if (mSocketIOService != null) {
            mSocketIOService.send(event, data);
            Utils.mLogD("send: event: " + event + ", data: " + data);
        }
    }

    public void sendSocket(String event, JSONObject data) {
        if (mSocketIOService != null) {
            mSocketIOService.send(event, data);
        }
    }

    public boolean isSocketConnected() {
        return mSocketIOService != null && mSocketIOService.isSocketConnected();
    }

}
