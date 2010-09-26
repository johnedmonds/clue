package remoting;

public class EchoService {
	public String echo(String text) {
		return "Server says: I received '" + text + "' from you.";
	}
}
