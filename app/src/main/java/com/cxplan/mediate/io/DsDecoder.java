package com.cxplan.mediate.io;

import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.protocol.MessageParser;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 *
 * @author kenny
 * Created on 2013-8-12
 */
public class DsDecoder extends CumulativeProtocolDecoder {

    public static final String MESSAGE_PARSER = "message_parser";

	public DsDecoder() {
	}
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {

        MessageParser parser = (MessageParser)session.getAttribute(MESSAGE_PARSER);
		if (parser == null) {
			parser = new MessageParser();
	    	session.setAttribute(MESSAGE_PARSER, parser);
	    	System.out.println("--------------Protocol Parser warning------------------");
		}
		parser.readMessage(in);
		
		if (parser.hasResult()) {
			for (Message md : parser.getResult()) {
				out.write(md);
			}
			
			parser.clearResult();
		}
		
		return false;
		
	}
	
}

