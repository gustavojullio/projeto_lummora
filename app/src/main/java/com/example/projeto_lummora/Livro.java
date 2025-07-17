package com.example.projeto_lummora;

import java.util.HashMap;
import java.util.Map;

public class Livro {
    private String id;
    private String titulo;
    private long tempoTotalSegundos;
    // NOVO CAMPO ADICIONADO
    private Map<String, Long> historicoDiario;

    public Livro() {
        // Inicializa o mapa para evitar erros
        this.historicoDiario = new HashMap<>();
    }

    public Livro(String id, String titulo) {
        this.id = id;
        this.titulo = titulo;
        this.tempoTotalSegundos = 0;
        this.historicoDiario = new HashMap<>();
    }

    // Getters e Setters existentes...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public long getTempoTotalSegundos() { return tempoTotalSegundos; }
    public void setTempoTotalSegundos(long tempoTotalSegundos) { this.tempoTotalSegundos = tempoTotalSegundos; }

    // NOVO GETTER E SETTER
    public Map<String, Long> getHistoricoDiario() {
        return historicoDiario;
    }
    public void setHistoricoDiario(Map<String, Long> historicoDiario) {
        this.historicoDiario = historicoDiario;
    }
}