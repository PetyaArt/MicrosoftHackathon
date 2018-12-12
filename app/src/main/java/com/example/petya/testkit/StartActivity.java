package com.example.petya.testkit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private int anime = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("fname", anime);
                startActivity(intent);
            }
        });
    }

    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                anime = 15;
                return true;
            case R.id.item2:
                anime = 20;
                return true;
            case R.id.item3:
                anime = 25;
                return true;
            case R.id.item4:
                anime = 30;
                return true;
            case R.id.item5:
                anime = 35;
                return true;
            case R.id.item6:
                anime = 40;
                return true;
            default:
                return false;
        }
    }
}
