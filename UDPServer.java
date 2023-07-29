import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class UDPServer extends Thread {
    public final static int port = 3434;
    public final static int buffer = 1024;
    public DatagramSocket socket;
    public ArrayList<InetAddress> clients;
    public ArrayList<Integer> ports;
    public ArrayList<ClientInfo> clients_info;


    public UDPServer() throws SocketException {
        this.socket = new DatagramSocket(port);
        this.clients = new ArrayList<>();
        this.ports = new ArrayList<>();
        this.clients_info = new ArrayList<>();
    }

    public static String convert_bytes_to_string(byte []bytes) {
        int i=0;
        StringBuilder sb = new StringBuilder();
        while(bytes[i]!=0){
            sb.append((char)bytes[i]);
            i++;
        }
        return sb.toString();
    }

    @Override
    public void run() {
        byte []buff = new byte[buffer];
        System.out.println("Server is running on port: " + port);
        while (true) {
            Arrays.fill(buff, (byte)0);
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            try {
                socket.receive(packet);

                String converted_content = convert_bytes_to_string(buff);

                InetAddress ip_addr = packet.getAddress();
                int port1 = packet.getPort();

                System.out.println(converted_content);

                if (converted_content.toUpperCase().contains("LOGIN")) {
                    String username = converted_content.split(" ")[1];
                    ClientInfo info = new ClientInfo(username, ip_addr, port1);
                    clients_info.add(info);
                    byte []content = "You are now logged in.".getBytes();
                    socket.send(new DatagramPacket(content, content.length, ip_addr, port1));
                } else if (converted_content.toUpperCase().contains("ADD-SONG")) {
                    String []song_info = converted_content.split(" ")[1].split("#");
                    Song song = new Song(song_info[0], song_info[1], Integer.parseInt(song_info[2]));
                    for (int i = 0; i < clients_info.size(); i++) {
                        if (Objects.equals(clients_info.get(i).name, converted_content.split(" ")[2])) {
                            clients_info.get(i).songs.add(song);
                            break;
                        }
                    }
                } else if (converted_content.toUpperCase().contains("GET-MUSIC")) {
                    String user = converted_content.split(" ")[1];
                    StringBuilder music = new StringBuilder();
                    for (int i = 0; i < clients_info.size(); i++) {
                        if (Objects.equals(clients_info.get(i).name, user)) {
                            for (int j = 0; j < clients_info.get(i).songs.size(); j++) {
                                music.append(clients_info.get(i).songs.get(j).toString()).append("\n");
                            }
                        }
                    }
                    byte []content = music.toString().getBytes();
                    socket.send(new DatagramPacket(content, content.length, ip_addr, port1));
                } else if (converted_content.toUpperCase().contains("RECOMMEND-MUSIC")) {
                    String user = converted_content.split(" ")[2];
                    String []song_info = converted_content.split(" ")[1].split("#");
                    Song song = new Song(song_info[0], song_info[1], Integer.parseInt(song_info[2]));
                    boolean found = false;
                    for (int i = 0; i < clients_info.size(); i++) {
                        if (Objects.equals(clients_info.get(i).name, user)) {
                            found = true;
                            byte []content = song.toString().getBytes();
                            socket.send(new DatagramPacket(content, content.length, clients_info.get(i).addr, clients_info.get(i).port));
                            break;
                        }
                    }
                    if (!found) {
                        byte []content = "User not found".getBytes();
                        socket.send(new DatagramPacket(content, content.length, ip_addr, port1));
                    }
                } else {
                    byte []content = "Unknown command".getBytes();
                    socket.send(new DatagramPacket(content, content.length, ip_addr, port1));
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws SocketException {
        UDPServer server = new UDPServer();
        server.start();
    }
}

class ClientInfo {
    public String name;
    public ArrayList<Song> songs;
    public InetAddress addr;
    public int port;

    public ClientInfo(String name, InetAddress addr, int port) {
        this.name = name;
        this.songs = new ArrayList<>();
        this.addr = addr;
        this.port = port;
    }
}

class Song {
    public String song_name;
    public String singer;
    public int year;

    public Song(String name, String singer, int year) {
        this.song_name = name;
        this.singer = singer;
        this.year = year;
    }

    @Override
    public String toString() {
        return this.song_name + " - " + this.singer + " (" + year + ")";
    }
}