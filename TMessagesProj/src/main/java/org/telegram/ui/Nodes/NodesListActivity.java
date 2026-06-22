package org.telegram.ui.Nodes;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.tl.TL_nodes;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

/**
 * My Nodes list — entry point. Shows all nodes for current account.
 * "Add Node" button at top opens WhatIsNodeActivity.
 */
public class NodesListActivity extends BaseFragment {

    private final ArrayList<TL_nodes.TL_node> nodes = new ArrayList<>();
    private NodesAdapter adapter;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesSidebarTitle));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // "Add Node" row
        LinearLayout addRow = new LinearLayout(context);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        addRow.setGravity(Gravity.CENTER_VERTICAL);
        addRow.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        addRow.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        addRow.setClickable(true);
        addRow.setFocusable(true);
        addRow.setBackground(Theme.getSelectorDrawable(false));
        addRow.setOnClickListener(v -> presentFragment(new WhatIsNodeActivity()));

        ImageView addIcon = new ImageView(context);
        addIcon.setImageResource(R.drawable.msg_add);
        addIcon.setColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton));
        addRow.addView(addIcon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView addText = new TextView(context);
        addText.setText(LocaleController.getString(R.string.NodesAddNode));
        addText.setTextSize(16);
        addText.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        addRow.addView(addText);
        root.addView(addRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        root.addView(new View(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1));

        // Nodes list
        ListView lv = new ListView(context);
        lv.setDividerHeight(0);
        lv.setDivider(null);
        adapter = new NodesAdapter(context);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) -> openNode(nodes.get(position)));
        root.addView(lv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f));

        ((FrameLayout) fragmentView).addView(root, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // Load nodes
        NodeController ctrl = NodeController.getInstance(currentAccount);
        if (ctrl.isMyNodesLoaded()) {
            nodes.addAll(ctrl.getCachedNodes());
            adapter.notifyDataSetChanged();
        } else {
            ctrl.loadMyNodes(() -> {
                nodes.clear();
                nodes.addAll(ctrl.getCachedNodes());
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        }

        return fragmentView;
    }

    private void openNode(TL_nodes.TL_node node) {
        // Open immediately — NodeChatListActivity loads full data itself
        presentFragment(new NodeChatListActivity(node.id));
    }

    private class NodesAdapter extends android.widget.BaseAdapter {
        private final Context context;
        NodesAdapter(Context ctx) { this.context = ctx; }
        @Override public int getCount() { return nodes.size(); }
        @Override public TL_nodes.TL_node getItem(int pos) { return nodes.get(pos); }
        @Override public long getItemId(int pos) { return nodes.get(pos).id; }

        @Override
        public View getView(int pos, View cv, android.view.ViewGroup parent) {
            if (cv == null) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
                row.setClickable(true);
                row.setBackground(Theme.getSelectorDrawable(false));
                row.setDescendantFocusability(android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS);

                BackupImageView av = new BackupImageView(context);
                av.setRoundRadius(AndroidUtilities.dp(20));
                av.setTag("av");
                row.addView(av, LayoutHelper.createLinear(48, 48, 0, 0, 14, 0));

                LinearLayout col = new LinearLayout(context);
                col.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(context);
                title.setTag("title");
                title.setTextSize(16);
                title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                title.setSingleLine();
                col.addView(title);
                TextView sub = new TextView(context);
                sub.setTag("sub");
                sub.setTextSize(13);
                sub.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
                col.addView(sub);
                row.addView(col, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                cv = row;
            }

            TL_nodes.TL_node node = getItem(pos);
            LinearLayout row = (LinearLayout) cv;
            row.setOnClickListener(v -> openNode(node));

            BackupImageView av = (BackupImageView) row.findViewWithTag("av");
            AvatarDrawable ad = new AvatarDrawable();
            ad.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            ad.setInfo(node.id, node.title, null);
            av.setImageDrawable(ad);

            ((TextView) row.findViewWithTag("title")).setText(node.title);
            ((TextView) row.findViewWithTag("sub")).setText(
                    LocaleController.formatString(R.string.NodesMembersOnline, node.members_count, node.online_count));

            return cv;
        }
    }
}
