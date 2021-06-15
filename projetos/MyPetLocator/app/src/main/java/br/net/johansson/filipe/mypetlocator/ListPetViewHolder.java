package br.net.johansson.filipe.mypetlocator;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ListPetViewHolder extends RecyclerView.ViewHolder {

    public TextView txtNomePet;
    public ListPetViewHolder(View itemView) {
        super(itemView);
        txtNomePet = (TextView) itemView.findViewById(R.id.txtNomePet);
    }

}
