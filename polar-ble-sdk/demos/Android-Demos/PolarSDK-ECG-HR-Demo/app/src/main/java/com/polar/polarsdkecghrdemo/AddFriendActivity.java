package com.polar.polarsdkecghrdemo;


import static com.polar.polarsdkecghrdemo.helpers.Person.isHrId;
import static com.polar.polarsdkecghrdemo.helpers.Person.isPhoneNumber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.polar.polarsdkecghrdemo.helpers.Mood;
import com.polar.polarsdkecghrdemo.helpers.Person;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddFriendActivity extends AppCompatActivity {
    private Person addPerson = new Person();
    private EditText idEditText;
    private EditText nameEditText;

    private EditText hrEditText;

    public void onReturn(View view) {
        finish();
    }

    @SuppressLint("SetTextI18n")
    private ActivityResultLauncher<Intent> qrScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String number = data.getStringExtra("ID");
                        addPerson.setId(number);
                        idEditText.setText(number);
                    }
                }
            });

    public void onQRScanner(View view) {
        Intent qrIntent = new Intent(this, QRScanActivity.class);
        qrScanLauncher.launch(qrIntent);
    }

    public void onAdd(View view) {
        String number = idEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String hrId = hrEditText.getText().toString();
        if (isPhoneNumber(number) && !name.matches("^\\s*$") && isHrId(hrId)) {
            addPerson.setPhoneNumber(number);
            if (addPerson.getId() == -1) {
                addPerson.setId(addPerson.getPhoneNumber());
            }
            idEditText.setText(addPerson.getPhoneNumber());
            addPerson.setName(name);
            addPerson.setMood(72);
            addPerson.setHrId(hrId);

            MainActivity.base.addToBase(addPerson);
            addPerson = new Person();
            Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
            clearEditTexts();
        } else {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
        }


    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_friend);

        idEditText = findViewById(R.id.idEditText);
        nameEditText = findViewById(R.id.nameEditText);
        hrEditText = findViewById(R.id.hrIdEditText);

        String receivedString = getIntent().getStringExtra("ID");
        if (receivedString != null) {
            Person p = MainActivity.base.getPerson(Integer.parseInt(receivedString));
            if (p == null) {
                return;
            }
            idEditText.setText(p.getPhoneNumber());
            nameEditText.setText(p.getName());
            hrEditText.setText(p.getHrId());
        }
    }

    public void onDelete(View view) {
        String number = idEditText.getText().toString();
        int hash = Person.makeId(number);
        if (MainActivity.base.delete(hash)) {
            clearEditTexts();
            Toast.makeText(this, "Deleted", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Unknown number", Toast.LENGTH_LONG).show();
        }


    }

    public void clearEditTexts() {
        idEditText.getText().clear();
        nameEditText.getText().clear();
        hrEditText.getText().clear();
    }
}