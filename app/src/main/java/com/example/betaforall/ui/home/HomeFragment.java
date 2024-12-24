package com.example.betaforall.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.betaforall.databinding.FragmentHomeBinding;
import com.example.betaforall.model.Equipment;
import com.example.betaforall.model.InventoryItem;
import com.example.betaforall.model.ResponsiblePerson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    EditText Number;
    String userId = currentUser != null ? currentUser.getUid() : null;

    private FragmentHomeBinding binding;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Инициализация AutoCompleteTextView из макета
        Number = binding.serialNumberEditText;
        AutoCompleteTextView cpuAutoComplete = binding.cpuAutoComplete;
        AutoCompleteTextView gpuAutoComplete = binding.gpuAutoComplete;
        AutoCompleteTextView motherboardAutoComplete = binding.motherboardAutoComplete;
        AutoCompleteTextView caseAutoComplete = binding.caseAutoComplete;
        AutoCompleteTextView powerSupplyAutoComplete = binding.powerSupplyAutoComplete;
        Spinner responsibleSpinner = binding.responsibleSpinner;

        // Инициализация кнопки "Собрать"
        Button assembleButton = binding.assembleButton;

        // Получение ссылки на базу данных Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Склад");

        // Загрузка данных для каждой категории
        loadCategoryData("Процессор", cpuAutoComplete);
        loadCategoryData("Видеокарта", gpuAutoComplete);
        loadCategoryData("Материнская плата", motherboardAutoComplete);
        loadCategoryData("Корпус", caseAutoComplete);
        loadCategoryData("Блок питания", powerSupplyAutoComplete);

        // Обработка нажатия на кнопку "Собрать"
        assembleButton.setOnClickListener(v -> {
            String Numbers = Number.getText().toString();
            String cpu = cpuAutoComplete.getText().toString().trim();
            String gpu = gpuAutoComplete.getText().toString().trim();
            String motherboard = motherboardAutoComplete.getText().toString().trim();
            String caseComponent = caseAutoComplete.getText().toString().trim();
            String powerSupply = powerSupplyAutoComplete.getText().toString().trim();

            // Получение выбранного ответственного
            String selectedResponsible = responsibleSpinner.getSelectedItem().toString();

            // Проверка и добавление комплектующих на склад, если их нет
            checkAndAddItemToInventory("Процессор", cpu);
            checkAndAddItemToInventory("Видеокарта", gpu);
            checkAndAddItemToInventory("Материнская плата", motherboard);
            checkAndAddItemToInventory("Корпус", caseComponent);
            checkAndAddItemToInventory("Блок питания", powerSupply);

            // Создание заказа
            List<String> комплектующие = new ArrayList<>();
            if (!cpu.isEmpty()) комплектующие.add(cpu);
            if (!gpu.isEmpty()) комплектующие.add(gpu);
            if (!motherboard.isEmpty()) комплектующие.add(motherboard);
            if (!caseComponent.isEmpty()) комплектующие.add(caseComponent);
            if (!powerSupply.isEmpty()) комплектующие.add(powerSupply);

            if (Numbers.isEmpty() || cpu.isEmpty() || gpu.isEmpty() || motherboard.isEmpty() || caseComponent.isEmpty() || powerSupply.isEmpty() || selectedResponsible.isEmpty()) {
                Toast.makeText(getContext(), "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                return;
            }

            // Получение userId текущего пользователя
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = currentUser != null ? currentUser.getUid() : null;

            if (userId != null) {
                // Создание списка комплектующих
                List<String> components = new ArrayList<>();
                components.add(cpu);
                components.add(gpu);
                components.add(motherboard);
                components.add(caseComponent);
                components.add(powerSupply);

                // Генерация уникального ID для сборки
                String equipmentId = UUID.randomUUID().toString();

                // Создание объекта Equipment
                Equipment equipment = new Equipment(equipmentId, Numbers , selectedResponsible, userId, components);

                // Сохранение в Firebase
                DatabaseReference equipmentRef = FirebaseDatabase.getInstance().getReference("equipment");
                equipmentRef.child(equipmentId).setValue(equipment)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Сборка сохранена", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Ошибка при сохранении сборки", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            }
        });
        // Получение ссылки на базу данных Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("responsiblePersons");

        // Загрузка данных для списка ответственных
        loadResponsiblePeople(responsibleSpinner);

        return root;
    }

    private void checkAndAddItemToInventory(String category, String itemName) {
        if (!itemName.isEmpty()) {
            // Проверка, есть ли такой компонент на складе
            databaseReference = FirebaseDatabase.getInstance().getReference("Склад");
            databaseReference.orderByChild("название").equalTo(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Если компонента нет в базе, добавляем его с количеством 0
                        String itemId = UUID.randomUUID().toString(); // Генерация уникального ID
                        InventoryItem newItem = new InventoryItem(itemId, itemName, category, 0); // Количество = 0

                        // Сохранение объекта в Firebase
                        databaseReference.child(itemId).setValue(newItem);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Ошибка загрузки данных: " + error.getMessage());
                }
            });
        }
    }


    private void loadResponsiblePeople(Spinner responsibleSpinner) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> responsibleList = new ArrayList<>();
                for (DataSnapshot responsibleSnapshot : snapshot.getChildren()) {
                    ResponsiblePerson responsiblePerson = responsibleSnapshot.getValue(ResponsiblePerson.class);
                    if (responsiblePerson != null) {
                        responsibleList.add(responsiblePerson.getОтветственный());
                    } else {
                        Log.e("Firebase", "Ответственный не найден");
                    }
                }

                // Установка данных в Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, responsibleList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                responsibleSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Ошибка загрузки данных: " + error.getMessage());
            }
        });
    }

    private void loadCategoryData(String category, AutoCompleteTextView autoCompleteTextView) {
        databaseReference.orderByChild("категория").equalTo(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> itemList = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                    if (item != null) {
                        itemList.add(item.getНазвание());
                    }
                }
                // Установка данных в AutoCompleteTextView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, itemList);
                autoCompleteTextView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
