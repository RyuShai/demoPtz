package com.example.tsuki.demoptz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.opencv.videoio.Videoio.CAP_PROP_POS_FRAMES;

public class ScreenBrightnessChange extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener,
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnBufferingUpdateListener {


    static {
        System.loadLibrary("native-lib");
        if (OpenCVLoader.initDebug()) {
            Log.e("ryu", "ngon");
        } else {
            Log.e("ryu", "f***");
        }
    }

    int get_current_et;
    Timer timer;
    int ambientValue = 0;
    int delayTime = 1000;
    Button auto, next, hide;
    boolean isAuto = false;
    ImageView img;
    RadioButton oldStyle, newStyle;


    TextureView textureView;
    MediaPlayer mp;
    List<Mat> totalFrame;
    boolean isRun = true;
    int biChia = 1;
    List<Float> listData;
    List<Float> result;
    MediaPlayer playSound;
    int frameCount = 0;
    float[][][] brightness_table = new float[2][6][7];
    int flash_interval = 1000;

    //
    private int site_id;
    private String token;
    private String nvr_id;
    private String src;
    private String cameraId = "100029"; //15125
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_screen_brightness_change);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();

        token = getIntent().getStringExtra("token");
        site_id = getIntent().getIntExtra("site_id", 0);
//        src = getIntent().getStringExtra("src");
//
//        Log.d("Thuy", "src " + src);



//        delayTime = intent.getIntExtra("RYU", 1000);
//        get_current_et = intent.getIntExtra("EXPOSURE",0);  // get Exposure time from ControlActivity

        auto = (Button) findViewById(R.id.auto);
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAuto) {
                    isAuto = true;
//                    img.setAlpha(0);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            IncreaseBrighness();
                        }
                    }, 0, delayTime);
//                    Log.e("ryu", "isplayijg: "+ mp.isPlaying());
//                    if(!isRun && !mp.isPlaying())
//                    {
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mp.start();
//                                img.setAlpha(0);
//                            }
//                        }, 5000);
//                    }
                }
            }
        });
        img = (ImageView) findViewById(R.id.imageView);
        ChangeImageColor(0);

        hide = (Button) findViewById(R.id.hide);
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(img.getAlpha()==(float) 0)
//                {
//                    img.setAlpha((float) 1.0);
//                }
//                else
//                {
//                    img.setAlpha((float)0);
//                }
                if (!isRun) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mp.start();
//                            textureView.setAlpha(0);

                        }
                    }, 5000);
                }
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
                delayTime = intent.getIntExtra("RYU", 1000);
            }
        });

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAuto) {
                    timer.cancel();
                    timer.purge();
                    isAuto = false;
                    Log.e("ryu", "cancel purge");

                }
//
//                IncreaseBrighness();
//                Mat matImg = new Mat(1080,1920, CvType.CV_8UC1,new Scalar(ambientValue));
//                Log.e("ryu", "mat size: "+matImg.rows() + " "+ matImg.cols());
//                Bitmap bmp = Bitmap.createBitmap(matImg.cols(), matImg.rows(), Bitmap.Config.RGB_565);
//                Utils.matToBitmap(matImg, bmp);
//                img.setImageBitmap(bmp);
                ChangeImageColor(ambientValue);
                ambientValue += 50;
                if (ambientValue > 250)
                    ambientValue = 0;

            }
        });

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        textureView.setAlpha(0);
        playSound = MediaPlayer.create(this, R.raw.beep);
        Log.e("Ryu", "create second page" + String.valueOf(delayTime));

        result = new ArrayList<>();
        listData = new ArrayList<>();
        totalFrame = new ArrayList<>();

        stringFromJNI();
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
        getDetailCamera(site_id,cameraId);
    }

    void ChangeImageColor(final int value) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Mat matImg = new Mat(3240, 2100, CvType.CV_8UC1, new Scalar(value));
                Log.e("ryu", "mat size: " + matImg.rows() + " " + matImg.cols());
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

    long lastTime = 0;

    void IncreaseBrighness() {
//        Log.e("ryu", "last: "+String.valueOf(System.currentTimeMillis()-lastTime)+" ambient"+ ambientValue);
//            if(ambientValue>250) {
//                ambientValue = 0;
//            }
//            ChangeImageColor(ambientValue);
//            lastTime = System.currentTimeMillis();
//            if(ambientValue == 0 )
//            {
//                long ftime = System.currentTimeMillis();
//                while(System.currentTimeMillis()-ftime<(delayTime+1000))
//                {
//
//                }
//            }
//            Log.e("ryu", "ambientValue: "+ ambientValue);

//            ambientValue+=50;
//        Log.e("ryu", "delay : "+delayTime);

        ChangeImageColor(ambientValue);
        ambientValue += 50;
        if (ambientValue > 250)
            ambientValue = 0;

    }

    //    void IncreaseBrighness2()
//    {
//       switch (ambientValue)
//       {
//           case 0:
//               ambientValue=56; break;
//           case 56:
//               ambientValue=84; break;
//           case 84:
//               ambientValue=112; break;
//           case 112:
//               ambientValue=126; break;
//           case 126:
//               ambientValue=140; break;
//           case 140:
//               ambientValue=168; break;
//           case 168:
//               ambientValue=196; break;
//           case 196:
//               ambientValue=224; break;
//           case 224:
//               ambientValue=252; break;
//           case 252:
//               ambientValue=0; break;
//       }
//        Settings.System.putInt(getApplicationContext().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,ambientValue);
//        Log.e("ryu", "ambientValue: "+ ambientValue);
//
//    }
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
////                textureView.setAlpha(0);
//            }
//        }, 10000);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (!isRun) {
            Surface s = new Surface(surface);

            try {
                mp = new MediaPlayer();
                mp.setDataSource(src);
                mp.setSurface(s);
                mp.prepare();

                mp.setOnBufferingUpdateListener(this);
                mp.setOnCompletionListener(this);
                mp.setOnPreparedListener(this);
                mp.setOnVideoSizeChangedListener(this);


            } catch (IllegalArgumentException e) {
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
        if (!isRun) {
            if (frameCount < 1500) {
                Mat mat = new Mat();
                Bitmap bm = textureView.getBitmap();
                bm = bm.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(bm, mat);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.resize(mat, mat, new Size(mat.cols()*0.3,mat.rows()*0.3));
                float addValue = getIntfromMat(mat.getNativeObjAddr());
                listData.add(addValue);
//                totalFrame.add(mat);
                frameCount++;
            } else {
                isRun = true;
                ImageDo();
            }

        }
    }

    void ImageDo() {
//        Log.e("ryu", "end: "+totalFrame.size());
        int stable = 0;
//        int totalPixelValue=0;
        float sub = 0, totalPlus = 0;
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


        ///////////////////
        for (int i = 0; i < listData.size() - 1; i++) {
            sub = listData.get(i + 1) - listData.get(i);
            if (sub < 2 && sub > (-2)) {
                stable++;
                totalPlus += listData.get(i);
            } else {
                if (stable > 10) {
                    totalPlus += listData.get(i);
                    stable++;
                    float rs = totalPlus / stable;
                    result.add(rs);
                    stable = 0;
                    totalPlus = 0;
                } else {
                    stable = 0;
                    totalPlus = 0;
                }

            }
        }
//        UniqueList();
        Log.e("ryu", "ryusult size: " + result.size());
        for (int i = 0; i < result.size(); i++) {
            Log.e("ryu", "result: " + result.get(i));
        }
        playSound.start();
        //save data to file
        //qua Ton => exposure time
        //update exposure time to camera = onvif
        //

        textureView.setAlpha(0);
        listData.clear();
    }


    void UniqueList() {
        HashSet<Float> hashSet = new HashSet<Float>();
        hashSet.addAll(result);
        result.clear();
        result.addAll(hashSet);
    }

    float get_brightness_level(Mat frame) {
        float brightness_level = 0;
        int n_pixel = 0;
        int width = frame.cols();
        int height = frame.rows();
        float[] hist = new float[256];
        float[] hist_accum = new float[256];
        for (int i = 0; i < 256; i++) {
            hist[i] = 0;
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int tmp = (int) frame.get(j, i)[0];
                hist[tmp] = hist[tmp] + 1;
            }
        }
        for (int i = 0; i < 256; i++) {
            hist[i] = hist[i] / (float) (width * height);
        }
        hist_accum[0] = hist[0];
        for (int i = 1; i < 256; i++) {
            hist_accum[i] = hist[i] + hist_accum[i - 1];

            if (hist_accum[i] > 0.95) {
                brightness_level = i;
                break;
            }
        }

        float brightness_level1 = brightness_level;
        brightness_level = 0;
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                brightness_level += (int) frame.get(j, i)[0];
                count++;
            }
        }
        brightness_level = (float) brightness_level / count;

        return (brightness_level);
    }

    int get_aperture_level(Vector<CalibrateData> input_data) {
        int aperture_level = 0;

        try {
            InputStream instream = openFileInput("/home/ntddung/Ex50.txt");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                try {
                    String[] tmp;
                    int n_line = 0;
                    while ((line = buffreader.readLine()) != null) {
                        tmp = line.split(" ");

                        for (int i = 0; i < tmp.length; i++) {
                            brightness_table[0][n_line][i] = Float.parseFloat(tmp[i]);
                        }
                        n_line++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            String error = "";
            error = e.getMessage();
        }

        try {
            InputStream instream = openFileInput("/home/ntddung/Ex150.txt");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                try {
                    String[] tmp;
                    int n_line = 0;
                    while ((line = buffreader.readLine()) != null) {
                        tmp = line.split(" ");

                        for (int i = 0; i < tmp.length; i++) {
                            brightness_table[0][n_line][i] = Float.parseFloat(tmp[i]);
                        }
                        n_line++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            String error = "";
            error = e.getMessage();
        }


        float[] distance = new float[7];
        for (int i = 0; i < 7; i++) {
            distance[i] = 0;
            for (int j = 0; j < input_data.size(); j++) {
                int current_exposure = input_data.get(j).exposure_time;
                int exposure_index = 0;
                float[][] brightness_level_tmp = new float[8][10];

                if (current_exposure == 50) {
                    exposure_index = 0;
                } else if (current_exposure == 100) {
                    exposure_index = 1;
                } else if (current_exposure == 150) {
                    exposure_index = 2;
                }

                for (int k = 0; k < 7; k++) {
                    for (int h = 0; h < 6; h++) {

                    }

                }
                for (int k = 0; k < 6; k++) {
                    distance[i] += (brightness_table[exposure_index][i][k] - input_data.get(j).brightness_level[k]) * (brightness_table[exposure_index][i][k] - input_data.get(j).brightness_level[k]);
                }
            }
        }
        float distance_min = distance[0];
        aperture_level = 0;
        for (int i = 0; i < 7; i++) {
            Log.d("i=\t", "\t" + distance[i]);
            if (distance_min > distance[i]) {
                distance_min = distance[i];
                aperture_level = i + 1;
            }
        }
        Log.d("VP9","aperture_level= \t" + aperture_level);
        return (aperture_level);
    }

    int main()
    {
        Vector <CalibrateData>  input_data = new Vector<>();
        //VideoCapture cap("/home/huuton/Desktop/Aperture/Video_Test/old_far300.mp4");
        VideoCapture cap = new VideoCapture("rtsp://admin:1111@10.11.11.56:554/av0_1");
        String IPCameraAddress = "10.11.11.56";
        int exposure_value[]={50,150,150,200,250,300,400,500,1000,2000,4000,6000,8000};
        //OnvifController onvifController(IPCameraAddress+":2000");
        int n_fpi= (int) (flash_interval/40);
        int n_stable_fpi=(int) (0.7*n_fpi);
//        // onvifController.setExposureTime( exposure_value[i], "VideoSource0");
        for (int i=0;i<2;i++)
        {
            //     // onvifController.setExposureTime(exposure_value[i], "VideoSource0");
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Intent
            //get_current_et=onvifController.getExposureTime();
            Log.d("i=", "\t current_exposuretime=\t"+ get_current_et);

            float[] brightness_array = new float[50];
            float[] brightness_result = new float[10];
            float previous_brightness_level_1=0;
            float previous_brightness_level_2=0;
            float current_brightness_level=0;
            int n_stable_frame=0; // number of frames that have the same brightness level
            float accumulate_brightness=0;
            int n_unstable_frame=0;
            int step_start=0;
            int index_array=0;
            float min_brightness=255;
            float max_brightness=0;
            float saturation_level=0;
            int count=0;
             for(;;)
             {
                 int index = (int)cap.get(CAP_PROP_POS_FRAMES );
                 Mat frame_in = new Mat();
                 count++;
                 boolean ret = cap.read(frame_in);
                 if (frame_in.empty())
                 {
                 break;
                 }
                 Mat frame =  new Mat();
                 if (frame_in.channels()==3)
                 {
                     Imgproc.cvtColor(frame_in, frame,Imgproc.COLOR_BGR2GRAY);
                 }
                 else
                 {
                 frame=frame_in;
                 }
                // imshow("Card Reader", frame);
                 // if ((index % 10) ==0)
                 // {
                 //   condition_flag=check_condition(frame);
                 // }
             // condition_flag=check_condition(frame);

                 int current_frame_id=0;
                 // if (condition_flag==1)
                 {
                     current_brightness_level=get_brightness_level(frame);
                     Log.d("VP9","current_brightness_level" + current_brightness_level);
                     if (current_brightness_level>max_brightness)
                     {
                         max_brightness=current_brightness_level;
                     }
                     if (current_brightness_level<min_brightness)
                     {
                         min_brightness=current_brightness_level;
                     }

                     //cout<<current_brightness_level<<"\t"<<previous_brightness_level<<endl;
                     if ((previous_brightness_level_1==0)||(previous_brightness_level_2==0)||
                     (current_brightness_level<(previous_brightness_level_1-3))||
                     (current_brightness_level>(previous_brightness_level_1+3))||
                     (current_brightness_level<(previous_brightness_level_2-3))||
                     (current_brightness_level>(previous_brightness_level_2+3)))

                     {
                         n_unstable_frame++;
                         if (n_unstable_frame>50)
                         {
                             index_array=0;
                         }
                         previous_brightness_level_2=previous_brightness_level_1;
                         previous_brightness_level_1=current_brightness_level;
                         if (n_stable_frame<n_stable_fpi)
                         {
                             n_stable_frame=0;
                             accumulate_brightness=0;
                             step_start=index;
                         }
                         else
                         {
                           //  cout<<"n_stable_frame=\t"<<n_stable_frame<<endl;
                             int n_brightness_level=((n_stable_frame-n_stable_fpi-1)/n_fpi);
                             int brightness_tmp=(int) accumulate_brightness/n_stable_frame;
                             for (int j =0;j<=n_brightness_level;j++)
                             {
                                 brightness_array[index_array]=brightness_tmp;
                                 index_array++;
                                 if (index_array>19)
                                 {
                                     break;
                                 }
                             }
                             Log.d("VP9","n_stable_frame=\t" + n_stable_frame + "\t n_brightness_level=\t" +
                                     n_brightness_level + "brightness_result=\t"+ brightness_array[index_array-1]);
                             //cout<<"n_stable_frame=\t"<<n_stable_frame<<"\t n_brightness_level=\t"<<n_brightness_level<<"brightness_result=\t"<< brightness_array[index_array-1]<<endl;
                             if (index_array>19)
                             {
                                 break;
                             }
                             // brightness_array[index_array]=(float) accumulate_brightness/n_stable_frame;
                             // cout<<"new value is \t"<<brightness_array[index_array]<<endl;
                             n_stable_frame=0;
                             accumulate_brightness=0;
                         }

                     }
                     else
                     {
                         n_stable_frame++;
                         n_unstable_frame=0;
                         if (n_stable_frame>150)
                         {
                            saturation_level=current_brightness_level;
                             break;
                         }
                         previous_brightness_level_2=previous_brightness_level_1;
                         previous_brightness_level_1=current_brightness_level;
                         accumulate_brightness+=current_brightness_level;
                     }

                 }
                 ////////////if( waitKey(1) == 27 ) break;
             }
             if (index_array<19)
             {
                 if (saturation_level>200)
                 {
                     continue;
                 }
                 else if (saturation_level<120)
                 {
                     break;
                 }
             }
             else
             {
                 //Bat dau xu li cac du lieu thu duoc
                 for (int j=0;j<index_array;j++)
                 {
                     Log.d("vp9", "brightness_level \t"+ j +"=" + brightness_array[j]);
                 }
                 int change_index=0;
                 for (int j=1;j<7;j++)
                 {
                     if (((brightness_array[j+1]+0.5*brightness_array[j])<brightness_array[j]))
                     {
                         change_index=j;
                         break;
                     }

                 }
                 Log.d("vp9", "change_index= "+change_index + "\t" );
                 boolean check_data=true; //check if the data is correctly capture
                 int count_index=0;
                 int []brightness_result_count = new int[10];
                 for (int j=0;j<6;j++)
                 {
                     brightness_result[i]=0;
                     brightness_result_count[i]=0;
                 }
                 for (int j=change_index+1;j<index_array;j++)
                 {
                     brightness_result[count_index]+=brightness_array[i];
                     brightness_result_count[count_index]++;
                     count_index++;
                     if (count_index==6)
                     {
                         count_index=0;
                     }
                 }
                 for (int j=0;j<6;j++)
                 {
                     brightness_result[i]=(float)(brightness_result[i]/ brightness_result_count[i]);
                     Log.d("vp9", "brightness_result \t"+i+brightness_result[i]);

                 }
             }
             CalibrateData data_tmp = new CalibrateData();
             data_tmp.exposure_time=exposure_value[i];
             for (int j=0;j<6;j++)
             {
                 data_tmp.brightness_level[j]=brightness_result[j];
             }
             input_data.add(data_tmp);
        }
        for (int i=0;i<input_data.size();i++)
        {
            Log.d("vp9", "exposure=\t"+ input_data.get(i).exposure_time);

            for (int j=0;j<6;j++)
            {
                Log.d("vp9", "input_data.get(i).brightness_level[j]" +input_data.get(i).brightness_level[j] );

            }
        }
        int aperture_level=get_aperture_level(input_data);
        return (0);
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
                    //src = detail0.optString("src");
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

}
