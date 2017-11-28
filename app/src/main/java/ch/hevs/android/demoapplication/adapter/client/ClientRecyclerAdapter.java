package ch.hevs.android.demoapplication.adapter.client;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.ClientEntity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;

public class ClientRecyclerAdapter extends RecyclerView.Adapter<ch.hevs.android.demoapplication.adapter.client.ClientRecyclerAdapter.ViewHolder> {

    private List<ClientEntity> mData;
    private RecyclerViewItemClickListener mListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mTextView;

        ViewHolder(TextView textView) {
            super(textView);
            mTextView = textView;
        }
    }

    public ClientRecyclerAdapter(List<ClientEntity> data, RecyclerViewItemClickListener listener) {
        mData = data;
        mListener = listener;
    }

    @Override
    public ch.hevs.android.demoapplication.adapter.client.ClientRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view, parent, false);
        final ch.hevs.android.demoapplication.adapter.client.ClientRecyclerAdapter.ViewHolder viewHolder = new ch.hevs.android.demoapplication.adapter.client.ClientRecyclerAdapter.ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(view, viewHolder.getAdapterPosition());
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.onItemLongClick(view, viewHolder.getAdapterPosition());
                return true;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ch.hevs.android.demoapplication.adapter.client.ClientRecyclerAdapter.ViewHolder holder, int position) {
        ClientEntity item = mData.get(position);
        holder.mTextView.setText(item.getFirstName() + " " + item.getLastName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(final List<ClientEntity> data) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mData.size();
            }

            @Override
            public int getNewListSize() {
                return data.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return (mData.get(oldItemPosition)).getId().equals(
                        (data.get(newItemPosition)).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ClientEntity newClient = data.get(newItemPosition);
                ClientEntity oldClient = mData.get(newItemPosition);
                return Objects.equals(newClient.getId(), oldClient.getId())
                        && Objects.equals(newClient.getFirstName(), oldClient.getFirstName())
                        && Objects.equals(newClient.getLastName(), oldClient.getLastName())
                        && newClient.getPassword() == oldClient.getPassword()
                        && newClient.getAdmin() == oldClient.getAdmin();
            }
        });
        mData = data;
        result.dispatchUpdatesTo(this);
    }
}
