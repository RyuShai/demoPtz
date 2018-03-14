package com.example.tsuki.demoptz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.tsuki.demoptz.retrofit.RequestBusiness;
import com.example.tsuki.demoptz.retrofit.SeverApiHelper;
import com.example.tsuki.demoptz.socket.PTZController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenBrightnessChange extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener,
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnBufferingUpdateListener,
        PTZController.SocketIOCameraListFragmentListener,
        PTZController.IConnectPtz
        {
            @Override
            public void onSucess() {
                com.example.tsuki.demoptz.utils.Utils.mLogE("connect socket onSucess");
            }

            @Override
            public void onError() {

            }

            @Override
            public void connectPtzSuccess(String data) {
                Toast.makeText(this, "connect camera success", Toast.LENGTH_SHORT).show();
            }

            public enum RecordState{
        EXPOSURE_50,
        EXPOSURE_150,
        NONE
    }
    static {
        System.loadLibrary("native-lib");
        if(OpenCVLoader.initDebug())
        {
            Log.e("ryu", "ngon");
        }
        else
        {
            Log.e("ryu", "f***");
        }
    }
    RecordState rcdState;
    Timer timer;
    int ambientValue = 0;
    int delayTime=1000;
    Button auto,next,hide;
    boolean isAuto=false;
    ImageView img;
    RadioButton oldStyle,newStyle;

    TextureView textureView;
    MediaPlayer mp;
    List<Mat> totalFrame;
    boolean isRun =true;
    int biChia = 1;
    List<Float> listData;
    List<Float> result50,result150;
    MediaPlayer playSound;
    int frameCount =0;
    ////////
    String token, cameraID;
    int site_id;
    private String nvr_id;
    String videoUrl="";
    /////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_screen_brightness_change);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        delayTime = intent.getIntExtra("RYU",1000);
        Log.e("Ryu", "create second page"+ String.valueOf(delayTime));
        //connect to stmc
        //1, get info use to connect server
        token = getIntent().getStringExtra("token");
        site_id = getIntent().getIntExtra("site_id", 0);
        cameraID = getIntent().getStringExtra("camera_id");
        rcdState = RecordState.EXPOSURE_50;
        //2. instance PTZ
        PTZController.getInstance().setActivity();
        PTZController.getInstance().setiConnectPtz(this);
        connectSocket(token);
        //3, connect camera to get video url
        getDetailCamera(site_id,cameraID);
        //
        result50 = new ArrayList<>();
        result150 = new ArrayList<>();
        listData = new ArrayList<>();
        totalFrame = new ArrayList<>();

        auto= (Button) findViewById(R.id.auto);
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAuto)
                {
                    isAuto = true;
//                    img.setAlpha(0);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            IncreaseBrighness();
                        }
                    },0,delayTime);
//                    Log.e("ryu", "isplayijg: "+ mp.isPlaying());
                    if(!mp.isPlaying())
                    {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mp.start();
//                                textureView.setAlpha(0f);
                                img.setAlpha(0f);
                            }
                        }, 5000);
                    }
                }
            }
        });
        img = (ImageView) findViewById(R.id.imageView);
        ChangeImageColor(0);

        hide = (Button) findViewById(R.id.hide);
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mp.start();
//                            textureView.setAlpha(0);
                            img.setAlpha(0f);
                        }
                    }, 5000);

            }
        });
        oldStyle = (RadioButton) findViewById(R.id.radioButton);
        oldStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newStyle.setChecked(false);
                ambientValue = 0;
            }
        });
        newStyle = (RadioButton) findViewById(R.id.radioButton2);
        newStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldStyle.setChecked(false);
                ambientValue = 0;
                Intent intent = getIntent();
                delayTime = intent.getIntExtra("RYU",1000);
            }
        });

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAuto)
                {
                    timer.cancel();
                    timer.purge();
                    isAuto = false;
                    Log.e("ryu","cancel purge");

                }
//
//                IncreaseBrighness();
//                Mat matImg = new Mat(1080,1920, CvType.CV_8UC1,new Scalar(ambientValue));
//                Log.e("ryu", "mat size: "+matImg.rows() + " "+ matImg.cols());
//                Bitmap bmp = Bitmap.createBitmap(matImg.cols(), matImg.rows(), Bitmap.Config.RGB_565);
//                Utils.matToBitmap(matImg, bmp);
//                img.setImageBitmap(bmp);
                ChangeImageColor(ambientValue);
                ambientValue+=50;
                if(ambientValue>250)
                    ambientValue=0;

            }
        });
        mp = new MediaPlayer();
        textureView = (TextureView) findViewById(R.id.textureView);

//        textureView.setAlpha(0);
        playSound = MediaPlayer.create(this,R.raw.beep);

        stringFromJNI();
        Settings.System.putInt(getApplicationContext().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,255);
    }

    void ChangeImageColor(final int value)
    {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Mat matImg = new Mat(3240,2100, CvType.CV_8UC1,new Scalar(value));
//                Log.e("ryu", "mat size: "+matImg.rows() + " "+ matImg.cols());
                Bitmap bmp = Bitmap.createBitmap(matImg.cols(), matImg.rows(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(matImg, bmp);
                img.setImageBitmap(bmp);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
    }
    long lastTime=0;
    void IncreaseBrighness()
    {
        ChangeImageColor(ambientValue);
        ambientValue+=50;
        if(ambientValue>250)
            ambientValue=0;

    }

    public native String stringFromJNI();
    public native int getIntfromMat(long matAdrr);
    public native void setInt2Mat(long matAdrr, int value);

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mediap) {
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mp.start();
//                img.setAlpha(0f);
////                textureView.setAlpha(0);
//            }
//        }, 10000);
    Log.e("ryu", "onPrepared");
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        if(rcdState==RecordState.EXPOSURE_50)
        {
            Log.e("ryu","onSurfaceTextureAvailable");
            Surface s = new Surface(surface);

            try
            {

                mp.setDataSource(videoUrl);
                mp.setSurface(s);
                mp.prepare();

                mp.setOnBufferingUpdateListener(this);
                mp.setOnCompletionListener(this);
                mp.setOnPreparedListener(this);
                mp.setOnVideoSizeChangedListener(this);



            }
            catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.e("ryu","onSurfaceTextureUpdated");
        if(isRun)
        {
            if(frameCount<1500)
            {
                Mat mat = new Mat();
                Bitmap bm = textureView.getBitmap();
                bm = bm .copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(bm, mat);
                Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
//                Imgproc.resize(mat, mat, new Size(mat.cols()*0.3,mat.rows()*0.3));
                float addValue = getIntfromMat(mat.getNativeObjAddr());
                listData.add(addValue);
//                totalFrame.add(mat);
                frameCount++;
            }
            else
            {
                isRun=false;

                ImageDo();
            }

        }
    }

    void ImageDo()
    {
        Log.e("ryu", "ImageDo: ");
        int stable=0;
//        int totalPixelValue=0;
        float sub=0, totalPlus=0;
//        Log.e("ryu","row: "+ totalFrame.get(0).rows()+" height: "+ totalFrame.get(0).cols());
//        int width = totalFrame.get(0).cols();
//        int heith = totalFrame.get(0).rows();
//        biChia = width*heith;
//        listData.clear();
        /////////////////
//        for(int pos=0; pos<totalFrame.size();pos++)
//        {
//            Mat mat = totalFrame.get(pos);
//            totalPixelValue = getIntfromMat(mat.getNativeObjAddr());
//            Log.e("ryu", "origin: "+ totalPixelValue/(biChia)+ " "+ listData.size());
//            listData.add((float) totalPixelValue/(biChia));
//            totalPixelValue =0;
//        }

        if(rcdState==RecordState.EXPOSURE_50)
        {
            ///////////////////
            for(int i=0; i<listData.size()-1;i++)
            {
                sub = listData.get(i+1)-listData.get(i);
                if(sub<2 && sub>(-2))
                {
                    stable++;
                    totalPlus +=listData.get(i);
                }
                else
                {
                    if(stable>10)
                    {
                        totalPlus+=listData.get(i);
                        stable++;
                        float rs = totalPlus/stable;
                        result50.add(rs);
                        stable =0;
                        totalPlus=0;
                    }
                    else
                    {
                        stable=0;
                        totalPlus=0;
                    }

                }
            }
//        UniqueList();
            Log.e("ryu","ryusult 50 size: " + result50.size());
            for(int i=0; i<result50.size();i++)
            {
                Log.e("ryu", "result: " + result50.get(i));
            }
            rcdState = RecordState.EXPOSURE_150;
            isRun = true;
            setExposureTime();
        }else if(rcdState==RecordState.EXPOSURE_150)
        {
            ///////////////////
            for(int i=0; i<listData.size()-1;i++)
            {
                sub = listData.get(i+1)-listData.get(i);
                if(sub<2 && sub>(-2))
                {
                    stable++;
                    totalPlus +=listData.get(i);
                }
                else
                {
                    if(stable>10)
                    {
                        totalPlus+=listData.get(i);
                        stable++;
                        float rs = totalPlus/stable;
                        result150.add(rs);
                        stable =0;
                        totalPlus=0;
                    }
                    else
                    {
                        stable=0;
                        totalPlus=0;
                    }

                }
            }
//        UniqueList();
            Log.e("ryu","ryusult 150 size: " + result150.size());
            for(int i=0; i<result150.size();i++)
            {
                Log.e("ryu", "result: " + result150.get(i));
            }
            rcdState= RecordState.NONE;
        }
        playSound.start();
        frameCount=0;
//        textureView.setAlpha(0);
        listData.clear();
    }

    void UniqueList()
    {
        HashSet<Float> hashSet = new HashSet<Float>();
        hashSet.addAll(result50);
        result50.clear();
        result50.addAll(hashSet);
    }

    private void getDetailCamera(int site_id, final String cameraId) {
        Call<JsonElement> call = SeverApiHelper.getInstance().getAPIService().detailCamera(RequestBusiness.queryDetail(token), RequestBusiness.fieldDetail(cameraId, String.valueOf(site_id)));

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                com.example.tsuki.demoptz.utils.Utils.mLogE("JsonElement: " + new Gson().toJson(response.body()));
                try {
                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    JSONObject result = jsonObject.optJSONObject("result");

                    JSONArray dataArr = result.optJSONArray("data");
                    JSONObject detail0 = dataArr.optJSONObject(0);
                    nvr_id = detail0.optString("device_server_id");
                    Log.e("ryu", "nvr: "+ nvr_id + " cameraID "+ cameraId);
                    PTZController.getInstance().connectCamPTZ(nvr_id, cameraId);
                    PTZController.getInstance().sendOnvifJoin(cameraId);

                    //set video url then init texture view
                    videoUrl = detail0.optString("src");
                    Log.e("ryu","url: "+videoUrl);
                    //set exposure time
                    setExposureTime();
                    // set url to MediaPlayer
                    videoUrl = "http://4co2.vp9.tv/chn/DNG101/v.m3u8";
                    textureView.setSurfaceTextureListener(ScreenBrightnessChange.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, Throwable t) {

            }
        });
    }
    void setExposureTime()
    {
        Log.e("ryu","setExposureTime");
        if(rcdState == RecordState.EXPOSURE_50)
            PTZController.getInstance().sendExposureTime(nvr_id, cameraID, 50);
        else if(rcdState == RecordState.EXPOSURE_150)
            PTZController.getInstance().sendExposureTime(nvr_id, cameraID, 150);
    }

    private void connectSocket(String token) {
        PTZController.getInstance().setSocketIOCameraListFragmentListener(this);
        PTZController.getInstance().connectSocket(token, this);
    }
}