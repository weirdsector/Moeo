package moeo.moeo.common;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/*
 	Bluetooth socket 
 	Written by J.Yang in 2012
 	
 	BTSock - CServer : initserver -> find connection --> CConection 
 	       - CClient : initclient -> find remote device --> CConection.
 	       - CConection : start connection by object BluetoothDevice with remote site.
 	
 	* Listener example       
	bt = new BTSock();    
    bt.setOnReceive( new BTSock.OnReceiveListener()
	{
		@Override
	    public int OnReceive(byte b)
		{
			Log.d("bluetooth",Integer.toHexString(b));
			return 0;
		}
	});        
 
 */
public class BTSock 
{
	private static final int REQUEST_ENABLE_BT = 1;
	private static final UUID DEVUUID =	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public byte[] 	m_buffer = new byte[4096];			
	public	int		nLength=0;
	
	public BluetoothAdapter m_bt = null;
	CServer m_server = null;
	CClient m_client = null;
	boolean bConnected = false;
	public Activity m_parent;
	CConnection m_conn = null;	
	
	Thread thClient = null;
	Thread thServer = null;
	Thread thConn	= null;
		
	public BTSock()
	{	
	}
	
	public boolean Open()
	{
		if (m_bt==null)		
			m_bt = BluetoothAdapter.getDefaultAdapter();
		
		if (m_bt==null)	return false;
		if (m_bt.isEnabled()==false)	return false;		
		return true;
	}
	
	public void Close()
	{
		if (m_bt==null)	return;
		if (m_bt.isEnabled())
		{
			if (m_client!=null)
			{
				if (m_conn!=null)	m_conn.Close();
				m_client.Close();
			}
			m_client	= null;
		}
		m_bt	= null;
		bConnected = false;
	}
	
	public int GetLength()	{ return nLength;}
	public byte[] GetBuffer()	{ return m_buffer;}	
	
	public void ShowBTDlg()
	{
		if (m_bt.isEnabled()==true)	return;
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		m_parent.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}
	
	public void Scan()
	{
		//Intent serverIntent = new Intent(m_parent, DeviceListActivity.class);
		//m_parent.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}
	
	// Server mode.
	public void InitServer()
	{
		m_server	= new CServer();
		new Thread(m_server).start();
	}
	
	public boolean IsConnected()
	{
		return bConnected;
	}
	
	// Client mode.
	// Try to connect remote device.
	public boolean InitClient(String st,String stMac)
	{
		BluetoothDevice device = GetDevice(st,stMac);
		if (device==null)	
		{
			Log.d("bluetooth","Failed!!!!!!!1");
			return false;
		}
		
		m_client = new CClient(device);
		new Thread(m_client).start();		
		return true;
	}
	
	public boolean InitClient(BluetoothDevice device)
	{		
		if (device==null)	
		{
			Log.d("bluetooth","Failed!!!!!!!1");
			return false;
		}
		
		m_client = new CClient(device);
		thClient = new Thread(m_client);
		thClient.start();

		return true;
	}
	
	// listener.
	public OnReceiveListener receivelistener = null;
	public interface OnReceiveListener
	{
		public abstract int OnReceive(byte b);
		public abstract int OnConnect(boolean b);
	}
	
	public void setOnReceive(OnReceiveListener listener)
	{
		receivelistener = listener;
	}	
	
	// Kill server and restart client mode for a given socket.
	private void Connect(BluetoothSocket sock,BluetoothDevice device)
	{
		if (m_server!=null)	m_server.Close();
		m_server = null;
		
		m_conn 	= new CConnection(sock);
		thConn	 = new Thread(m_conn);
		thConn.start();		
	}	
		
	// send 
	public int Send(byte[] buffer, int nLen)
	{
		if (m_conn==null)	return 0;
		
		try 
		{
			m_conn.m_out.write(buffer,0,nLen);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nLen;
	}
	
	private BluetoothDevice GetDevice(String st,String stMac)
	{
		Set<BluetoothDevice> pairedDevices = m_bt.getBondedDevices();
		
		if (pairedDevices.size()>0) 
		{	
			// Check same name
			int n = 0;
			BluetoothDevice ret=null;
			for (BluetoothDevice device : pairedDevices) 
			{
				String stDev = device.getName();
				if (stDev.equals(st))	
				{
					n++;
					ret	= device;
				}
			}		
			
			Log.d("bluetooth",Integer.toString(n));
			if (n==0)	return null;
			if (n==1)	return ret;
			
			// name is same so check mac address.
			for (BluetoothDevice device : pairedDevices) 
			{
				String stDev = device.getAddress();								
				if (stDev.equals(stMac))	return device;
			}
		}
		return null;	
	}
		
	private void startActivityForResult(Intent enableBtIntent,int requestEnableBt) 
	{
	}

	
	class CServer implements Runnable
	{
		BluetoothServerSocket m_sock=null;
		
		public CServer()
		{
			try 
			{
				m_sock = m_bt.listenUsingRfcommWithServiceRecord("BTSock",DEVUUID);
			} catch (IOException e) {e.printStackTrace();}
		}

		@Override
		public void run() 
		{
			BluetoothSocket sock = null;
			while(sock==null)
			{
				try 
				{
					sock = m_sock.accept();
				} 
				catch (IOException e) {	e.printStackTrace();}				
				if (sock!=null)
					Connect(sock,sock.getRemoteDevice());
			}
			
			Log.d("bluetooth","start connection");
			bConnected = true;
			m_server = null;
		}
		
		public void Close()
		{
			try 
			{
				m_sock.close();
			} 
			catch (IOException e) {	e.printStackTrace();}
			m_sock = null;
		}
	}
	
	private class CConnection implements Runnable
	{
	    private BluetoothSocket m_sock;
	    private InputStream m_in=null;
	    private OutputStream m_out=null;	    
	    
	    public CConnection(BluetoothSocket sock) 
	    {	        
	    	m_sock	= sock;
	    	try 
	    	{
				m_in	= m_sock.getInputStream();
				m_out	= m_sock.getOutputStream();
			} 
	    	catch (IOException e) {	e.printStackTrace();	}
	    }
	    
	    public void Close()
	    {
	    	if (thConn!=null)	thConn.interrupt();
	    	thConn	= null;
	    	
	    	try {
				m_in.close();
				m_in = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	try {
				m_out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    public void run() 
	    {	
	        while(true)
	        {	        	
				try 
				{
					if (m_in==null)	break;
					
					nLength = m_in.read(m_buffer);															
					if (receivelistener!=null)
					receivelistener.OnReceive(m_buffer[0]);
				} 
				catch (IOException e) {	e.printStackTrace(); break;}
	        }	        
	    }  	    	    
	}
	
	class CClient implements Runnable
	{
		BluetoothSocket m_sock=null;
		BluetoothDevice m_device=null;

		public CClient(BluetoothDevice device)
		{
			m_device = device;
			try 
			{
				//m_sock = m_device.createInsecureRfcommSocketToServiceRecord(DEVUUID);
				//m_sock	= m_device.createRfcommSocketToServiceRecord(DEVUUID);

				//m_sock =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
				m_sock=createBluetoothSocket(m_device);


			} catch (IOException e) {e.printStackTrace();}
		}

		private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
				throws IOException {
			if(Build.VERSION.SDK_INT >= 10){
				try {
					final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
					return (BluetoothSocket) m.invoke(device, DEVUUID);
				} catch (Exception e) {
					Log.e("dd", "Could not create Insecure RFComm Connection",e);
				}
			}
			return  device.createRfcommSocketToServiceRecord(DEVUUID);
		}

		@Override
		public void run() 
		{
			m_bt.cancelDiscovery();
			
			try 
			{
				m_sock.connect();
			} 
			catch (IOException e) 
			{
				try 
				{
					m_sock.close();
				} 
				catch (IOException e1) { e1.printStackTrace();	}
				return;
			}
			Connect(m_sock,m_device);
			Log.d("bluetooth","start connection");
			bConnected = true;
			
			if (receivelistener!=null)
				receivelistener.OnConnect(true);
		}
		
		public void Close()
		{
			try 
			{
				if (thClient!=null)	thClient.interrupt();
				thClient = null;
		
				m_sock.close();
			} 
			catch (IOException e) {	e.printStackTrace();}
			m_sock = null;
		}
	}
}
