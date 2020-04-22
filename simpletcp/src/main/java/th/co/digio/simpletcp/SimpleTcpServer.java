package th.co.digio.simpletcp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @author supitsara
 */
public class SimpleTcpServer implements SimpleTcpServiceImp {
    private static final String TAG = SimpleTcpServer.class.getSimpleName();

    private TcpService service;
    private InetAddress inetAddress;

    private int port = 2000;
    private boolean isRunning = false;
    private boolean isConnected = false;

    private OnDataReceivedListener dataReceivedListener;

    public SimpleTcpServer(int port) {
        this.port = port;
    }

    public void start() {
        if (!this.isRunning) {
            service = new TcpService(port, this);
            service.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            service.killTask();
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getTargetInetAddress() {
        return inetAddress;
    }

    public void onMessageIncoming(byte[] data, String ip) {
        dataReceivedListener.onDataReceived(data, ip);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public OnDataReceivedListener getDataReceivedListener() {
        return dataReceivedListener;
    }

    @Override
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    @Override
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.dataReceivedListener = listener;
    }

    public interface OnDataReceivedListener {
        void onDataReceived(byte[] data, String ip);
    }

    private static class TcpService extends AsyncTask<Void, Void, Void> {
        private ServerSocket serverSocket;
        private SimpleTcpServiceImp simpleTcpServiceImp;

        private int port;
        private Boolean taskState = true;

        public TcpService(int port, SimpleTcpServiceImp simpleTcpServiceImp) {
            this.port = port;
            this.simpleTcpServiceImp = simpleTcpServiceImp;
        }

        public void killTask() {
            taskState = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket = null;
            while (taskState) {
                try {
                    serverSocket = new ServerSocket(port);
                    serverSocket.setSoTimeout(1000);
                    socket = serverSocket.accept();
                    simpleTcpServiceImp.setInetAddress(socket.getInetAddress());
                    simpleTcpServiceImp.setConnected(true);
                } catch (IOException e) {
                    Log.w(TAG, "Socket Timeout");
                }

                while (taskState && simpleTcpServiceImp.isConnected() && socket != null) {
                    try {
                        socket.setSoTimeout(1000);
                        InputStream inputStream = socket.getInputStream();
                        final byte[] recv = TcpUtils.readBytes(inputStream);
                        if (simpleTcpServiceImp.getDataReceivedListener() != null && recv.length > 0) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    simpleTcpServiceImp.getDataReceivedListener().onDataReceived(recv, simpleTcpServiceImp.getInetAddress().getHostAddress());
                                }
                            });
                        } else {
                            break;
                        }
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        String outgoingMsg = "%OK%" + System.getProperty("line.separator");
                        out.write(outgoingMsg);
                        out.flush();
                    } catch (NullPointerException e) {
                        simpleTcpServiceImp.setConnected(false);
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
