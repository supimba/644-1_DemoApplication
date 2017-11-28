package ch.hevs.android.demoapplication.ui.fragment.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.adapter.account.AccountRecyclerAdapter;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;

/**
 * A fragment representing a list of Items.
 */
public class AccountsFragment extends Fragment {

    private static final String TAG = "AccountsFragment";

    private List<AccountEntity> mAccounts;
    private RecyclerView mRecyclerView;
    private AccountRecyclerAdapter mAdapter;

    public AccountsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.accounts_fragment_title));
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        mAccounts = new ArrayList<>();
        mAdapter = new AccountRecyclerAdapter(new RecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Log.d(TAG, "clicked position:" + position);
                Log.d(TAG, "clicked on: " + mAccounts.get(position).getName());

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, AccountFragment.newInstance(mAccounts.get(position)), "AccountDetails")
                        .addToBackStack("mAccounts")
                        .commit();
            }

            @Override
            public void onItemLongClick(View v, int position) {
                Log.d(TAG, "longClicked position:" + position);
                Log.d(TAG, "longClicked on: " + mAccounts.get(position).getName());
                createDeleteDialog(position);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.accounts_fragment_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts_list, container, false);

        mRecyclerView = view.findViewById(R.id.accountsRecyclerView);
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, EditAccountFragment.newInstance(null), "CreateAccount")
                        .addToBackStack("mAccounts")
                        .commit();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mAdapter);
            updateAccounts();
        }
    }

    private void createDeleteDialog(final int position) {
        final AccountEntity account = mAccounts.get(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.row_delete_item, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(R.string.fragment_title_delete_account));
        alertDialog.setCancelable(false);

        final TextView deleteMessage = view.findViewById(R.id.tv_delete_item);
        deleteMessage.setText(String.format(getString(R.string.account_delete_msg), account.getName()));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast toast = Toast.makeText(getContext(), getString(R.string.account_deleted), Toast.LENGTH_LONG);
                mAdapter.deleteAccount(account);
                toast.show();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(view);
        alertDialog.show();
    }

    private void updateAccounts() {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("accounts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mAccounts.clear();
                            mAccounts.addAll(toAccounts(dataSnapshot));
                            mAdapter.updateData(mAccounts);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                    }
                });
    }

    private List<AccountEntity> toAccounts(DataSnapshot snapshot) {
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<AccountEntity> accounts = new ArrayList<>();
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            AccountEntity entity = childSnapshot.getValue(AccountEntity.class);
            entity.setId(childSnapshot.getKey());
            entity.setOwner(user);
            accounts.add(entity);
        }
        return accounts;
    }
}
