package com.goodvin1709.corgigallery.controller.impl;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import com.goodvin1709.corgigallery.activity.GalleryActivity;
import com.goodvin1709.corgigallery.controller.CacheListener;
import com.goodvin1709.corgigallery.controller.ControllerStatus;
import com.goodvin1709.corgigallery.controller.DownloadListener;
import com.goodvin1709.corgigallery.controller.GalleryController;
import com.goodvin1709.corgigallery.model.Image;
import com.goodvin1709.corgigallery.model.ImageStatus;
import com.goodvin1709.corgigallery.pool.TaskPool;
import com.goodvin1709.corgigallery.pool.impl.TaskPoolExecutor;
import com.goodvin1709.corgigallery.pool.task.ImageDownloadTask;
import com.goodvin1709.corgigallery.pool.task.ImageLoadTask;
import com.goodvin1709.corgigallery.pool.task.ImageSaveTask;
import com.goodvin1709.corgigallery.pool.task.ListDownloadTask;
import com.goodvin1709.corgigallery.utils.CacheUtils;
import com.goodvin1709.corgigallery.utils.Logger;
import com.goodvin1709.corgigallery.utils.impl.CacheUtilsImpl;

import java.util.ArrayList;
import java.util.List;

public class GalleryControllerImpl implements GalleryController, DownloadListener, CacheListener {

    private ControllerStatus status;
    private final TaskPool pool;
    private List<Image> images;
    private Handler handler;
    private CacheUtils cache;

    public GalleryControllerImpl() {
        cache = new CacheUtilsImpl(this);
        images = new ArrayList<Image>();
        pool = new TaskPoolExecutor();
        status = ControllerStatus.CREATED;
    }

    @Override
    public void attachHandler(Handler handler) {
        this.handler = handler;
        if (status == ControllerStatus.CREATED) {
            startDownloadImagesList();
        }
    }

    @Override
    public void loadURLList() {
        if (!isLoadingList()) {
            startDownloadImagesList();
        }
    }

    @Override
    public boolean isLoadingList() {
        return status == ControllerStatus.LOADING;
    }

    @Override
    public boolean isConnectionError() {
        return status == ControllerStatus.CONNECTION_ERROR;
    }

    @Override
    public boolean isListLoaded() {
        return status == ControllerStatus.LOADED;
    }

    @Override
    public List<Image> getImages() {
        return images;
    }

    @Override
    public void loadImage(Image image, ImageView view) {
        if (cache.isCachedInMemory(image)) {
            loadImageFromMemory(image, view);
        } else if (cache.isCachedInExternal(image)) {
            loadImageFromExternal(image, view);
        } else {
            DownloadImage(image);
        }
    }

    private void loadImageFromMemory(Image image, ImageView view) {
        cache.loadBitmapFromMemoryCache(image, view);
    }

    private void loadImageFromExternal(Image image, ImageView view) {
        if (image.getStatus() != ImageStatus.CACHING) {
            image.setStatus(ImageStatus.CACHING);
            pool.addTaskToPool(new ImageLoadTask(image, view, this));
        }
    }

    private void DownloadImage(Image image) {
        if (image.getStatus() != ImageStatus.LOADING) {
            image.setStatus(ImageStatus.LOADING);
            pool.addTaskToPool(new ImageDownloadTask(image, this));
        }
    }

    @Override
    public void onListDownloaded(List<Image> images) {
        status = ControllerStatus.LOADED;
        this.images = images;
        Logger.log("List of Images has been downloaded [%d images].", images.size());
        showOnView(GalleryActivity.DOWNLOADING_LIST_COMPLETE_MSG_ID);
    }

    @Override
    public void onDownloadListError() {
        status = ControllerStatus.CONNECTION_ERROR;
        showOnView(GalleryActivity.CONNECTION_ERROR_MSG_ID);
        Logger.log("Error while downloading image list.");
    }

    @Override
    public void onImageDownloaded(Image image, Bitmap bitmap) {
        Logger.log("Image[%s] downloaded.", image.getUrl());
        pool.addTaskToPool(new ImageSaveTask(image, bitmap, this));
    }

    @Override
    public void onDownloadImageError(Image image) {
        image.setStatus(ImageStatus.LOADING_ERROR);
        Logger.log("Error while downloading Image[%s]", image.getUrl());
    }

    @Override
    public void onImageCachedToMemory(Image image) {
        showOnView(GalleryActivity.GALLERY_IMAGES_UPDATED);
        Logger.log("Image[%s] saved to memory cache.", image.getUrl());
    }

    @Override
    public void onImageCachedToExternal(Image image) {
        image.setStatus(ImageStatus.IDLE);
        showOnView(GalleryActivity.GALLERY_IMAGES_UPDATED);
        Logger.log("Image[%s] saved to external cache.", image.getUrl());
    }

    @Override
    public void onSaveCacheError(Image image) {
        image.setStatus(ImageStatus.CACHED_ERROR);
        Logger.log("Error while saving image %s to cache.", image.getUrl());
    }

    @Override
    public void onLoadCacheError(Image image) {
        image.setStatus(ImageStatus.CACHED_ERROR);
        Logger.log("Error while loading Image[%s] from cache.", image.getUrl());
    }

    @Override
    public void onImageLoadedFromMemoryCache(Image image) {
        image.setStatus(ImageStatus.IDLE);
        Logger.log("Image[%s] loaded from memory cache.", image.getUrl());
    }

    @Override
    public void onImageLoadedFromExternalCache(Image image, Bitmap bitmap) {
        if (bitmap != null) {
            cache.saveBitmapToMemoryCache(image, bitmap);
        }
        Logger.log("Image[%s] loaded from external cache.", image.getUrl());
    }

    private void startDownloadImagesList() {
        status = ControllerStatus.LOADING;
        pool.addTaskToPool(new ListDownloadTask(this));
        Logger.log("Started downloading image list.");
        showOnView(GalleryActivity.DOWNLOADING_LIST_STARTED_MSG_ID);
    }

    private void showOnView(int msgId) {
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(msgId));
        }
    }
}