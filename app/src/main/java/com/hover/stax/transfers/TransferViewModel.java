package com.hover.stax.transfers;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.hover.stax.actions.Action;
import com.hover.stax.channels.Channel;
import com.hover.stax.requests.Request;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.schedules.Schedule;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.StagedViewModel;

import java.util.List;

import static com.hover.stax.transfers.TransferStage.REVIEW;
import static com.hover.stax.transfers.TransferStage.REVIEW_DIRECT;

public class TransferViewModel extends StagedViewModel {
	final private String TAG = "TransferViewModel";

	private String type = Action.P2P;

	private MediatorLiveData<List<Action>> filteredActions = new MediatorLiveData<>();
	private MediatorLiveData<Action> activeAction = new MediatorLiveData<>();

	private MutableLiveData<String> amount = new MutableLiveData<>();
	private MutableLiveData<StaxContact> contact = new MutableLiveData<>();
	private MutableLiveData<String> note = new MutableLiveData<>();

	private MutableLiveData<Integer> amountError = new MutableLiveData<>();
	private MutableLiveData<Integer> recipientError = new MutableLiveData<>();
	private MutableLiveData<Integer> pageError = new MutableLiveData<>();

	protected LiveData<Request> request = new MutableLiveData<>();

	public TransferViewModel(Application application) {
		super(application);
		stage.setValue(TransferStage.AMOUNT);

		filteredActions.addSource(activeChannel, this::loadActions);
		activeAction.addSource(filteredActions, this::setActiveActionIfOutOfDate);
	}

	void setType(String transaction_type) {
		type = transaction_type;
	}

	String getType() {
		return type;
	}

	protected void setActiveChannelIfNull(List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			if (schedule.getValue() != null)
				setActiveChannel(schedule.getValue().channel_id);
			else if (request.getValue() != null)
				activeChannel.setValue(getChannelByInstId(request.getValue().requester_institution_id));
			else if (activeChannel.getValue() == null)
				activeChannel.setValue(channels.get(0));
		}
	}

	void setActiveChannel(String channelString) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) {
			return;
		}
		for (Channel c : selectedChannels.getValue()) {
			if (c.toString().equals(channelString)) {
				activeChannel.setValue(c);
				return;
			}
		}
	}

	void setActiveChannel(List<Action> actions) {
		if (actions == null || actions.size() == 0) { return; }
		activeChannel.setValue(getChannelById(actions.get(0).channel_id));
	}

	private Channel getChannelByInstId(int id) {
		if (selectedChannels.getValue() == null || selectedChannels.getValue().size() == 0) return null;
		for (Channel c : selectedChannels.getValue()) {
			if (c.institutionId == id) {
				return c;
			}
		}
		return null;
	}

	public void loadActions(Channel channel) {
		if (channel != null) {
			new Thread(() -> {
				if (request.getValue() != null)
					filteredActions.postValue(repo.getActions(getChannelIds(), request.getValue().requester_institution_id));
				else
					filteredActions.postValue(repo.getActions(channel.id, type));
			}).start();
		}
	}

	public void loadActions(Request r) {

		if (r != null && selectedChannels.getValue() != null && selectedChannels.getValue().size() > 0) {
			new Thread(() -> {
				List<Action> actions = repo.getActions(getChannelIds(), r.requester_institution_id);
				filteredActions.postValue(actions);
				if (actions.size() <= 0)
					pageError.postValue(R.string.whoopsie);
			}).start();
			activeChannel.addSource(filteredActions, this::setActiveChannel);
		}
	}

	private int[] getChannelIds() {
		List<Channel> channels = selectedChannels.getValue();
		int[] ids = new int[channels.size()];
		for (int c = 0; c < channels.size(); c++)
			ids[c] = channels.get(c).id;
		return ids;
	}

	LiveData<List<Action>> getActions() {
		return filteredActions;
	}

	private void setActiveActionIfOutOfDate(List<Action> actions) {
		if (actions != null && actions.size() > 0 && (activeAction.getValue() == null || !actions.contains(activeAction.getValue())))
			activeAction.setValue(actions.get(0));
	}

	void setActiveAction(Action action) {
		activeAction.postValue(action);
	}
	void setActiveAction(String actionString) {
		if (filteredActions.getValue() == null || filteredActions.getValue().size() == 0) {
			return;
		}
		for (Action a : filteredActions.getValue()) {
			if (a.toString().equals(actionString))
				activeAction.postValue(a);
		}
	}

	void setActiveAction(List<Action> actions) {
		if (actions == null || actions.size() == 0) return;
		activeAction.postValue(actions.get(0));
	}

	LiveData<Action> getActiveAction() {
		if (activeAction == null) {
			activeAction = new MediatorLiveData<>();
		}
		return activeAction;
	}

	void setAmount(String a) {
		amount.postValue(a);
	}

	LiveData<String> getAmount() {
		if (amount == null) {
			amount = new MutableLiveData<>();
		}
		return amount;
	}
	LiveData<Integer> getAmountError() {
		if (amountError == null) { amountError = new MutableLiveData<>(); }
		return amountError;
	}

	void setContact(String contact_ids) {
		if (contact_ids == null) return;
		new Thread(() -> {
			List<StaxContact> contacts = repo.getContacts(contact_ids.split(","));
			if (contacts.size() > 0) contact.postValue(contacts.get(0));
		}).start();
	}

	void setContact(StaxContact c) {
		contact.setValue(c);
	}

	LiveData<StaxContact> getContact() {
		if (contact == null) { contact = new MutableLiveData<>(); }
		return contact;
	}

	void setRecipient(String r) {
		if (contact.getValue() != null && contact.getValue().toString().equals(r)) { return; }
		contact.setValue(new StaxContact(r));
	}

	LiveData<Integer> getRecipientError() {
		if (recipientError == null) { recipientError = new MutableLiveData<>(); }
		return recipientError;
	}

	LiveData<Integer> getPageError() {
		if (pageError == null) { pageError = new MutableLiveData<>(); }
		return pageError;
	}

	public LiveData<Schedule> getSchedule() {
		if (schedule == null) { schedule = new MutableLiveData<>(); }
		return schedule;
	}

	public LiveData<Request> getRequest() {
		if (request == null) { request = new MutableLiveData<>(); }
		return request;
	}

	void setNote(String r) {
		note.postValue(r);
	}

	LiveData<String> getNote() {
		if (note == null) {
			note = new MutableLiveData<>();
			note.setValue(" ");
		}
		return note;
	}

	@Override
	public void goToNextStage() {
		stage.postValue(goToNextStage(stage.getValue()));
	}

	@Override
	public void goToPrevStage() {
		stage.postValue(goToPrevStage(stage.getValue()));
	}

	private StagedEnum goToNextStage(StagedEnum currentStage) {
		StagedEnum next = currentStage.next();
		if (!stageRequired((TransferStage) next))
			next = goToNextStage(next);
		return next;
	}

	private StagedEnum goToPrevStage(StagedEnum currentStage) {
		StagedEnum prev = currentStage.prev();
		if (!stageRequired((TransferStage) prev))
			prev = goToPrevStage(prev);
		return prev;
	}

	boolean stageRequired(TransferStage ts) {
		switch (ts) {
			case TO_NETWORK:
				return requiresActionChoice();
			case RECIPIENT:
				return (request.getValue() == null || !request.getValue().hasRequesterInfo()) && activeAction.getValue() != null && activeAction.getValue().requiresRecipient();
			case NOTE:
				return activeAction.getValue() != null && activeAction.getValue().allowsNote();
			default:
				return true;
		}
	}

	boolean requiresActionChoice() { // in last case, should have request network as choice
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 && (filteredActions.getValue().size() > 1 || filteredActions.getValue().get(0).hasDiffToInstitution());
	}

	boolean hasActionsLoaded() {
		return filteredActions.getValue() != null && filteredActions.getValue().size() > 0 &&
			(filteredActions.getValue().size() > 1 || (activeAction.getValue() != null && activeAction.getValue().hasToInstitution()));
	}

	boolean stageValidates() {
		if (stage.getValue() == null) return false;
		switch ((TransferStage) stage.getValue()) {
			case AMOUNT:
				return isErrorFree((MutableLiveData) amount, amountError, R.string.amount_fielderror);
			case FROM_ACCOUNT:
				return isErrorFree((MutableLiveData) activeChannel, pageError, R.string.fromacct_fielderror)
					       && isErrorFree((MutableLiveData) filteredActions, pageError, R.string.actions_fielderror);
			case TO_NETWORK:
				return isErrorFree((MutableLiveData) activeAction, pageError, R.string.recipientnetwork_fielderror);
			case RECIPIENT:
				return isErrorFree((MutableLiveData) contact, recipientError, R.string.recipient_fielderror);
			case NOTE: return true;
			case REVIEW:
			case REVIEW_DIRECT:
			default:
				Log.e(TAG, "active channel: " + activeChannel.getValue());
				Log.e(TAG, "active action: " + activeAction.getValue());
				Log.e(TAG, "contact: " + contact.getValue());
			    return isErrorFree((MutableLiveData) activeChannel, pageError, R.string.whoopsie) &&
				           isErrorFree((MutableLiveData) activeAction, pageError, R.string.whoopsie) &&
				           (!activeAction.getValue().requiresRecipient() || isErrorFree((MutableLiveData) contact, pageError, R.string.whoopsie));
		}
	}

	private boolean isErrorFree(MutableLiveData<Object> whichProp, MutableLiveData<Integer> whichError, int errorString) {
		if (whichProp.getValue() == null ||
			    (whichProp.getValue() instanceof String && ((String) whichProp.getValue()).isEmpty()) ||
			        (whichProp.getValue() instanceof StaxContact && ((StaxContact) whichProp.getValue()).getPhoneNumber().isEmpty()) ||
			        (whichProp.getValue() instanceof List && ((List) whichProp.getValue()).size() == 0)) {
			whichError.setValue(errorString);
			return false;
		} else
			whichError.setValue(null);
		return true;
	}

	boolean isDone() { return stage.getValue() == REVIEW || stage.getValue() == REVIEW_DIRECT; }

	public void decrypt(String encryptedLink) {
		request = repo.decrypt(encryptedLink, getApplication());
		filteredActions.addSource(request, this::loadActions);
	}

	public void view(Request r) {
		setAmount(r.amount);
		setRecipient(r.requester_number);
		setStage(chooseRequestStage(r));
	}

	private TransferStage chooseRequestStage(Request r) {
		if (r.amount.isEmpty()) return TransferStage.AMOUNT;
		else if (filteredActions.getValue() == null || filteredActions.getValue().size() <= 0) return TransferStage.FROM_ACCOUNT;
		else return TransferStage.REVIEW_DIRECT;
	}

	public void view(Schedule s) {
		schedule.setValue(s);
		setType(s.type);
		setActiveChannel(s.channel_id);
		setAmount(s.amount);
		setContact(s.recipient_ids);
		setNote(s.note);
		setStage(TransferStage.REVIEW_DIRECT);
	}

	public void schedule() {
		Schedule s = new Schedule(activeAction.getValue(), futureDate.getValue(), repeatSaved.getValue(), frequency.getValue(), endDate.getValue(),
			contact.getValue(), amount.getValue(), note.getValue(), getApplication());
		saveSchedule(s);
	}

	public void checkSchedule() {
		if (schedule.getValue() != null) {
			Schedule s = schedule.getValue();
			if (s.end_date <= DateUtils.today()) {
				s.complete = true;
				repo.update(s);
			}
		}
	}

	public void saveContact() {
		if (contact.getValue() != null) {
			new Thread(() -> {
				StaxContact c = contact.getValue();
				c.lastUsedTimestamp = DateUtils.now();
				repo.insertOrUpdate(contact.getValue());
			}).start();
		}
	}
}