package com.hover.stax.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.requests.Request;
import com.hover.stax.schedules.Schedule;

import java.util.List;

public class FutureViewModel extends AndroidViewModel {
	private final String TAG = "ScheduledViewModel";

	private DatabaseRepo repo;

	private LiveData<List<Schedule>> schedules;
	private LiveData<List<Request>> requests;

	public FutureViewModel(Application application) {
		super(application);
		repo = new DatabaseRepo(application);

		schedules = new MutableLiveData<>();
		schedules = repo.getFutureTransactions();

		requests = new MutableLiveData<>();
		requests = repo.getLiveRequests();
	}

	public LiveData<List<Schedule>> getScheduled() {
		return schedules;
	}

	public LiveData<List<Request>> getRequests() {
		return requests;
	}
}
