package remoting;

import flex.messaging.FlexContext;

public class EchoService {
	public String echo(String text) {
		FlexContext.getFlexClient().setAttribute("test", text);
		return "Server says: I received '" + text + "' from you.";
	}
}
