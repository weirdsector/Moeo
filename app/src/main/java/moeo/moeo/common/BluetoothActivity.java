package moeo.moeo.common;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import moeo.moeo.R;

//BluetoothLescan
public class BluetoothActivity extends Activity 
{
    private final static UUID uuid = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");
    
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() 
    {
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
 
            // Whenever a remote Bluetooth device is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.i("TESTING1","TESTING1");

                adapter.add(bluetoothDevice.getName() + "\n"
                        + bluetoothDevice.getAddress());
            }
        }
    };
    private BluetoothAdapter bluetoothAdapter;
    private ListView listview;
    private ArrayAdapter adapter;
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.6f;
        getWindow().setAttributes(layoutParams);
        
        // Fullscreen.
        {
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_bluetooth);

        listview = (ListView) findViewById(R.id.listView1);
        
        // ListView Item Click Listener
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) 
            {            	            
                // ListView Clicked item value
                String  itemValue = (String) listview.getItemAtPosition(position);

                String MAC = itemValue.substring(itemValue.length() - 17);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);

                Toast.makeText(getApplicationContext(), "Connecting device.. Wait until Bluetooth icon is activated.", Toast.LENGTH_SHORT).show();
            	   
            	Intent intent = new Intent();            	              	
            	intent.putExtra("device",bluetoothDevice );            	
            	setResult(RESULT_OK,intent);
            	finish();   
            }  
        });  

        adapter = new ArrayAdapter 
                (this,android.R.layout.simple_list_item_1);
        listview.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoverDevices();
        
        Toast.makeText(getApplicationContext(), "Scanning Bluetooth devices...",
                Toast.LENGTH_SHORT).show();
        
        ListeningThread t = new ListeningThread();
        t.start();        
    }
   
    protected void discoverDevices()    
    {
        // To scan for remote Bluetooth devices
        if (bluetoothAdapter.startDiscovery()) 
        {
        } 
        else 
        {
            Toast.makeText(getApplicationContext(), "Scanning is delayed.",
                    Toast.LENGTH_SHORT).show(); 
        }
    }

    protected void makeDiscoverable()
    {
        // Make local device discoverable
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the BroadcastReceiver for ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();        
        this.unregisterReceiver(broadcastReceiver);        
    }
    
    @Override 
	protected void onStop() 
	{ 		
		super.onStop();
		finish();
	}

    private class ListeningThread extends Thread 
    {
        private final BluetoothServerSocket bluetoothServerSocket;

        public ListeningThread() 
        {
            BluetoothServerSocket temp = null;
            try 
            {
                Log.i("TESTING","TESTING");

                temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid);
            } catch (IOException e) {
                Log.i("TESTING","ERROR IO");

                e.printStackTrace();
            }
            bluetoothServerSocket = temp;
        }

        public void run() 
        {
            BluetoothSocket bluetoothSocket;
            // This will block while listening until a BluetoothSocket is returned
            // or an exception occurs
            while (true) 
            {
                try {
                    Log.i("TESTING","TRYING");

                    bluetoothSocket = bluetoothServerSocket.accept(3000);
                    Log.i("TESTING",bluetoothSocket.toString());

                } catch (IOException e) {
                    Log.i("TESTING","IO2");

                    break;
                }

                // If a connection is accepted
                if (bluetoothSocket != null)
                {
                    Log.i("TESTING","ACCEPTED");

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "A connection has been accepted.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Code to manage the connection in a separate thread
                   /*
                       manageBluetoothConnection(bluetoothSocket);
                   */

                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Cancel the listening socket and terminate the thread
        public void cancel() 
        {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectingThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectingThread(BluetoothDevice device) {

            BluetoothSocket temp = null;
            bluetoothDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                temp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = temp;
        }

        public void run() {
            // Cancel discovery as it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // This will block until it succeeds in connecting to the device
                // through the bluetoothSocket or throws an exception
                bluetoothSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }

            // Code to manage the connection in a separate thread
            /*
               manageBluetoothConnection(bluetoothSocket);
            */ 
        }

        // Cancel an open connection and terminate the thread
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
