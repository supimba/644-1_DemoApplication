package ch.hevs.android.demoapplication.adapter.account;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;

public class AccountRecyclerAdapter extends RecyclerView.Adapter<ch.hevs.android.demoapplication.adapter.account.AccountRecyclerAdapter.ViewHolder> {

    public static final String TAG = "AccountRecyclerAdapter";

    private List<AccountEntity> mAccounts;
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

    public AccountRecyclerAdapter(RecyclerViewItemClickListener listener) {
        mAccounts = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public ch.hevs.android.demoapplication.adapter.account.AccountRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view, parent, false);
        final ch.hevs.android.demoapplication.adapter.account.AccountRecyclerAdapter.ViewHolder viewHolder = new ch.hevs.android.demoapplication.adapter.account.AccountRecyclerAdapter.ViewHolder(v);
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
    public void onBindViewHolder(ch.hevs.android.demoapplication.adapter.account.AccountRecyclerAdapter.ViewHolder holder, int position) {
        AccountEntity item = mAccounts.get(position);
        holder.mTextView.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return mAccounts.size();
    }

    public void setData(final List<AccountEntity> data) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mAccounts.size();
            }

            @Override
            public int getNewListSize() {
                return data.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return (mAccounts.get(oldItemPosition).getId() ==
                        data.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                AccountEntity newAccount = data.get(newItemPosition);
                AccountEntity oldAccount = mAccounts.get(newItemPosition);
                return newAccount.getId() == oldAccount.getId()
                        && Objects.equals(newAccount.getName(), oldAccount.getName())
                        && Objects.equals(newAccount.getBalance(), oldAccount.getBalance());
            }
        });
        mAccounts = data;
        result.dispatchUpdatesTo(this);
    }

    public void updateData(List<AccountEntity> accounts) {
        mAccounts.clear();
        mAccounts.addAll(accounts);
        notifyDataSetChanged();
    }

    public void deleteAccount(final AccountEntity account) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getId())
                .removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Delete failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Delete successful!");
                            mAccounts.remove(account);
                            notifyDataSetChanged();
                        }
                    }
                });
    }
}
