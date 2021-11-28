package com.testmodule.helpers;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.view.View;
import android.view.ViewStructure;
import android.view.autofill.AutofillId;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class Field {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Field(AssistStructure.ViewNode node)
    {
        int Id = node.getId();
        //int TrackingId = node.geth $"{node.Id}_{node.GetHashCode()}";
        String IdEntry = node.getIdEntry();
        AutofillId AutofillId = node.getAutofillId();
        int AutofillType = node.getAutofillType();
        int InputType = node.getInputType();
        boolean isFocused = node.isFocused();
        boolean Selected = node.isSelected();
        boolean Clickable = node.isClickable();
        int Visible = node.getVisibility();
        List<String> Hints = FilterForSupportedHints(node.getAutofillHints());
        String Hint = node.getHint();
        List<String> AutofillOptions = node.getAutofillOptions() != null ? new ArrayList<>(): null;
        ViewStructure.HtmlInfo HtmlInfo = node.getHtmlInfo();
        AssistStructure.ViewNode Node = node;

        if (node.getAutofillValue() != null)
        {
            if (node.getAutofillValue().isList())
            {
                CharSequence[] autofillOptions = node.getAutofillOptions();
                if (autofillOptions != null && autofillOptions.length > 0)
                {
                    int ListValue  = node.getAutofillValue().getListValue();
                    CharSequence TextValue = autofillOptions[node.getAutofillValue().getListValue()];
                }
            }
            else if (node.getAutofillValue().isDate())
            {
                Long DateValue = node.getAutofillValue().getDateValue();
            }
            else if (node.getAutofillValue().isText())
            {
                CharSequence TextValue = node.getAutofillValue().getTextValue();
            }
            else if (node.getAutofillValue().isToggle())
            {
                boolean ToggleValue = node.getAutofillValue().getToggleValue();
            }
        }
    }

    private static List<String> FilterForSupportedHints(String[] hints)
    {
        if(hints != null) {
            for(String h:hints) {
                if(IsValidHint(h))
                return new ArrayList<>();
            }
        }
        return null;
    }

    private static Boolean IsValidHint(String hint)
    {
        switch (hint)
        {
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE:
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY:
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
            case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
            case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
            case View.AUTOFILL_HINT_EMAIL_ADDRESS:
            case View.AUTOFILL_HINT_PHONE:
            case View.AUTOFILL_HINT_NAME:
            case View.AUTOFILL_HINT_PASSWORD:
            case View.AUTOFILL_HINT_POSTAL_ADDRESS:
            case View.AUTOFILL_HINT_POSTAL_CODE:
            case View.AUTOFILL_HINT_USERNAME:
                return true;
            default:
                return false;
        }
    }

}
