package com.example.betaforall.model;

public class ResponsiblePerson {
    private String ответственный;  // Поле совпадает с ключом в Firebase

    // Конструктор по умолчанию (обязателен для Firebase)
    public ResponsiblePerson() {}

    // Конструктор с параметром
    public ResponsiblePerson(String ответственный) {
        this.ответственный = ответственный;
    }

    // Геттер для "ответственный"
    public String getОтветственный() {  // Измени имя метода
        return ответственный;
    }

    // Сеттер для "ответственный"
    public void setОтветственный(String ответственный) {
        this.ответственный = ответственный;
    }
}
