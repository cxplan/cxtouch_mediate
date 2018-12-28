package com.cxplan.projection.mediate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    public static final String ACTION_CONNECTION_DISCONNECTED = "action.connection.disconnected";
    public static final String ACTION_CONNECTION_CONNECTED = "action.connection.connected";
    public static final String ACTION_CONNECTION_INFO = "action.connection.info";
    public static final String ACTION_CONNECTION_REMARK = "action.connection.remark";

    private static final int REQUEST_CODE = 1;
    private Button inputerButton;
    private String serverHost = null;
    private int port;
    private TextView tv_info;
    private TextView tv_device;
    private TextView tv_time;
    private TextView tv_serial;
    private LinearLayout ll_show;
    private TextView mTvVersion;
    private TextView mTvConnection;
    private TextView mTvName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputerButton = (Button) findViewById(R.id.inputerButton);
        inputerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();
            }
        });
    }
}
