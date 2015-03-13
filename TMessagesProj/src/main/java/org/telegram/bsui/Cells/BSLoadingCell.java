package org.telegram.bsui.Cells;

import android.content.Context;

import org.telegram.android.AndroidUtilities;
import org.telegram.ui.Cells.LoadingCell;


/**
 * Created by Ji on 19.01.2015.
 */
public class BSLoadingCell extends LoadingCell {
    public BSLoadingCell(Context context) {
        super(context);
    }

    @Override
    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }
}
