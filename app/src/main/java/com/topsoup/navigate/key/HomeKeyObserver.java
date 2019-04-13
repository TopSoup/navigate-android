package  com.topsoup.navigate.key;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.topsoup.navigate.activity.SOSActivity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HomeKeyObserver {
    private Context mContext;
    private IntentFilter mIntentFilter;
    private IntentFilter mIntentFilterUp;
    private OnHomeKeyListener mOnHomeKeyListener;
    private HomeKeyBroadcastReceiver mHomeKeyBroadcastReceiver;

    public HomeKeyObserver(Context context) {
        this.mContext = context;
    }

    //注册广播接收者
    public void startListen(){
        mIntentFilter = new IntentFilter("android.intent.action.PTT.down");
        mIntentFilterUp = new IntentFilter("android.intent.action.PTT.up");
        mHomeKeyBroadcastReceiver=new HomeKeyBroadcastReceiver();
        mContext.registerReceiver(mHomeKeyBroadcastReceiver, mIntentFilter);
        mContext.registerReceiver(mHomeKeyBroadcastReceiver, mIntentFilterUp);
        //System.out.println("HomeKey----> 开始监听");
    }

    //取消广播接收者
    public void stopListen(){
        if (mHomeKeyBroadcastReceiver!=null) {
            mContext.unregisterReceiver(mHomeKeyBroadcastReceiver);
            //System.out.println("HomeKey----> 停止监听");
        }
    }

    // 对外暴露接口
    public void setHomeKeyListener(OnHomeKeyListener homeKeyListener) {
        mOnHomeKeyListener = homeKeyListener;
    }

    // 回调接口
    public interface OnHomeKeyListener {
        void onHomeKeyPressed();
        void onHomeKeyReleased();
        void onHomeKeyLongPressed();
    }

    private Timer timer = new Timer();
    //延时方法一 计时器：
    TimerTask task = null;


    //广播接收者
    class HomeKeyBroadcastReceiver extends BroadcastReceiver{
        //唤醒屏幕并且解锁
        public void wakeUpAndUnlock(Context context){
            KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");

            //屏幕锁可用
            kl.reenableKeyguard();
            //解锁
            kl.disableKeyguard();
            //获取电源管理器对象
            PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
            //点亮屏幕
            wl.acquire();
            //释放
            wl.release();
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            //System.out.println("onReceive----> action "+action);
            if (Objects.equals(action, "android.intent.action.PTT.down")) {
                mOnHomeKeyListener.onHomeKeyPressed();

                if (task != null) {
                    task.cancel();
                }

                task = new TimerTask() {
                    @Override
                    public void run() {
                        //System.out.println("HomeKey----> 长按按按SOS");
                        mOnHomeKeyListener.onHomeKeyLongPressed();
                        wakeUpAndUnlock(context);
                        context.startActivity(new Intent(context, SOSActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                };
                timer.schedule(task,3000);//3秒后之后执行
            }

            if (Objects.equals(action, "android.intent.action.PTT.up")) {
                mOnHomeKeyListener.onHomeKeyReleased();

                task.cancel();
            }
        }
    }
}

