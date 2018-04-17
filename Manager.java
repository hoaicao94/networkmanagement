package com.os.hoai.byewifi;

import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Manager extends Activity {

    Button btnRead;
    TextView textResult;

    ListView listViewNode;
    ArrayList<Node> listNote;
    ArrayAdapter<Node> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager);

        String username = getIntent().getStringExtra("Username");

        TextView tv = (TextView)findViewById(R.id.editTextusername);
        tv.setText(username);

        btnRead = (Button)findViewById(R.id.readclient);
        textResult = (TextView)findViewById(R.id.result);

        listViewNode = (ListView)findViewById(R.id.nodelist);
        listNote = new ArrayList<>();
        ArrayAdapter<Node> adapter =
                new ArrayAdapter<Node>(
                        Manager.this,
                        android.R.layout.simple_list_item_1,
                        listNote);
        listViewNode.setAdapter(adapter);

        listViewNode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Node node = (Node) parent.getAdapter().getItem(position);
                Toast.makeText(Manager.this,
                        "MAC:\t" + node.mac + "\n" +
                                "IP:\t" + node.ip + "\n" +
                                "company:\t" + node.company + "\n" +
                                "country:\t" + node.country + "\n" +
                                "addressL1:\t" + node.addressL1 + "\n" +
                                "addressL2:\t" + node.addressL2 + "\n" +
                                "addressL3:\t" + node.addressL3 + "\n" +
                                "type:\t" + node.type + "\n" +
                                "startHex:\t" + node.startHex + "\n" +
                                "endHex:\t" + node.endHex + "\n" +
                                "startDec:\t" + node.startDec + "\n" +
                                "endDec:\t" + node.endDec,
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskReadAddresses(listNote, listViewNode).execute();
            }
        });
    }



    class Node {
        String ip;
        String mac;

        String jsonBody;
        String startHex;
        String endHex;
        String startDec;
        String endDec;
        String company;
        String addressL1;
        String addressL2;
        String addressL3;
        String country;
        String type;

        String remark;

        String queryString = "http://www.macvendorlookup.com/api/v2/";

        Node(String ip, String mac){
            this.ip = ip;
            this.mac = mac;
            queryMacVendorLookup();
        }

        @Override
        public String toString() {
            return "IP: " + ip + "\n" + "MAC: " + mac + "\n" + company + "\n" + remark;
        }

        private String sendQuery(String qMac) throws IOException{
            String result = "";

            URL searchURL = new URL(queryString + qMac);

            HttpURLConnection httpURLConnection = (HttpURLConnection) searchURL.openConnection();

            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader,
                        8192);

                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    result += line;
                }

                bufferedReader.close();
            }

            return result;
        }


        private void ParseResult(String json){

            try {
                JSONArray jsonArray = new JSONArray(json);
                JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                startHex = jsonObject.getString("startHex");
                endHex = jsonObject.getString("endHex");
                startDec = jsonObject.getString("startDec");
                endDec = jsonObject.getString("endDec");
                company = jsonObject.getString("company");
                addressL1 = jsonObject.getString("addressL1");
                addressL2 = jsonObject.getString("addressL2");
                addressL3 = jsonObject.getString("addressL3");
                country = jsonObject.getString("country");
                type = jsonObject.getString("type");
                remark = "OK";

            } catch (JSONException e) {
                e.printStackTrace();
                remark = e.getMessage();
            }

        }

        private void queryMacVendorLookup(){
            try {
                jsonBody = sendQuery(mac);
                ParseResult(jsonBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class TaskReadAddresses extends AsyncTask<Void, Node, Void> {

        ArrayList<Node> array;
        ListView listView;

        TaskReadAddresses(ArrayList<Node> array, ListView v){
            listView = v;
            this.array = array;
            array.clear();
            textResult.setText("querying...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            readAddresses();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            textResult.setText("Done");
        }

        @Override
        protected void onProgressUpdate(Node... values) {
            listNote.add(values[0]);
            ((ArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();

        }

        private void readAddresses() {

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] splitted = line.split(" +");
                    if (splitted != null && splitted.length >= 4) {
                        String ip = splitted[0];
                        String mac = splitted[3];
                        if (mac.matches("..:..:..:..:..:..")) {
                            Node thisNode = new Node(ip, mac);
                            publishProgress(thisNode);
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

