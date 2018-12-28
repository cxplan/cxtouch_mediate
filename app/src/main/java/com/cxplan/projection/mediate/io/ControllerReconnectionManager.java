package com.cxplan.projection.mediate.io;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.CXApplication;

import java.util.Random;

/**
 * Handles the automatic reconnection process. Every time a connection is dropped without
 * the application explictly closing it, the manager automatically tries to reconnect to
 * the controller server.<p>
 *
 * The reconnection mechanism will try to reconnect periodically:
 * <ol>
 *  <li>For the first minute it will attempt to connect once every ten seconds.
 *  <li>For the next five minutes it will attempt to connect once a minute.
 *  <li>If that fails it will indefinitely try to connect once every five minutes.
 * </ol>
 *
 */
public class ControllerReconnectionManager {
    private static final String TAG = "ControllerReconnectionManager";

    private static ControllerReconnectionManager instance;

    public static ControllerReconnectionManager getInstance() {
        if (instance == null) {
            instance = new ControllerReconnectionManager();
        }
        return instance;
    }

    // Holds the connection to the server
    private Thread reconnectionThread;
    private int randomBase = new Random().nextInt(5) + 5; // between 5 and 10 seconds
    
    // Holds the state of the reconnection
    boolean done = true;

    ControllerReconnectionManager() {
    }

    public void openMonitor() {
        done = false;
    }
    public void closeMonitor() {
        done = true;
    }
    /**
     * Returns true if the reconnection mechanism is enabled.
     *
     * @return true if automatic reconnections are allowed.
     */
    private boolean isReconnectionAllowed() {
        ControllerConnection connection = CXApplication.getInstance().getControllerConnection();
        if (connection == null) {
            return false;
        }
        return !done && !connection.isConnected();
    }

    /**
     * Starts a reconnection mechanism if it was configured to do that.
     * The algorithm is been executed when the first connection error is detected.
     * <p/>
     * The reconnection mechanism will try to reconnect periodically in this way:
     * <ol>
     * <li>First it will try 6 times every 10 seconds.
     * <li>Then it will try 10 times every 1 minute.
     * <li>Finally it will try indefinitely every 5 minutes.
     * </ol>
     */
    synchronized protected void reconnect() {
        if (this.isReconnectionAllowed()) {
            LogUtil.e("*********status: start to reconnect.");
            // Since there is no thread running, creates a new one to attempt
            // the reconnection.
            // avoid to run duplicated reconnectionThread -- fd: 16/09/2010
            if (reconnectionThread!=null && reconnectionThread.isAlive()) return;
            
            reconnectionThread = new Thread() {
             			
                /**
                 * Holds the current number of reconnection attempts
                 */
                private int attempts = 0;

                /**
                 * Returns the number of seconds until the next reconnection attempt.
                 *
                 * @return the number of seconds until the next reconnection attempt.
                 */
                private int timeDelay() {
                    attempts++;
                    if (attempts > 15) {
                	    return 7;      // between 2.5 and 7.5 minutes (~5 minutes)
                    }
                    if (attempts > 10) {
                	    return 5;       // between 30 and 90 seconds (~1 minutes)
                    }
                    return 3;       // 10 seconds
                }

                /**
                 * The process will try the reconnection until the connection succeed or the user
                 * cancell it
                 */
                public void run() {
                    boolean success = false;
                    // The process will try to reconnect until the connection is established or
                    // the user cancel the reconnection process {@link Connection#disconnect()}
                    while (ControllerReconnectionManager.this.isReconnectionAllowed()) {
                        // Find how much time we should wait until the next reconnection
                        int remainingSeconds = timeDelay();
                        // Sleep until we're ready for the next reconnection attempt. Notify
                        // listeners once per second about how much time remains before the next
                        // reconnection attempt.
                        while (ControllerReconnectionManager.this.isReconnectionAllowed() &&
                                remainingSeconds > 0)
                        {
                            try {
                                Thread.sleep(1000);
                                remainingSeconds--;
                            }
                            catch (InterruptedException e1) {
                            }
                        }

                        // Makes a reconnection attempt
                        try {
                            if (ControllerReconnectionManager.this.isReconnectionAllowed()) {
                                LogUtil.e("-------starting to connect to controller attempts = " + attempts);
//                                MyApplication.getInstance().getRecorder().getStreamClient().reconnect();
                                success = true;
                            } else {
                                LogUtil.e("*********Controller need not to be reconnected.");
                            }
                        }
                        catch (Exception e) {
                            success = false;
                            LogUtil.e("Reconnecting to controller server failed: " + e.getMessage(), e);
                        }
                    }
                    if (success) {
                        LogUtil.i(TAG, "Reconnect successfully");
                    } else {
                        LogUtil.i(TAG, "Some error occurs in Reconnecting ");
                    }
                }
            };
            reconnectionThread.setName("Node Reconnection Manager");
            reconnectionThread.setDaemon(true);
            reconnectionThread.start();
        }
    }
}