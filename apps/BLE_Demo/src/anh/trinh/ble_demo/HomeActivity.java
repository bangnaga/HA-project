package anh.trinh.ble_demo;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.crypto.spec.OAEPParameterSpec;

import org.apache.http.entity.ByteArrayEntity;

import android.R.integer;
import android.os.Bundle;
import android.os.IBinder;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.renderscript.Byte4;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import anh.trinh.ble_demo.data.BTMsgTypeDef;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.DeviceTypeDef;
import anh.trinh.ble_demo.data.NodeInfoDefination;
import anh.trinh.ble_demo.data.ProcessMsgPacket;

public class HomeActivity extends FragmentActivity implements TabListener {
	
	
	private final static String TAG = HomeActivity.class.getSimpleName();

    public static final String			EXTRAS_DEVICE_NAME 		= "DEVICE_NAME";
    public static final String			EXTRAS_DEVICE_ADDRESS	= "DEVICE_ADDRESS";

    private TextView 					mConnectionState;
    private String 						mDeviceName;
    private String 						mDeviceAddress;
    private ViewPager 					viewPager;
    private TabsPagerAdapter			mPagerAdapter;
    private ActionBar					actionBar;
    //Tab titles
    private String[]					actionTabs				= {"Device Control", "Scenes"};
    
    public BluetoothLeService 			mBluetoothLeService;
 
    private boolean 					mConnected 				= false;
    
    public BluetoothGattCharacteristic  mWriteCharacteristic;
    private ArrayList<BluetoothMessage> mBTMsg 					= new ArrayList<BluetoothMessage>();
    public ArrayList<DeviceInfo>        mDevInfoList 			= new ArrayList<DeviceInfo>();
    private DeviceInfo					mDeviceInfo				= new DeviceInfo();
    private ProcessMsgPacket            mProcessMsgPacket		= new ProcessMsgPacket(this);

    // Code to manage Service life cycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
                Log.i(TAG, "BLE Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                Log.i(TAG, "BLE Disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Get Characteristic support to write
            	mWriteCharacteristic = getWriteCharacteristic(mBluetoothLeService.getSupportedGattServices());
            	if(mWriteCharacteristic == null){
            		showDialog("BLE device don't support to write :(");
            		finish();
            	}
            	mBluetoothLeService.setCharacteristicNotification(mWriteCharacteristic, true);
            	Log.i(TAG, mWriteCharacteristic.getUuid().toString());
            	
            	//------- TEST--------<<<<<<<<<<
            	
            	 // initial static device list for expandable listview
//                for(int i = 0; i < 4; i++){
//                	DeviceInfo mDevInfo = new DeviceInfo();
//                	switch (i) {
//        			case 0:
//        				mDevInfo.setDevID(0x00000003);
//        				mDevInfo.setDevVal((short) 0);
//        				break;
//        			case 1:
//        				mDevInfo.setDevID(0x00000144);
//        				mDevInfo.setDevVal((short) 0);
//        				break;
//        			case 2:
//        				mDevInfo.setDevID(0x000000041);
//        				mDevInfo.setDevVal((short) 0);
//        				break;
//        			case 3:
//        				mDevInfo.setDevID(0x00000102);
//        				mDevInfo.setDevVal((short) 0);
//        				break;
//        			default:
//        				mDevInfo.setDevID(0x00000107);
//        				mDevInfo.setDevVal((short) 0);
//        				break;
//        			}
//                	mDevInfoList.add(mDevInfo);
//                }
                
            	//-------------->>>>>>>>>>>
            	
            	// Request number of devices
            } else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)){
            	mBluetoothLeService.readCharacteristic(mWriteCharacteristic);
            	
            } else if (BluetoothLeService.ACTION_DATA_READ.equals(action)){
//            	Log.i(TAG, intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
//            	showDialog(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            	
//            	mBTMsg.add(getBLEMessage(intent));
            	//timeout --> process msg queue
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
            	showDialog("Receive ACK");
            }
        }
    };
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    final Intent intent = getIntent();
        mDeviceName 		= intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress 		= intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager); 
        actionBar = getActionBar();
        actionBar.setTitle(mDeviceName);
        
        mPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        
        viewPager.setAdapter(mPagerAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        // Adding tabs
        for (String tab_name : actionTabs){
        	actionBar.addTab(actionBar.newTab()
        			.setText(tab_name)
        			.setTabListener(this));
        }
        
        /**
         * on swiping the viewpager make respective tab select
         */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				// TODO Auto-generated method stub
				actionBar.setSelectedNavigationItem(pos);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
     
	}
    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		 switch(item.getItemId()) {
	         case R.id.menu_connect:
	             mBluetoothLeService.connect(mDeviceAddress);
	             return true;
	         case R.id.menu_disconnect:
	             mBluetoothLeService.disconnect();
	             return true;
	         case android.R.id.home:
	             onBackPressed();
	             return true;
		 }
		return super.onOptionsItemSelected(item);
	}
	
	 private void updateConnectionState(final int resourceId) {
		 runOnUiThread(new Runnable() {
			 @Override
			 public void run() {
				 mConnectionState.setText(resourceId);
	         }
	      });
	 }
	 
	 private static IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
	        return intentFilter;
	 }
	 
	 /***
	  * 
	  * @param gattServices
	  * @return
	  */
	 private BluetoothGattCharacteristic getWriteCharacteristic(List<BluetoothGattService> gattServices)
	 {
		 if(gattServices == null){
			 return null;
		 }
		 
		 List<BluetoothGattCharacteristic>	gattCharacteristics;
		 int charaProp;
		 
		 for(BluetoothGattService gattService : gattServices){
			 gattCharacteristics = gattService.getCharacteristics();
			 for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
				charaProp = gattCharacteristic.getProperties(); 
				 if( (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0){
					 return gattCharacteristic;
				 }
			 }
		 }
		 
		 return null;
	 }
	  
	 /**
	  * Show pop-up message
	  * 
	  * @param msg
	  */
    public void showDialog(CharSequence msg){
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    
    
    // Precess ActionBar Tabs
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	  
}
