import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPClient {
    public static final int port = 3434;

    public static void main(String[] args) throws UnknownHostException, SocketException {
        String hostname = InetAddress.getLocalHost().getHostName();
        DatagramSocket socket = new DatagramSocket();

        SendMessage sendMessage = new SendMessage(socket, hostname);
        ReadMessage readMessage = new ReadMessage(socket);

        System.out.println("Commands:");
        System.out.println("login username");
        System.out.println("add-song song_name#singer#year username (use _ instead of white spaces for song name)");
        System.out.println("get-music username");
        System.out.println("recommend-music song_name#singer#year username (use _ instead of white spaces for song name)");

        sendMessage.start();
        readMessage.start();
    }
}

class SendMessage extends Thread {
    public DatagramSocket socket;
    public String hostname;

    public SendMessage(DatagramSocket socket, String hostname) {
        this.socket = socket;
        this.hostname = hostname;
    }

    public void sendMessageHandler(String message_content) throws IOException {
        byte []buff = message_content.getBytes();
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(buff, buff.length, address, UDPClient.port);
        socket.send(packet);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            try {
                sendMessageHandler(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class ReadMessage extends Thread {
    public DatagramSocket socket;
    public byte []buff;
    public ReadMessage(DatagramSocket socket) {
        this.socket = socket;
        this.buff = new byte[1024];
    }

    @Override
    public void run() {
        while(true) {
            try {
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                socket.receive(packet);
                String received = UDPServer.convert_bytes_to_string(buff);
                System.out.println(received);
                buff = new byte[1024];
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}