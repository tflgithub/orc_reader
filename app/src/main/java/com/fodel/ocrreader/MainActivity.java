package com.fodel.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,OcrCaptureActivity.class),100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK==resultCode)
        {
            if(requestCode==100)
            {
                final UnitedArabEmiratesIDCard unitedArabEmiratesIDCard=data.getParcelableExtra("data");
                Toast.makeText(MainActivity.this,"Name:"+unitedArabEmiratesIDCard.name+"\nID Number:"+unitedArabEmiratesIDCard.IDCardNumber,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
