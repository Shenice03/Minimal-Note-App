package ca.yorku.eecs.mack.notetakingapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import java.io.ByteArrayOutputStream;
import android.util.Base64;

import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

public class NoteActivity extends Activity {

    private EditText titleEditText;
    private EditText inputEditText;
    private EditText titleInput;
    private boolean templateInserted = false;

    private boolean isDrawingMode = false;

    private int currentDrawingColor = Color.BLACK;
    private int position = -1;


    private DrawingView drawingCanvas;

    private TextView staticTextView;

    private BatterySyncManager batteryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_note_page);

        batteryManager = BatterySyncManager.getInstance(this);

        // Setting up the background's color depending on mode
        if (batteryManager.isDarkModeLast()) {
            getWindow().getDecorView()
                    .setBackgroundColor(ContextCompat
                            .getColor(this, R.color.backgroundLight));
        }
        else {
            getWindow().getDecorView()
                    .setBackgroundColor(ContextCompat
                            .getColor(this, R.color.backgroundDark));
        }

        staticTextView = findViewById(R.id.textViewStaticDisplay);

        titleInput = findViewById(R.id.editTextTitle);
        inputEditText = findViewById(R.id.editTextInput);

        // canvas and typing input
        inputEditText = findViewById(R.id.editTextInput);
        drawingCanvas = findViewById(R.id.drawingCanvas);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);

        if (intent != null && intent.hasExtra("title") && intent.hasExtra("content")) {
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");

            titleInput.setText(title);
            inputEditText.setText(content);

            if (intent.hasExtra("drawing")) {
                String drawingBase64 = intent.getStringExtra("drawing");
                if (drawingBase64 != null && !drawingBase64.isEmpty()) {
                    byte[] bytes = Base64.decode(drawingBase64, Base64.DEFAULT);
                    Bitmap drawingBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    drawingCanvas.loadFromBitmap(drawingBitmap);
                    if (isDrawingMode) {
                        staticTextView.setText(content);
                    }
                }
            }
        }

        // top bar
        ImageButton backButton = findViewById(R.id.buttonBack);
        ImageButton menuButton = findViewById(R.id.buttonMenu);
        titleEditText = findViewById(R.id.editTextTitle);

        // toolbar buttons
        ImageButton toggleKeyboardBtn = findViewById(R.id.buttonToggleKeyboard);
        ImageButton drawBtn = findViewById(R.id.buttonEnableDrawing);
        ImageButton eraseBtn = findViewById(R.id.buttonEraseDrawing);



        // back navigation
        backButton.setOnClickListener(v -> {
            Bitmap drawingBitmap = drawingCanvas.getBitmapSnapshot();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            drawingBitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            String base64Drawing = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String title = titleEditText.getText().toString().trim();
            String content = isDrawingMode ?
                    staticTextView.getText().toString().trim() :
                    inputEditText.getText().toString().trim();

            //Note note = new Note(title, content);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            resultIntent.putExtra("position", position);
            resultIntent.putExtra("drawing", base64Drawing);
            setResult(Activity.RESULT_OK, resultIntent);

            drawingBitmap.recycle();
            finish();
        });

        // page Settings popup menu
        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(NoteActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.page_settings_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_page_template) {
                    showPageTemplateOptions();
                    return true;
                } else if (id == R.id.menu_page_colour) {
                    showPageColourBottomSheet();
                    return true;
                }
                return false;

            });

            popupMenu.show();
        });

        toggleKeyboardBtn.setOnClickListener(v -> {
            isDrawingMode = false;

            inputEditText.setVisibility(View.VISIBLE);
            staticTextView.setVisibility(View.GONE);

            inputEditText.setFocusable(true);
            inputEditText.setFocusableInTouchMode(true);
            inputEditText.setClickable(true);
            inputEditText.setLongClickable(true);
            inputEditText.setCursorVisible(true);
            inputEditText.requestFocus();

            inputEditText.setOnTouchListener(null);

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT);
            }

            Toast.makeText(this, "Typing mode enabled", Toast.LENGTH_SHORT).show();
        });

        // draw mode
        drawBtn.setOnClickListener(v -> {
            isDrawingMode = true;
            drawingCanvas.setEraserMode(false);
            Toast.makeText(this, "Drawing mode enabled", Toast.LENGTH_SHORT).show();

            String typedText = inputEditText.getText().toString();
            staticTextView.setText(typedText);
            staticTextView.setVisibility(View.VISIBLE);

            inputEditText.setVisibility(View.GONE);

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
            }

            Toast.makeText(this, "Drawing mode enabled", Toast.LENGTH_SHORT).show();
        });

        // save note
        ImageButton saveButton = findViewById(R.id.buttonSaveNote);
        saveButton.setOnClickListener(v -> {
            titleInput = findViewById(R.id.editTextTitle);
            String content = inputEditText.getText().toString().trim();
            String title = titleInput.getText().toString().trim();

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap drawingBitmap = drawingCanvas.getBitmapSnapshot();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String base64Drawing = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Note note = new Note(title, content);
            note.setDrawingImageBase64(base64Drawing);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            resultIntent.putExtra("drawing", base64Drawing); // 打包画笔图像
            resultIntent.putExtra("position", position);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // erase mode
        eraseBtn.setOnClickListener(v -> {
            /*if (isDrawingMode) {
                Toast.makeText(this, "Erasing...", Toast.LENGTH_SHORT).show();

                // future erase logic goes here
            }*/

            drawingCanvas.setEraserMode(true);
            Toast.makeText(this, "Eraser enabled", Toast.LENGTH_SHORT).show();
        });

        SharedPreferences prefs = getSharedPreferences("NotePrefs", MODE_PRIVATE);
        int savedColor = prefs.getInt("note_bg_color", 0xFFFFFFFF); // default white

        View root = findViewById(R.id.drawingContainer);
        root.setBackgroundColor(savedColor);

        ImageButton stylusColorBtn = findViewById(R.id.buttonStylusColor);

        stylusColorBtn.setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.bottom_sheet_stylus_colors, null);
            final android.app.Dialog colorSheet = new android.app.Dialog(NoteActivity.this, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
            colorSheet.setContentView(view);
            colorSheet.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            colorSheet.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            colorSheet.show();

            View colorBlack = view.findViewById(R.id.colorBlack);
            View colorRed = view.findViewById(R.id.colorRed);
            View colorBlue = view.findViewById(R.id.colorBlue);
            View colorGreen = view.findViewById(R.id.colorGreen);

            View.OnClickListener colorClickListener = circle -> {
                if (circle.getId() == R.id.colorBlack) setDrawingColor(Color.BLACK);
                else if (circle.getId() == R.id.colorRed) setDrawingColor(Color.parseColor("#F44336"));
                else if (circle.getId() == R.id.colorBlue) setDrawingColor(Color.parseColor("#2196F3"));
                else if (circle.getId() == R.id.colorGreen) setDrawingColor(Color.parseColor("#4CAF50"));

                colorSheet.dismiss();
            };

            colorBlack.setOnClickListener(colorClickListener);
            colorRed.setOnClickListener(colorClickListener);
            colorBlue.setOnClickListener(colorClickListener);
            colorGreen.setOnClickListener(colorClickListener);
        });

        if (savedInstanceState != null) {
            byte[] byteArray = savedInstanceState.getByteArray("drawing_bitmap");
            if (byteArray != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                drawingCanvas.loadFromBitmap(bitmap);
            }
        }

    }

    private void showPageColourBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_page_colour, null);
        final android.app.Dialog bottomSheetDialog = new android.app.Dialog(NoteActivity.this, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.show();

        // get all color views
        View colorBeige = view.findViewById(R.id.colorBeige);
        View colorWhite = view.findViewById(R.id.colorWhite);
        View colorBrown = view.findViewById(R.id.colorBrown);
        View colorLightGray = view.findViewById(R.id.colorLightGray);

        // reference the full page root
        View root = findViewById(R.id.drawingContainer);

        View.OnClickListener colorClickListener = v -> {
            int color = 0xFFFFFFFF;

            if (v.getId() == R.id.colorBeige) color = 0xFFF5F5DC;
            else if (v.getId() == R.id.colorWhite) color = 0xFFFFFFFF;
            else if (v.getId() == R.id.colorBrown) color = 0xFFF1E3DA;
            else if (v.getId() == R.id.colorLightGray) color = 0xFFF2F2F2;

            // saves colour selection
            SharedPreferences prefs = getSharedPreferences("NotePrefs", MODE_PRIVATE);
            root.setBackgroundColor(color);
            prefs.edit().putInt("note_bg_color", color).apply();
            bottomSheetDialog.dismiss();
        };

        colorBeige.setOnClickListener(colorClickListener);
        colorWhite.setOnClickListener(colorClickListener);
        colorBrown.setOnClickListener(colorClickListener);
        colorLightGray.setOnClickListener(colorClickListener);
    }

    private void showPageTemplateOptions() {
        PopupMenu templateMenu = new PopupMenu(NoteActivity.this, findViewById(R.id.buttonMenu));
        templateMenu.getMenu().add("Weekly Planner");
        templateMenu.getMenu().add("Daily Planner");

        templateMenu.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            if (selected.equals("Weekly Planner")) {
                insertWeeklyPlannerTemplate();
            } else if (selected.equals("Daily Planner")) {
                insertDailyPlannerTemplate();
            }

            return true;
        });

        templateMenu.show();
    }

    private void insertWeeklyPlannerTemplate() {

        if (templateInserted) {
            Toast.makeText(this, "A template is already inserted in this note", Toast.LENGTH_SHORT).show();
            return;
        }

            String weeklyTemplate =
                    "📅 Weekly Planner\n" +
                            "\n" +
                            "Week of: \n\n" +
                            "🎯 Weekly Goals:\n" +
                            "\nGoal 1:\n" +
                            "\nGoal 2:\n" +
                            "\nGoal 3:\n\n" +
                            "\n🗓️ Week Overview:\n" +
                            "\nMonday:\n" +
                            "\nTuesday:\n" +
                            "\n Wednesday:\n" +
                            "\nThursday:\n" +
                            "\nFriday:\n" +
                            "\nSaturday:\n" +
                            "\nSunday:\n\n" +
                            "📝Notes:\n...\n";

            String currentText = inputEditText.getText().toString();
            inputEditText.setText(currentText + "\n\n" + weeklyTemplate);

        templateInserted = true;
        }
    private void insertDailyPlannerTemplate() {

        if (templateInserted) {
            Toast.makeText(this, "A template is already inserted in this note", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Enable typing to insert and edit a template", Toast.LENGTH_SHORT).show();

        String dailyTemplate =
                "📝 Daily Planner\n\n" +
                        "Date: \n\n" +
                        "Top 3 Priorities:\n\n" +
                        "Priority 1:\n" +
                        "Priority 2:\n" +
                        "Priority 3:\n\n" +
                        "⏰ Time-block Schedule:\n\n" +
                        "05:00 AM - \n" +
                        "06:00 AM - \n" +
                        "07:00 AM - \n" +
                        "08:00 AM - \n" +
                        "09:00 AM - \n" +
                        "10:00 AM - \n" +
                        "11:00 AM - \n" +
                        "☀️             - \n" +
                        "01:00 PM - \n" +
                        "02:00 PM - \n" +
                        "03:00 PM - \n" +
                        "04:00 PM - \n" +
                        "05:00 PM - \n" +
                        "06:00 PM - \n" +
                        "07:00 PM - \n" +
                        "08:00 PM - \n" +
                        "09:00 PM - \n" +
                        "10:00 PM - \n" +
                        "11:00 PM - \n" +
                        "🌙             - \n" +
                        "01:00 AM - \n" +
                        "02:00 AM - \n" +
                        "03:00 AM - \n" +
                        "04:00 AM - \n\n" +
                        "📝 Notes / Reflections:\n\n\n";

        String currentText = inputEditText.getText().toString();
        inputEditText.setText(currentText + "\n\n" + dailyTemplate);

        templateInserted = true;

    }

    private void setDrawingColor(int color) {
        currentDrawingColor = color;

        if (drawingCanvas != null) {
            drawingCanvas.setPaintColor(color);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bitmap drawingSnapshot = drawingCanvas.getBitmapSnapshot();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        drawingSnapshot.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        outState.putByteArray("drawing_bitmap", byteArray);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drawingCanvas != null) {
            drawingCanvas.clearTempResources();
        }
    }
}
