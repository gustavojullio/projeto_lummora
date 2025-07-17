package com.example.projeto_lummora;

public class Tarefa {
    private String id;
    private String nome;
    private String tipo;
    private long dataTimestamp;

    public Tarefa() {

    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public long getDataTimestamp() { return dataTimestamp; }
    public void setDataTimestamp(long dataTimestamp) { this.dataTimestamp = dataTimestamp; }
}