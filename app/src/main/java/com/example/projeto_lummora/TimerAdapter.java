package com.example.projeto_lummora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {
    private Context context;
    private List<Disciplina> disciplinaList;
    private OnDisciplinaClickListener onDisciplinaClickListener;
    private String runningDisciplinaId = null;


    public TimerAdapter(Context context, List<Disciplina> disciplinaList) {
        this.context = context;
        this.disciplinaList = disciplinaList;
    }

    public void setOnDisciplinaClickListener(OnDisciplinaClickListener listener) {
        this.onDisciplinaClickListener = listener;
    }

    public void setRunningDisciplinaId(String id) {
        this.runningDisciplinaId = id;
    }


    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_timer, parent, false);
        return new TimerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        Disciplina disciplina = disciplinaList.get(position);
        holder.tituloMateria.setText(disciplina.getTitulo());

        // Formata o tempo de segundos para HH:MM:SS
        long seconds = disciplina.getTempoTotalSegundos();
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(seconds),
                TimeUnit.SECONDS.toMinutes(seconds) % 60,
                seconds % 60);

        holder.tempo.setText(timeFormatted);

        // Altera o ícone do botão play/pause
        if (disciplina.getId().equals(runningDisciplinaId)) {
            holder.btnIniciarTimer.setImageResource(R.drawable.pause);
        } else {
            holder.btnIniciarTimer.setImageResource(R.drawable.play);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onDisciplinaClickListener != null) {
                onDisciplinaClickListener.onItemClick(disciplina);
            }
        });

        holder.btnIniciarTimer.setOnClickListener(v -> {
            if (onDisciplinaClickListener != null) {
                onDisciplinaClickListener.onPlayClick(disciplina, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return disciplinaList.size();
    }


    public interface OnDisciplinaClickListener {
        void onItemClick(Disciplina disciplina);
        void onPlayClick(Disciplina disciplina, int position);
    }

    public static class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView tituloMateria, tempo;
        ImageButton btnIniciarTimer;

        public TimerViewHolder(View itemView) {
            super(itemView);
            tituloMateria = itemView.findViewById(R.id.tituloMateria);
            tempo = itemView.findViewById(R.id.tempo);
            btnIniciarTimer = itemView.findViewById(R.id.btnIniciarTimer);
        }
    }
}