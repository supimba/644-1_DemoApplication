package ch.hevs.android.demoapplication.ui.fragment.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.adapter.ListAdapter;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;

public class TransactionFragment extends Fragment {

    private final String TAG = "TransactionFragment";

    private String mUserUid;
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

    public TransactionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_transaction));

        mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (mUserUid == null) {
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
        if (mUserUid.isEmpty()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } else {
            populateForm();
            Log.d(TAG, "Form populated.");
        }
    }

    private void populateForm() {
        mSpinnerToClient = getView().findViewById(R.id.spinner_toClient);
        mAdapterClient = new ListAdapter<>(getContext(), R.layout.row_client, new ArrayList<ClientEntity>());
        mSpinnerToClient.setAdapter(mAdapterClient);
        mSpinnerToClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateToAccount((ClientEntity) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mSpinnerFromAccount = getView().findViewById(R.id.spinner_from);
        mAdapterFromAccount = new ListAdapter<>(getContext(), R.layout.row_client, new ArrayList<AccountEntity>());
        mSpinnerFromAccount.setAdapter(mAdapterFromAccount);
        mSpinnerFromAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFromAccount = (AccountEntity) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(mUserUid)
                .child("accounts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mOwnAccounts.clear();
                            mOwnAccounts.addAll(toAccounts(dataSnapshot, mUserUid));
                            mAdapterAccount.updateData(mOwnAccounts);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                    }
                });

        FirebaseDatabase.getInstance()
                .getReference("clients")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (int i = 0; i < mClients.size(); i++) {
                                if (mClients.get(i).getUid().equals(mUserUid)) {
                                    mClients.remove(i);
                                    break;
                                }
                            }
                            mClients.clear();
                            mClients.addAll(toClients(dataSnapshot));
                            mAdapterClient.updateData(mClients);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                    }
                });


        final Toast toast = Toast.makeText(getContext(), getString(R.string.transaction_executed), Toast.LENGTH_LONG);
        Button transactionBtn = getActivity().findViewById(R.id.btn_transaction);
        transactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeTransaction(toast);
            }
        });
    }

    private void populateToAccount(final ClientEntity recipient) {
        mSpinnerAccount = getView().findViewById(R.id.spinner_toAcc);
        mAdapterAccount = new ListAdapter<>(getContext(), R.layout.row_client, new ArrayList<AccountEntity>());
        mSpinnerAccount.setAdapter(mAdapterAccount);
        mSpinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mToAccount = (AccountEntity) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(recipient.getUid())
                .child("accounts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mClientAccounts.clear();
                            mClientAccounts.addAll(toAccounts(dataSnapshot, recipient.getUid()));
                            mAdapterAccount.updateData(mClientAccounts);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                    }
                });
    }

    private void executeTransaction(final Toast toast) {
        EditText amountEditText = getActivity().findViewById(R.id.transaction_amount);
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

        final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                rootReference
                        .child("clients")
                        .child(mFromAccount.getOwner())
                        .child("accounts")
                        .child(mFromAccount.getUid())
                        .updateChildren(mFromAccount.toMap());

                rootReference
                        .child("clients")
                        .child(mToAccount.getOwner())
                        .child("accounts")
                        .child(mToAccount.getUid())
                        .updateChildren(mToAccount.toMap());
                return null;
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.d(TAG, "Transaction failure!", databaseError.toException());
                    toast.setText("Transaction failed!");
                } else {
                    Log.d(TAG, "Transaction successful!");
                }
                toast.show();
            }
        });
    }

    private List<AccountEntity> toAccounts(DataSnapshot snapshot, String clientUid) {
        List<AccountEntity> accounts = new ArrayList<>();
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            AccountEntity entity = childSnapshot.getValue(AccountEntity.class);
            entity.setUid(childSnapshot.getKey());
            entity.setOwner(clientUid);
            accounts.add(entity);
        }
        return accounts;
    }

    private List<ClientEntity> toClients(DataSnapshot snapshot) {
        List<ClientEntity> clients = new ArrayList<>();
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            ClientEntity entity = childSnapshot.getValue(ClientEntity.class);
            entity.setUid(childSnapshot.getKey());
            clients.add(entity);
        }
        return clients;
    }
}
