package com.example.betaforall.model;

public class InventoryItem {
    private String id;
    private String название;
    private String категория;
    private int количество;

    // Конструктор по умолчанию (обязательно для Firebase)
    public InventoryItem() {}

    // Конструктор с параметрами
    public InventoryItem(String id, String название, String категория, int количество) {
        this.id = id;
        this.название = название;
        this.категория = категория;
        this.количество = количество;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getНазвание() {
        return название;
    }

    public void setНазвание(String название) {
        this.название = название;
    }

    public String getКатегория() {
        return категория;
    }

    public void setКатегория(String категория) {
        this.категория = категория;
    }

    public int getКоличество() {
        return количество;
    }

    public void setКоличество(int количество) {
        this.количество = количество;
    }
}
