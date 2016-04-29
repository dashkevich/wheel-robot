package robotics.wheeltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Бацька on 09.04.2016.
 */
public class WiFiConnection {

    private ServerSocket serverSocket;
    private ArrayList<Socket> clientSockets;
    private Thread serverThread;
    private static final int SERVERPORT = 27015;

    OnNewClientEvent newClientEvent;

    WiFiConnection(){
        clientSockets = new ArrayList<>();
    }

    public void addNewClientEvent(OnNewClientEvent listener){
        newClientEvent = listener;
    }

    public void startServer(){

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

    }

    public void stopServer(){
        serverThread.interrupt();
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

                    newClientEvent.onConnect(socket);

                    //return;

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


    public interface OnNewClientEvent{
        void onConnect(Socket socket);
    }
}
