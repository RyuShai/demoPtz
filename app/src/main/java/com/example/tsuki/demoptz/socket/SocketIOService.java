package com.example.tsuki.demoptz.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import socketio.nhankv.com.client.IO;
import socketio.nhankv.com.client.Socket;
import socketio.nhankv.com.emitter.Emitter;

public class SocketIOService extends Service {
    private final String TAG = getClass().getName();
    private Socket mSocket;
    //    private String mToken;
    private IO.Options mOptions = new IO.Options();
    private boolean mIsConnect = false;
    private boolean serviceBinded = false;
    private final LocalBinder mBinder = new LocalBinder();

//    private boolean isUnOnvifJoin = false;

    public class LocalBinder extends Binder {
        public SocketIOService getService() {
            return SocketIOService.this;
        }
    }

    public void setServiceBinded(boolean serviceBinded) {
        this.serviceBinded = serviceBinded;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return serviceBinded;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeSocketHandlers();
        stopSelf();
        mSocket = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, " onStartCommand start initializeSocket");
        if (intent != null) {
            if (intent.getStringExtra(SocketConstants.TOKEN) != null) {
                String token = intent.getStringExtra(SocketConstants.TOKEN);
                initializeSocket(token);
            }
        }
        return START_STICKY;
    }

    private void initializeSocket(String token) {
        try {
            if ((token != null) && (!token.equals(""))) {
                if (mSocket == null) {
                    mOptions.query = "token=" + token;
                    mSocket = IO.socket(SocketConstants.URL_SOCKET_VP9, mOptions);
                    addSocketHandlers();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void addSocketHandlers() {
        if (!mIsConnect) {
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_ERROR, onError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onErrorTimeout);
            mSocket.on(SocketConstants.ONVIF_JOIN, onJoin);
            mSocket.on(SocketConstants.ONVIF_CONTROLLER, onvifController);
            mSocket.on(SocketConstants.ONVIF_RESULT, onvifresult);
            mSocket.on(SocketConstants.ONVIF_CONNECT, onvifConnect);
//            Log.e(TAG, " add Event socket finish ");
            mSocket.connect();
        }
    }

    public void removeSocketHandlers() {
        try {
            if (mIsConnect) {
                mSocket.disconnect();
                mSocket.off(Socket.EVENT_CONNECT, onConnect);
                mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
                mSocket.off(Socket.EVENT_ERROR, onError);
                mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onErrorTimeout);
                mSocket.off(SocketConstants.ONVIF_JOIN, onJoin);
                mSocket.off(SocketConstants.ONVIF_CONTROLLER, onvifController);
                mSocket.off(SocketConstants.ONVIF_RESULT, onvifresult);
                mSocket.off(SocketConstants.ONVIF_CONNECT, onvifConnect);
                mSocket = null;
                mIsConnect = false;
//                Log.e(TAG, "DisConnect Socket Finish");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mIsConnect = false;
            Log.e(TAG, " Connect Socket Io fail!");
            Intent intent = new Intent(SocketConstants.CONNECT_SOCKET_ERROR);
            broadcastEvent(intent);
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mIsConnect = false;
            Log.e(TAG, " Error accuracy token!");
            Intent intent = new Intent(SocketConstants.CONNECT_SOCKET_ERROR);
            broadcastEvent(intent);
        }
    };

    private Emitter.Listener onErrorTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mIsConnect = false;
            Log.e(TAG, " Request connect socket time out");
            Intent intent = new Intent(SocketConstants.CONNECT_SOCKET_ERROR);
            broadcastEvent(intent);
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG, "  Socket Io Ok");
            mIsConnect = true;
            Intent intent = new Intent(SocketConstants.CONNECT_SOCKET_SUCCESS);
            broadcastEvent(intent);
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                final JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                String message = jsonObject.getString("message");
                if (message.equals("Join success")) {
                    Intent intent = new Intent(SocketConstants.ONVIF_JOIN);
                    intent.putExtra(SocketConstants.DATA, jsonObject.toString());
                    broadcastEvent(intent);

                } else {
                    Intent intent = new Intent(SocketConstants.ONVIF_JOIN);
                    intent.putExtra(SocketConstants.DATA, jsonObject.toString());
                    broadcastEvent(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onvifController = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jData = new JSONObject(String.valueOf(args[0]));
                Intent intent = new Intent(SocketConstants.ONVIF_CONTROLLER);
                intent.putExtra(SocketConstants.DATA, jData.toString());
                broadcastEvent(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onvifresult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jData = new JSONObject(String.valueOf(args[0]));
                Intent intent = new Intent(SocketConstants.ONVIF_RESULT);
                intent.putExtra(SocketConstants.DATA, jData.toString());
                broadcastEvent(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onvifConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jData = new JSONObject(String.valueOf(args[0]));
                Intent intent = new Intent(SocketConstants.ONVIF_CONNECT);
                intent.putExtra(SocketConstants.DATA, jData.toString());
                broadcastEvent(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void send(String event, String data) {
        try {
            if (mSocket != null) {
                mSocket.emit(event, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String event, JSONObject data) {
        try {
            if (mSocket != null) {
                mSocket.emit(event, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastEvent(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean isSocketConnected() {
        return mSocket != null && mSocket.connected();
    }
}
