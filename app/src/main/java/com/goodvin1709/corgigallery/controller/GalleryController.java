package com.goodvin1709.corgigallery.controller;

import android.os.Handler;

import com.goodvin1709.corgigallery.model.Image;

import java.util.List;

public interface GalleryController {

    void attachHandler(Handler handler);

    void loadURLList();

    boolean isLoadingList();

    boolean isConnectionError();

    boolean isListLoaded();

    List<Image> getImages();

    void getBitmap(Image image, int size);
}
