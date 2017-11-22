package ch.hevs.android.demoapplication.ui.fragment.account;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.adapter.RecyclerAdapter;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;
import ch.hevs.android.demoapplication.viewmodel.AccountListViewModel;

/**
 * A fragment representing a list of Items.
 */
public class AccountsFragment extends Fragment {

    private static final String TAG = "AccountsFragment";

    private List<AccountEntity> mAccounts;
    private RecyclerView mRecyclerView;
    private AccountListViewModel mViewModel;

    public AccountsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.accounts_fragment_title));
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (user == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        AccountListViewModel.Factory factory = new AccountListViewModel.Factory(
                getActivity().getApplication(), user);
        mViewModel = ViewModelProviders.of(this, factory).get(AccountListViewModel.class);
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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.accountsRecyclerView);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
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
            String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
            observeViewModel(mViewModel);
            if (mAccounts == null) {
                mAccounts = new ArrayList<>();
            }
            mRecyclerView.setAdapter(new RecyclerAdapter<>(mAccounts, new RecyclerViewItemClickListener() {
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
            }));
        }
    }

    private void createDeleteDialog(final int position) {
        final AccountEntity account = mAccounts.get(position);
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
                mViewModel.deleteAccount(getView(), account);
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
                    mAccounts = accountEntities;
                    ((RecyclerAdapter) mRecyclerView.getAdapter()).setData(mAccounts);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }
}
