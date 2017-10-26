package ch.hevs.android.demoapplication.ui.fragment.account;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.db.async.account.GetAccount;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.viewmodel.AccountListViewModel;

public class AccountFragment extends Fragment {

    private final String TAG = "AccountFragment";
    private static final String ARG_PARAM1 = "accountId";

    private AccountListViewModel viewModel;
    private AccountEntity account;
    private TextView tvBalance;
    private NumberFormat defaultFormat;

    public AccountFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param account AccountEntity.
     * @return A new instance of fragment AccountFragment.
     */
    public static AccountFragment newInstance(AccountEntity account) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARAM1, account.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_account));

        if (getArguments() != null) {
            Long accountId = getArguments().getLong(ARG_PARAM1);
            try {
                account = new GetAccount(getContext()).execute(accountId).get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        viewModel = ViewModelProviders.of(this).get(AccountListViewModel.class);
        observeViewModel(viewModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (account != null) {
            tvBalance = (TextView) getActivity().findViewById(R.id.accBalance);
            defaultFormat = NumberFormat.getCurrencyInstance(MainActivity.getCurrentLocale(getContext()));
            tvBalance.setText(defaultFormat.format(account.getBalance()));

            Button depositBtn = (Button) getActivity().findViewById(R.id.depositButton);
            depositBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    generateDialog(R.string.action_deposit);
                }
            });

            Button withdrawBtn = (Button) getActivity().findViewById(R.id.withdrawButton);
            withdrawBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    generateDialog(R.string.action_withdraw);
                }
            });
            Log.i(TAG, "Form populated.");
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void generateDialog(final int action) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.account_actions, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(action));
        alertDialog.setCancelable(false);


        final EditText accountMovement = (EditText) view.findViewById(R.id.account_movement);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Double amount = Double.parseDouble(accountMovement.getText().toString());
                Toast toast = Toast.makeText(getContext(), getString(R.string.error_withdraw), Toast.LENGTH_LONG);

                if (action == R.string.action_withdraw) {
                    Log.i(TAG, "Withdrawal: " + amount.toString());
                    if (account.getBalance() < amount) {
                        toast.show();
                    } else {
                        account.setBalance(account.getBalance() - amount);
                        viewModel.updateAccount(getContext(), account);
                        tvBalance.setText(defaultFormat.format(account.getBalance()));
                    }
                }
                if (action == R.string.action_deposit) {
                    Log.i(TAG, "Deposit: " + amount.toString());
                    account.setBalance(account.getBalance() + amount);
                    viewModel.updateAccount(getContext(), account);
                    tvBalance.setText(defaultFormat.format(account.getBalance()));
                }
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
            public void onChanged(@Nullable List<AccountEntity> accountEntities) {}
        });
    }
}
