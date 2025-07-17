package com.example.projeto_lummora;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> items;
    private final Context context;
    private OnTarefaClickListener listener;

    private static final int TIPO_MES = 0;
    private static final int TIPO_TAREFA = 1;

    public interface OnTarefaClickListener {
        void onTarefaClick(Tarefa tarefa);
    }

    public AgendaAdapter(Context context, List<Object> items, OnTarefaClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TIPO_MES;
        } else {
            return TIPO_TAREFA;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TIPO_MES) {
            View view = inflater.inflate(R.layout.item_mes_header, parent, false);
            return new MesHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_tarefa, parent, false);
            return new TarefaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TIPO_MES) {
            MesHeaderViewHolder headerHolder = (MesHeaderViewHolder) holder;
            headerHolder.bind((String) items.get(position));
        } else {
            TarefaViewHolder tarefaHolder = (TarefaViewHolder) holder;
            tarefaHolder.bind((Tarefa) items.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder para o Cabeçalho do Mês
    static class MesHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtMesAno;
        MesHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMesAno = itemView.findViewById(R.id.txtMesAno);
        }
        void bind(String mesAno) {
            txtMesAno.setText(mesAno);
        }
    }

    // ViewHolder para o Item da Tarefa
    static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView txtDiaDaSemana, txtDiaDoMes, txtNomeTarefa;
        TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDiaDaSemana = itemView.findViewById(R.id.txtDiaDaSemana);
            txtDiaDoMes = itemView.findViewById(R.id.txtDiaDoMes);
            txtNomeTarefa = itemView.findViewById(R.id.txtNomeTarefa);
        }

        void bind(final Tarefa tarefa, final OnTarefaClickListener listener) {
            Date data = new Date(tarefa.getDataTimestamp());
            Locale br = new Locale("pt", "BR");

            txtDiaDaSemana.setText(new SimpleDateFormat("E", br).format(data).replace(".", ""));
            txtDiaDoMes.setText(new SimpleDateFormat("dd", br).format(data));
            txtNomeTarefa.setText(tarefa.getNome());

            // Define a cor baseada no tipo da tarefa
            switch (tarefa.getTipo()) {
                case "Prova":
                    txtNomeTarefa.setBackgroundColor(Color.parseColor("#E53935")); // Vermelho
                    break;
                case "Trabalho":
                    txtNomeTarefa.setBackgroundColor(Color.parseColor("#FDD835")); // Amarelo
                    txtNomeTarefa.setTextColor(Color.BLACK);
                    break;
                case "Tarefa":
                default:
                    txtNomeTarefa.setBackgroundColor(Color.parseColor("#1E88E5")); // Azul
                    break;
            }

            itemView.setOnClickListener(v -> listener.onTarefaClick(tarefa));
        }
    }
}