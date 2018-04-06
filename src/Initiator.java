import java.io.IOException;

public class Initiator {
	public Initiator() throws IOException {
		Server s1 = new Server(7000);
		
		s1.start();
	}

	public static void main(String[] args) throws IOException {

		Initiator initiator = new Initiator();
	}

}
