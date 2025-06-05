package com.example.projeto_lummora;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {
    private Context context;
    private List<Timer> timerList;
    private OnItemClickListener onItemClickListener;

    public TimerAdapter(Context context, List<Timer> timerList) {
        this.context = context;
        this.timerList = timerList;
    }

    @Override
    public TimerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_timer, parent, false);
        return new TimerViewHolder(view);
    }

    // um botão, dois text
    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        Timer timer = timerList.get(position);

        String digitosHora = String.valueOf(timer.getHora());
        String digitosMin = String.valueOf(timer.getMin());

        if(digitosHora.length() < 2)
            digitosHora = "0" + digitosHora;

        if(digitosMin.length() < 2)
            digitosMin = "0" + digitosMin;

        String t = digitosHora + ":" + digitosMin;

        holder.tituloMateria.setText(timer.getTitulo());
        holder.tempo.setText(t);


    }

    @Override
    public int getItemCount() { return timerList.size(); }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Timer timer);
    }

    public static class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView tituloMateria, tempo;

        public TimerViewHolder(View itemView) {
            super(itemView);
            tituloMateria = itemView.findViewById(R.id.tituloMateria);
            tempo = itemView.findViewById(R.id.tempo);
        }
    }


}
