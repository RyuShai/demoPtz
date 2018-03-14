package com.example.tsuki.demoptz.retrofit;

/**
 * Created by Tsuki on 6/5/2017.
 */

public class SeverApiHelper {
    private APIService mAPIService;

    private static SeverApiHelper ourInstance;

    public static SeverApiHelper getInstance() {
        if (ourInstance == null) {
            ourInstance = new SeverApiHelper();
        }
        return ourInstance;
    }

    private SeverApiHelper() {
        mAPIService = BaseRetrofit.createService(APIService.class);
    }

    public APIService getAPIService() {
        return mAPIService;
    }

    public void setAPIService(APIService APIService) {
        mAPIService = APIService;
    }

    public void resetInstance() {
        ourInstance = null;
    }
}
