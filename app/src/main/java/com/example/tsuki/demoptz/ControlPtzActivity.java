package com.example.tsuki.demoptz;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.tsuki.demoptz.retrofit.RequestBusiness;
import com.example.tsuki.demoptz.retrofit.SeverApiHelper;
import com.example.tsuki.demoptz.socket.PTZController;
import com.example.tsuki.demoptz.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Android Studio
 * Author: tsuki
 * Time: 03/02/2018
 */

public class ControlPtzActivity extends AppCompatActivity implements PTZController.SocketIOCameraListFragmentListener, View.OnClickListener, PTZController.IConnectPtz {


    private String cameraId; //15125
    private EditText edtCameraId, edtDeviceId; //a8:17:02:c2:27:f3
    private int site_id;
    private String token;
    private String nvr_id;
    private SeekBar seekBarLight;
    private SeekBar seekExpose ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);
        CheckPermission();

        token = getIntent().getStringExtra("token");
        site_id = getIntent().getIntExtra("site_id", 0);
//        Utils.mLogE("token: " + token);

        initView();
        PTZController.getInstance().setActivity();
        PTZController.getInstance().setiConnectPtz(this);
        connectSocket(token);
    }
    void CheckPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));

                startActivityForResult(intent, 200);

            }
        }
    }

    private void initView() {
        ImageView imgDown = findViewById(R.id.img_down);
        ImageView imgDownLeft = findViewById(R.id.img_downleft);
        ImageView imgDownRight = findViewById(R.id.img_downright);
        ImageView imgLeft = findViewById(R.id.img_left);
        ImageView imgRight = findViewById(R.id.img_right);
        ImageView imgUp = findViewById(R.id.img_up);
        ImageView imgUpLeft = findViewById(R.id.img_upleft);
        ImageView imgUpRight = findViewById(R.id.img_upright);
        seekBarLight = findViewById(R.id.seek_lightsetting);
        seekExpose = findViewById(R.id.seekExpose);
        RelativeLayout zoomIn = findViewById(R.id.zoomin);
        RelativeLayout zoomOut = findViewById(R.id.zoomout);
        edtCameraId = findViewById(R.id.edtCameraId);
        Button buttonGetDetail = findViewById(R.id.btnGetDetail);
        Button buttonChange = findViewById(R.id.btnChange);

        seekBarLight.setMax(250);
        seekExpose.setMax(8000);

        imgDown.setOnClickListener(this);
        imgDownLeft.setOnClickListener(this);
        imgDownRight.setOnClickListener(this);
        imgLeft.setOnClickListener(this);
        imgRight.setOnClickListener(this);
        imgUp.setOnClickListener(this);
        imgUpLeft.setOnClickListener(this);
        imgUpRight.setOnClickListener(this);
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        buttonGetDetail.setOnClickListener(this);
        buttonChange.setOnClickListener(this);
        seekBarLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() == 0) {
                    PTZController.getInstance().sendBrightnessPtz(nvr_id, cameraId, 1);
                } else {
                    PTZController.getInstance().sendBrightnessPtz(nvr_id, cameraId, seekBar.getProgress());
                }
                Utils.mLogE("set bright: nvr_id: " + nvr_id + " cameraId: " + cameraId);
            }
        });

        seekExpose.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() <= 25) {
                    PTZController.getInstance().sendExposureTime(nvr_id, cameraId, 25);
                } else {
                    PTZController.getInstance().sendExposureTime(nvr_id, cameraId, seekBar.getProgress());
                }
                Utils.mLogE("set exposure: nvr_id: " + nvr_id + " cameraId: " + cameraId);
            }
        });
    }

    private void connectSocket(String token) {
        PTZController.getInstance().setSocketIOCameraListFragmentListener(this);
        PTZController.getInstance().connectSocket(token, this);
    }

    @Override
    public void onSucess() {
        Utils.mLogE("connect socket onSucess");
    }

    @Override
    public void onError() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_down:
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0, -0.5f, 0);
                Toast.makeText(this, "click down", Toast.LENGTH_SHORT).show();
                break;
            case R.id.img_downleft:
                Toast.makeText(this, "click down left", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0.5f, -0.5f, 0);
                break;
            case R.id.img_downright:
                Toast.makeText(this, "click down right", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, -0.5f, -0.5f, 0);
                break;
            case R.id.img_left:
                Toast.makeText(this, "click left", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, -0.5f, 0, 0);
                break;
            case R.id.img_right:
                Toast.makeText(this, "click right", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0.5f, 0, 0);
                break;
            case R.id.img_up:
                Toast.makeText(this, "click up", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0, 0.5f, 0);
                break;
            case R.id.img_upleft:
                Toast.makeText(this, "click up left", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, -0.5f, 0.5f, 0);
                break;
            case R.id.img_upright:
                Toast.makeText(this, "click up right", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0.5f, 0.5f, 0);
                break;
            case R.id.zoomin:
                Toast.makeText(this, "click zoomin", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0, 0, 0.1f);
                break;
            case R.id.zoomout:
                Toast.makeText(this, "click zoomout", Toast.LENGTH_SHORT).show();
                PTZController.getInstance().sendRemotePtz(nvr_id, cameraId, 0, 0, -0.1f);
                break;
            case R.id.btnGetDetail:
                cameraId = edtCameraId.getText().toString();
                if (cameraId.length() > 0) {
                    getDetailCamera(site_id, cameraId);
                } else {
                    Toast.makeText(this, "Hay nhap Camera id", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnChange:
                Intent intent = new Intent(ControlPtzActivity.this, ScreenBrightnessChange.class);
                intent.putExtra("EXPOSURE",seekExpose.getProgress());
                //intent.putExtra("BRIGHTNESS",seekBarLight.getProgress());
                startActivity(intent);
            default:
                break;
        }
    }

    private void getDetailCamera(int site_id, final String cameraId) {
        Call<JsonElement> call = SeverApiHelper.getInstance().getAPIService().detailCamera(RequestBusiness.queryDetail(token), RequestBusiness.fieldDetail(cameraId, String.valueOf(site_id)));

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                Utils.mLogE("JsonElement: " + new Gson().toJson(response.body()));
                try {
                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    JSONObject result = jsonObject.optJSONObject("result");

                    JSONArray dataArr = result.optJSONArray("data");
                    JSONObject detail0 = dataArr.optJSONObject(0);
                    nvr_id = detail0.optString("device_server_id");
                    PTZController.getInstance().connectCamPTZ(nvr_id, cameraId);
                    PTZController.getInstance().sendOnvifJoin(cameraId);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, Throwable t) {

            }
        });
    }

    @Override
    public void connectPtzSuccess(String data) {
        Toast.makeText(this, "connect camera success", Toast.LENGTH_SHORT).show();
    }
}
