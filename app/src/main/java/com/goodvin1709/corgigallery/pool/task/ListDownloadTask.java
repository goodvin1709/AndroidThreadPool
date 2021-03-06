package com.goodvin1709.corgigallery.pool.task;

import com.goodvin1709.corgigallery.controller.DownloadListener;
import com.goodvin1709.corgigallery.model.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ListDownloadTask implements Runnable {

    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private DownloadListener handler;
    private String listURL;

    public ListDownloadTask(String listURL, DownloadListener handler) {
        this.listURL = listURL;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            downloadList();
        } catch (IOException e) {
            handler.onDownloadListError();
        }
    }

    private void downloadList() throws IOException {
        URL url = new URL(listURL);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setUseCaches(false);
        connection.connect();
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        ArrayList<Image> list = new ArrayList<>();
        String line;
        while ((line = input.readLine()) != null) {
            list.add(new Image(line));
        }
        input.close();
        handler.onListDownloaded(list);
        handler = null;
    }
}
