package com.pyrasfaleiavaris.emergencynotification;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {

    private ArrayList<String> phones = new ArrayList<>();
    private static final int PERMISSION_REQUEST_SEND_SMS = 0;
    private int clicked = 0;
    public final String ACCOUNT_SID = BuildConfig.API_ID;
    public final String ACCOUNT_AUTH = BuildConfig.API_AUTH;
    public static final String FLOW_SID = BuildConfig.API_FLOW;
    public static final String TO_NUMBER = BuildConfig.API_TO;
    public static final String FROM_NUMBER = BuildConfig.API_FROM;
    public static final String CALL = BuildConfig.CALL;
    public static final String SMS = BuildConfig.SMS;
    public static final String SMS_CALL = BuildConfig.SMS_CALL;
    public static final String SMS_SMS = BuildConfig.SMS_SMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Button emergency_btn = findViewById(R.id.emergency_btn_direct_sms);
        Button emergency_btn_twilio_sms = findViewById(R.id.emergency_btn_twilio_sms_direct);
        Button emergency_btn_twilio_call_sms = findViewById(R.id.emergency_btn_twilio_call_direct);
        Button emergency_btn_twilio_sms_api = findViewById(R.id.emergency_btn_twilio_sms_web);
        Button emergency_btn_twilio_call_api = findViewById(R.id.emergency_btn_twilio_call_web);
        fill_numbers();
        emergency_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = 1;
                SMS_handler();
            }
        });
        emergency_btn_twilio_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = 2;
                SMS_handler();
            }
        });

        emergency_btn_twilio_call_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = 3;
                SMS_handler();
            }
        });

        emergency_btn_twilio_sms_api.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = 4;
                API_call(1);
            }
        });

        emergency_btn_twilio_call_api.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = 5;
               API_call(2);
            }
        });
    }

    private void SMS_handler(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)){
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_SEND_SMS);
            }
        }
        else {
            switch(clicked) {
                case 1:
                    send_SMS_direct();
                    break;
                case 2:
                    send_SMS_twilio_direct();
                    break;
                case 3:
                    send_SMS_twilio_direct_for_call();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (clicked==1) {
                        send_SMS_direct();
                    }
                    else if (clicked==2){
                        send_SMS_twilio_direct();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void send_SMS_direct() {
        SmsManager smsManager = SmsManager.getDefault();

        for (int i = 0; i < phones.size(); i++){
            smsManager.sendTextMessage(phones.get(i), null,
                    "ΠΥΡΑΣΦΑΛΕΙΑ ΒΑΡΗΣ\nΕΠΕΙΓΟΝ\nΜΕΤΑΒΕΙΤΕ ΑΜΕΣΑ ΣΤΟΝ ΣΤΑΘΜΟ\n\n"
                    , null, null);
        }
        Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
    }

    private void send_SMS_twilio_direct() {
        SmsManager smsManager = SmsManager.getDefault();

        smsManager.sendTextMessage(FROM_NUMBER, null
                , SMS_SMS, null, null);
        Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
    }

    private void send_SMS_twilio_direct_for_call() {
        SmsManager smsManager = SmsManager.getDefault();

        smsManager.sendTextMessage(FROM_NUMBER, null
                , SMS_CALL, null, null);
        Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
    }


    private void API_call(int type) {
        RequestBody body;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String credential = Credentials.basic(ACCOUNT_SID, ACCOUNT_AUTH);

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        if (type==1) {
            body = RequestBody.create(mediaType, "To=" + TO_NUMBER + "&From=" + FROM_NUMBER + "&Parameters={\"type\":\""+ SMS + "\"}");
        } else {
            body = RequestBody.create(mediaType, "To=" + TO_NUMBER + "&From=" + FROM_NUMBER + "&Parameters={\"type\":\"" + CALL + "\"}");
        }
        Request request = new Request.Builder()
                .url("https://studio.twilio.com/v1/Flows/" + FLOW_SID + "/Executions")
                .method("POST", body)
                .addHeader("Authorization", credential)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
            Toast.makeText(getApplicationContext(), "Action Successful", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Action failed, please try again", Toast.LENGTH_LONG).show();
        }
    }

    private void fill_numbers() {
        String[] phone_array = getResources().getStringArray(R.array.phones);
        Collections.addAll(phones, phone_array);
    }
}