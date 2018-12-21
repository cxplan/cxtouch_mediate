package com.cxplan.mediate.inputer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class InputerReceiver extends BroadcastReceiver {

    private static final String TAG = Constant.TAG_PREFIX + "IR";

    @Override
    public void onReceive(Context context, Intent intent) {
        try
        {
            Intent paramIntent = new Intent(intent);
            paramIntent.setComponent(new ComponentName(context, CXTouchIME.class));
            context.startService(paramIntent);
            LogUtil.i(TAG, "start service ok");
            return;
        }
        catch (Throwable e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }
}
