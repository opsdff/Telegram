package org.telegram.ui.Nodes;

import android.content.Context;
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
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.tl.TL_nodes;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

/** Invite links list for node. Design: Node Links/ */
public class NodeLinksActivity extends BaseFragment {

    private final long nodeId;
    private final ArrayList<TL_nodes.TL_nodeInviteLink> links = new ArrayList<>();
    private LinksAdapter adapter;

    public NodeLinksActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesInviteLinks));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
                else if (id == 1) createNewLink();
            }
        });
        actionBar.createMenu().addItem(1, R.drawable.ic_ab_other);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // New Link button
        LinearLayout newLinkRow = new LinearLayout(context);
        newLinkRow.setOrientation(LinearLayout.HORIZONTAL);
        newLinkRow.setGravity(Gravity.CENTER_VERTICAL);
        newLinkRow.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        newLinkRow.setClickable(true);
        newLinkRow.setBackground(Theme.getSelectorDrawable(false));
        newLinkRow.setOnClickListener(v -> createNewLink());

        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.msg_link);
        icon.setColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton));
        newLinkRow.addView(icon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView tv = new TextView(context);
        tv.setText(LocaleController.getString(R.string.NodesNewLink));
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        newLinkRow.addView(tv);
        root.addView(newLinkRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ListView lv = new ListView(context);
        lv.setDividerHeight(0);
        adapter = new LinksAdapter(context);
        lv.setAdapter(adapter);
        root.addView(lv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f));

        ((FrameLayout) fragmentView).addView(root, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        loadLinks();
        return fragmentView;
    }

    private void loadLinks() {
        TL_nodes.TL_nodes_getInviteLinks req = new TL_nodes.TL_nodes_getInviteLinks();
        req.node_id = nodeId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodes_inviteLinks) {
                links.clear();
                links.addAll(((TL_nodes.TL_nodes_inviteLinks) res).links);
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        }));
    }

    private void createNewLink() {
        TL_nodes.TL_nodes_createInviteLink req = new TL_nodes.TL_nodes_createInviteLink();
        req.node_id = nodeId;
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodeInviteLink) {
                links.add(0, (TL_nodes.TL_nodeInviteLink) res);
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        }));
    }

    private void revokeLink(TL_nodes.TL_nodeInviteLink link) {
        if (getParentActivity() == null) return;
        new AlertDialog.Builder(getParentActivity())
                .setTitle(LocaleController.getString(R.string.NodesRevokeLink))
                .setMessage(LocaleController.getString(R.string.NodesRevokeLinkConfirm))
                .setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> {
                    TL_nodes.TL_nodes_revokeInviteLink req = new TL_nodes.TL_nodes_revokeInviteLink();
                    req.link = link.link;
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        link.revoked = true;
                        if (adapter != null) adapter.notifyDataSetChanged();
                    }));
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private class LinksAdapter extends android.widget.BaseAdapter {
        private final Context context;
        LinksAdapter(Context ctx) { this.context = ctx; }
        @Override public int getCount() { return links.size(); }
        @Override public TL_nodes.TL_nodeInviteLink getItem(int pos) { return links.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View cv, android.view.ViewGroup parent) {
            if (cv == null) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
                row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                LinearLayout col = new LinearLayout(context);
                col.setOrientation(LinearLayout.VERTICAL);
                TextView link = new TextView(context);
                link.setTag("link");
                link.setTextSize(15);
                link.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                link.setSingleLine();
                col.addView(link);
                TextView uses = new TextView(context);
                uses.setTag("uses");
                uses.setTextSize(13);
                uses.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
                col.addView(uses);
                row.addView(col, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                TextView revokeBtn = new TextView(context);
                revokeBtn.setTag("revoke");
                revokeBtn.setText(LocaleController.getString(R.string.NodesRevokeLink));
                revokeBtn.setTextSize(13);
                revokeBtn.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
                row.addView(revokeBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

                cv = row;
            }
            TL_nodes.TL_nodeInviteLink lnk = getItem(pos);
            LinearLayout row = (LinearLayout) cv;
            ((TextView) row.findViewWithTag("link")).setText(lnk.link);
            ((TextView) row.findViewWithTag("uses")).setText(lnk.revoked ? "Revoked" : lnk.usage + " uses");
            TextView revokeBtn = (TextView) row.findViewWithTag("revoke");
            revokeBtn.setVisibility(lnk.revoked ? View.GONE : View.VISIBLE);
            revokeBtn.setOnClickListener(v -> revokeLink(lnk));
            return cv;
        }
    }
}
