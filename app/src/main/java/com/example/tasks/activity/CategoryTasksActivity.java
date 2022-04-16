package com.example.tasks.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tasks.R;
import com.example.tasks.adapter.FinishedTaskAdapter;
import com.example.tasks.adapter.OnHoldTaskAdapter;
import com.example.tasks.adapter.ViewPagerAdapter;
import com.example.tasks.data_base.SQLiteHelper;
import com.example.tasks.fragment.FinishedTaskFragment;
import com.example.tasks.fragment.TasksOnHoldFragment;
import com.example.tasks.model.TaskModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class CategoryTasksActivity extends AppCompatActivity {

    int catAdaptPosition;
    String categoryName;
    TasksOnHoldFragment fragOnHoldTasks;
    FinishedTaskFragment fragFinishedTasks;
    AlertDialog.Builder builder;
    ActivityResultLauncher<Intent> actResult;
    OnHoldTaskAdapter adaptOnHoldTasks;
    FinishedTaskAdapter adaptFinishedTasks;
    SQLiteHelper myDB;
    TabLayout tabLayout;
    ViewPager2 vp2;
    Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        catAdaptPosition = getIntent().getIntExtra("position", -1);
        categoryName = getIntent().getStringExtra("categoryName");

        setTitle(categoryName);

        getSupportActionBar().setElevation(0);
        setContentView(R.layout.activity_category_tasks);
        init();
    }

    public void init() {
        myDB = new SQLiteHelper(this);
        builder = new AlertDialog.Builder(this);
        startBtnAdd();
        startActivityResult();
        startAdaptersAndFragments();
        startTabLayout();
        startViewPager();
    }

    public void startBtnAdd() {
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        Intent createTaskActivity = new Intent(this, CreateTaskActivity.class);

        createTaskActivity.putExtra("categoryName", categoryName);
        btnAdd.setOnClickListener(v -> actResult.launch(createTaskActivity));
    }

    public void startActivityResult() {
        actResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (resultCode != Activity.RESULT_CANCELED) {
                        TaskModel task = result.getData().getParcelableExtra("task");
                        if (resultCode == 1) {
                            adaptOnHoldTasks.addTask(task);
                        } else if (resultCode == 2) {
                            int position = result.getData().getIntExtra("position", 0);
                            adaptOnHoldTasks.updateTask(position, task);
                        } else if (resultCode == 3) {
                            int position = result.getData().getIntExtra("position", 0);
                            adaptFinishedTasks.updateTask(position, task);
                        }
                    }
                }
        );
    }

    public void startAdaptersAndFragments() {
        adaptOnHoldTasks = new OnHoldTaskAdapter(this, catAdaptPosition, actResult, myDB, categoryName);
        adaptFinishedTasks = new FinishedTaskAdapter(this, catAdaptPosition, actResult, myDB, categoryName);

        adaptOnHoldTasks.setFinishedTasksAdapter(adaptFinishedTasks);
        adaptFinishedTasks.setOnHoldTaskAdapter(adaptOnHoldTasks);

        fragOnHoldTasks = new TasksOnHoldFragment(adaptOnHoldTasks);
        fragFinishedTasks = new FinishedTaskFragment(adaptFinishedTasks);
    }

    public void startTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                myOnTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public void startViewPager() {
        ViewPagerAdapter vpAdapter = new ViewPagerAdapter(this);
        vp2 = findViewById(R.id.viewPager2);

        vpAdapter.addFragment(fragOnHoldTasks, "Em espera");
        vpAdapter.addFragment(fragFinishedTasks, "Concluídas");
        vp2.setAdapter(vpAdapter);

        new TabLayoutMediator(
                tabLayout, vp2, (tab, position) -> tab.setText(vpAdapter.getTitles().get(position))
        ).attach();
    }

    public void startSortBuilder() {
        if (isFirstTabSelected()) {
            builder.setMessage("Deseja ordenar por data de expiração?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                adaptOnHoldTasks.sortTasksArrayBySlaDate();
                dialog.dismiss();
            });
            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setMessage("Deseja ordenar por data de conclusão?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                adaptFinishedTasks.sortTasksArrayByFinishedDate();
                dialog.dismiss();
            });
            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        }
        builder.show();
    }

    public void startDeleteAllBuilder() {
        if (isFirstTabSelected()) {
            builder.setMessage("Deseja excluir todas as tarefas em espera?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                myDB.deleteOnHoldTasks();
                adaptOnHoldTasks.deleteAllTasks();
                dialog.dismiss();
            });
            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setMessage("Deseja excluir todas as tarefas concluídas?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                myDB.deleteFinishedTasks();
                adaptFinishedTasks.deleteAllTasks();
                dialog.dismiss();
            });
            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        }
        builder.show();
    }

    public void startFinishAllBuilder() {
        builder.setMessage("Deseja concluir todas as tarefas?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            ArrayList<TaskModel> tasks = adaptOnHoldTasks.getAllTasks();
            adaptFinishedTasks.putTasksAsFinished(tasks);
            adaptFinishedTasks.addAllTasks(tasks);
            adaptOnHoldTasks.deleteAllTasks();
            dialog.dismiss();
        });
        builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void startUndoAllBuilder() {
        builder.setMessage("Deseja desfazer todas as tarefas?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            ArrayList<TaskModel> tasks = adaptFinishedTasks.getAllTasks();
            adaptOnHoldTasks.putTasksAsOnHold(tasks);
            adaptOnHoldTasks.addAllTasks(tasks);
            adaptFinishedTasks.deleteAllTasks();
            dialog.dismiss();
        });
        builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void myOnTabSelected(TabLayout.Tab tab) {
        if (myMenu != null) {
            if (tab.getPosition() == 0) {
                myMenu.getItem(2).setVisible(true);
                myMenu.getItem(3).setVisible(false);
            } else {
                myMenu.getItem(2).setVisible(false);
                myMenu.getItem(3).setVisible(true);
            }
        }
    }

    public boolean isFirstTabSelected() {
        return tabLayout.getTabAt(0).isSelected();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuBuilder m = (MenuBuilder) menu;
        myMenu = menu;

        m.setOptionalIconsVisible(true);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        menu.getItem(3).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sortBySlaDate)
            startSortBuilder();
        else if (id == R.id.deleteAll)
            startDeleteAllBuilder();
        else if (id == R.id.finishAll)
            startFinishAllBuilder();
        else if (id == R.id.undoAll)
            startUndoAllBuilder();
        return super.onOptionsItemSelected(item);
    }
}