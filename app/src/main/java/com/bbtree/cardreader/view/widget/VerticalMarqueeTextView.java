package com.bbtree.cardreader.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.bbtree.cardreader.entity.eventbus.ScreenSaverCircleEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class VerticalMarqueeTextView extends AppCompatTextView {
    private long duration;
    private int pixelYOffSet;
    private int pixelCount;
    private int mScrollY;
    private int mHeight;
    private boolean isMarquee;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * Creates a vertically auto scrolling marquee of a TextView within an
     * Activity. The (long) duration in milliseconds between calls to the next
     * scrollBy(0, pixelYOffSet). Defaults to 65L. The (int) amount of Y pixels
     * to scroll by defaults to 1.
     */
    public VerticalMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Creates a vertically auto scrolling marquee of a TextView within an
     * Activity. The (long) duration in milliseconds between calls to the next
     * scrollBy(0, pixelYOffSet). Defaults to 65L. The (int) amount of Y pixels
     * to scroll by defaults to 1.
     */
    public VerticalMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates a vertically auto scrolling marquee of a TextView within an
     * Activity. The (long) duration in milliseconds between calls to the next
     * scrollBy(0, pixelYOffSet). Defaults to 65L. The (int) amount of Y pixels
     * to scroll by defaults to 1.
     */
    public VerticalMarqueeTextView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialize fields and start the marquee.
     */
    private void init() {
        setDuration(65l);
        setPixelYOffSet(1);
    }

    /**
     * @return Returns the (long) duration in milliseconds between calls to the
     * next scrollBy(0, pixelYOffSet).
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration Sets the (long) duration in milliseconds between calls to the
     *                 next scrollBy(0, pixelYOffSet). Defaults to 65L if value is
     *                 less than or equal to 0.
     */
    public void setDuration(long duration) {
        if (duration <= 0) {
            this.duration = 65l;
        } else {
            this.duration = duration;
        }

    }

    /**
     * @param pixelYOffSet Sets the (int) amount of Y pixels to scroll by. Defaults to 1
     *                     if value is less.
     */
    public void setPixelYOffSet(int pixelYOffSet) {
        if (pixelYOffSet < 1) {
            this.pixelYOffSet = 1;
        } else {
            this.pixelYOffSet = pixelYOffSet;
        }
    }

    /**
     * Starts the marquee. May only be called once.
     */
    public void startMarquee() {
        compositeDisposable.add(Observable.interval(65, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                               @Override
                               public void accept(@NonNull Long aLong) throws Exception {
                                   if (aLong == 1) isMarquee = true;
                                   // 延迟计算控件高度和总高度
                                   if (aLong == 50) {
                                       pixelCount = (VerticalMarqueeTextView.this).getLineHeight() * (VerticalMarqueeTextView.this).getLineCount();
                                       mHeight = getHeight();
                                       return;
                                   }
                                   // 延迟6.5s滚动
                                   if (aLong < 100) {
                                       return;
                                   }
                                   // 滚动到据最底端控件高度距离停止
                                   if ((VerticalMarqueeTextView.this).getScrollY() >= (pixelCount - mHeight)) {
                                       isMarquee = false;
                                       compositeDisposable.clear();
                                       ScreenSaverCircleEvent event = new ScreenSaverCircleEvent();
                                       event.setCircleDelayTime(6500);
                                       EventBus.getDefault().post(event);
                                   } else {
                                       mScrollY += pixelYOffSet;
                                       // A20板子4.2.2系统有问题
                                       // (VerticalMarqueeTextView.this).scrollBy(0, pixelYOffSet);
                                       (VerticalMarqueeTextView.this).scrollTo(0, mScrollY);
                                   }
                                   (VerticalMarqueeTextView.this).invalidate();
                               }
                           }
                        , new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {

                            }
                        }));
    }

    public boolean isMarquee() {
        return isMarquee;
    }
}