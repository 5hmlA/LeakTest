package com.jonas.leaktest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jonas.leaktest.application.LeakApp;

/**
 * 查看是否会发生内存泄漏 主要看 被谁引用，引用者的存活时间(生命周期)是否大于自己
 * 生命周期长的引用生命周期短的 导致生命周期短的 无法在该被回收的时候回收
 *
 * 1，单例造成的内存泄漏
 * 2，非静态内部类对象和匿名内部类会持有外部类对象的引用 OuterClass$InnerClass
 * 3，Handler造成的内存泄漏
 */
public class MainActivity extends AppCompatActivity
{

    private static Other mOther = new Other();//外部类
//    private static 内部类 nei2 = new 内部类();//错误
//    private 内部类 nei2 = new 内部类();//正确


    private LeakApp mLeakApp;
    private static String test = "start";
    //静态字段的 生命周期 和进程 一致
    private static 内部类 nei;//nei持有MainActivity对象的引用，MainActivity在被销毁之后应该被回收，而nei为static期生命周期和应用进程一样长
                                //导致MainActivity对象无法被回收

    class 内部类{
        //内部类对象持有外部类对象的引用
        //静态内部类的对象将不会持有外部类对象的引用 static class 内部类{
    }

    //非静态匿名内部类对象持有外部类对象的引用
    private Handler mLeakyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //1.当一个Android应用启动的时候，会自动创建一个供应用主线程使用的Looper实例。
            //Looper的主要工作就是一个一个处理消息队列中的消息对象。在Android中，
            //所有Android框架的事件（比如Activity的生命周期方法调用和按钮点击等）都是放入到消息中，
           // 然后加入到Looper要处理的消息队列中，由Looper负责一条一条地进行处理。主线程中的Looper生命周期和当前应用一样长。

            // 2.当一个Handler在主线程进行了初始化之后，我们发送一个target为这个Handler的消息到Looper处理的消息队列时，
           // 实际上已经发送的消息已经包含了一个Handler实例的引用，
           // 只有这样Looper在处理到这条消息时才可以调用Handler#handleMessage(Message)完成消息的正确处理。
        }
    };
    //非静态匿名内部类对象持有外部类对象的引用
    private static Runnable sRunnable = new Runnable() {
        @Override
        public void run()
        {
            System.out.println("静态runnable对象");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLeakApp = (LeakApp)getApplicationContext();
        System.out.println(test);

//        SingleYou instance = SingleYou.getInstance(this);//MainActivity对象被SingleYou所引用(单例对象是静态的)导致内存泄漏
//
//        nei = new 内部类();//非静态内部类对象会持有外部类对象的引用，由于nei对象是static修饰的，其存活期和进程一致，只要进程没被销毁nei就一直存在，而nei
                            //引用了外部activity对象导致 activity在销毁后无法被回收（解决方法，内部类定义成静态内部类，nei定义成非静态变量）

//        mLeakyHandler.postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                //new Runnable这里也是匿名内部类实现的 也会持有 MainActivity对象的引用
//                System.out.println("message from mLeakyHandler,delay 5000ms");
//            }
//        }, 1000*60*5);

        // 当我们执行了Activity的finish方法，被延迟的消息会在被处理之前存在于主线程消息队列中5分钟，
        // 而这个消息中又包含了Handler的引用，而Handler是一个匿名内部类的实例，其持有外面的SampleActivity的引用，
        // 所以这导致了SampleActivity无法回收，进行导致SampleActivity持有的很多资源都无法回收，这就是我们常说的内存泄露。

        mLeakyHandler.postDelayed(sRunnable, 1000*60*5);//handler和runnable都定义成静态内部类形式 解决内存泄漏

    }

    public void jump(View v)
    {
        test = "change";
        SecondActivity.startActivity(this);
    }

}
