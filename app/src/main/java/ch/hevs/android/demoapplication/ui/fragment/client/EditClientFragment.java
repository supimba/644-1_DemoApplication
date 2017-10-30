package ch.hevs.android.demoapplication.ui.fragment.client;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.db.async.client.GetClient;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.viewmodel.ClientListViewModel;

public class EditClientFragment extends Fragment {

    private final String TAG = "EditClientFragment";
    private static final String ARG_PARAM1 = "clientEmail";

    private ClientListViewModel mViewModel;
    private ClientEntity mClient;
    private boolean mAdminMode;
    private boolean mEditMode;
    private Toast mToast;
    private String mClientEmail;

    private EditText mEtFirstName;
    private EditText mEtLastName;
    private EditText mEtEmail;
    private EditText mEtPwd1;
    private EditText mEtPwd2;
    private Switch mAdminSwitch;

    public EditClientFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param client ClientEntity.
     * @return A new instance of fragment EditClientFragment.
     */
    public static EditClientFragment newInstance(ClientEntity client) {
        EditClientFragment fragment = new EditClientFragment();
        Bundle args = new Bundle();

        if (client != null) {
            args.putString(ARG_PARAM1, client.getEmail());
        } else {
            args.putString(ARG_PARAM1, "create");
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mAdminMode = settings.getBoolean(MainActivity.PREFS_ADM, false);
        mViewModel = ViewModelProviders.of(this).get(ClientListViewModel.class);
        observeViewModel(mViewModel);

        if (getArguments() != null) {
            mClientEmail = getArguments().getString(ARG_PARAM1);
            if (mClientEmail.equals("create")) {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_create_client));
                mToast = Toast.makeText(getContext(), getString(R.string.client_created), Toast.LENGTH_LONG);
                mEditMode = false;
            } else {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_edit_client));
                mToast = Toast.makeText(getContext(), getString(R.string.client_edited), Toast.LENGTH_LONG);
                mEditMode = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_client, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeForm();
        if (mEditMode) {
            try {
                mClient = new GetClient(getView()).execute(mClientEmail).get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            populateForm();
        }
    }

    private void initializeForm() {
        mEtFirstName = (EditText) getActivity().findViewById(R.id.firstName);
        mEtLastName = (EditText) getActivity().findViewById(R.id.lastName);
        mEtEmail = (EditText) getActivity().findViewById(R.id.email);
        mEtPwd1 = (EditText) getActivity().findViewById(R.id.password);
        mEtPwd2 = (EditText) getActivity().findViewById(R.id.passwordRep);
        mAdminSwitch = (Switch) getActivity().findViewById(R.id.adminSwitch);
        Button saveBtn = (Button) getActivity().findViewById(R.id.editButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveChanges(mEtFirstName.getText().toString(), mEtLastName.getText().toString(), mEtEmail.getText().toString(), mEtPwd1.getText().toString(), mEtPwd2.getText().toString(), mAdminSwitch.isChecked())){
                    getActivity().onBackPressed();
                    mToast.show();
                }
            }
        });
    }

    private void populateForm() {
        mEtFirstName.setText(mClient.getFirstName());
        mEtLastName.setText(mClient.getLastName());
        mEtEmail.setText(mClient.getEmail());
        mEtEmail.setFocusable(false);
        mEtEmail.setLongClickable(false);
        mEtEmail.setEnabled(false);
        mAdminSwitch.setChecked(mClient.isAdmin());
        if (!mAdminMode) {
            mAdminSwitch.setVisibility(View.GONE);
        }
    }

    private boolean saveChanges(String firstName, String lastName, String email, String pwd, String pwd2, boolean admin) {
        if (mEditMode) {
            if (!pwd.equals(pwd2)) {
                mEtPwd1.setError(getString(R.string.error_incorrect_password));
                mEtPwd1.requestFocus();
                mEtPwd1.setText("");
                mEtPwd2.setText("");
                return false;
            }
            mClient.setFirstName(firstName);
            mClient.setLastName(lastName);
            mClient.setPassword(pwd);
            mClient.setAdmin(admin);
            mViewModel.updateClient(getView(), mClient);
        } else {
            if (!pwd.equals(pwd2) || pwd.length() < 5) {
                mEtPwd1.setError(getString(R.string.error_invalid_password));
                mEtPwd1.requestFocus();
                mEtPwd1.setText("");
                mEtPwd2.setText("");
                return false;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEtEmail.setError(getString(R.string.error_invalid_email));
                mEtEmail.requestFocus();
                return false;
            }
            ClientEntity newClient = new ClientEntity();
            newClient.setFirstName(firstName);
            newClient.setLastName(lastName);
            newClient.setEmail(email);
            newClient.setPassword(pwd);
            newClient.setAdmin(admin);

            if (!mViewModel.addClient(getView(), newClient)) {
                mEtEmail.setError(getString(R.string.error_invalid_email));
                mEtEmail.requestFocus();
                return false;
            }
        }
        mToast.show();
        return true;
    }

    private void observeViewModel(ClientListViewModel viewModel) {
        viewModel.getClients().observe(this, new Observer<List<ClientEntity>>() {
            @Override
            public void onChanged(@Nullable List<ClientEntity> clientEntities) {
            }
        });
    }
}
