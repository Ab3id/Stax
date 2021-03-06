package com.hover.stax.security;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.permissions.PermissionDialog;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.channels.ChannelsAdapter;
import com.hover.stax.utils.UIHelper;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PermissionsFragment extends Fragment {

	public final static int PHONE_REQUEST = 0, SMS_REQUEST = 1;
	private final static String PHONE = "phone";
	private PinsViewModel viewModel;
	private String currentAsk = null;
	private boolean askStarted = false;
	private AlertDialog dialog;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(PinsViewModel.class);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_choose_channels)));
		currentAsk = getArguments().getString("perm_type", PHONE);
		return inflater.inflate(R.layout.fragment_permissions, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.phone_description).setVisibility(currentAsk.equals(PHONE) ? View.VISIBLE : View.GONE);
		view.findViewById(R.id.hard_description).setVisibility(currentAsk.equals(PHONE) ? View.GONE : View.VISIBLE);
		view.findViewById(R.id.grant_btn).setOnClickListener(v -> startRequest());
		setUpSelectedChannels(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!currentAsk.equals(PHONE) && askStarted)
			requestNext();
	}

	private void startRequest() {
		askStarted = true;
		requestNext();
	}

	private void requestNext() {
		PermissionHelper ph = new PermissionHelper(getContext());
		if (currentAsk.equals(PHONE) && !ph.hasPhonePerm())
			requestPhone(ph);
		else if (!ph.hasSmsPerm())
			requestSMS(ph);
		else if (!ph.hasOverlayPerm())
			requestOverlay();
		else if (!ph.hasAccessPerm())
			requestAccessibility();
		else if (getActivity() != null) {
			Amplitude.getInstance().logEvent(getString(R.string.granted_sdk_permissions));
			getActivity().setResult(RESULT_OK);
			getActivity().finish();
		}
		//NavHostFragment.findNavController(this).navigate(R.id.navigation_pin_entry);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode != PHONE_REQUEST)
			requestNext();
	}

	private void requestPhone(PermissionHelper ph) {
		Amplitude.getInstance().logEvent(getString(R.string.request_permphone));
		ph.requestPhone(getActivity(), PHONE_REQUEST);
	}

	private void requestSMS(PermissionHelper ph) {
		Amplitude.getInstance().logEvent(getString(R.string.request_permsms));
		ph.requestBasicPerms(getActivity(), SMS_REQUEST);
	}

	public void requestOverlay() {
		Amplitude.getInstance().logEvent(getString(R.string.request_permoverlay));
		if (dialog != null) dialog.dismiss();
		dialog = new PermissionDialog(getContext(), PermissionDialog.OVERLAY).createDialog(getActivity());
	}

	public void requestAccessibility() {
		Amplitude.getInstance().logEvent(getString(R.string.request_permaccessibility));
		if (dialog != null) dialog.dismiss();
		Hover.setAfterPermissionReturnActivity("com.hover.stax.security.PinsActivity", getContext());
		dialog = new PermissionDialog(getContext(), PermissionDialog.ACCESS).createDialog(getActivity());
	}

	private void setUpSelectedChannels(View view) {
		RecyclerView selectedRecyclerView = view.findViewById(R.id.selected_recycler);
		viewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
			selectedRecyclerView.setLayoutManager(gridLayoutManager);
			selectedRecyclerView.setHasFixedSize(true);
			ChannelsAdapter selectedAdapter = new ChannelsAdapter(channels, null);
			selectedRecyclerView.setAdapter(selectedAdapter);
		});
	}
}
