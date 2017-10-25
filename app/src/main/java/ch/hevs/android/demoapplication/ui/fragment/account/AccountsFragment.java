package ch.hevs.android.demoapplication.ui.fragment.account;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.adapter.RecyclerAdapter;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;
import ch.hevs.android.demoapplication.viewmodel.AccountListViewModel;

/**
 * A fragment representing a list of Items.
 */
public class AccountsFragment extends Fragment {

    private static final String TAG = "AccountsFragment";

    private List<AccountEntity> accounts;
    private RecyclerView recyclerView;
    private AccountListViewModel viewModel;

    public AccountsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.accounts_fragment_title));
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String user = settings.getString(MainActivity.PREFS_USER, null);

        if (user == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        AccountListViewModel.Factory factory = new AccountListViewModel.Factory(
                getActivity().getApplication(), user);
        viewModel = ViewModelProviders.of(this, factory).get(AccountListViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.accountsRecyclerView);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, EditAccountFragment.newInstance(null), "CreateAccount")
                        .addToBackStack("accounts")
                        .commit();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (recyclerView != null) {
            SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
            String user = settings.getString(MainActivity.PREFS_USER, null);
            observeViewModel(viewModel);
            if (accounts == null) {
                accounts = new ArrayList<>();
            }
            recyclerView.setAdapter(new RecyclerAdapter<>(accounts, new RecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    Log.d(TAG, "clicked position:" + position);
                    Log.d(TAG, "clicked on: " + accounts.get(position).getName());

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.flContent, AccountFragment.newInstance(accounts.get(position)), "AccountDetails")
                            .addToBackStack("accounts")
                            .commit();
                }

                @Override
                public void onItemLongClick(View v, int position) {
                    Log.d(TAG, "longClicked position:" + position);
                    Log.d(TAG, "longClicked on: " + accounts.get(position).getName());

                    createDeleteDialog(position);
                }
            }));
        }
    }

    private void createDeleteDialog(final int position) {
        final AccountEntity account = accounts.get(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.row_delete_item, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(R.string.fragment_title_delete_account));
        alertDialog.setCancelable(false);

        final TextView deleteMessage = (TextView) view.findViewById(R.id.tv_delete_item);
        deleteMessage.setText(String.format(getString(R.string.account_delete_msg), account.getName()));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast toast = Toast.makeText(getContext(), getString(R.string.account_deleted), Toast.LENGTH_LONG);
                viewModel.deleteAccount(getContext(), account);
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

    private void observeViewModel(AccountListViewModel viewModel) {
        viewModel.getAccounts().observe(this, new Observer<List<AccountEntity>>() {
            @Override
            public void onChanged(@Nullable List<AccountEntity> accountEntities) {
                if (accountEntities != null) {
                    accounts = accountEntities;
                    ((RecyclerAdapter) recyclerView.getAdapter()).setData(accounts);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }
}
