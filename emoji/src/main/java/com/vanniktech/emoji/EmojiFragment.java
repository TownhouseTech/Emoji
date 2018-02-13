package com.vanniktech.emoji;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;

import static com.vanniktech.emoji.Utils.checkNotNull;

public final class EmojiFragment extends Fragment {
    View rootView;
    Activity context;

    @NonNull
    RecentEmoji recentEmoji;
    @NonNull
    VariantEmoji variantEmoji;
    @NonNull
    EmojiVariantPopup variantPopup;

    EmojiEditText emojiEditText;

    @Nullable
    OnEmojiPopupShownListener onEmojiPopupShownListener;
    @Nullable
    OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
    @Nullable
    OnEmojiClickListener onEmojiClickListener;
    @Nullable
    OnEmojiPopupDismissListener onEmojiPopupDismissListener;

    int height = -3;

    public EmojiFragment() {
    }

    private void init(@NonNull final View rootView, @NonNull final EmojiEditText emojiEditText,
                      @Nullable final RecentEmoji recent, @Nullable final VariantEmoji variant) {
        this.context = Utils.asActivity(rootView.getContext());
        this.rootView = rootView.getRootView();
        this.emojiEditText = emojiEditText;
        this.recentEmoji = recent != null ? recent : new RecentEmojiManager(context);
        this.variantEmoji = variant != null ? variant : new VariantEmojiManager(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final OnEmojiLongClickListener longClickListener = new OnEmojiLongClickListener() {
            @Override
            public void onEmojiLongClick(@NonNull final EmojiImageView view, @NonNull final Emoji emoji) {
                variantPopup.show(view, emoji);
            }
        };

        final OnEmojiClickListener clickListener = new OnEmojiClickListener() {
            @Override
            public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {
                emojiEditText.input(emoji);

                recentEmoji.addEmoji(emoji);
                variantEmoji.addVariant(emoji);
                imageView.updateEmoji(emoji);

                if (onEmojiClickListener != null) {
                    onEmojiClickListener.onEmojiClick(imageView, emoji);
                }

                variantPopup.dismiss();
            }
        };

        variantPopup = new EmojiVariantPopup(this.rootView, clickListener);
        final EmojiView emojiView = new EmojiView(context, clickListener, longClickListener, recentEmoji, variantEmoji);
        emojiView.setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClick(final View v) {
                emojiEditText.backspace();

                if (onEmojiBackspaceClickListener != null) {
                    onEmojiBackspaceClickListener.onEmojiBackspaceClick(v);
                }
            }
        });

        if (this.height != -3) {
            emojiView.getRootView().setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, this.height));
        }

        return emojiView;

    }

    @Override
    public void onStop() {
        super.onStop();
        variantPopup.dismiss();
        recentEmoji.persist();
        variantEmoji.persist();
        if (onEmojiPopupDismissListener != null) {
            onEmojiPopupDismissListener.onEmojiPopupDismiss();
        }
    }

    /**
     * Used to set the height of the emoji keyboard view. Otherwise will fill the screen. Set before adding Fragment
     * @param height Can be int or ViewGroup.LayoutParams.WRAP_CONTENT or ViewGroup.LayoutParams.MATCH_PARENT
     */
    public void setHeight(int height) {
        this.height = height;
    }

    public static final class Builder {
        @NonNull private final View rootView;
        @Nullable private OnEmojiPopupShownListener onEmojiPopupShownListener;
        @Nullable private OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
        @Nullable private OnEmojiClickListener onEmojiClickListener;
        @Nullable private OnEmojiPopupDismissListener onEmojiPopupDismissListener;
        @Nullable private RecentEmoji recentEmoji;
        @Nullable private VariantEmoji variantEmoji;

        private Builder(final View rootView) {
            this.rootView = checkNotNull(rootView, "The root View can't be null");
        }

        /**
         * @param rootView The root View of your layout.xml which will be used for calculating the height
         *                 of the keyboard.
         * @return builder For building the {@link EmojiPopup}.
         */
        @CheckResult
        public static EmojiFragment.Builder fromRootView(final View rootView) {
            return new EmojiFragment.Builder(rootView);
        }

        @CheckResult public EmojiFragment.Builder setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener) {
            onEmojiClickListener = listener;
            return this;
        }

        @CheckResult public EmojiFragment.Builder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener) {
            onEmojiPopupShownListener = listener;
            return this;
        }

        @CheckResult public EmojiFragment.Builder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener) {
            onEmojiPopupDismissListener = listener;
            return this;
        }

        @CheckResult public EmojiFragment.Builder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener) {
            onEmojiBackspaceClickListener = listener;
            return this;
        }

        /**
         * Allows you to pass your own implementation of recent emojis. If not provided the default one
         * {@link RecentEmojiManager} will be used.
         *
         * @since 0.2.0
         */
        @CheckResult public EmojiFragment.Builder setRecentEmoji(@Nullable final RecentEmoji recent) {
            recentEmoji = recent;
            return this;
        }

        /**
         * Allows you to pass your own implementation of variant emojis. If not provided the default one
         * {@link VariantEmojiManager} will be used.
         *
         * @since 0.5.0
         */
        @CheckResult public EmojiFragment.Builder setVariantEmoji(@Nullable final VariantEmoji variant) {
            variantEmoji = variant;
            return this;
        }

        @CheckResult public EmojiFragment build(@NonNull final EmojiEditText emojiEditText) {
            EmojiManager.getInstance().verifyInstalled();
            checkNotNull(emojiEditText, "EmojiEditText can't be null");

            final EmojiFragment emojiFragment = new EmojiFragment();
            emojiFragment.init(rootView, emojiEditText, recentEmoji, variantEmoji);
            emojiFragment.onEmojiClickListener = onEmojiClickListener;
            emojiFragment.onEmojiPopupShownListener = onEmojiPopupShownListener;
            emojiFragment.onEmojiPopupDismissListener = onEmojiPopupDismissListener;
            emojiFragment.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
            return emojiFragment;
        }
    }

}
