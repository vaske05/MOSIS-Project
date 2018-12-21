package com.mosisproject.mosisproject.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.service.BluetoothConnectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class AddFriendFragment extends Fragment {

    private static final String TAG = AddFriendFragment.class.getSimpleName();
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

    boolean friendshipSuccess;

    private OnFragmentInteractionListener mListener;

    public AddFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.navigation_add_friend);

        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //findViewsByIds(view);
        status = (TextView) view.findViewById(R.id.status);
        btnConnect = (Button) view.findViewById(R.id.btn_connect);
        listView = (ListView) view.findViewById(R.id.list);
        inputLayout = (TextInputLayout) view.findViewById(R.id.input_layout);
        btnSend = (Button) view.findViewById(R.id.btn_send);

        inputLayout.getEditText().setText("");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userId = firebaseUser.getUid();
                sendMessage(userId);
            }
        });

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
        chatAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, chatMessages);
        listView.setAdapter(chatAdapter);

        // Inflate the layout for this fragment
        return view;
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
                    friendshipSuccess = false;

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            final User user = dataSnapshot.getValue(User.class);
                            if(!checkFriendship(user, readMessage)) {

                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked
                                                user.addFriend(readMessage); //Add new friend
                                                databaseReference.setValue(user);

                                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(readMessage);
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        final User userSender = dataSnapshot.getValue(User.class);
                                                        userSender.addFriend(user.getId()); //Add new friend in User sender
                                                        databaseReference.setValue(userSender);
                                                        friendshipSuccess = true;
                                                        Toast.makeText(getContext(), "You became friend with. " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage(connectingDevice.getName() + " wants to be a friend with you.")
                                        .setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener)
                                        .show();
                            }
                            else {
                                Toast.makeText(getContext(), "You are already friends.", Toast.LENGTH_SHORT).show();
                            }
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
                    Toast.makeText(getContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    if(getContext() != null) {
                        Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return false;
        }
    });

    /**
     *
     * @param user user object
     * @param readMessage friend id
     * @return true if user have this friend in friendList
     */
    private boolean checkFriendship(User user,String readMessage ) {//TODO: PROVERI OVO
        List<String> friendList = user.getFriendsList();
        for(int i = 0; i < friendList.size(); i++) {
            if(friendList.get(i).equals(readMessage)) {
                return true;
            }
        }
        return false;
    }

    private void showPrinterPickDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.device_adapter_view);
        dialog.setTitle("Bluetooth Devices");

        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listViewPaired = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listViewDiscovered = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listViewPaired.setAdapter(pairedDevicesAdapter);
        listViewDiscovered.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(discoveryFinishReceiver, filter);

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

        //Cancel button click
        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                discoveredDevicesAdapter.clear();

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

    private void sendMessage(String message) {
        if(chatController.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            Toast.makeText( getContext(),"Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    private void checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {
        if(bluetoothAdapter == null) {
            Toast.makeText(getContext(),"Bluetooth is not available.", Toast.LENGTH_LONG).show();
            getActivity().finish(); //TODO proveri da li radi
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new BluetoothConnectionService(getContext(),handler);
                }
                else {
                    Toast.makeText(getContext(), "Bluetooth still disabled, turn off application", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
            else {
                chatController = new BluetoothConnectionService(getContext(), handler);
            }
        } catch (Exception e) {
            Log.e("BLUETOOTH ERROR:", e.getMessage());
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
    public void onDestroy() {
        super.onDestroy();
        if(chatController != null) {
            chatController.stop();
        }
    }

    private final BroadcastReceiver discoveryFinishReceiver;

    {
        discoveryFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                // discoveredDevicesAdapter.clear();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.w("BLUETOOTH DEVICE", bluetoothDevice.getName());
                    Log.w("BOND STATE", String.valueOf(bluetoothDevice.getBondState()));
                    if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (discoveredDevicesAdapter.getCount() > 0) {
                            if (discoveredDevicesAdapter.getItem(0) == getString(R.string.none_found)) {
                                discoveredDevicesAdapter.clear();
                            }
                        }
                        discoveredDevicesAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
                    }
                    else if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        discoveredDevicesAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (discoveredDevicesAdapter.getCount() == 0) {
                        discoveredDevicesAdapter.add(getString(R.string.none_found));
                    }
                }
            }
        };
    }
}
