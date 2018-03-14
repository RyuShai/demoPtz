
package com.example.tsuki.demoptz.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Site {

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
    private String siteImage;
    @SerializedName("children")
    @Expose
    private List<Child> children = null;

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

    public String getSiteImage() {
        return siteImage;
    }

    public void setSiteImage(String siteImage) {
        this.siteImage = siteImage;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

}
