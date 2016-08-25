package com.intelliviz.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.intelliviz.stockhawk.R;

/**
 * Created by edm on 6/13/2015.
 */
public class SimpleTextDialog extends DialogFragment {
    public static final String EXTRA_LABEL = "com.intelliviz.SimpleTextDialog.label";
    public static final String EXTRA_TEXT = "com.intelliviz.SimpleTextDialog.text";
    public static final String EXTRA_TITLE = "com.intelliviz.SimpleTextDialog.title";
    public static final String EXTRA_NUMERIC = "com.intelliviz.SimpleTextDialog.numeric";
    public static final int FRAG_ID = 1;
    private boolean mIsNumeric;
    private String mLabel;
    private String mText;
    private String mTitle;
    private TextView mLabelText;
    private EditText mEditText;
    private OnClickListener mListener;

    public interface OnClickListener {
        public void onClickOk(String text);
    }

    public SimpleTextDialog() {
    }

    public static SimpleTextDialog newInstance(String title, String text, String label, boolean numberic) {
        SimpleTextDialog fragment = new SimpleTextDialog();

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_LABEL, label);
        args.putSerializable(EXTRA_TEXT, text);
        args.putSerializable(EXTRA_TITLE, title);
        args.putBoolean(EXTRA_NUMERIC, numberic);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.simple_text_layout, container, false);
        mLabelText = (TextView) view.findViewById(R.id.editTextLabel);
        mEditText = (EditText) view.findViewById(R.id.editTextView);


        Button okButton = (Button) view.findViewById(R.id.okSimpleButton);
        Button cancelButton = (Button) view.findViewById(R.id.cancelSimpleButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                SimpleTextDialog.this.dismiss();
                sendResult(Activity.RESULT_OK, text);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleTextDialog.this.dismiss();
                sendResult(Activity.RESULT_CANCELED, "");
            }
        });

        if(mIsNumeric) {
            setNumeric();
        }

        mLabelText.setText(mLabel);
        mEditText.setText(mText);
        getDialog().setTitle(mTitle);
        setCancelable(false);

        return view;
    }

    /**
     * Do not set any UI here; it is not ready. Just read parameters passed in and save them.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLabel = (String) getArguments().getSerializable(EXTRA_LABEL);
        mText = (String) getArguments().getSerializable(EXTRA_TEXT);
        mTitle = (String) getArguments().getSerializable(EXTRA_TITLE);
        mIsNumeric = getArguments().getBoolean(EXTRA_NUMERIC);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setNumeric() {
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void sendResult(int resultCode, String data) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_TEXT, data);

        getActivity().sendBroadcast(i);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}
