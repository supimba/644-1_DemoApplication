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

    private final String TAG = getClass().getSimpleName();

    private ClientEntity loggedIn;
    private AccountEntity fromAccount;
    private AccountEntity toAccount;

    private List<AccountEntity> clientAccounts;
    private List<AccountEntity> ownAccounts;
    private List<ClientEntity> clients;

    private Spinner fromAccountSpinner;
    private Spinner toClientSpinner;
    private Spinner toAccountSpinner;

    private ListAdapter<AccountEntity> fromAccountAdapter;
    private ListAdapter<ClientEntity> clientAdapter;
    private ListAdapter<AccountEntity> toAccountAdapter;

    public TransactionFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_transaction));

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String user = settings.getString(MainActivity.PREFS_USER, null);
        if (user == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        try {
            loggedIn = new GetClient(getContext()).execute(user).get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
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
        if (loggedIn != null) {
            populateForm();
            Log.i(TAG, "Form populated.");
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void populateForm() {
        try {
            ownAccounts = new GetOwnAccounts(getContext()).execute(loggedIn.getEmail()).get();
            clients = new GetClients(getContext()).execute().get();
            clients.remove(loggedIn);
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getEmail().equals(loggedIn.getEmail())) {
                    clients.remove(i);
                    break;
                }
            }

            toClientSpinner = (Spinner) getView().findViewById(R.id.spinner_toClient);
            clientAdapter = new ListAdapter<>(getContext(), R.layout.row_client, clients);
            toClientSpinner.setAdapter(clientAdapter);
            toClientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    populateToAccount((ClientEntity) parent.getItemAtPosition(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            fromAccountSpinner = (Spinner) getView().findViewById(R.id.spinner_from);
            fromAccountAdapter = new ListAdapter<>(getContext(), R.layout.row_client, ownAccounts);
            fromAccountSpinner.setAdapter(fromAccountAdapter);
            fromAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    fromAccount = (AccountEntity) parent.getItemAtPosition(position);
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
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void populateToAccount(ClientEntity recipient) {
        try {
            clientAccounts = new GetOwnAccounts(getContext()).execute(recipient.getEmail()).get();
            toAccountSpinner = (Spinner) getView().findViewById(R.id.spinner_toAcc);
            toAccountAdapter = new ListAdapter<>(getContext(), R.layout.row_client, clientAccounts);
            toAccountSpinner.setAdapter(toAccountAdapter);
            toAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    toAccount = (AccountEntity) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
        } catch (Exception e) {
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
        if (fromAccount.getBalance() - amount < 0.0d) {
            amountEditText.setError(getString(R.string.error_transaction));
            amountEditText.requestFocus();
            return;
        }
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        new TransactionAccount(getContext()).execute(Pair.create(fromAccount, toAccount));
    }
}
