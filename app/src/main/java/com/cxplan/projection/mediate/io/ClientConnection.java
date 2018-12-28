package com.cxplan.projection.mediate.io;

import com.cxplan.projection.mediate.message.JID;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.MessageFilter;
import com.cxplan.projection.mediate.message.MessageListener;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2018/5/17.
 *
 * @author kenny
 */
public class ClientConnection {

    protected JID id;
    protected IoSession session;

    /**
     * A collection of PacketCollectors which collects packets for a specified filter
     * and perform blocking and polling operations on the result queue.
     */
    protected final Collection<MessageCollector> collectors = new ConcurrentLinkedQueue<MessageCollector>();

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);


    public JID getId() {
        return id;
    }

    public void setId(JID id) {
        this.id = id;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        if (this.session == session) {
            return;
        }
        if (this.session != null) {
            this.session.close();
        }
        this.session = session;
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }
    /**
     * Send message to device.
     * @param msg
     * @throws IOException
     */
    public void sendMessage(Message msg) throws MessageException {
        if (session == null || !session.isConnected()) {
            throw new MessageException("The session is not connected, the sending operation will be aborted.");
        }

        IoBuffer buffer = msg.getBinary();
        buffer.flip();
        session.write(buffer);
    }

    /**
     * Close all network connection.
     */
    public void closeNetworkResource() {
        close();
    }
    /**
     * Creates a new packet collector for this connection. A packet filter determines
     * which messages will be accumulated by the collector.
     * This mode is suit for synchronized operation.
     *
     * @param messageFilter the message filter to use.
     * @return a new message collector.
     */
    public MessageCollector createPacketCollector(MessageFilter messageFilter) {
        MessageCollector collector = new MessageCollector(this, messageFilter);
        // Add the collector to the list of active collectors.
        collectors.add(collector);
        return collector;
    }

    /**
     * This mode is suit for asynchronous operation.
     * @param messageFilter the message filter to use.
     * @param listener The message listener to message.
     * @return a new message collector.
     */
    public MessageCollector createPacketCollector(MessageFilter messageFilter, MessageListener listener) {
        MessageCollector collector = new MessageCollector(this, messageFilter, listener);
        // Add the collector to the list of active collectors.
        collectors.add(collector);
        return collector;
    }
    /**
     * Remove a packet collector of this connection.
     *
     * @param collector a packet collectors which was created for this connection.
     */
    protected void removePacketCollector(MessageCollector collector) {
        collectors.remove(collector);
    }

    /**
     * Get the collection of all packet collectors for this connection.
     *
     * @return a collection of packet collectors for this connection.
     */
    protected Collection<MessageCollector> getPacketCollectors() {
        return collectors;
    }

    /**
     * The delegate for Message Collector because the method(processMessage) of message collector
     * can't be accessed by subclass of client connection.
     * @param collector message collector.
     * @param message message
     * @return
     */
    protected boolean processMessage(MessageCollector collector, Message message) {
        return collector.processMessage(message);
    }

    public void addPropertyListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void close() {
        if (session != null) {
            session.closeNow();
            session = null;
        }
    }
}
