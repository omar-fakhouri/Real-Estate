package com.app.models;

import android.net.Uri;

import com.google.firebase.Timestamp;

import java.util.List;

public class Property {


    private String id;
    private String title;
    private String location;
    private String price;
    private String purpose;
    private String userId;
    private int views;
    private Timestamp createdAt;
    private String mainImage;
    private String description;
    private String phone;
    private String address;
    private String bed;
    private String bath;
    private String area;
    private String amenity;
    private String category;
    private String furnished;
    private double latitude;

    private double longitude;
    private boolean available;
    private String mainImageUrl;
    private List<String> galleryImages;
    public Property(){}

    public Property(String id, String title, String location, String price, String purpose, String userId, int views, Timestamp createdAt, String mainImage, String description, String phone, String address, String bed, String bath, String area, String amenity, String category, String furnished, double latitude, double longitude, boolean available, String mainImageUrl, List<String> galleryImages) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.price = price;
        this.purpose = purpose;
        this.userId = userId;
        this.views = views;
        this.createdAt = createdAt;
        this.mainImage = mainImage;
        this.description = description;
        this.phone = phone;
        this.address = address;
        this.bed = bed;
        this.bath = bath;
        this.area = area;
        this.amenity = amenity;
        this.category = category;
        this.furnished = furnished;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
        this.mainImageUrl = mainImageUrl;
        this.galleryImages = galleryImages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBed() {
        return bed;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    public String getBath() {
        return bath;
    }

    public void setBath(String bath) {
        this.bath = bath;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAmenity() {
        return amenity;
    }

    public void setAmenity(String amenity) {
        this.amenity = amenity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFurnished() {
        return furnished;
    }

    public void setFurnished(String furnished) {
        this.furnished = furnished;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public List<String> getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(List<String> galleryImages) {
        this.galleryImages = galleryImages;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}