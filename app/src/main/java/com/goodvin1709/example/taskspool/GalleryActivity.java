package com.goodvin1709.example.taskspool;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.goodvin1709.example.taskspool.dialog.download.DownloadDialog;
import com.goodvin1709.example.taskspool.dialog.download.impl.DownloadDialogImpl;
import com.goodvin1709.example.taskspool.impl.GalleryPresenterImpl;

import java.lang.ref.WeakReference;

public class GalleryActivity extends Activity {

    private static final String DOWNLOAD_DIALOG_STATE_KEY = "DOWNLOAD_DIALOG_STATE";
    public static final int DOWNLOADING_LIST_STARTED_MSG = 0xfa;
    public static final int DOWNLOADING_LIST_COMPLETE_MSG = 0xfb;
    public static final int DOWNLOADING_ERROR = 0xfc;

    private GalleryPresenter galleryPresenter;
    private DownloadDialog downloadDialog;
    private final GalleryHandler handler = new GalleryHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_actvity);
        downloadDialog = new DownloadDialogImpl(this);
        initPresenter();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return galleryPresenter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DOWNLOAD_DIALOG_STATE_KEY, downloadDialog.isShowing());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(DOWNLOAD_DIALOG_STATE_KEY)) {
            showDownloadProgress();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void initPresenter() {
        GalleryPresenter presenter = (GalleryPresenter) getLastNonConfigurationInstance();
        if (presenter == null) {
            galleryPresenter = new GalleryPresenterImpl(handler);
            galleryPresenter.startDownloadImagesList();
        } else {
            galleryPresenter = presenter;
            galleryPresenter.attachViewHandler(handler);
        }
    }

    private void showDownloadProgress() {
        downloadDialog.show();
    }

    private void hideDownloadProgress() {
        downloadDialog.hide();
    }

    private void showConnectionError() {
        downloadDialog.hide();
        Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show();
    }

    private static class GalleryHandler extends Handler {
        private final WeakReference<GalleryActivity> view;

        GalleryHandler(GalleryActivity view) {
            this.view = new WeakReference<GalleryActivity>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOADING_LIST_STARTED_MSG:
                    view.get().showDownloadProgress();
                    break;
                case DOWNLOADING_LIST_COMPLETE_MSG:
                    view.get().hideDownloadProgress();
                    break;
                case DOWNLOADING_ERROR:
                    view.get().showConnectionError();
                    break;
            }
        }
    }
}
