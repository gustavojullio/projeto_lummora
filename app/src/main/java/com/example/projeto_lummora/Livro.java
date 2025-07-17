package com.example.projeto_lummora;

public class Livro {
    private String id;
    private String titulo;
    private long tempoTotalSegundos;


    public Livro() {
    }

    public Livro(String id, String titulo) {
        this.id = id;
        this.titulo = titulo;
        this.tempoTotalSegundos = 0;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public long getTempoTotalSegundos() {
        return tempoTotalSegundos;
    }

    public void setTempoTotalSegundos(long tempoTotalSegundos) {
        this.tempoTotalSegundos = tempoTotalSegundos;
    }
}