package com.example.tsuki.demoptz;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tsuki.demoptz.model.LoginData;
import com.example.tsuki.demoptz.retrofit.RequestBusiness;
import com.example.tsuki.demoptz.retrofit.SeverApiHelper;
import com.example.tsuki.demoptz.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonLogin = findViewById(R.id.btn_login);
        final EditText edtUser = findViewById(R.id.tv_username);
        final EditText edtPass = findViewById(R.id.tv_password);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtUser.getText().toString().length() > 0 && edtPass.getText().toString().length() > 0) {
                    login(edtUser.getText().toString(), Utils.encypt(edtPass.getText().toString()));
                }
            }
        });
    }

    private void login(String username, String password) {
        final String formatAgent = "%s %s/%s";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            String agent = String.format(formatAgent, getString(R.string.app_name), info.versionName, "Android");
            Call<LoginData> call = SeverApiHelper.getInstance().getAPIService().login(RequestBusiness.login(username, "", password, agent));
            call.enqueue(new Callback<LoginData>() {
                @Override
                public void onResponse(@NonNull Call<LoginData> call, @NonNull Response<LoginData> response) {
                    Log.e("eee", "response: " + response.toString());
                    LoginData loginData = response.body();
                    if (loginData != null) {
                        if (loginData.getCode() != 0) {
                            Toast.makeText(MainActivity.this, "login error: " + loginData.getStatus(), Toast.LENGTH_SHORT).show();
                        } else {
                            String token = loginData.getToken();
                            int site_id = loginData.getSite().getSiteId();
                            Intent intent = new Intent(MainActivity.this, ControlPtzActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("site_id", site_id);
                            startActivity(intent);
                            finish();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginData> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
