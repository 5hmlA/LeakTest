package com.jonas.leaktest.application;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author jiangzuyun.
 * @see [相关类/方法]
 * @since [产品/模版版本]
 * @deprecated
 */
public class LeakApp extends Application{

    private RefWatcher mLeakWatcher;

    @Override
    public void onCreate(){
        super.onCreate();
        mLeakWatcher = LeakCanary.install(this);
    }

}
