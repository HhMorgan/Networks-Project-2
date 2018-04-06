import java.io.*;
import java.util.*;
import java.net.*;

public class Server extends Thread {
	Socket socket;
	ServerSocket serverSocket;
	public static Vector<ClientServerThreads> clientsArray = new Vector<>();
	public static Vector<Integer> clientServerArray = new Vector<>();

	public static void vectorPrint(Vector<ClientServerThreads> clientsArray) {
		System.out.print("Current connected cilents : ");
		for (ClientServerThreads o : clientsArray) {
			System.out.print(o.name + ", ");
		}
		System.out.println();
	}

	public static boolean nameVectorContains(String name) {
		for (ClientServerThreads o : Server.clientsArray) {
			if (o.name.equals(name)) {

				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		try {
			while (true) {
				socket = serverSocket.accept();

				System.out.println("New request received at : " + socket);

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				System.out.println("Creating a new client...");

				String username = dataInputStream.readUTF();
				while (Server.nameVectorContains(username)) {
					dataOutputStream.writeUTF("This username is taken, please choose a different username");
					username = dataInputStream.readUTF();
				}
				dataOutputStream.writeUTF("You are now connected to server\nType Help() to get the commands list\n");
				ClientServerThreads match = new ClientServerThreads(socket, username, dataInputStream,
						dataOutputStream, ois, oos);

				Thread t = new Thread(match);

				System.out.println("Adding this client to the online client list");

				clientsArray.add(match);
				clientServerArray.add(clientsArray.size() % 2);
				// System.out.println("size : "+clientServerArray.size());

				vectorPrint(clientsArray);
				t.start();

				System.out.println("Client number : " + clientsArray.size());
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public Server(int socketNumber) throws IOException {
		this.serverSocket = new ServerSocket(socketNumber);
	}
}

class ClientServerThreads implements Runnable {

	Scanner scn = new Scanner(System.in);
	public String name;
	final DataInputStream dataInputStream;
	final DataOutputStream dataOutputStream;
	final ObjectInputStream ois;
    final ObjectOutputStream oos;
	Socket socket;
	Queue<String> processes = new LinkedList<String>();

	public static String vectorPrintString(Vector<ClientServerThreads> ar) {
		String ret = ("Current connected cilents : ");
		for (ClientServerThreads o : ar) {
			ret += (o.name + ", ");
		}
		return ret;
	}

	public static boolean countCommas(String msg) {
		int count = 0;
		boolean flag = false;
		for (int i = 0; i < msg.length(); i++) {
			if (flag && ((int) msg.charAt(i)) == 44) {
				return false;
			}
			if (((int) msg.charAt(i)) == 44 && !flag) {
				flag = true;
				count++;
				continue;
			}
			if (flag && ((int) msg.charAt(i)) != 44) {
				flag = false;
			}
		}

		return count == 3;
	}

	public static void vectorPrint(Vector<ClientServerThreads> clientArray) {
		System.out.print("Current connected cilents : ");
		for (ClientServerThreads o : clientArray) {
			System.out.print(o.name + ", ");
		}
		System.out.println();
	}

	public ClientServerThreads(Socket socket, String name, DataInputStream dataInputStream,
			DataOutputStream dataOutputStream,ObjectInputStream ois,ObjectOutputStream oos) {
		this.dataInputStream = dataInputStream;
		this.dataOutputStream = dataOutputStream;
		this.oos=oos;
		this.ois=ois;
		this.name = name;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("The socket has been opened");
		String received;
		while (true) {
			if (this.socket != null && this.socket.isConnected()) {
				try {
					received = dataInputStream.readUTF();
					if (received.equals("BYE") || received.equals("QUIT")) {

						try {
							int k = 0;
							for (ClientServerThreads cst : Server.clientsArray) {
								if (cst.name.equals(this.name)) {
									Server.clientServerArray.remove(k);
									break;
								}
								k++;
							}
							Server.clientsArray.remove(this);

							System.out.println("A user has logged off");

							vectorPrint(Server.clientsArray);
							this.dataInputStream.close();
							this.dataOutputStream.close();

						} catch (IOException e) {

						}
						break;
					}

					if (received.equals("GetMemberList()")) {
						dataOutputStream.writeUTF(vectorPrintString(Server.clientsArray));
						continue;
					}

					if ((((received.split("\n"))[0]).split(" ")[0]).equals("GET")) {
						processes.add(received);
						String[] messageSegmants = received.split("\n");
						String method = (messageSegmants[0].split(" "))[0];
						String url = (messageSegmants[0].split(" "))[1];
						String version = (messageSegmants[0].split(" "))[2];
						String host = (messageSegmants[1]);
						String acceptedFormat = (messageSegmants[2]);
						String connection = (messageSegmants[3]);

						if(!(acceptedFormat.equalsIgnoreCase("png")||acceptedFormat.equalsIgnoreCase("jpeg")||acceptedFormat.equalsIgnoreCase("mp4")||acceptedFormat.equalsIgnoreCase("txt"))){
							this.dataOutputStream.writeUTF("NOT A VALID FORMAT");
							continue;
						} 
						
						if(!acceptedFormat.equals((url.split("\\."))[1])){
							this.dataOutputStream.writeUTF("FORMAT DOES NOT MATCH");
							continue;
						}
						
						File folder = new File("docroot");
						File[] listOfFiles = folder.listFiles();
						ArrayList<String> fileNames = new ArrayList<String>();
						for (int i = 0; i < listOfFiles.length; i++) {
							if (listOfFiles[i].isFile()) {
								fileNames.add(listOfFiles[i].getName());
							}
						}
						
						if(fileNames.contains(url)){
							int fileIndex=fileNames.indexOf(url);
							String httpResponse="";
							httpResponse+="200 OK 1.1\n";
							httpResponse+=new Date()+"\n";
							httpResponse+=acceptedFormat+"\n";
							httpResponse+=connection;
							this.dataOutputStream.writeUTF(httpResponse);
							byte[] bytesArray = readBytesFromFile("docroot" + File.separator + url);
							oos.writeObject(bytesArray);
							//this.oos.writeObject(listOfFiles[fileIndex]);
						}else{
							String httpResponse="";
							httpResponse+="404 NOT FOUND 1.1\n";
							httpResponse+=new Date()+"\n";
							httpResponse+=acceptedFormat+"\n";
							httpResponse+=connection;
							this.dataOutputStream.writeUTF(httpResponse);
						}
						processes.remove();
					} else {
						this.dataOutputStream.writeUTF("invalid command");
					}
				} catch (IOException e) {

					e.printStackTrace();
				}

			}

		}
	}
	
	private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

}
