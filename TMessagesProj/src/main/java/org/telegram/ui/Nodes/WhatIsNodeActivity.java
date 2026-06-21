package org.telegram.ui.Nodes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

public class WhatIsNodeActivity extends BaseFragment {

    private TextView titleView;
    private TextView descView;
    private TextView createButton;
    private TextView joinButton;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frame = (FrameLayout) fragmentView;
        frame.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        // Illustration placeholder (dark-theme duck image from design, will be lottie or placeholder)
        ImageView img = new ImageView(context);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageResource(R.drawable.msg_filled_info);
        img.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        content.addView(img, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 80, 0, 40));

        titleView = new TextView(context);
        titleView.setText(LocaleController.getString(R.string.NodesWhatIsNodeTitle));
        titleView.setTextSize(22);
        titleView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setGravity(Gravity.CENTER);
        content.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 12));

        descView = new TextView(context);
        descView.setText(LocaleController.getString(R.string.NodesWhatIsNodeDesc));
        descView.setTextSize(15);
        descView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        descView.setGravity(Gravity.CENTER);
        content.addView(descView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 0));

        frame.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // Bottom buttons
        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.VERTICAL);
        buttons.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(32));

        createButton = new TextView(context);
        createButton.setText(LocaleController.getString(R.string.NodesCreateNode));
        createButton.setTextSize(16);
        createButton.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        createButton.setTextColor(Color.WHITE);
        createButton.setGravity(Gravity.CENTER);
        createButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(8),
                Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        createButton.setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
        createButton.setOnClickListener(v -> {
            presentFragment(new NewNodeActivity(false));
        });
        buttons.addView(createButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        joinButton = new TextView(context);
        joinButton.setText(LocaleController.getString(R.string.NodesJoinViaLink));
        joinButton.setTextSize(15);
        joinButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        joinButton.setGravity(Gravity.CENTER);
        joinButton.setPadding(0, AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12));
        joinButton.setOnClickListener(v -> {
            presentFragment(new JoinNodeActivity());
        });
        buttons.addView(joinButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        frame.addView(buttons, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        return fragmentView;
    }
}
