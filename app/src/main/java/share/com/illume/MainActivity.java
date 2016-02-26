package share.com.illume;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private StringBuilder sb = new StringBuilder();

    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;
    WifiManager wifiManager;
    ArrayAdapter<String> adapter;
    BroadcastReceiver mReceiver;
    String wifis[];
    String ssid[];
    List<String> wifiList;
    ProgressDialog pDialog;
    String connectionSSID,connectionPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        listView= (ListView) findViewById(R.id.lvWifiNetworks);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ShowList().execute();
                Log.e("STATUS: ","New list of wifi");
            }
        });
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            Log.e("STATUS","Device does not support wifi");
        } else {
            Log.e("STATUS", "Device support wifi");
            wifiManager.setWifiEnabled(true);
        }
        getWifiNetworksList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                final AlertDialog alert = adb.create();
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                final View viewConnectoionLayout = inflater.inflate(R.layout.connection_wifi_layout, null);
                final EditText edtSSID = (EditText) viewConnectoionLayout.findViewById(R.id.edtNetworkSSID);
                final EditText edtPassword = (EditText) viewConnectoionLayout.findViewById(R.id.edtNetworkPassword);
                Button btnSubmit = (Button) viewConnectoionLayout.findViewById(R.id.btnSubmit);
                Button btnCancel = (Button) viewConnectoionLayout.findViewById(R.id.btnCancel);
                connectionSSID = wifiList.get(position).toString();
                edtSSID.setText("SSID: " + wifiList.get(position).toString());
                alert.setTitle("Wifi authentication");
                alert.setView(viewConnectoionLayout);
                alert.show();
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!(edtPassword.getText().toString().equals(""))) {
                            alert.dismiss();
                            connectionPassword = edtPassword.getText().toString();
                            Snackbar.make(view, edtSSID.getText().toString() + "\n" + "Password: " + edtPassword.getText().toString(), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                            new ConnecttoWifi(connectionSSID, connectionPassword).execute();
                        } else {
                            edtPassword.setError("Enter Password");
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void getWifiNetworksList(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver(new BroadcastReceiver(){
            @SuppressLint("UseValueOf") @Override
            public void onReceive(Context context, Intent intent) {
            sb = new StringBuilder();
            List<String> stringList = new ArrayList<String>();
            wifiList = new ArrayList<String>();
            List<ScanResult> scanList;
            scanList = wifiManager.getScanResults();
            wifis = new String[scanList.size()];
            for(int i = 0; i < scanList.size(); i++){
                sb.append(new Integer(i+1).toString() + ". ");
                sb.append((scanList.get(i)).toString());
                String ssde = String.valueOf(scanList.get(i)).substring(0, String.valueOf(scanList.get(i)).indexOf(","));
                String substr = ssde.substring(6,ssde.length());
                wifis[i] = substr;
                stringList.add(i,substr);
                wifiList.add(i,substr);
                sb.append("\n\n");
            }
            ssid = new String[scanList.size()];
            for(int j = 0; j < scanList.size() ;j++)
            {
                Log.e("STRRING LIST", stringList.get(j));
            }
            Log.e("ALLLLLLLLL WIFI ",sb.toString());

            List<ScanResult> networkList = wifiManager.getScanResults();
            //get current connected SSID for comparison to ScanResult
            WifiInfo wi = wifiManager.getConnectionInfo();
            String currentSSID = wi.getSSID();

            if (networkList != null) {
                for (ScanResult network : networkList)
                {
                    //check if current connected SSID
                    if (currentSSID.equals(network.SSID)){
                        //get capabilities of current connection
                        String Capabilities =  network.capabilities;
                        Log.d ("DEKHO DEKHO ", network.SSID + " capabilities : " + Capabilities);

                        if (Capabilities.contains("WPA2")) {
                            //do something
                            Toast.makeText(MainActivity.this, "WPA2", Toast.LENGTH_SHORT).show();
                        }
                        else if (Capabilities.contains("WPA")) {
                            //do something
                            Toast.makeText(MainActivity.this, "WPA", Toast.LENGTH_SHORT).show();
                        }
                        else if (Capabilities.contains("WEP")) {
                            //do something
                            Toast.makeText(MainActivity.this, "WEP", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            ArrayAdapter<String> aa = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,stringList);
            listView.setAdapter(aa);
            }
        },filter);
        wifiManager.startScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.wifi) {
            if(item.getTitle().toString().equals("Turn Wifi On"))
            {
                item.setTitle("Turn Wifi Off");
                wifiManager.setWifiEnabled(true);
                item.setIcon(getResources().getDrawable(R.drawable.wifi_on));
            }
            else if(item.getTitle().toString().equals("Turn Wifi Off")){
                item.setTitle("Turn Wifi On");
                item.setIcon(getResources().getDrawable(R.drawable.wifi_off));
                wifiManager.setWifiEnabled(false);
                List<String> list = new ArrayList<String>();
                listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, list));
//                new ShowList().execute();
            }
            return true;
        }
        else if (id == R.id.hotspot) {
            if (item.getTitle().toString().equals("Turn Hotspot Off")) {
                item.setTitle("Turn Hotspot On");
                item.setIcon(getResources().getDrawable(R.drawable.hotspot_on));

                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                final AlertDialog alert = adb.create();
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                final View viewConnectoionLayout = inflater.inflate(R.layout.connection_hotspot_layout, null);
                final EditText edtSSID = (EditText) viewConnectoionLayout.findViewById(R.id.edtNetworkSSID);
//                final EditText edtPassword = (EditText) viewConnectoionLayout.findViewById(R.id.edtNetworkPassword);
                Button btnSubmit = (Button) viewConnectoionLayout.findViewById(R.id.btnSubmit);
                Button btnCancel = (Button) viewConnectoionLayout.findViewById(R.id.btnCancel);
                alert.setTitle("Hotspot name");
                alert.setView(viewConnectoionLayout);
                alert.show();
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!(edtSSID.getText().toString().equals(""))) {
                            alert.dismiss();
                            String hotspotName = edtSSID.getText().toString();
//                            Snackbar.make(view, edtSSID.getText().toString() + "\n" + "Password: " + edtPassword.getText().toString(), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                            new OpenHotspot(hotspotName).execute();
                        } else {
                            edtSSID.setError("Enter name of hotspot");
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        item.setIcon(getResources().getDrawable(R.drawable.hotspot_off));
                    }
                });

            } else if (item.getTitle().toString().equals("Turn Hotspot On")) {
                item.setTitle("Turn Hotspot Off");
                item.setIcon(getResources().getDrawable(R.drawable.hotspot_off));
//                wifiManager.setWifiEnabled(false);
//                wifiManager.disconnect();
            }
        }
            return super.onOptionsItemSelected(item);
    }

    public class OpenHotspot extends AsyncTask {
        String ssid;
        public OpenHotspot(String ssid)
        {
            this.ssid = ssid;
            Log.e("SSID ", ssid);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setCancelable(true);
            pDialog.setTitle("Processing");
            pDialog.setMessage("Creating hotspot of " + ssid);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            if(wifiManager.isWifiEnabled())
            {
                wifiManager.setWifiEnabled(false);
            }

            WifiConfiguration netConfig = new WifiConfiguration();

            netConfig.SSID = ssid;
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            try{
                Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                boolean apstatus=(Boolean) setWifiApMethod.invoke(wifiManager, netConfig,true);

                Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
                while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){};
                Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
                int apstate=(Integer)getWifiApStateMethod.invoke(wifiManager);
                Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
                netConfig=(WifiConfiguration)getWifiApConfigurationMethod.invoke(wifiManager);
                Log.e("CLIENT", "\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");

            } catch (Exception e) {
                Log.e(this.getClass().toString(), "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(pDialog.isShowing())
            {
                pDialog.dismiss();
            }
            Log.e("STATUS: ","CONNECTED");
        }
    }

    public class ConnecttoWifi extends AsyncTask {
        String ssid,password;
        public ConnecttoWifi(String ssid,String password)
        {
            this.ssid = ssid;
            this.password = password;
            Log.e("SSID ", ssid);
            Log.e("PASSWORD", password);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setCancelable(true);
            pDialog.setTitle("Processing");
            pDialog.setMessage("Connecting to " + ssid);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "\"" + ssid + "\"";
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
            // For open network
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiManager.addNetwork(wifiConfiguration);
            List<WifiConfiguration> wifiConfig = wifiManager.getConfiguredNetworks();
            for(WifiConfiguration i : wifiConfig)
            {
                if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();

                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(pDialog.isShowing())
            {
                pDialog.dismiss();
            }
            Log.e("STATUS: ","CONNECTED");
        }
    }

    public class ShowList extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            getWifiNetworksList();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            wifiManager.setWifiEnabled(true);
            wifiManager.startScan();
            Intent intent=new Intent();
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setAction("share.com.illume");
            intent.putExtra("MyData", "1000");
            sendBroadcast(intent);
//               adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,versions);
            //  listView.setAdapter(adapter);
//
//          class WiFiScanReceiver extends BroadcastReceiver{
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//                        String action = intent.getAction();
//                        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
//                            List<ScanResult> wifiScanResultList = wifiManager.getScanResults();
//                            for (int i = 0; i < wifiScanResultList.size(); i++) {
//                                String hotspot = (wifiScanResultList.get(i)).toString();
//                                 Toast.makeText(getApplicationContext(), "hrllldfgkfdkg", Toast.LENGTH_LONG).show();
//                                //  adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hotspot);
//                                adapter.add(hotspot);
//                                listView.setAdapter(adapter);
//                            }
//                        }
//                    }
//                }
            // IntentFilter intentFilter=new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            // this.registerReceiver(mReceiver, intentFilter);



            // Intent intent=new Intent();
            //  intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            //   intent.setAction("com.example.BroadcastReceiver");
            // intent.putExtra("Foo", "Bar");
            //      sendBroadcast(intent);
            //  WiFiScanReceiver wifiread=new WiFiScanReceiver();
            //   wifiread.onReceive(this,intent);
            //      IntentFilter intentFilter=new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            //   registerReceiver(wifiread, intentFilter);


            //  IntentFilter intentFilter=new IntentFilter("com.example.BroadcastReceiver");
            //    WiFiScanReceiver wifiread=new WiFiScanReceiver();
            //  registerReceiver(wifiread, intentFilter);
        }
        else{
            wifiManager.setWifiEnabled(false);
        }
    }
}
