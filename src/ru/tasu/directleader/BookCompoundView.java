package ru.tasu.directleader;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BookCompoundView extends RelativeLayout implements OnClickListener {
    private static final String TAG = "BookCompoundView";
    
    private Drawable drawableBackground, drawableForeground;
    
    private Drawable firstBMDrawable;
    private Drawable secondBMDrawable;
    private Drawable thirdBMDrawable;
    
    private int firstBMTextColor;
    private int secondBMTextColor;
    private int thirdBMTextColor;
    
    private Drawable captionDrawable;
    private String captionText;
    private int captionTextColor;
    private int captionTextSize;
    
    private TextView greenView, yellowView, redView; 
    private ImageView bookImage, bookImageOverlay;
    
    private TextView captionTextView;
    private ImageView captionImageView;
    
    private LinearLayout bookOverlayLayout;
    
    private OnClickListener onClickListener = null;

    public BookCompoundView(Context context) {
        this(context, null, 0);
    }
    public BookCompoundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public BookCompoundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.book_compound_view, this);
        
        greenView = (TextView) findViewById(R.id.greenView);
        yellowView = (TextView) findViewById(R.id.yellowView);
        redView = (TextView) findViewById(R.id.redView);
        
        bookImage = (ImageView) findViewById(R.id.bookImage);
        bookImageOverlay = (ImageView) findViewById(R.id.bookImageOverlay);
        bookImageOverlay.setOnClickListener(this);
        
        captionTextView = (TextView) findViewById(R.id.bookTextView);
        captionImageView = (ImageView) findViewById(R.id.bookTypeImage);
        
        bookOverlayLayout = (LinearLayout) findViewById(R.id.bookOverlayLayout);
        
        init(attrs, defStyle);
        update();
        this.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Be sure to remove the listener, or it will be called on every draw pass
                BookCompoundView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                // captionImageView должен быть вдвое меньше bookOverlayLayout
                int width = bookOverlayLayout.getMeasuredWidth();
//                Log.v(TAG, "bookOverlayLayout.getWidth() " + width);
                captionImageView.getLayoutParams().width = width/2;
                return false;
            }
        });
    }
    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BookCompoundView, defStyle, 0);
        
        drawableBackground = a.getDrawable(R.styleable.BookCompoundView_drawableBackground);
        drawableForeground = a.getDrawable(R.styleable.BookCompoundView_drawableForeground);
        
        firstBMDrawable = a.getDrawable(R.styleable.BookCompoundView_firstBMDrawable);
        secondBMDrawable = a.getDrawable(R.styleable.BookCompoundView_secondBMDrawable);
        thirdBMDrawable = a.getDrawable(R.styleable.BookCompoundView_thirdBMDrawable);
        
        firstBMTextColor = a.getColor(R.styleable.BookCompoundView_firstBMTextColor, Color.WHITE);
        secondBMTextColor = a.getColor(R.styleable.BookCompoundView_secondBMTextColor, Color.BLACK);
        thirdBMTextColor = a.getColor(R.styleable.BookCompoundView_thirdBMTextColor, Color.WHITE);
        
        captionDrawable = a.getDrawable(R.styleable.BookCompoundView_captionDrawable);
        captionTextColor = a.getColor(R.styleable.BookCompoundView_captionTextColor, Color.WHITE);
        captionText = a.getString(R.styleable.BookCompoundView_captionText);
        captionTextSize = a.getDimensionPixelSize(R.styleable.BookCompoundView_captionTextSize, 20);
        
        a.recycle();
    }
    private void update() {
        bookImage.setImageDrawable(drawableBackground);
        bookImageOverlay.setImageDrawable(drawableForeground);
        
        greenView.setTextColor(firstBMTextColor);
        yellowView.setTextColor(secondBMTextColor);
        redView.setTextColor(thirdBMTextColor);
        
        if (firstBMDrawable != null) {
            greenView.setBackgroundDrawable(firstBMDrawable);
        } else {
            greenView.setVisibility(View.GONE);
        }
        if (secondBMDrawable != null) {
            yellowView.setBackgroundDrawable(secondBMDrawable);
        } else {
            yellowView.setVisibility(View.GONE);
        }
        if (thirdBMDrawable != null) {
            redView.setBackgroundDrawable(thirdBMDrawable);
        } else {
            redView.setVisibility(View.GONE);
        }
        
        captionTextView.setText(captionText);
        captionTextView.setTextSize(captionTextSize);
        captionTextView.setTextColor(captionTextColor);
        captionImageView.setImageDrawable(captionDrawable);
        
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
        // captionImageView должен быть вдвое меньше bookOverlayLayout
//        int width = bookOverlayLayout.getMeasuredWidth();
//        Log.v(TAG, "bookOverlayLayout.getWidth() " + width);
        //captionImageView.getLayoutParams().width = width/2;
//        Log.v(TAG, "oldw " + oldw);
//        Log.v(TAG, "w " + w);
//        Log.v(TAG, "captionTextSize " + captionTextSize);
//        captionTextSize *= width/textW;
//        Log.v(TAG, "captionTextSize " + captionTextSize);
//        captionTextView.setTextSize(captionTextSize);
    }
        
    public void setTypeface(Typeface tf) {
        greenView.setTypeface(tf);
        yellowView.setTypeface(tf);
        redView.setTypeface(tf);
        captionTextView.setTypeface(tf);
    }
    public void setGreenCount(int count) {
        greenView.setText(String.valueOf(count));
    }
    public void setYellowCount(int count) {
        yellowView.setText(String.valueOf(count));    
    }
    public void setRedCount(int count) {
        redView.setText(String.valueOf(count));
    }
    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }
    @Override
    public void onClick(View v) {
        if (this.onClickListener != null) {
            this.onClickListener.onClick(this);
        }
    }
}
