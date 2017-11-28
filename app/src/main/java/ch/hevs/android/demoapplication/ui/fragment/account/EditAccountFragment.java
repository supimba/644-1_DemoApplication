package ch.hevs.android.demoapplication.ui.fragment.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;

public class EditAccountFragment extends Fragment {

    private final String TAG = "EditAccountFragment";
    private static final String ARG_PARAM1 = "accountId";

    private AccountEntity mAccount;
    private String mUserUid;
    private String mAccountId;
    private boolean mEditMode;
    private Toast mToast;

    private EditText mEtAccountName;

    public EditAccountFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param account AccountEntity.
     * @return A new instance of fragment EditAccountFragment.
     */
    public static EditAccountFragment newInstance(AccountEntity account) {
        EditAccountFragment fragment = new EditAccountFragment();
        Bundle args = new Bundle();

        if (account != null) {
            args.putString(ARG_PARAM1, account.getId());
        } else {
            args.putString(ARG_PARAM1, "");
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (mUserUid.isEmpty()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        if (getArguments() != null) {
            mAccountId = getArguments().getString(ARG_PARAM1);
            if (mAccountId == "") {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_create_account));
                mToast = Toast.makeText(getContext(), getString(R.string.account_created), Toast.LENGTH_LONG);
                mEditMode = false;
            } else {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_edit_account));
                mToast = Toast.makeText(getContext(), getString(R.string.account_edited), Toast.LENGTH_LONG);
                mEditMode = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeForm();
        if (mEditMode) {
            FirebaseDatabase.getInstance()
                    .getReference("clients")
                    .child(mUserUid)
                    .child("accounts")
                    .child(mAccountId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mAccount = dataSnapshot.getValue(AccountEntity.class);
                                populateForm();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                        }
                    });
        }
    }

    private void initializeForm() {
        mEtAccountName = getActivity().findViewById(R.id.accountName);
        mEtAccountName.requestFocus();
        Button saveBtn = getActivity().findViewById(R.id.createAccountButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges(mEtAccountName.getText().toString());
            }
        });
    }

    private void populateForm() {
        mEtAccountName.setText(mAccount.getName());
    }

    private void saveChanges(String accountName) {
        if (mEditMode) {
            mAccount.setName(accountName);
            updateAccount(mAccount);
        } else {
            AccountEntity newAccount = new AccountEntity();
            newAccount.setOwner(mUserUid);
            newAccount.setBalance(0.0d);
            newAccount.setName(accountName);
            addAccount(newAccount);
        }
    }

    private void addAccount(final AccountEntity account) {
        account.setId(UUID.randomUUID().toString());
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getId())
                .setValue(account, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Insert failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Insert successful!");
                            getActivity().onBackPressed();
                            mToast.show();
                        }
                    }
                });
    }

    private void updateAccount(final AccountEntity account) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getId())
                .updateChildren(account.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Update failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Update successful!");
                            getActivity().onBackPressed();
                            mToast.show();
                        }
                    }
                });
    }
}
