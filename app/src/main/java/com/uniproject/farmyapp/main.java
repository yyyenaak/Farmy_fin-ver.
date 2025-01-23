package com.uniproject.farmyapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class main extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private TextView receivedDataTextView;

    private static final String DEVICE_ADDRESS = "98:DA:60:09:D9:12"; // HC-06의 MAC 주소
    private static final UUID UUID_HC06 = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    // 스위치 선언
    private Switch toggleTemperatureSensor, toggleFan, toggleWaterPump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 수신 데이터 텍스트뷰 초기화
        receivedDataTextView = findViewById(R.id.card_content);

        // 스위치 초기화
        toggleTemperatureSensor = findViewById(R.id.toggle_card1); // 온도 센서
        toggleFan = findViewById(R.id.toggle_card2); // 팬
        toggleWaterPump = findViewById(R.id.toggle_card3); // 물 펌프

        // 블루투스 권한 요청
        requestBluetoothPermissions();

        // 블루투스 연결 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectToBluetooth();

        // 스위치 리스너 설정
        setupSwitchListeners();

        // GPT 액티비티로 이동 버튼 설정
        Button moveToGptButton = findViewById(R.id.move_gpt);
        moveToGptButton.setOnClickListener(v -> {
            Intent intent = new Intent(main.this, gpt.class);
            startActivity(intent);
        });
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "블루투스 권한이 허용되었습니다.");
            } else {
                Log.e(TAG, "블루투스 권한이 거부되었습니다.");
            }
        }
    }

    private void connectToBluetooth() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "이 장치는 블루투스를 지원하지 않습니다.");
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_HC06);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

            Log.d(TAG, "블루투스 장치에 성공적으로 연결되었습니다.");
            startListeningForData();

        } catch (SecurityException e) {
            Log.e(TAG, "블루투스 권한이 없습니다. 권한을 요청하세요.", e);
        } catch (IOException e) {
            Log.e(TAG, "블루투스 장치에 연결하는 중 오류 발생", e);
        }
    }

    private void setupSwitchListeners() {
        // 온습도 센서 스위치
        toggleTemperatureSensor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String command = isChecked ? "M" : "A"; // F: 팬 ON, f: 팬 OFF
            sendBluetoothCommand(command);
            Toast.makeText(this, "Mode " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 팬 스위치
        toggleFan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String command = isChecked ? "F" : "f"; // F: 팬 ON, f: 팬 OFF
            sendBluetoothCommand(command);
            Toast.makeText(this, "Fan " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 물 펌프 스위치
        toggleWaterPump.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String command = isChecked ? "W" : "w"; // W: 물 펌프 ON, w: 물 펌프 OFF
            sendBluetoothCommand(command);
            Toast.makeText(this, "Water Pump " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });
    }

    // RGB 값 전송을 위한 메서드
    private void sendRGBCommand(int r, int g, int b) {
        if (outputStream != null) {
            try {
                // RGB 값을 전송 (형식: "255,255,255")
                String command = r + "," + g + "," + b;
                outputStream.write(command.getBytes());
                outputStream.flush();
                Log.d(TAG, "RGB Command 전송: " + command);
            } catch (IOException e) {
                Log.e(TAG, "RGB 명령 전송 중 오류 발생", e);
            }
        } else {
            Log.e(TAG, "출력 스트림이 초기화되지 않았습니다.");
        }
    }

    // 기본 블루투스 명령 전송 메서드
    private void sendBluetoothCommand(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
                Log.d(TAG, "블루투스 명령 전송: " + command);
            } catch (IOException e) {
                Log.e(TAG, "블루투스 명령 전송 중 오류 발생", e);
            }
        } else {
            Log.e(TAG, "출력 스트림이 초기화되지 않았습니다.");
        }
    }

    private void startListeningForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if (inputStream != null && (bytes = inputStream.read(buffer)) > 0) {
                        String receivedData = new String(buffer, 0, bytes).trim();
                        Log.d(TAG, "Received Data: " + receivedData);

                        // 데이터 처리
                        String[] dataParts = receivedData.split(",");
                        if (dataParts.length >= 3) {
                            String data1 = dataParts[0].trim();
                            String data2 = dataParts[1].trim();
                            String data3 = dataParts[2].trim();

                            // UI 업데이트
                            runOnUiThread(() -> {
                                updateReceivedDataTextView(data1);
                                updateCardContent2(data2);
                                updateCardContent3(data3);
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "데이터 수신 중 오류 발생", e);
                    break;
                }
            }
        }).start();
    }

    private void updateCardContent(String data) {
        TextView cardContent = findViewById(R.id.card_content);
        cardContent.setText(data);
    }

    private void updateCardContent2(String data) {
        TextView cardContent2 = findViewById(R.id.card_content2);
        cardContent2.setText(data);
    }

    private void updateCardContent3(String data) {
        TextView cardContent3 = findViewById(R.id.card_content3);
        cardContent3.setText(data);
    }

    private void updateReceivedDataTextView(String data) {
        receivedDataTextView.setText(data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "블루투스 소켓을 닫는 중 오류 발생", e);
        }
    }
}