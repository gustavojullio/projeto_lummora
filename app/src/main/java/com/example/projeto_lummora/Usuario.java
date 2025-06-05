package com.example.projeto_lummora;

public class Usuario {
    private String nome;
    private String celular;
    private String email;

    // Construtor vazio necessário para o Firebase
    public Usuario() {
    }

    // Construtor com parâmetros
    public Usuario(String nome, String celular, String email) {
        this.nome = nome;
        this.celular = celular;
        this.email = email;
    }

    // Getters e Setters necessários para o Firebase
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
