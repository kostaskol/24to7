package gr.mapeu.a24to7_rebuild.HelpfulClasses;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/*
 * Simple Alert Builder class for easier Alert Dialog building
 */
public class AlertBuilder {
    private Context context;
    private String message;
    private String title;
    private DialogInterface.OnClickListener positive;
    private String positiveText;
    private DialogInterface.OnClickListener negative;
    private String negativeText;
    private AlertDialog dialog;

    public AlertBuilder (Context context, String message, String title, DialogInterface.OnClickListener positive,
                         DialogInterface.OnClickListener negative) {
        this.context = context;
        this.message = message;
        this.title = title;
        this.positive = positive;
        this.negative = negative;
    }

    public AlertBuilder (Context context, String message, String title, DialogInterface.OnClickListener positive,
                         String positiveText, DialogInterface.OnClickListener negative, String negativeText) {
        this.context = context;
        this.message = message;
        this.title = title;
        this.positive = positive;
        this.positiveText = positiveText;
        this.negative = negative;
        this.negativeText = negativeText;
    }

    public AlertBuilder (Context context, String message, String title) {
        this.context = context;
        this.message = message;
        this.title = title;
    }

    public void showDialog() {

        if (positive == null || negative == null) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setPositiveButton(android.R.string.ok, positive)
                    .setCancelable(false)
                    .show();
        } else if (this.positiveText != null && this.negativeText != null){
            dialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setNegativeButton(this.negativeText, negative)
                    .setPositiveButton(this.positiveText, positive)
                    .setCancelable(false)
                    .show();
        } else {
            dialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setNegativeButton(android.R.string.no, negative)
                    .setPositiveButton(android.R.string.yes, positive)
                    .setCancelable(false)
                    .show();
        }
    }

    public boolean isShowing() {
        if (this.dialog != null) {
            return this.dialog.isShowing();
        }
        return false;
    }

    public void dismiss() {
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
    }
}
