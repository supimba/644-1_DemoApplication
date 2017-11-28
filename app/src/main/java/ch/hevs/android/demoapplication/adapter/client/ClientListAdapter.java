package ch.hevs.android.demoapplication.adapter.client;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.ClientEntity;

public class ClientListAdapter extends ArrayAdapter<ClientEntity> {

    private int mResource;
    private List<ClientEntity> mData = new ArrayList<>();

    public ClientListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ClientEntity> data) {
        super(context, resource, data);
        mResource = resource;
        mData = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public ClientEntity getItem(int position) {
        return mData.get(position);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        ch.hevs.android.demoapplication.adapter.client.ClientListAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(mResource, parent, false);

            viewHolder = new ch.hevs.android.demoapplication.adapter.client.ClientListAdapter.ViewHolder();
            viewHolder.itemView = convertView.findViewById(R.id.tvClientView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ch.hevs.android.demoapplication.adapter.client.ClientListAdapter.ViewHolder) convertView.getTag();
        }
        ClientEntity item = getItem(position);
        if (item != null) {
            viewHolder.itemView.setText(item.getFirstName() + " " + item.getLastName());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView itemView;
    }
}
