package com.wei.vocalgame03;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

interface OnLevelSelected {
    void onSelected(int value);
}

public class MainActivity extends Activity {
    public static final Integer AUDIO_PERMISSION = 1;
    private FrameLayout menuFrame;
    private FrameLayout gameContainer;
    private EditText editText;
    private ImageButton btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isPermissionGranted()) {
            requestPermission();
        }

        menuFrame = findViewById(R.id.menu_frame);
        gameContainer = findViewById(R.id.game_container);
        editText = findViewById(R.id.edtSpeechText);
        btn = findViewById(R.id.wand_button);

        showMainMenu();
    }

    private void showMainMenu() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View menuView = inflater.inflate(R.layout.menu_main, menuFrame, false);

        Button newGameBtn = menuView.findViewById(R.id.btn_new_game);
        Button skipBtn = menuView.findViewById(R.id.btn_skip);

        newGameBtn.setOnClickListener(v -> startNewGame(1));
        skipBtn.setOnClickListener(v -> {
            levelSelector(value -> {
                if (value == -1) return;
                startNewGame(value);
            });
        });

        menuFrame.addView(menuView);
    }

    private void levelSelector(OnLevelSelected callback) {
        {
            NumberPicker picker = new NumberPicker(this);
            picker.setMinValue(1);
            picker.setMaxValue(6);
            picker.setWrapSelectorWheel(false);

            new AlertDialog.Builder(this)
                    .setTitle("Select a level")
                    .setView(picker)
                    .setPositiveButton("OK", (dialog, which) -> {
                        callback.onSelected(picker.getValue());
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        callback.onSelected(-1);
                    }).show();
        }
    }

    private void startNewGame(int startlevel) {
        menuFrame.removeAllViews();
        menuFrame.setVisibility(View.GONE);

        GameSurface gameSurface = new GameSurface(this, btn, editText, startlevel);
        gameContainer.addView(gameSurface);
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_PERMISSION && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

}

