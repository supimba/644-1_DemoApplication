package ch.hevs.android.demoapplication.adapter;

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
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.entity.ClientEntity;

public class ListAdapter<T> extends ArrayAdapter<T> {

    private int mResource;
    private List<T> mData = new ArrayList<>();

    public ListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<T> data) {
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
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        return getCustomView(position, convertView, parent);
    }

    public T getItem(int position) {
        return mData.get(position);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        ListAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(mResource, parent, false);

            viewHolder = new ListAdapter.ViewHolder();
            viewHolder.itemView = convertView.findViewById(R.id.tvClientView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ListAdapter.ViewHolder) convertView.getTag();
        }
        T item = getItem(position);
        if (item != null) {
            if (item.getClass().equals(AccountEntity.class))
                viewHolder.itemView.setText(((AccountEntity) item).getName());
            if (item.getClass().equals(ClientEntity.class))
                viewHolder.itemView.setText(((ClientEntity) item).getFirstName() + " " + ((ClientEntity) item).getLastName());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView itemView;
    }
}
