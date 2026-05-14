package pa.ac.utp.agrotrackapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class adapterMortalidad extends RecyclerView.Adapter<adapterMortalidad.ViewHolder> {

    private List<String> listaIds; // Aquí iría tu modelo de datos real

    public adapterMortalidad(List<String> listaIds) {
        this.listaIds = listaIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // AQUÍ CONECTAMOS TU XML DE LA TARJETA
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_mortalidad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Aquí asignarías los datos reales, por ahora un ejemplo:
        holder.tvId.setText("ID: " + listaIds.get(position));
    }

    @Override
    public int getItemCount() {
        return listaIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvCausa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvTrazabilidad);
            tvCausa = itemView.findViewById(R.id.tvCausa);
        }
    }
}
