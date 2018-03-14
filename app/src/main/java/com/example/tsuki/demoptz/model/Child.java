
package com.example.tsuki.demoptz.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Child {

    @SerializedName("site_id")
    @Expose
    private int siteId;
    @SerializedName("site_name")
    @Expose
    private String siteName;
    @SerializedName("site_address")
    @Expose
    private String siteAddress;
    @SerializedName("site_image")
    @Expose
    private Object siteImage;

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteAddress() {
        return siteAddress;
    }

    public void setSiteAddress(String siteAddress) {
        this.siteAddress = siteAddress;
    }

    public Object getSiteImage() {
        return siteImage;
    }

    public void setSiteImage(Object siteImage) {
        this.siteImage = siteImage;
    }

}
