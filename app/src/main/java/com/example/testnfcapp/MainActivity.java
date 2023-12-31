package com.example.testnfcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    boolean isReading;
    boolean isWriting;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writingTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView edit_message;
    TextView nfc_contents;
    Button ActivateButton;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit_message= findViewById(R.id.edit_message);
        nfc_contents= findViewById(R.id.nfc_contents);
        ActivateButton= findViewById(R.id.ActivatedButton);
        context =this;

        //Writing button - device starts searching for a tag to write to
        ActivateButton.setOnClickListener(v -> {
            isWriting = true;
            isReading = false;
            Toast.makeText(context,"Waiting for tag", Toast.LENGTH_LONG).show();
        });

        //Reading button - device starts searching for a tag to read
        Button readButton = findViewById(R.id.readNFCbutton);

        readButton.setOnClickListener(v -> {
            isReading = true;
            isWriting = false;
            Toast.makeText(context,"Waiting for tag", Toast.LENGTH_LONG).show();
        });




        nfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null) {
            Toast.makeText(this,"This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Gives priority to foreground application for NFC I/O
        pendingIntent = PendingIntent.getActivity(this,0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected=new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter emptyTagDetected=new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try{
            tagDetected.addDataType("*/*");
            emptyTagDetected.addDataType("*/*");

        }
        catch (IntentFilter.MalformedMimeTypeException e){
            throw new RuntimeException("fail", e);
        }
        writingTagFilters=new IntentFilter[] {tagDetected, };
    }

    private void write(String text, Tag tag){

        //Maximum storage
        if (text.length() > 144){
            Toast.makeText(this,"Too long", Toast.LENGTH_SHORT).show();
            return;
        }

        //Appends spaces until String length is divisible by 4 - we must write to the tag in groups of 4 bytes
        StringBuilder textBuilder = new StringBuilder(text);
        for(int i = 0; i< textBuilder.length() % 4; i++){
            textBuilder.append(" ");
        }
        text = textBuilder.toString();


        byte[] letters = text.getBytes(StandardCharsets.US_ASCII);

        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            for (int i = 0; i < letters.length / 4; i++){
                //Ugh
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ultralight.writePage(i+8, IntStream.range(i*4, i*4+3).map(a -> letters[a]).toString().getBytes());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }

    private String read(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        try {
            mifare.connect();
            byte[] payload = mifare.readPages(4);
            return new String(payload, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
        } finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }




    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (isReading){
                nfc_contents.setText(read(myTag));
                isReading = false;
            }
            //TODO: Remove hardcoded string
            else if (isWriting){
                write("test", myTag);
                isWriting = false;
            }
        }



    }

    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }

    private void WriteModeOn() {
        writeMode=true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writingTagFilters, null);
    }

    private void WriteModeOff() {
        writeMode=false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}