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

    private ClientListViewModel viewModel;
    private ClientEntity client;
    private boolean adminMode;
    private boolean editMode;
    private Toast toast;

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPwd;
    private EditText etPwd2;
    private Switch adminSwitch;

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
        adminMode = settings.getBoolean(MainActivity.PREFS_ADM, false);
        viewModel = ViewModelProviders.of(this).get(ClientListViewModel.class);
        observeViewModel(viewModel);

        if (getArguments() != null) {
            String clientMail = getArguments().getString(ARG_PARAM1);
            if (clientMail.equals("create")) {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_create_client));
                toast = Toast.makeText(getContext(), getString(R.string.client_created), Toast.LENGTH_LONG);
                editMode = false;
            } else {
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.fragment_title_edit_client));
                try {
                    client = new GetClient(getContext()).execute(clientMail).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                toast = Toast.makeText(getContext(), getString(R.string.client_edited), Toast.LENGTH_LONG);
                editMode = true;
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
        if (editMode)
            populateForm();
    }

    private void initializeForm() {
        etFirstName = (EditText) getActivity().findViewById(R.id.firstName);
        etLastName = (EditText) getActivity().findViewById(R.id.lastName);
        etEmail = (EditText) getActivity().findViewById(R.id.email);
        etPwd = (EditText) getActivity().findViewById(R.id.password);
        etPwd2 = (EditText) getActivity().findViewById(R.id.passwordRep);
        adminSwitch = (Switch) getActivity().findViewById(R.id.adminSwitch);
        Button saveBtn = (Button) getActivity().findViewById(R.id.editButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveChanges(etFirstName.getText().toString(), etLastName.getText().toString(), etEmail.getText().toString(), etPwd.getText().toString(), etPwd2.getText().toString(), adminSwitch.isChecked())){
                    getActivity().onBackPressed();
                    toast.show();
                }
            }
        });
    }

    private void populateForm() {
        etFirstName.setText(client.getFirstName());
        etLastName.setText(client.getLastName());
        etEmail.setText(client.getEmail());
        etEmail.setFocusable(false);
        etEmail.setLongClickable(false);
        etEmail.setEnabled(false);
        adminSwitch.setChecked(client.isAdmin());
        if (!adminMode) {
            adminSwitch.setVisibility(View.GONE);
        }
    }

    private boolean saveChanges(String firstName, String lastName, String email, String pwd, String pwd2, boolean admin) {
        if (editMode) {
            if (!pwd.equals(pwd2)) {
                etPwd.setError(getString(R.string.error_incorrect_password));
                etPwd.requestFocus();
                etPwd.setText("");
                etPwd2.setText("");
                return false;
            }
            client.setFirstName(firstName);
            client.setLastName(lastName);
            client.setPassword(pwd);
            client.setAdmin(admin);
            viewModel.updateClient(getContext(), client);
        } else {
            if (!pwd.equals(pwd2) || pwd.length() < 5) {
                etPwd.setError(getString(R.string.error_invalid_password));
                etPwd.requestFocus();
                etPwd.setText("");
                etPwd2.setText("");
                return false;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.error_invalid_email));
                etEmail.requestFocus();
                return false;
            }
            ClientEntity newClient = new ClientEntity();
            newClient.setFirstName(firstName);
            newClient.setLastName(lastName);
            newClient.setEmail(email);
            newClient.setPassword(pwd);
            newClient.setAdmin(admin);

            if (!viewModel.addClient(getContext(), newClient)) {
                etEmail.setError(getString(R.string.error_invalid_email));
                etEmail.requestFocus();
                return false;
            }
        }
        toast.show();
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
