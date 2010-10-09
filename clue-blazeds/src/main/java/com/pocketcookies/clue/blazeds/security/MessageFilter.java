package com.pocketcookies.clue.blazeds.security;

import java.util.List;

import flex.messaging.client.FlexClient;
import flex.messaging.client.FlexClientOutboundQueueProcessor;
import flex.messaging.messages.Message;

public class MessageFilter extends FlexClientOutboundQueueProcessor {
	@Override
	public void add(List outboundQueue, Message message) {
		final FlexClient client = this.getFlexClient();
		if (client.getAttribute("suspect") == message.getHeader("suspect")
				&& client.getAttribute("gameId") == message.getHeader("gameId")) {
			super.add(outboundQueue, message);
			System.out.println("Message delivered");
		} else
			System.out.println("Message not delivered");
	}
}
