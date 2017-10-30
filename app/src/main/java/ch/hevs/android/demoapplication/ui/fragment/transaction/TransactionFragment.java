package ch.hevs.android.demoapplication.ui.fragment.transaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.adapter.ListAdapter;
import ch.hevs.android.demoapplication.db.async.account.GetOwnAccounts;
import ch.hevs.android.demoapplication.db.async.account.TransactionAccount;
import ch.hevs.android.demoapplication.db.async.client.GetClient;
import ch.hevs.android.demoapplication.db.async.client.GetClients;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;

public class TransactionFragment extends Fragment {

    private final String TAG = "TransactionFragment";

    private String mUser;
    private ClientEntity mLoggedIn;
    private AccountEntity mFromAccount;
    private AccountEntity mToAccount;

    private List<AccountEntity> mClientAccounts;
    private List<AccountEntity> mOwnAccounts;
    private List<ClientEntity> mClients;

    private Spinner mSpinnerFromAccount;
    private Spinner mSpinnerToClient;
    private Spinner mSpinnerAccount;

    private ListAdapter<AccountEntity> mAdapterFromAccount;
    private ListAdapter<ClientEntity> mAdapterClient;
    private ListAdapter<AccountEntity> mAdapterAccount;

    public TransactionFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_transaction));

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mUser = settings.getString(MainActivity.PREFS_USER, null);
        if (mUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mLoggedIn = new GetClient(getView()).execute(mUser).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (mLoggedIn != null) {
            populateForm();
            Log.i(TAG, "Form populated.");
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void populateForm() {
        try {
            mOwnAccounts = new GetOwnAccounts(getView()).execute(mLoggedIn.getEmail()).get();
            mClients = new GetClients(getView()).execute().get();
            mClients.remove(mLoggedIn);
            for (int i = 0; i < mClients.size(); i++) {
                if (mClients.get(i).getEmail().equals(mLoggedIn.getEmail())) {
                    mClients.remove(i);
                    break;
                }
            }

            mSpinnerToClient = (Spinner) getView().findViewById(R.id.spinner_toClient);
            mAdapterClient = new ListAdapter<>(getContext(), R.layout.row_client, mClients);
            mSpinnerToClient.setAdapter(mAdapterClient);
            mSpinnerToClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    populateToAccount((ClientEntity) parent.getItemAtPosition(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            mSpinnerFromAccount = (Spinner) getView().findViewById(R.id.spinner_from);
            mAdapterFromAccount = new ListAdapter<>(getContext(), R.layout.row_client, mOwnAccounts);
            mSpinnerFromAccount.setAdapter(mAdapterFromAccount);
            mSpinnerFromAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mFromAccount = (AccountEntity) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
            final Toast toast = Toast.makeText(getContext(), getString(R.string.transaction_executed), Toast.LENGTH_LONG);
            Button transactionBtn = (Button) getActivity().findViewById(R.id.btn_transaction);
            transactionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    executeTransaction();
                    toast.show();
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void populateToAccount(ClientEntity recipient) {
        try {
            mClientAccounts = new GetOwnAccounts(getView()).execute(recipient.getEmail()).get();
            mSpinnerAccount = (Spinner) getView().findViewById(R.id.spinner_toAcc);
            mAdapterAccount = new ListAdapter<>(getContext(), R.layout.row_client, mClientAccounts);
            mSpinnerAccount.setAdapter(mAdapterAccount);
            mSpinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mToAccount = (AccountEntity) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void executeTransaction() {
        EditText amountEditText = (EditText) getActivity().findViewById(R.id.transaction_amount);
        Double amount = Double.parseDouble(amountEditText.getText().toString());
        if (amount < 0.0d) {
            amountEditText.setError(getString(R.string.error_transaction_negativ));
            amountEditText.requestFocus();
        }
        if (mFromAccount.getBalance() - amount < 0.0d) {
            amountEditText.setError(getString(R.string.error_transaction));
            amountEditText.requestFocus();
            return;
        }
        mFromAccount.setBalance(mFromAccount.getBalance() - amount);
        mToAccount.setBalance(mToAccount.getBalance() + amount);
        new TransactionAccount(getView()).execute(Pair.create(mFromAccount, mToAccount));
    }
}
