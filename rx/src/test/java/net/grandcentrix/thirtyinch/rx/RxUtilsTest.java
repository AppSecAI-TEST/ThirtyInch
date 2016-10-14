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

package net.grandcentrix.thirtyinch.rx;

import net.grandcentrix.thirtyinch.TiView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;

public class RxUtilsTest {

    private TiMockPresenter mPresenter;

    private RxTiPresenterSubscriptionHandler mSubscriptionHandler;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        mView = mock(TiView.class);
        mPresenter = new TiMockPresenter();
        mSubscriptionHandler = new RxTiPresenterSubscriptionHandler(mPresenter);
    }

    @After
    public void tearDown() throws Exception {
        mPresenter = null;
        mView = null;
    }

    @Test
    public void testDeliverLatestCacheToViewViewNotReady() throws Exception {
        mPresenter.create();

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestCacheToView(mPresenter))
                .subscribe(testSubscriber);

        mPresenter.attachView(mView);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(3));
    }

    @Test
    public void testDeliverLatestCacheToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestCacheToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverLatestToViewViewNotReady() throws Exception {
        mPresenter.create();

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testSubscriber);

        mPresenter.attachView(mView);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(3));
    }

    @Test
    public void testDeliverLatestToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverToViewViewNotReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }


    @Test
    public void testManageSubscription() throws Exception {
        mPresenter.create();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageSubscription(testSubscriber);

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));

        mPresenter.destroy();

        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageSubscriptionDestroyed() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        try {
            mSubscriptionHandler.manageSubscription(testSubscriber);
            Assert.fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("DESTROYED"));
        }
    }

    @Test
    public void testManageSubscriptionUnsubscribed() throws Exception {
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        testSubscriber.unsubscribe();
        mSubscriptionHandler.manageSubscription(testSubscriber);
        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageViewSubscription() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mock(TiView.class));
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageViewSubscription(testSubscriber);

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));

        mPresenter.detachView();

        testSubscriber.assertUnsubscribed();
    }


    @Test
    public void testSleep() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageViewSubscription(testSubscriber);
        mPresenter.detachView();

        testSubscriber.assertUnsubscribed();
        assertThat(mPresenter.getView(), nullValue());
        assertThat(mPresenter.onDetachCalled, equalTo(1));
    }

    @Test
    public void testSleepBeforeWakeUp() throws Exception {
        mPresenter.create();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageViewSubscription(testSubscriber);
        mPresenter.detachView();

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));
        assertThat(mPresenter.onDetachCalled, equalTo(0));
    }
}
