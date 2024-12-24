package com.example.betaforall.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.betaforall.MainActivity;
import com.example.betaforall.R;
import com.example.betaforall.model.Equipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView userEmailTextView;
    private Button logoutButton;
    private ListView orderListView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final int PICK_IMAGE_REQUEST = 1; // Код для выбора изображения
    private static final String AVATAR_FILE_NAME = "avatar_image.png"; // Имя файла для аватара

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Инициализация FirebaseAuth и DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("equipment");
        
        ImageButton userAvatarImageView = root.findViewById(R.id.choose_avatar_button);
        if (userAvatarImageView != null) {
            // Вставьте код для загрузки изображения
        } else {
            Log.e("DashboardFragment", "ImageButton не найден");
        }
        // Находим элементы интерфейса
        userEmailTextView = root.findViewById(R.id.user_email);
        logoutButton = root.findViewById(R.id.logout_button);
        orderListView = root.findViewById(R.id.order_list);
        ImageButton chooseAvatarButton = root.findViewById(R.id.choose_avatar_button); // Кнопка выбора аватарки

        // Получаем текущего пользователя
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Выводим email пользователя (для справки)
            userEmailTextView.setText(currentUser.getEmail());
            loadAllEquipment();
            loadAvatarImage(); // Загружаем аватарку
        }

        // Обработчик кнопки выхода
        logoutButton.setOnClickListener(v -> logoutUser());

        // Обработчик кнопки выбора аватарки
        chooseAvatarButton.setOnClickListener(v -> chooseImage());
      

        return root;
    }

    private void chooseImage() {
        // Открываем галерею устройства для выбора изображения
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            try {
                // Получаем изображение по URI
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

                // Подгоняем размер изображения
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, dpToPx(150), dpToPx(150), false);

                // Сохраняем изображение в Internal Storage
                saveImageToInternalStorage(resizedBitmap);

                // Устанавливаем изображение в ImageButton
                ImageButton userAvatarImageView = getView().findViewById(R.id.choose_avatar_button);
                userAvatarImageView.setImageBitmap(resizedBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для конвертации dp в пиксели
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void loadAllEquipment() {
        // Логируем начало выгрузки всех записей
        Log.d("DashboardFragment", "Загружаем все записи из таблицы equipment.");

        // Получаем все данные из базы (без фильтрации по пользователю)
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d("DashboardFragment", "Данные не найдены.");
                    return;
                }

                // Получаем текущего пользователя
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid(); // ID текущего пользователя

                    // Перебираем все записи
                    List<String> orders = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Equipment equipment = snapshot.getValue(Equipment.class);
                        if (equipment != null) {
                            // Логируем найденные данные для отладки
                            Log.d("DashboardFragment", "Инвентарный номер: " + equipment.getИнвентарныйНомер());
                            Log.d("DashboardFragment", "Ответственный: " + equipment.getОтветственный());
                            Log.d("DashboardFragment", "Комплектующие: " + String.join(", ", equipment.getКомплектующие()));
                            Log.d("DashboardFragment", "Пользователь ID: " + equipment.getПользовательId());

                            // Фильтруем по ID пользователя
                            if (userId.equals(equipment.getПользовательId())) {
                                // Добавляем заказ в список, если пользователь совпадает
                                orders.add("Инвентарный номер: " + equipment.getИнвентарныйНомер() +
                                        "\nОтветственный: " + equipment.getОтветственный() +
                                        "\nКомплектующие: " + String.join(", ", equipment.getКомплектующие()));
                            }
                        }
                    }

                    // Если заказы для текущего пользователя не найдены, выводим сообщение в лог
                    if (orders.isEmpty()) {
                        Log.d("DashboardFragment", "Заказы для пользователя не найдены.");
                    }

                    // Отображаем отфильтрованные заказы в ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, orders);
                    orderListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Если ошибка при загрузке данных, выводим сообщение об ошибке
                Toast.makeText(getActivity(), "Ошибка при загрузке данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DashboardFragment", "Ошибка загрузки данных: " + databaseError.getMessage());
            }
        });
    }

    private void logoutUser() {
        // Выход из Firebase
        mAuth.signOut();

        // Показать сообщение о выходе
        Toast.makeText(getActivity(), "Выход из системы", Toast.LENGTH_SHORT).show();

        // Перенаправление на экран входа
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    // Метод для сохранения изображения в Internal Storage
    private void saveImageToInternalStorage(Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            // Получаем файл для сохранения изображения
            File file = new File(getActivity().getFilesDir(), AVATAR_FILE_NAME);
            fos = new FileOutputStream(file);
            // Сохраняем изображение в файл
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.d("DashboardFragment", "Изображение успешно сохранено в Internal Storage");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Метод для загрузки изображения из Internal Storage
    private void loadAvatarImage() {
        // Убедимся, что фрагмент уже был создан
        if (getView() != null) {
            ImageButton userAvatarImageView = getView().findViewById(R.id.choose_avatar_button);
            if (userAvatarImageView != null) {
                // Ваш код для загрузки изображения
            }
        } else {
            Log.e("DashboardFragment", "getView() возвращает null");
        }
    }

}
