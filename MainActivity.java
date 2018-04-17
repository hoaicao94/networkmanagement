package com.os.hoai.byewifi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    Button b1;
    EditText ed1, ed2;
    TextView tx1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.cButton);
        ed1 = (EditText) findViewById(R.id.editTextusername);
        ed2 = (EditText) findViewById(R.id.editText6);
        tx1.setVisibility(View.GONE);


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed1.getText().toString().equals("Shena") &&
                        ed2.getText().toString().equals("Password")) {
                    Toast.makeText(getApplicationContext(),
                            "Signing in", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(MainActivity.this, Manager.class);
                    MainActivity.this.startActivity(myIntent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Try Again", Toast.LENGTH_SHORT).show();
                    tx1.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
