package robotics.wheeltest;

import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by Бацька on 10.04.2016.
 */


public class WiFiToSerialProxy extends Thread{

    private Socket socket;
    private SerialPort serialPort;
    private TextView debugView;

    Handler updateConversationHandler;

    WiFiToSerialProxy(Socket s, SerialPort p, TextView d){
        socket = s;
        serialPort = p;
        debugView = d;

        updateConversationHandler = new Handler();
    }

    @Override
    public void run() {

        byte buffer[] = new byte[1024*256];
        InputStream in = null;

        try {
            in = socket.getInputStream();
        } catch (IOException e){
            return;
        }

        int n = 0;
        while (!Thread.currentThread().isInterrupted()) {

            try {

                //читаем данные из сокета
                n = in.available();
                if(n > 0){
                    in.read(buffer,0,n);

                    //колхоз, но че поделать...
                    byte toSerial[] = new byte[n];
                    for(int i=0; i<n; i++){
                        toSerial[i] = buffer[i];
                    }

                    //запись в сериал порт
                    String s = new String(toSerial);
                    updateConversationHandler.post(new updateUIThread(s));
                   //serialPort.write(toSerial);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class updateUIThread implements Runnable {

        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override

        public void run() {
            debugView.append(msg);
        }
    }
}