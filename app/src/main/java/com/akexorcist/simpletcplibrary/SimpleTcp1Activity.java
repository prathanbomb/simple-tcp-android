package com.akexorcist.simpletcplibrary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import th.co.digio.simpletcp.SimpleTcpClient;
import th.co.digio.simpletcp.SimpleTcpServer;
import th.co.digio.simpletcp.TcpUtils;

public class SimpleTcp1Activity extends AppCompatActivity {
    public static final int TCP_PORT = 21111;

    private SimpleTcpServer simpleTcpServer;

    private TextView textViewIpAddress;
    private EditText editTextMessage, editTextIpAddress;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_tcp_1);

        textViewIpAddress = findViewById(R.id.textViewIpAddress);
        editTextMessage = findViewById(R.id.editTextMessage);
        editTextIpAddress = findViewById(R.id.editTextIpAddress);
        buttonSend = findViewById(R.id.buttonSend);

        TcpUtils.forceInputIp(editTextIpAddress);

        textViewIpAddress.setText(TcpUtils.getIpAddress(this) + ":" + TCP_PORT);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (editTextMessage.getText().length() > 0) {
                    String message = editTextMessage.getText().toString();
                    String ip = editTextIpAddress.getText().toString();

                    // Send message without callback
                    SimpleTcpClient.send(message, ip, TCP_PORT);

                    // Send message and waiting for callback
//                    SimpleTcpClient.send(message, ip, TCP_PORT, new SimpleTcpClient.SendCallback() {
//                        public void onSuccess(String tag) {
//							Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
//						}
//						public void onFailed(String tag) {
//							Toast.makeText(getApplicationContext(), "onFailed", Toast.LENGTH_SHORT).show();
//						}
//					}, "TAG");
                }
            }
        });

        simpleTcpServer = new SimpleTcpServer(TCP_PORT);
        simpleTcpServer.setOnDataReceivedListener(new SimpleTcpServer.OnDataReceivedListener() {
            public void onDataReceived(String message, String ip) {
                Toast.makeText(getApplicationContext(), message + " : " + ip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onResume() {
        super.onResume();
        simpleTcpServer.start();
    }

    public void onStop() {
        super.onStop();
        simpleTcpServer.stop();
    }
}
