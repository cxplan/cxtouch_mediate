package com.cxplan.projection.mediate.script.message;

/**
 * @author kenny
 */
public class CommandConstant {

    /**
     * The commands received by controller
     */
    public static final String CMD_CONTROLLER_SYSTEM = "c_system";//System management command.
    public static final String CMD_CONTROLLER_SCRIPT_VIEW_NODE = "c_view_node";//The view node for script command.
    /**
     * The commands sent to device
     */
    public static final String CMD_DEVICE_CREATE_SESSION = "d_create";//create session with device for remote session (controller).
    public static final String CMD_DEVICE_SCRIPT_SPAN = "d_span";//Span the specified component by coordinates.
    public static final String CMD_DEVICE_SCRIPT_DUMP = "d_dump";//dump the hierarchy views
    public static final String CMD_DEVICE_SCRIPT_WAIT_IDLE = "d_wait_idle";//wait device idle event.
    public static final String CMD_DEVICE_SCRIPT_WAIT_VIEW = "d_wait_view";//wait view presented on screen.
}
