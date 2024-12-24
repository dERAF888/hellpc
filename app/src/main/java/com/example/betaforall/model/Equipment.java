package com.example.betaforall.model;

import java.util.List;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.List;

public class Equipment {
    private String инвентарныйНомер;
    private String ответственный;
    private String пользовательId;
    private List<String> комплектующие; // Новое поле для списка комплектующих

    // Конструктор по умолчанию
    public Equipment() {}

    // Конструктор с параметрами
    public Equipment(String id, String инвентарныйНомер, String ответственный, String пользовательId, List<String> комплектующие) {
        this.инвентарныйНомер = инвентарныйНомер;
        this.ответственный = ответственный;
        this.пользовательId = пользовательId;
        this.комплектующие = комплектующие; // Инициализация нового поля
    }



    public String getИнвентарныйНомер() {
        return инвентарныйНомер;
    }

    public void setИнвентарныйНомер(String инвентарныйНомер) {
        this.инвентарныйНомер = инвентарныйНомер;
    }

    public String getОтветственный() {
        return ответственный;
    }

    public void setОтветственный(String ответственный) {
        this.ответственный = ответственный;
    }

    public String getПользовательId() {
        return пользовательId;
    }

    public void setПользовательId(String пользовательId) {
        this.пользовательId = пользовательId;
    }

    public List<String> getКомплектующие() {
        return комплектующие;
    }

    public void setКомплектующие(List<String> комплектующие) {
        this.комплектующие = комплектующие;
    }
}
