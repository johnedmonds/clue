package com.pocketcookies.clue.blazeds.security;

import java.util.List;

import flex.messaging.client.FlexClientOutboundQueueProcessor;
import flex.messaging.messages.Message;

public class MessageFilter extends FlexClientOutboundQueueProcessor {
	@Override
	public void add(List outboundQueue, Message message) {
		super.add(outboundQueue, message);
		System.out.println("SuspectId:"
				+ this.getFlexClient().getAttribute("suspectOrdinal"));
	}
}
