package com.kdt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;

/**
 * Seekbar with ability to handle ranges and increments
 */
@SuppressLint("AppCompatCustomView")
public class CustomSeekbar extends SeekBar {
    private int mMin = 0;
    private int mIncrement = 1;
    private SeekBar.OnSeekBarChangeListener mListener;

    /** When using increments, this flag is used to prevent double calls to the listener */
    private boolean mInternalChanges = false;

    public CustomSeekbar(Context context) {
        super(context);
        setup(null);
    }

    public CustomSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    public CustomSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(attrs);
    }

    public CustomSeekbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(attrs);
    }

    public void setIncrement(int increment) {
        mIncrement = increment;
    }

    public void setRange(int min, int max) {
        mMin = min;
        setMax(max - min);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(applyIncrement(progress - mMin));
    }

    @Override
    public void setProgress(int progress, boolean animate) {
        super.setProgress(applyIncrement(progress - mMin), animate);
    }

    @Override
    public synchronized int getProgress() {
        return applyIncrement(super.getProgress() + mMin);
    }

    @Override
    public synchronized void setMin(int min) {
        super.setMin(min);
        mMin = min;
    }

    /**
     * Wrapper to allow for a listener to be set around the internal listener
     */
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mListener = l;
    }

    public void setup(@Nullable AttributeSet attrs) {
        try (TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CustomSeekbar)) {
            mIncrement = attributes.getInt(R.styleable.CustomSeekbar_seekBarIncrement, 1);
        }

        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            /** Store the previous progress to prevent double calls with increments */
            private int previousProgress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mInternalChanges) return;
                mInternalChanges = true;

                progress += mMin;
                progress = applyIncrement(progress);

                if (progress != previousProgress) {
                    if (mListener != null) {
                        previousProgress = progress;
                        mListener.onProgressChanged(seekBar, progress, fromUser);
                    }
                }

                // Forces the thumb to snap to the increment
                setProgress(progress);
                mInternalChanges = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mInternalChanges) return;

                if (mListener != null) {
                    mListener.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mInternalChanges) return;
                mInternalChanges = true;

                setProgress(seekBar.getProgress());

                if (mListener != null) {
                    mListener.onStopTrackingTouch(seekBar);
                }
                mInternalChanges = false;
            }
        });
    }

    /**
     * Apply increment to the progress
     * @param progress Progress to apply increment to
     * @return Progress with increment applied
     */
    private int applyIncrement(int progress) {
        if (mIncrement < 1) return progress;

        progress = progress / mIncrement;
        progress = progress * mIncrement;
        return progress;
    }
}
