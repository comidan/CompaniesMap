package com.dev.companiesmap;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderActivity extends AppCompatActivity {

    private Button setOrder;
    private EditText orderName, orderDescription, orderAddress;
    private NumberPicker quantity;
    private DatePicker deliveryDate;
    private String queryValues = "(";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        setTitle("Ordine");
        setOrder = (Button) findViewById(R.id.order_set);
        orderName = (EditText) findViewById(R.id.order_name);
        orderDescription = (EditText) findViewById(R.id.order_description);
        orderAddress = (EditText) findViewById(R.id.order_delivery_address);
        quantity = (NumberPicker) findViewById(R.id.order_quantity);
        deliveryDate = (DatePicker) findViewById(R.id.order_delivery_date);
        setOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {

                    private Object[] data;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        data = new Object[]{getIntent().getIntExtra("LOCATION_TYPE", 0),
                                            getIntent().getStringExtra("LOCATION_NAME"),
                                            orderAddress.getText().toString(),
                                            getIntent().getStringExtra("LOCATION_ADDRESS"),
                                            orderName.getText().toString(),
                                            orderDescription.getText().toString(),
                                            getIntent().getStringExtra("LOCATION_DATE"),
                                            quantity.getValue(),
                                            deliveryDate.getYear() + "/" + deliveryDate.getMonth() + "/" + deliveryDate.getDayOfMonth()};
                    }

                    @Override
                    protected String doInBackground(Void[] params) {
                        try {
                            for (int index = 0; index < data.length; index++) {
                                if (data[index] instanceof String)
                                    queryValues += (index == 0 ? "" : ",'") + ((String) data[index]) + "'";
                                else if (data[index] instanceof Integer)
                                    queryValues += (index == 0 ? "" : ",") + ((Integer) data[index]);
                            }
                            queryValues += ");";
                            String sql = "INSERT INTO orders (TYPE, Name, Address, LocationAddress, OrderContent, Description, Date, Quantity, OrderDate) " +
                                    "VALUES " + queryValues;
                            String link = "http://192.168.1.186/neworders.php";  //just set webserver name or its IP
                            String data = URLEncoder.encode("query", "UTF-8") + "=" +
                                    URLEncoder.encode(sql, "UTF-8");
                            Log.v("QUERY", sql);
                            URL url = new URL(link);
                            URLConnection conn = url.openConnection();
                            conn.setDoOutput(true);
                            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                            wr.write(data);
                            wr.flush();
                            BufferedReader reader = new BufferedReader(new
                                    InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            // Read Server Response
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                                break;
                            }
                            return sb.toString();
                        } catch (Exception e) {
                            return new String("Exception: " + e.getMessage());
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        Log.v("REPORT", s);
                        finish();
                    }
                }.execute();
            }
        });
    }
}
