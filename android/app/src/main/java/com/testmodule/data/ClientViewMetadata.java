package com.testmodule.data;

import android.service.autofill.SaveInfo;
import android.view.autofill.AutofillId;

import java.util.Arrays;
import java.util.List;

/**
 * In this simple implementation, the only view data we collect from the client are autofill hints
 * of the views in the view hierarchy, the corresponding autofill IDs, and the {@link SaveInfo}
 * based on the hints.
 */
public class ClientViewMetadata {
    private final List<String> mAllHints;
    private final int mSaveType;
    private final AutofillId[] mAutofillIds;
    private final String mWebDomain;
    private final AutofillId[] mFocusedIds;

    public ClientViewMetadata(List<String> allHints, int saveType, AutofillId[] autofillIds,
                              AutofillId[] focusedIds, String webDomain) {
        mAllHints = allHints;
        mSaveType = saveType;
        mAutofillIds = autofillIds;
        mWebDomain = webDomain;
        mFocusedIds = focusedIds;
    }

    public List<String> getAllHints() {
        return mAllHints;
    }

    public AutofillId[] getAutofillIds() {
        return mAutofillIds;
    }

    public AutofillId[] getFocusedIds() {
        return mFocusedIds;
    }

    public int getSaveType() {
        return mSaveType;
    }

    public String getWebDomain() {
        return mWebDomain;
    }

    @Override public String toString() {
        return "ClientViewMetadata{" +
                "mAllHints=" + mAllHints +
                ", mSaveType=" + mSaveType +
                ", mAutofillIds=" + Arrays.toString(mAutofillIds) +
                ", mWebDomain='" + mWebDomain + '\'' +
                ", mFocusedIds=" + Arrays.toString(mFocusedIds) +
                '}';
    }
}
