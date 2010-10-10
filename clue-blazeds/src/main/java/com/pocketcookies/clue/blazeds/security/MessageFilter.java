package com.pocketcookies.clue.blazeds.security;

import java.util.List;

import com.pocketcookies.clue.blazeds.config.SessionAttributeKeys;

import flex.messaging.client.FlexClient;
import flex.messaging.client.FlexClientOutboundQueueProcessor;
import flex.messaging.messages.Message;

public class MessageFilter extends FlexClientOutboundQueueProcessor {
	@Override
	public void add(@SuppressWarnings("rawtypes") List outboundQueue,
			Message message) {
		final FlexClient client = this.getFlexClient();
		if (client.getAttribute(SessionAttributeKeys.SUSPECT) == message
				.getHeader("suspect")
				&& client.getAttribute(SessionAttributeKeys.GAME_ID) == message
						.getHeader("gameId"))
			super.add(outboundQueue, message);
	}
}
