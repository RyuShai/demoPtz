package com.example.tsuki.demoptz.socket;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.tsuki.demoptz.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by toannq
 * on 8/1/17.
 */

public class PTZController implements SocketListener {
    private final String TAG = getClass().getName();
    private AppSocketListener mAppSocketListener;
    private SocketIOCameraListFragmentListener socketIOCameraListFragmentListener;
    private static PTZController mPtzController = new PTZController();
    private boolean isConnectPTZ;
    private IConnectPtz iConnectPtz;

    public void setiConnectPtz(IConnectPtz iConnectPtz) {
        this.iConnectPtz = iConnectPtz;
    }

    public static PTZController getInstance() {
        return mPtzController;
    }

    public void setActivity() {
        if (mAppSocketListener == null) {
            mAppSocketListener = AppSocketListener.getInstance();
            mAppSocketListener.setmSocketListener(this);  // Thiết lập đăng ký lắng nghe sự kiện khi socket trả về.
        }
    }

    public void setSocketIOCameraListFragmentListener(SocketIOCameraListFragmentListener socketIOCameraListFragmentListener) {
        this.socketIOCameraListFragmentListener = socketIOCameraListFragmentListener;
    }

    public void sendOnvifJoin(String cameraId) {
        try {
            JSONObject jData = new JSONObject();
            jData.put("camera_id", cameraId);
            mAppSocketListener.sendSocket(SocketConstants.ONVIF_JOIN, jData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void connectCamPTZ(String deviceSeverId, String cameraId) {
        try {
            JSONObject jData = new JSONObject();
            jData.put(SocketConstants.ACTION, SocketConstants.ACTION_CONNECT);
            jData.put(SocketConstants.DEVICE_SERVER_ID, deviceSeverId);
            jData.put(SocketConstants.CAMERA_ID, cameraId);
            mAppSocketListener.sendSocket(SocketConstants.ONVIF_CONNECT, jData.toString());
            Log.e(TAG, "jData: " + jData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    public void getSystemTimePTZ(ProfileCam cams_list) {
//        try {
//            JSONObject jData = new JSONObject();
//            jData.put(SocketConstants.ACTION, SocketConstants.ACTION_GETSYSDATETIME);
//            jData.put(SocketConstants.DEVICE_SERVER_ID, cams_list.getmDeviceServerId());
//            jData.put(SocketConstants.CAMERA_ID, cams_list.getmCamId());
//            mAppSocketListener.sendSocket(SocketConstants.ONVIF_CONTROLLER, jData.toString());
//            Log.d(TAG, "jData: " + jData.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void setSystemTimePTZ(ProfileCam cams_list, String dateTimeType,
//                                 String timezone, boolean daylightSavings, String dateTime) {
//        try {
//            JSONObject jData = new JSONObject();
//            jData.put(SocketConstants.ACTION, SocketConstants.ACTION_SETSYSDATETIME);
//            jData.put(SocketConstants.DEVICE_SERVER_ID, cams_list.getmDeviceServerId());
//            jData.put(SocketConstants.CAMERA_ID, cams_list.getmCamId());
//            jData.put("dateTimeType", dateTimeType);
//            jData.put("timezone", "WIB" + timezone);
//            jData.put("daylightSavings", daylightSavings);
//            jData.put("dateTime", dateTime);
//            mAppSocketListener.sendSocket(SocketConstants.ONVIF_CONTROLLER, jData.toString());
//            Log.d(TAG, "jData: " + jData.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public void sendRemotePtz(String deviceServerId, String cameraId, float x, float y, float zoom) {
        try {
            JSONObject jData = new JSONObject();
            jData.put(SocketConstants.ACTION, SocketConstants.ACTION_CONTINUOUSMOVE);
            jData.put(SocketConstants.DEVICE_SERVER_ID, deviceServerId);
            jData.put(SocketConstants.CAMERA_ID, cameraId);
            jData.put("x", x);
            jData.put("y", y);
            jData.put("zoom", zoom);
            mAppSocketListener.sendSocket(SocketConstants.ONVIF_CONTROLLER, jData.toString());
            Log.e(TAG, "sendRemote jData: " + jData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendBrightnessPtz(String deviceServerId, String cameraId, float brightness) {
        try {
            JSONObject jData = new JSONObject();
            jData.put(SocketConstants.ACTION, SocketConstants.ACTION_SETIMAGINGSETTING);
            jData.put(SocketConstants.DEVICE_SERVER_ID, deviceServerId);
            jData.put(SocketConstants.CAMERA_ID, cameraId);
            jData.put("brightness", brightness);
            mAppSocketListener.sendSocket(SocketConstants.ONVIF_CONTROLLER, jData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendExposureTime(String deviceServerId, String cameraId, float exposure) {
        try {
            JSONObject jData = new JSONObject();
            jData.put(SocketConstants.ACTION, SocketConstants.ACTION_SETIMAGINGSETTING);
            jData.put(SocketConstants.DEVICE_SERVER_ID, deviceServerId);
            jData.put(SocketConstants.CAMERA_ID, cameraId);
            jData.put("exposure", exposure);
            mAppSocketListener.sendSocket(SocketConstants.ONVIF_CONTROLLER, jData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSocketConnected() {
        if (socketIOCameraListFragmentListener != null) {
            socketIOCameraListFragmentListener.onSucess();
        }
    }

    @Override
    public void onSocketDisconnected() {
        if (socketIOCameraListFragmentListener != null) {
            socketIOCameraListFragmentListener.onError();
        }
    }

    @Override
    public void onJoinFinish(String data) {
//        Utils.mLogE("onJoinFinish " + data);
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("error")) {
                if (jsonObject.getString("error").equals("0")) {
                    if (jsonObject.getString("message").equals("Join onvif success")) {
                        isConnectPTZ = true;
                        Utils.mLogE("join socket ptz success");
                        if (iConnectPtz != null) {
                            iConnectPtz.connectPtzSuccess(data);
                        }
                    } else {
                        isConnectPTZ = false;
                    }
                } else {
                    isConnectPTZ = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewCamera(String data) {
        Log.d(TAG, " onNewCamera jData: ");
    }

    @Override
    public void onGetNews(String data) {
        Log.d(TAG, " onGetNews jData: ");
    }

    @Override
    public void onvifController(String data) {
        Log.d(TAG, " onvifController jData: ");
    }

    @Override
    public void onvifResultData(String data) {
    }

    @Override
    public void onvifConnect(String data) {
        Utils.mLogE(" onvifConnect jData: " + data);
    }

    @Override
    public void onCameraChanger(String data) {

    }

    public void connectSocket(String token, Context context) {
        if (!mAppSocketListener.isSocketConnected()) {
            mAppSocketListener.initialize(token, context);
        }
    }

    public void disConnectSocket() {
        if (mAppSocketListener != null) {
            isConnectPTZ = false;
            mAppSocketListener.disconnect();
            mAppSocketListener.setmSocketListener(null);
        }
    }

    public AppSocketListener getmAppSocketListener() {
        return mAppSocketListener;
    }

    public boolean isConnectPTZ() {
        return isConnectPTZ;
    }

    public interface SocketIOCameraListFragmentListener {
        void onSucess();

        void onError();
    }

    public interface IConnectPtz {
        void connectPtzSuccess(String data);
    }
}
