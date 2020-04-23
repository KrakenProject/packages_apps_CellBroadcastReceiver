/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.cellbroadcastreceiver.unit;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.IThermalService;
import android.os.PowerManager;
import android.telephony.SmsCbMessage;
import android.widget.TextView;

import com.android.cellbroadcastreceiver.CellBroadcastAlertDialog;
import com.android.cellbroadcastreceiver.CellBroadcastAlertService;

import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class CellBroadcastAlertDialogTest extends
        CellBroadcastActivityTestCase<CellBroadcastAlertDialog> {

    @Mock
    private NotificationManager mMockedNotificationManager;

    @Mock
    private IPowerManager.Stub mMockedPowerManagerService;

    @Mock
    private IThermalService.Stub mMockedThermalService;

    @Captor
    private ArgumentCaptor<Integer> mInt;

    @Captor
    private ArgumentCaptor<Notification> mNotification;

    private PowerManager mPowerManager;

    public CellBroadcastAlertDialogTest() {
        super(CellBroadcastAlertDialog.class);
    }

    @Override
    protected Intent createActivityIntent() {
        ArrayList<SmsCbMessage> messageList = new ArrayList<>(1);
        messageList.add(CellBroadcastAlertServiceTest.createMessage(12412));

        Intent intent = new Intent(getInstrumentation().getTargetContext(),
                        CellBroadcastAlertDialog.class);
        intent.putParcelableArrayListExtra(CellBroadcastAlertService.SMS_CB_MESSAGE_EXTRA,
                        messageList);
        return intent;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        injectSystemService(NotificationManager.class, mMockedNotificationManager);
        // PowerManager is a final class so we can't use Mockito to mock it, but we can mock
        // its underlying service.
        doReturn(true).when(mMockedPowerManagerService).isInteractive();
        mPowerManager = new PowerManager(mContext, mMockedPowerManagerService,
                mMockedThermalService, null);
        injectSystemService(PowerManager.class, mPowerManager);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTitleAndMessageText() throws Throwable {
        startActivity();
        waitForMs(100);

        CharSequence alertString =
                getActivity().getResources().getText(com.android.cellbroadcastreceiver.R.string
                        .cmas_presidential_level_alert);
        assertTrue(getActivity().getTitle().toString().startsWith(alertString.toString()));
        assertTrue(((TextView) getActivity().findViewById(
                com.android.cellbroadcastreceiver.R.id.alertTitle)).getText().toString()
                .startsWith(alertString.toString()));

        assertEquals(CellBroadcastAlertServiceTest.createMessage(34596).getMessageBody(),
                ((TextView) getActivity().findViewById(
                        com.android.cellbroadcastreceiver.R.id.message)).getText().toString());

        stopActivity();
    }

    public void testAddToNotification() throws Throwable {
        startActivity();
        waitForMs(100);
        stopActivity();
        waitForMs(100);
        verify(mMockedNotificationManager, times(1)).notify(mInt.capture(),
                mNotification.capture());
        Bundle b = mNotification.getValue().extras;

        assertEquals(1, (int) mInt.getValue());

        assertTrue(getActivity().getTitle().toString().startsWith(
                b.getCharSequence(Notification.EXTRA_TITLE).toString()));
        assertEquals(CellBroadcastAlertServiceTest.createMessage(98235).getMessageBody(),
                b.getCharSequence(Notification.EXTRA_TEXT));
    }
}