/*
 * Copyright (C) 2016 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;


public class SamplePresenter extends TiPresenter<SampleView> {

    private RxTiPresenterSubscriptionHandler mSubscriptionHandler
            = new RxTiPresenterSubscriptionHandler(this);

    private static final String TAG = SamplePresenter.class.getSimpleName();

    public SamplePresenter() {
        super(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        mSubscriptionHandler.manageSubscription(Observable.interval(0, 37, TimeUnit.MILLISECONDS)
                .compose(RxTiPresenterUtils.<Long>deliverLatestCacheToView(this))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(final Long alive) {
                        getView().showText("I'm a fragment and alive for " + (alive * 37) + "ms");
                    }
                }));
    }
}
