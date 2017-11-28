package ch.hevs.android.demoapplication.ui.fragment.account;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;

public class AccountFragment extends Fragment {

    private final String TAG = "AccountFragment";
    private static final String ARG_PARAM1 = "accountId";

    private AccountEntity mAccount;
    private String mUser;
    private String mAccountId;
    private TextView mTvBalance;
    private NumberFormat mDefaultFormat;

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
        args.putString(ARG_PARAM1, account.getUid());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_account));
        mUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (mUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        if (getArguments() != null) {
            mAccountId = getArguments().getString(ARG_PARAM1);
        }
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
        mTvBalance = getActivity().findViewById(R.id.accBalance);
        mTvBalance.setVisibility(View.INVISIBLE);
        if (mAccountId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("clients")
                    .child(mUser)
                    .child("accounts")
                    .child(mAccountId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                mAccount = dataSnapshot.getValue(AccountEntity.class);
                                mAccount.setUid(mAccountId);
                                mAccount.setOwner(mUser);
                                initiateView();
                            } else {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getAccount: onCancelled", databaseError.toException());
                        }
                    });
        }
    }

    private void generateDialog(final int action) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.account_actions, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(action));
        alertDialog.setCancelable(false);


        final EditText accountMovement = view.findViewById(R.id.account_movement);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Double amount = Double.parseDouble(accountMovement.getText().toString());
                Toast toast = Toast.makeText(getContext(), getString(R.string.error_withdraw), Toast.LENGTH_LONG);

                if (action == R.string.action_withdraw) {
                    Log.d(TAG, "Withdrawal: " + amount.toString());
                    if (mAccount.getBalance() < amount) {
                        toast.show();
                    } else {
                        mAccount.setBalance(mAccount.getBalance() - amount);
                        updateAccount(mAccount);
                    }
                }
                if (action == R.string.action_deposit) {
                    Log.d(TAG, "Deposit: " + amount.toString());
                    mAccount.setBalance(mAccount.getBalance() + amount);
                    updateAccount(mAccount);
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

    private void initiateView() {
        ((MainActivity) getActivity()).setActionBarTitle(mAccount.getName());
        mDefaultFormat = NumberFormat.getCurrencyInstance(MainActivity.getCurrentLocale(getContext()));
        mTvBalance.setText(mDefaultFormat.format(mAccount.getBalance()));
        mTvBalance.setVisibility(View.VISIBLE);

        Button depositBtn = getActivity().findViewById(R.id.depositButton);
        depositBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDialog(R.string.action_deposit);
            }
        });

        Button withdrawBtn = getActivity().findViewById(R.id.withdrawButton);
        withdrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDialog(R.string.action_withdraw);
            }
        });
        Log.d(TAG, "Form populated.");
    }

    private void updateAccount(final AccountEntity account) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getUid())
                .updateChildren(account.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Update failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Update successful!");
                            mTvBalance.setText(mDefaultFormat.format(mAccount.getBalance()));
                        }
                    }
                });
    }
}
