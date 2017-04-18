package com.repgate.sales;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.repgate.sales.util.LogUtil;
import com.repgate.sales.util.Validation;

/**
 * Created by developer on 1/14/16.
 */
public class AppRepgate extends Application {

    private Intent mIntent = null;
    public static AppRepgate instance;

    public final Intent getIntent() {
        if (mIntent == null)
            mIntent = new Intent();

        return mIntent;
    }

    public final void setIntent(Intent paramIntent) {
        mIntent = paramIntent;
    }

    public AppRepgate() {
        instance = this;
    }

    public static AppRepgate getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        Validation.initialize(this.getApplicationContext());

        LogUtil.deleteLogFile();

        initImageLoader(getApplicationContext());

    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(1 * 512 * 1024); // 500K
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.imageDecoder(new BaseImageDecoder(true));
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

}
