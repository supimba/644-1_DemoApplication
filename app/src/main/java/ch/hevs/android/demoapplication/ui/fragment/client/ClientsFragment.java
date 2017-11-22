package ch.hevs.android.demoapplication.ui.fragment.client;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import ch.hevs.android.demoapplication.entity.ClientEntity;
import ch.hevs.android.demoapplication.ui.activity.LoginActivity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;
import ch.hevs.android.demoapplication.util.RecyclerViewItemClickListener;
import ch.hevs.android.demoapplication.viewmodel.ClientListViewModel;

public class ClientsFragment extends Fragment {

    private static final String TAG = "ClientsFragment";

    private List<ClientEntity> mClients;
    private RecyclerView mRecyclerView;
    private ClientListViewModel mViewModel;

    public ClientsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.clients_fragment_title));
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        Boolean admin = settings.getBoolean(MainActivity.PREFS_ADM, false);
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (user == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        if (!admin) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
        mViewModel = ViewModelProviders.of(this).get(ClientListViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.clients_fragment_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.clientsRecyclerView);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, EditClientFragment.newInstance(null), "CreateClient")
                        .addToBackStack("accounts")
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
            observeViewModel(mViewModel);
            if (mClients == null) {
                mClients = new ArrayList<>();
            }
            mRecyclerView.setAdapter(new RecyclerAdapter<>(mClients, new RecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    Log.d(TAG, "clicked position:" + position);
                    Log.d(TAG, "clicked on: " + mClients.get(position).getId());

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.flContent, EditClientFragment.newInstance(mClients.get(position)), "EditClient")
                            .addToBackStack("mClients")
                            .commit();
                }

                @Override
                public void onItemLongClick(View v, int position) {
                    Log.d(TAG, "longClicked position:" + position);
                    Log.d(TAG, "longClicked on: " + mClients.get(position).getId());

                    createDeleteDialog(position);
                }
            }));
        }
    }

    private void createDeleteDialog(final int position) {
        final ClientEntity client = mClients.get(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.row_delete_item, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getString(R.string.fragment_title_delete_client));
        alertDialog.setCancelable(false);

        final TextView deleteMessage = (TextView) view.findViewById(R.id.tv_delete_item);
        deleteMessage.setText(String.format(getString(R.string.client_delete_msg), client.getId()));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast toast = Toast.makeText(getContext(), getString(R.string.client_deleted), Toast.LENGTH_LONG);
                mViewModel.deleteClient(getView(), client);
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

    private void observeViewModel(ClientListViewModel viewModel) {
        viewModel.getClients().observe(this, new Observer<List<ClientEntity>>() {
            @Override
            public void onChanged(@Nullable List<ClientEntity> clientEntities) {
                if (clientEntities != null) {
                    mClients = clientEntities;
                    ((RecyclerAdapter) mRecyclerView.getAdapter()).setData(mClients);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }
}
