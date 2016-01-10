package com.jonas.leaktest;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.jonas.leaktest.application.LeakApp;
import java.lang.ref.WeakReference;

/**
 * 另一个内存泄漏代码示例： https://github.com/david-wei/LightersTest
 * 查看是否会发生内存泄漏 主要看 被谁引用，引用者的存活时间(生命周期)是否大于自己 生命周期长的引用生命周期短的
 * 导致生命周期短的 无法在该被回收的时候回收
 *
 * 1，单例造成的内存泄漏
 * 2，非静态内部类对象和匿名内部类会持有外部类对象的引用 OuterClass$InnerClass
 * 3，Handler造成的内存泄漏
 */
public class MainActivity extends AppCompatActivity {

  private static Other mOther = new Other();//外部类
//    private static 内部类 nei2 = new 内部类();//错误
//    private 内部类 nei2 = new 内部类();//正确

  @Bind(R.id.textView)
  TextView mTextView;

  private LeakApp mLeakApp;
  private static String test = "start";
  //静态字段的 生命周期 和进程 一致
  private static 内部类 nei;//nei持有MainActivity对象的引用，MainActivity在被销毁之后应该被回收，而nei为static期生命周期和应用进程一样长
  //导致MainActivity对象无法被回收

  class 内部类 {
    //内部类对象持有外部类对象的引用
    //静态内部类的对象将不会持有外部类对象的引用 static class 内部类{
  }

//  内部类持有外部类Activity的引用，当Handler对象有Message在排队，则无法释放，进而导致Activity对象不能释放。
//  如果是声明为static，则该内部类不持有外部Acitivity的引用，则不会阻塞Activity对象的释放。
//  如果声明为static后，可在其内部声明一个弱引用（WeakReference）引用外部类。
  //非静态内部类导致内存泄漏，非静态 + 延时消息 将导致内存泄漏
  private static Handler mLeakyHandler = new Handler() {

    @Override//虽然handler是 类，但是mLeakyHandler复写了handleMessage属于内部类
    public void handleMessage(Message msg) {
      //1.当一个Android应用启动的时候，会自动创建一个供应用主线程使用的Looper实例。
      //Looper的主要工作就是一个一个处理消息队列中的消息对象。在Android中，
      //所有Android框架的事件（比如Activity的生命周期方法调用和按钮点击等）都是放入到消息中，
      // 然后加入到Looper要处理的消息队列中，由Looper负责一条一条地进行处理。主线程中的Looper生命周期和当前应用一样长。

      // 2.当一个Handler在主线程进行了初始化之后，我们发送一个target为这个Handler的消息到Looper处理的消息队列时，
      // 实际上已经发送的消息已经包含了一个Handler实例的引用，
      // 只有这样Looper在处理到这条消息时才可以调用Handler#handleMessage(Message)完成消息的正确处理。

      //3，handlemessage在activity销毁之后 才收到消息 而非静态handler对象 引用了activity对象
      System.out.println("handleMessage--运行在主线程--收到消息");
    }
  };

  //定义静态内部类 使用弱引用引用外部类对象
  static class LeakHandler extends Handler {
    // 内部声明一个弱引用，引用外部类
    private WeakReference<MainActivity > activityWeakReference;
    public LeakHandler(MainActivity activity) {
      activityWeakReference= new WeakReference<MainActivity >(activity);
    }
    // ... ...
  }

  //非静内部类对象持有外部类对象的引用
  private static Runnable sRunnable = new Runnable() {
    @Override
    public void run() {
      System.out.println("静态runnable对象");
    }
  };

  @Bind(R.id.imageView)
  ImageView mImageView;

  private static Drawable background;//在屏幕旋转时有可能引起内存泄露，在android3.0之前是有内存泄露，在3.0之后无内存泄露！

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mLeakApp = (LeakApp) getApplicationContext();
//        {
//            //ImageView leakimageView = new ImageView(getApplicationContext());
//            ImageView leakimageView = new ImageView(this);//持有activity的引用
////            background = getDrawable(R.mipmap.ic_launcher);
//            if (null == background) {//在屏幕旋转时有可能引起内存泄露
////           当一个Drawable绑定到了View上，实际上这个View对象就会成为这个Drawable的一个callback成员变量，
////          静态的background持有ImageView对象leakimageView的引用，而leakimageView只有Activity的引用，
////          而Activity会持有其他更多对象的引用。sBackground生命周期要长于Activity。当屏幕旋转时，
////          Activity无法被销毁，这样就产生了内存泄露问题。
//              background = getResources().getDrawable(R.drawable.ic_launcher);
//            }
//  //setImageDrawable内持有view(当前为imageview)的引用。3.0之后采用软引用，所以在android3.0之前是有内存泄露，在3.0之后无内存泄露！
//            leakimageView.setImageDrawable(background);
//            setContentView(leakimageView);
//        }

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    System.out.println(test);

//        SingleYou instance = SingleYou.getInstance(this);//MainActivity对象被SingleYou所引用(单例对象是静态的)导致内存泄漏
//
//        nei = new 内部类();//非静态内部类对象会持有外部类对象的引用，由于nei对象是static修饰的，其存活期和进程一致，只要进程没被销毁nei就一直存在，而nei
    //引用了外部activity对象导致 activity在销毁后无法被回收（解决方法，内部类定义成静态内部类，nei定义成非静态变量）

//    {
//      //handler导致内存泄漏
//      mLeakyHandler.postDelayed(new Runnable() {
//        @Override
//        public void run() {
//          //new Runnable这里也是匿名内部类实现的 也会持有 MainActivity对象的引用
//          System.out.println("message from mLeakyHandler,delay 5000ms");
//        }
//      }, 1000 * 60 * 5);
//
//      //当我们执行了Activity的finish方法，被延迟的消息会在被处理之前存在于主线程消息队列中5分钟，
//      // 而这个消息中又包含了Handler的引用，而Handler是一个非静态内部类的实例，其持有外面的SampleActivity的引用，
//      //所以这导致了SampleActivity无法回收，进行导致SampleActivity持有的很多资源都无法回收，这就是我们常说的内存泄露。
//
    //mLeakyHandler.postDelayed内部调用sendMessageDelayed
//    被延迟的消息会在被处理之前存在于主线程消息队列中10分钟，而这个消息中又包含了Handler的引用，
//    而Handler是一个非静态内部类的实例，其持有外面的MainActivity的引用
//      mLeakyHandler.postDelayed(sRunnable, 1000 * 10 );//handler和runnable都定义成静态内部类形式 解决内存泄漏
//      mLeakyHandler.sendEmptyMessageDelayed(0, 1000 * 10);//当mLeakyHandler为非static时 导致内存泄漏
//    }


//    3.Context的引用问题
//    mTextView.setOnClickListener(new View.OnClickListener() {
//
//      @Override
//      public void onClick(View v) {
//        //主线程运行，，实测 toast 时间太短  并未内存泄漏(可忽略内存泄漏)
//        Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_LONG).show();//toast中 引用activity对象
//        //安全使用toast ==让Toast持有ApplicationContext;其实只要不是Layout相关，Context都可以使用ApplicationContext;
//        System.out.println(Looper.getMainLooper()==Looper.myLooper());
////        sActivity = MainActivity.this;
//      }
//    });

//    //4,线程中(该Thread是匿名内部类，所以会隐式的持有外部类（这里也就是Activity）)
    new Thread("leak-test") {
      @Override
      public void run() {
        //网络请求

//        解决方式：多种多样; 不使用匿名内部类，或者整个应用维护一个线程池，或者维护一个线程+消息队列，
//        后两种都是让线程不依赖于Activity从而达到避免内存泄露的目的；
        while (true) {
          SystemClock.sleep(1000);
          System.out.println("无限循环线程。。");
        }

      }
    }.start();
    //这里的 new Thread并不是匿名内部类，导致内存泄漏的是 匿名内部类new Runnable()
    new Thread(new Runnable() {
      @Override
      public void run() {

      }
    });


  }

  private static AppCompatActivity sActivity;

  public void jump(View v) {
    test = "change";
    SecondActivity.startActivity(this);
  }

}
