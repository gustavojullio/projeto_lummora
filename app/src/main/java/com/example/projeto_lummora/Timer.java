package com.example.projeto_lummora;

public class Timer {

    private int hora;
    private int min;
    private String titulo;

    public Timer(int h, int m, String t) throws Exception {
        setHora(h);
        setMin(m);
        setTitulo(t);
    }

    public void setHora(int h) throws Exception {
        if(h == 0)
            throw new Exception("A hora não pode ser zero");
        this.hora = h;
    }

    public int getHora() {
        return this.hora;
    }

    public void setMin(int m) throws Exception {
        this.min = m;
    }

    public int getMin() {
        return this.min;
    }

    public void setTitulo(String t) throws Exception {
        if(t.isEmpty())
            throw new Exception("O título da matéria não pode estar vazio.");
        this.titulo = t;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String toString() {
        return "Titulo: " + getTitulo() + " [" + getHora() + ":" + getMin() + "]";
    }

}
