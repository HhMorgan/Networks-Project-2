import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class Client {
	final static int ServerPort1 = 7000;
	static boolean loop = true;
	static int serverPort;
	Scanner scn;
	Socket socket;
	DataInputStream dataInputStream;
	DataOutputStream dataOutputStream;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	// GUI Attributes
	static JPanel top;
	static JPanel bottom;
	static JButton sendButton;
	static JTextArea clientSend;
	static JTextArea clientReceived;
	static JScrollPane jsp;
	static JScrollPane jsp1;
	//static JComboBox connectionBox;

	public static boolean isValid(String input, int length) {
		if (input.length() < length) {
			return false;
		}
		if (input.length() <= 6) {
			return false;
		}
		String commandPart = input.substring(0, 5);
		if (!(commandPart.equals("Join(")))
			return false;
		if (input.charAt(input.length() - 1) != ')')
			return false;

		return true;
	}

	public static String validJoin(String usernameCommand) {
		Scanner scn = new Scanner(System.in);
		String commandPart = usernameCommand.substring(0, 4);
		while (((!(commandPart.equals("Join("))) && usernameCommand.charAt(usernameCommand.length() - 1) != ')')
				&& usernameCommand.length() <= 6) {
			usernameCommand = scn.nextLine();
			while (!isValid(usernameCommand, 4)) {
				usernameCommand = scn.nextLine();
			}
			commandPart = usernameCommand.substring(0, 3);
		}
		return usernameCommand.substring(5, usernameCommand.length() - 1);
	}

	public void start() throws UnknownHostException, IOException {
		/*scn = new Scanner(System.in);

		System.out.println("please enter the 'Join(username)' command with the username that you want to use");
		String usernameCommand = scn.nextLine();

		while (!isValid(usernameCommand, 4)) {
			System.out.println("You have entered a wrong command, please enter the 'Join(username)' command");
			usernameCommand = scn.nextLine();
		}*/
		
		int random = (int) (Math.random() * 1000000);
		
		String username = "Client" + random;
		
		random = (int) (Math.random() * 1000000);
		
		username = username + random;

		socket = new Socket("localhost", ServerPort1);

		dataInputStream = new DataInputStream(socket.getInputStream());
		dataOutputStream = new DataOutputStream(socket.getOutputStream());
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());

		dataOutputStream.writeUTF(username);
	}

	public void sndMsg() {
		Thread messageSending = new Thread(new Runnable() {
			@Override
			public void run() {
				sendButton.addActionListener(new ActionListener()
				{

					@Override
					public void actionPerformed(ActionEvent e) {
						String httpResponse;
						
						String message = clientSend.getText();

						try {
							if (message.equals("Help()")) {
								System.out.println(
										"The available commands are as follows :\nMethod URL VERSION\nHOST\nAccepted format\nConnection");
								clientReceived.append("The available commands are as follows :\nMethod URL VERSION\nHOST\nAccepted format\nConnection\n");
							} else {
								if (message.length() > 5 && message.substring(0, 5).equals("Join(")) {
									dataOutputStream.writeUTF(message.substring(5, message.length() - 1));
								} else {
									
									//message = "GET " + message + " 1.1" + "\n" + "localhost" + "\n" + message.split("\\.")[1] + "\n" + connectionBox.getSelectedItem().toString();
									
									if (((message.split(" "))[0]).equals("GET")) {
										String[] messageSegmants = message.split("\\r?\\n");
										System.out.println(Arrays.toString(messageSegmants));
										//for (int i = 1; i < 4; i++) {
										//}
										System.out.println(Arrays.toString(messageSegmants));
										String method = (messageSegmants[0].split(" "))[0];
										String url = (messageSegmants[0].split(" "))[1];
										String version = (messageSegmants[0].split(" "))[2];
										String host = (messageSegmants[1]);
										String acceptedFormat = (messageSegmants[2]);
										
										String connection = (messageSegmants[3]);
										String messageToServer=messageSegmants[0]+"\n"+messageSegmants[1]+"\n"+messageSegmants[2]+"\n"+connection;
										if(!(connection.equalsIgnoreCase("keep-alive")||connection.equals("close"))){
											System.out.println("Invalid connection status");
											clientReceived.append("Invalid connection status\n");
										}
										dataOutputStream.writeUTF(messageToServer);

										/*try {
											ois.readObject();
										} catch (ClassNotFoundException e) {
											// TODO Auto-generated catch block
											//e.printStackTrace();
										}*/
										//System.out.printf("%s %s %s\n%s\n%s\n%s\n", method, url, version, host,acceptedFormat, connection);

										if (connection.equals("close")) {
											String closeStatement="BYE";
											dataOutputStream.writeUTF(closeStatement);
											Thread.sleep(100);
											socket.close();
											loop = false;
											System.out.println("You have logged off");
											clientReceived.append("You have logged off\n");
											System.exit(0);
										}
									}else{
										System.out.println("Invalid command	");
										clientReceived.append("Invalid command\n");
									}	
								}
							}
						} catch (IOException e1) {
							System.out.println("Invalid input, restart the console");
							clientReceived.append("Invalid input, restart the console\n");
						} catch (InterruptedException e1) {
							System.out.println("Invalid input, restart the console");
							clientReceived.append("Invalid input, restart the console\n");
						}
					}
				});
				
			}
		});
				messageSending.start();
	}

	public void rcvMsg() {
		Thread messageReading = new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {
					if (loop) {
						try {
							if (socket != null && socket.isConnected()) {
								String msg = dataInputStream.readUTF();
								System.out.println(msg);
								clientReceived.append(msg + "\n");
								String[] httpResponseArray = msg.split("\n");
								String httpCode = (httpResponseArray[0].split(" "))[0];
								int generatedNo = (int) (Math.random() * 1000000);
								int generatedNo2 = (int) (Math.random() * 1000000);


								if(httpCode.equals("200")) 
								{
									byte[] bytesArray = (byte[]) ois.readObject();
									String acceptedFormat = (httpResponseArray[2]);
									try (FileOutputStream fos = new FileOutputStream("user" + File.separator + generatedNo + generatedNo2 + "." + acceptedFormat)) {
										fos.write(bytesArray);
									}
								}
							} else {
								if (socket.isClosed())
									break;
							}
						} catch (IOException e) {

						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							dataInputStream.close();
							dataOutputStream.close();
						} catch (IOException e) {
						}

						break;
					}
				}
			}
		});
		messageReading.start();
	}

	public static void main(String args[]) throws UnknownHostException, IOException {
		
		JFrame clientWindow = new JFrame();
		//clientWindow.setBackground(Color.black);
		clientWindow.setBounds(340, 120, 550,550);

		Font font1 = new Font("Century Gothic", Font.PLAIN, 16);
		String[] ComboBoxChoices = {"keep-alive", "close"};
		//connectionBox = new JComboBox(ComboBoxChoices);
		clientWindow.setResizable(false);
		clientReceived = new JTextArea();
		clientReceived.setFont(font1);
		clientReceived.setEditable(false);
		//clientReceived.setPreferredSize(new Dimension(520,400));
		clientReceived.setVisible(true);
		clientSend = new JTextArea();
		clientSend.setPreferredSize(new Dimension(440,50));
		clientSend.setVisible(true);
		clientSend.setFont(font1);
		sendButton = new JButton();
		//sendButton.setOpaque(false);
		//sendButton.setContentAreaFilled(false);
		//sendButton.setBorderPainted(false);
		//sendButton.setVisible(true);
		sendButton.setVisible(true);
		sendButton.setPreferredSize(new Dimension(90,50));
		sendButton.setText("Send");
		sendButton.setBackground(Color.GREEN);

		top = new JPanel();
		top.setVisible(true);
		top.setSize(250, 250);
		bottom = new JPanel();
		bottom.setVisible(true);
		bottom.setSize(250,250);

		DefaultCaret caret = (DefaultCaret) clientReceived.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		clientReceived.setCaret(caret);
		jsp = new JScrollPane(clientReceived);
		jsp.setAutoscrolls(true);
		jsp.setPreferredSize(new Dimension(540,400));
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		DefaultCaret caret1 = (DefaultCaret) clientSend.getCaret();
		caret1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		clientSend.setCaret(caret1);
		jsp1 = new JScrollPane(clientSend);
		jsp1.setAutoscrolls(true);
		jsp1.setPreferredSize(new Dimension(250,100));
		jsp1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		top.add(jsp, BorderLayout.CENTER);
		bottom.add(jsp1, BorderLayout.WEST);
		//bottom.add(connectionBox, BorderLayout.CENTER);
		bottom.add(sendButton, BorderLayout.EAST);

		clientWindow.add(top,BorderLayout.NORTH);
		clientWindow.add(bottom, BorderLayout.SOUTH);

		clientWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientWindow.setVisible(true);
		clientWindow.setTitle("Aeropostal");
		
		Client client = new Client();
		client.start();
		client.sndMsg();
		client.rcvMsg();


	}
}