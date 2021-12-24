package com.example.tasks;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class UpdateActivity extends AppCompatActivity {

    TaskModel taskToBeUpdated = new TaskModel();
    EditText etTask, etDate, etFocus;
    Button btnClear, btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        startVariables();
        setIntentData();
        setListeners();
    }

    private void startVariables() {
        etTask = findViewById(R.id.inputEditTextTask2);
        etDate = findViewById(R.id.inputEditTextDate2);
        etFocus = findViewById(R.id.etFocus2);
        btnClear = findViewById(R.id.btnClear2);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Enables textMultiLine EditText with ActionDone button (without Enter button)
        etTask.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etTask.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void setListeners() {
        Calendar calendar = Calendar.getInstance();
        final int currentYear = calendar.get(Calendar.YEAR);
        final int currentMonth = calendar.get(Calendar.MONTH);
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        etDate.setOnClickListener((v) -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this, (view, year, month, day) -> {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                etDate.setText(date);
            }, currentYear, currentMonth, currentDay);
            datePickerDialog.show();
            etFocus.requestFocus();
        });

        btnClear.setOnClickListener((v) -> {
            etTask.setText("");
            etDate.setText("");
            etFocus.requestFocus();
        });

        btnUpdate.setOnClickListener((v) -> {
            if (etTask.getText().toString().equals("") || etDate.getText().toString().equals("")) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                taskToBeUpdated.setId(getIntent().getLongExtra("id", 0));
                taskToBeUpdated.setName(etTask.getText().toString());
                taskToBeUpdated.setSlaDate(etDate.getText().toString());
                SQLiteHelper myDB = new SQLiteHelper(this);
                myDB.updateTask(taskToBeUpdated);
            }
        });
    }

    private void setIntentData() {
        etTask.setText(getIntent().getStringExtra("name"));
        etDate.setText(getIntent().getStringExtra("slaDate"));
    }
}