package com.hover.stax.ui.security;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.models.StaxServicesModel;
import com.hover.stax.ui.chooseService.ChooseServicesActivity;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;

public class SecurityFragment extends Fragment  {
	private SecurityViewModel securityViewModel;
@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	securityViewModel = new ViewModelProvider(this).get(SecurityViewModel.class);
	View root = inflater.inflate(R.layout.fragment_security, container, false);
	securityViewModel.loadServices();

	root.findViewById(R.id.removePinsButtonId).setOnClickListener(view->{
		startActivity(new Intent(getActivity(), ChooseServicesActivity.class));
	});

	AppCompatSpinner spinner = root.findViewById(R.id.defaultAccountSpinner);

	securityViewModel.getServicesForDefaultAccount().observe(getViewLifecycleOwner(), staxServicesModels -> {
		ArrayList<String> staxServiceNames = new ArrayList<>();
		for(StaxServicesModel model : staxServicesModels) {
			staxServiceNames.add(model.getServiceName());
		}
		UIHelper.loadSpinnerItems(staxServiceNames, spinner, getContext());
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				securityViewModel.setNewDefaultAccount(position);
				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if(textView !=null){
					textView.setTextColor(getResources().getColor(R.color.colorWhite));
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


	});
	return  root;

}

}
