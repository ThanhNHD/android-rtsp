package com.thesis.myapplication.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;

import java.util.Objects;
import java.util.Optional;

public class NetworkUtils {
    public static String getDeviceIpAddress(Context context) {
        ConnectivityManager connectivityManager=
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Optional<String> test = Objects.requireNonNull(connectivityManager.getLinkProperties(
                        connectivityManager.getActiveNetwork()
                )).getLinkAddresses().stream()
                .filter(linkAddress ->
                        linkAddress.getAddress().getAddress().length==4)
                .findFirst()
                .map(linkAddress -> linkAddress.getAddress().getHostAddress());
        return test.orElse("error");
    }
}