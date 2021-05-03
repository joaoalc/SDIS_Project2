package messages;

import javax.net.ssl.SSLSocket;

public class HandleConnection implements Runnable {

    private SSLSocket sslSocket;

    public HandleConnection(SSLSocket s){
        this.sslSocket = s;
    }

    @Override
    public void run() {

    }
}