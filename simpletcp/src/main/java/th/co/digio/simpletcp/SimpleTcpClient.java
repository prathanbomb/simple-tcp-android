package th.co.digio.simpletcp;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author supitsara
 */
public class SimpleTcpClient {
    public static void send(byte[] message, String ip, int port) {
        send(message, ip, port, null, null);
    }

    public static void send(byte[] message, String ip, int port, SendCallback callback, String tag) {
        new TCPSend(message, ip, port, callback, tag).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public interface SendCallback {
        void onSuccess(String tag);

        void onFailed(Exception e);
    }

    private static class TCPSend extends AsyncTask<Void, Void, Void> {
        private SendCallback callback;
        private byte[] message;
        private String ip;
        private String tag;
        private int port;

        public TCPSend(byte[] message, String ip, int port, SendCallback callback, String tag) {
            this.message = message;
            this.ip = ip;
            this.port = port;
            this.callback = callback;
            this.tag = tag;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Socket socket = new Socket(ip, port);
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                outputStream.write(message);
                outputStream.flush();

                if (callback != null) {
                    callback.onSuccess(tag);
                }
                socket.close();
            } catch (IOException e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
            return null;
        }
    }
}
