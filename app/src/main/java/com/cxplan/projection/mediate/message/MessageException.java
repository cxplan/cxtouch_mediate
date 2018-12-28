package com.cxplan.projection.mediate.message;

/**
 * Created on 2018/4/16.
 *
 * @author kenny
 */
public class MessageException extends Exception {

    public MessageException() {
    }

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }

}
