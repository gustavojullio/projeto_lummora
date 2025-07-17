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

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.LivroViewHolder> {
    private Context context;
    private List<Livro> livroList;
    private OnLivroClickListener onLivroClickListener;
    private String runningLivroId = null;

    public LivroAdapter(Context context, List<Livro> livroList) {
        this.context = context;
        this.livroList = livroList;
    }

    public void setOnLivroClickListener(OnLivroClickListener listener) {
        this.onLivroClickListener = listener;
    }

    public void setRunningLivroId(String id) {
        this.runningLivroId = id;
    }

    @NonNull
    @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_livro, parent, false);
        return new LivroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivroViewHolder holder, int position) {
        Livro livro = livroList.get(position);
        holder.tituloLivro.setText(livro.getTitulo());

        long seconds = livro.getTempoTotalSegundos();
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(seconds),
                TimeUnit.SECONDS.toMinutes(seconds) % 60,
                seconds % 60);
        holder.tempoLeitura.setText(timeFormatted);

        if (livro.getId().equals(runningLivroId)) {
            holder.btnIniciarLeitura.setImageResource(R.drawable.pause);
        } else {
            holder.btnIniciarLeitura.setImageResource(R.drawable.play);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onLivroClickListener != null) {
                onLivroClickListener.onItemClick(livro);
            }
        });

        holder.btnIniciarLeitura.setOnClickListener(v -> {
            if (onLivroClickListener != null) {
                onLivroClickListener.onPlayClick(livro, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return livroList.size();
    }

    public interface OnLivroClickListener {
        void onItemClick(Livro livro);
        void onPlayClick(Livro livro, int position);
    }

    public static class LivroViewHolder extends RecyclerView.ViewHolder {
        TextView tituloLivro, tempoLeitura;
        ImageButton btnIniciarLeitura;

        public LivroViewHolder(View itemView) {
            super(itemView);
            tituloLivro = itemView.findViewById(R.id.tituloLivro);
            tempoLeitura = itemView.findViewById(R.id.tempoLeitura);
            btnIniciarLeitura = itemView.findViewById(R.id.btnIniciarLeitura);
        }
    }
}