package com.cxplan.cxscript.message;

/**
 * @author kenny
 */
public class CommandConstant {

    /**
     * The commands received by controller
     */
    public static final String CMD_CONTROLLER_SYSTEM = "c_system";//System management command.
    /**
     * The commands sent to device
     */
    public static final String CMD_DEVICE_CREATE_SESSION = "d_create";//create session with device for remote session (controller).
    public static final String CMD_DEVICE_SPAN = "d_span";//Span the specified component by coordinates.
    public static final String CMD_DEVICE_DUMP = "d_dump";//dump the hierarchy views
}
