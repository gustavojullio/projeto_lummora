package com.example.projeto_lummora;

public class Disciplina {
    private String id;
    private String titulo;
    private long tempoTotalSegundos;
    
    public Disciplina() {
    }

    public Disciplina(String id, String titulo) {
        this.id = id;
        this.titulo = titulo;
        this.tempoTotalSegundos = 0;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public long getTempoTotalSegundos() {
        return tempoTotalSegundos;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTempoTotalSegundos(long tempoTotalSegundos) {
        this.tempoTotalSegundos = tempoTotalSegundos;
    }
}