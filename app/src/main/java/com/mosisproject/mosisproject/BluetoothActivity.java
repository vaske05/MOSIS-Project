package com.mosisproject.mosisproject;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.service.BluetoothConnectionService;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = BluetoothActivity.class.getSimpleName();
    private TextView status;
    private Button btnConnect;
    private ListView listView;
    private Dialog dialog;
    private TextInputLayout inputLayout;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private BluetoothAdapter bluetoothAdapter;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    View btnSend;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private BluetoothConnectionService chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_bluetooth);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        findViewsByIds();

        //check device support bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothSupport(bluetoothAdapter);

        //show bluetooth devices dialog when click connect button
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrinterPickDialog();
            }
        });

        //set chat adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, chatMessages);
        listView.setAdapter(chatAdapter);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:
                        case BluetoothConnectionService.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuffer);
                    chatMessages.add("Me: " + writeMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    final String readMessage = new String(readBuffer, 0, msg.arg1);

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            user.addFriend(readMessage.toString()); //Add new friend

                            databaseReference.setValue(user);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "SAVE FRIEND:failure -> " + databaseError.getMessage());

                        }
                    });

                    chatMessages.add(connectingDevice.getName() + ": " + readMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.device_adapter_view);
        dialog.setTitle("Bluetooth Devices");

        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listViewPaired = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listViewDiscovered = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listViewPaired.setAdapter(pairedDevicesAdapter);
        listViewDiscovered.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        listViewPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        listViewDiscovered.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setStatus(String s) {
        status.setText(s);
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds() {
        status = (TextView) findViewById(R.id.status);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        listView = (ListView) findViewById(R.id.list);
        inputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        btnSend = (Button) findViewById(R.id.btn_send);

        inputLayout.getEditText().setText("");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userId = firebaseUser.getUid();
                sendMessage(userId);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new BluetoothConnectionService(this,handler);
                }
                else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }

    }

    private void checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {
        if(bluetoothAdapter == null) {
            Toast.makeText(this,"Bluetooth is not available.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void sendMessage(String message) {
        if(chatController.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            Toast.makeText(this,"Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
        else {
            chatController = new BluetoothConnectionService(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(chatController != null) {
            if (chatController.getState() == BluetoothConnectionService.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatController != null) {
            chatController.stop();
        }
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            discoveredDevicesAdapter.clear();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };
}
