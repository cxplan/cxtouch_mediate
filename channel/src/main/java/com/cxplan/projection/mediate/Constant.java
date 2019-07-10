package com.cxplan.projection.mediate;

/**
 * Created by Kenny on 2018/6/22.
 *
 */
public class Constant {

    /**
     * The prefix for application intent for viewing log conveniently.
     */
    public static String TAG_PREFIX;

    public static String packageName;

    /**
     * The monkey action
     */
    public static final short MONKEY_TYPE = 1;
    public static final short MONKEY_PRESS = 2;
    public static final short MONKEY_MOUSE_DOWN = 3;
    public static final short MONKEY_MOUSE_MOVE = 4;
    public static final short MONKEY_MOUSE_UP = 5;
    public static final short MONKEY_WAKE = 9;//wake screen.
    public static final short MONKEY_SLEEP = 10;//wake screen.
    public static final short MONKEY_SCROLL = 13;
    public static final short MONKEY_SWITCH_INPUTER = 100;

    /**
     * The session constant
     */
    public static final String CLIENT_SESSION = "session";
}
