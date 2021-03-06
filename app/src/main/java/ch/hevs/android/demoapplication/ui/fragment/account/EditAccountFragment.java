package ch.hevs.android.demoapplication.ui.fragment.account;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.db.async.account.GetAccount;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.viewmodel.AccountListViewModel;

public class EditAccountFragment extends Fragment {

    private final String TAG = "EditAccountFragment";
    private static final String ARG_PARAM1 = "accountId";

    private AccountListViewModel mViewModel;
    private AccountEntity mAccount;
    private Long mAccountId;
    private String mUser;
    private boolean mEditMode;
    private Toast mToast;

    private EditText mEtAccountName;

    public EditAccountFragment() { }

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
            args.putLong(ARG_PARAM1, account.getId());
        } else {
            args.putLong(ARG_PARAM1, -1L);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mUser = settings.getString(MainActivity.PREFS_USER, null);
        mViewModel = ViewModelProviders.of(this).get(AccountListViewModel.class);
        observeViewModel(mViewModel);

        if (mUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        if (getArguments() != null) {
            mAccountId = getArguments().getLong(ARG_PARAM1);
            if (mAccountId == -1L) {
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
            try {
                mAccount = new GetAccount(getView()).execute(mAccountId).get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            populateForm();
        }
    }

    private void initializeForm() {
        mEtAccountName = (EditText) getActivity().findViewById(R.id.accountName);
        mEtAccountName.requestFocus();
        Button saveBtn = (Button) getActivity().findViewById(R.id.createAccountButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges(mEtAccountName.getText().toString());
                getActivity().onBackPressed();
                mToast.show();
            }
        });
    }

    private void populateForm() {
        mEtAccountName.setText(mAccount.getName());
    }

    private void saveChanges(String accountName) {
        if (mEditMode) {
            mAccount.setName(accountName);
            mViewModel.updateAccount(getView(), mAccount);
        } else {
            AccountEntity newAccount = new AccountEntity();
            newAccount.setOwner(mUser);
            newAccount.setBalance(0.0d);
            newAccount.setName(accountName);
            mViewModel.addAccount(getView(), newAccount);
        }
    }

    private void observeViewModel(AccountListViewModel viewModel) {
        viewModel.getAccounts().observe(this, new Observer<List<AccountEntity>>() {
            @Override
            public void onChanged(@Nullable List<AccountEntity> accountEntities) {}
        });
    }
}
