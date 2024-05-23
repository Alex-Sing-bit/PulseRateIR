package com.polar.polarsdkecghrdemo;

import static com.polar.polarsdkecghrdemo.helpers.Person.isPhoneNumber;
import static com.polar.polarsdkecghrdemo.helpers.Person.makePN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.polar.polarsdkecghrdemo.helpers.Person;
import com.polar.polarsdkecghrdemo.helpers.PersonBase;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    public static PersonBase base;
    private Bitmap userQR = null;
    private EditText idEditText;
    private ImageView imageView;

    public void onIR(View view) {
        Intent intent = new Intent(this, IRActivity.class);
        startActivity(intent);
    }

    public void onAddFriend(View view) {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);
    }

    public void onFriendsList(View view) {
        Intent intent = new Intent(this, FriendsListActivity.class);
        startActivity(intent);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("personBase", "");
        base = gson.fromJson(json, PersonBase.class);
        if (base == null || base.base.isEmpty()) {
            base = new PersonBase();
        }

        imageView = findViewById(R.id.imageView);
        idEditText = findViewById(R.id.idEditText);
        json = sharedPreferences.getString("number", "");
        number = gson.fromJson(json, String.class);
        if (number != null && !Objects.equals(number, " ")) {
           idEditText.setText(number);
        }
        onChangeNumber(imageView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(base);
        editor.putString("personBase", json);

        json = gson.toJson(number);
        editor.putString("number", json);

        editor.apply();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String number = " ";
    public void onChangeNumber(View view) {
        String s = String.valueOf(idEditText.getText());
        if (isPhoneNumber(s)) {
            number = makePN(s);
        } else {
            Toast.makeText(this, "Put number in the format +7-000-000-00-00",
                    Toast.LENGTH_LONG).show();
        }
        userQR = bitMatrixToBitmap(makeQR(number));
        imageView.setImageBitmap(userQR);
    }
    private BitMatrix makeQR(String text) {
        try {
            BitMatrix b = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 1000, 1000);
            return b;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Bitmap bitMatrixToBitmap(BitMatrix bitMatrix) {
        if (bitMatrix == null) {
            return null;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }


}

//Сохранение базы на устройстве
//Сканер qr для получения id при добавлении человека
//Блокировать ориентацию экрана