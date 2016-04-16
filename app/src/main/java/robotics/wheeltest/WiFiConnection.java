package robotics.wheeltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Бацька on 09.04.2016.
 */
public class WiFiConnection {

    private ServerSocket serverSocket;
    private ArrayList<Socket> clientSockets;

    private Thread serverThread;

    private static final int SERVERPORT = 27015;

    WiFiConnection(){
        clientSockets = new ArrayList<>();
    }

    public void startServer(){

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

    }

    public void stopServer(){
        serverThread.interrupt();
    }

    public boolean isActiveConnections(){
        return clientSockets.size() > 0;
    }

    public Socket getClientSocket() {
        if(isActiveConnections()) {
            return clientSockets.get(0);
        }else{
            return null;
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    clientSockets.add(socket);

                    //CommunicationThread commThread = new CommunicationThread(socket);
                    //Thread t = new Thread(commThread);
                    //t.setDaemon(true);
                    //t.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                serverSocket.close();
            }catch (IOException e){

            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
