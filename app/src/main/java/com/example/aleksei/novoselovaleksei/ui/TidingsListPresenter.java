package com.example.aleksei.novoselovaleksei.ui;

import android.support.annotation.NonNull;

import com.example.aleksei.novoselovaleksei.data.Tiding;
import com.example.aleksei.novoselovaleksei.data.source.TidingRepository;
import com.example.aleksei.novoselovaleksei.utils.schedulers.BaseSchedulerProvider;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class TidingsListPresenter implements TidingsListContract.Presenter {

    @NonNull
    private final TidingRepository mTidingsRepository;

    @NonNull
    private final TidingsListContract.View mTidingListView;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @NonNull
    private CompositeSubscription mSubscriptions;

    public TidingsListPresenter(@NonNull TidingRepository tidingRepository,
                                @NonNull TidingsListContract.View tidingListView,
                                @NonNull BaseSchedulerProvider schedulerProvider) {
        mTidingsRepository = tidingRepository;
        mTidingListView = tidingListView;
        mSchedulerProvider = schedulerProvider;
        mSubscriptions = new CompositeSubscription();
        mTidingListView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadTidings(false, false);
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void loadTidings(boolean forceUpdate) {
        loadTidings(forceUpdate, true);
    }

    @Override
    public void refreshTidings() {
        if (!mTidingListView.isInternetAvailable()) {
            mTidingListView.showNoInternet();
            mTidingListView.setLoadingIndicator(false);
        } else {
            loadTidings(true);
        }
    }

    private void loadTidings(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mTidingListView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mTidingsRepository.refreshTidings();
        }

        mSubscriptions.clear();
        Subscription subscription = mTidingsRepository
                .getTidings()
                .flatMap(Observable::from)
                .toList()
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<List<Tiding>>() {
                    @Override
                    public void onCompleted() {
                        mTidingListView.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mTidingListView.showLoadingTidingsError();
                    }

                    @Override
                    public void onNext(List<Tiding> tidings) {
                        processTidings(tidings);
                    }
                });

        mSubscriptions.add(subscription);
    }

    private void processTidings(@NonNull List<Tiding> tidings) {
        if (tidings.isEmpty()) {
            processEmptyTidings();
        } else {
            Collections.sort(tidings, (left, right) -> (int) right.getPublicationDate() - (int) left.getPublicationDate());
            mTidingListView.showTidings(tidings);
        }
    }

    private void processEmptyTidings() {
        mTidingListView.showNoTidings();
    }
}
