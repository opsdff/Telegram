package org.telegram.ui.Nodes;

import android.content.Context;
import android.graphics.Color;
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
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_nodes;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

/**
 * The main "Node mode" screen: top-bar shows node name/avatar,
 * list shows chats, bottom bar has Calls / Chats / Settings tabs.
 * Design reference: Node Chat List/Main.png, Empty (Admin View).png, Empty (Member View).png
 */
public class NodeChatListActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private long nodeId;
    private TL_nodes.TL_nodes_fullNode fullNode;
    private final ArrayList<TL_nodes.TL_nodeChat> chats = new ArrayList<>();

    // Bottom tabs
    private View callsTab, chatsTab, settingsTab;
    private int currentTab = 1; // 0=calls, 1=chats, 2=settings

    // Content views
    private View emptyView;
    private ListView chatListView;
    private NodeChatsAdapter adapter;

    // FAB
    private ImageView fab;

    public NodeChatListActivity(long nodeId) {
        Bundle args = new Bundle();
        args.putLong("node_id", nodeId);
        arguments = args;
        this.nodeId = nodeId;
    }

    public NodeChatListActivity() {
        // for deeplink restore — node_id must be set via NodeController.currentFullNode
    }

    @Override
    public View createView(Context context) {
        // Load full node if not already in controller
        fullNode = NodeController.getInstance(currentAccount).getCurrentFullNode();
        if (fullNode != null) {
            nodeId = fullNode.node.id;
        }

        // ActionBar with hamburger menu (node name) and icons
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(fullNode != null ? fullNode.node.title : "Node");
        actionBar.setSubtitle(fullNode != null
                ? LocaleController.formatString(R.string.NodesMembersOnline,
                fullNode.node.members_count, fullNode.node.online_count)
                : "");
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    // open sidebar / go back
                    NodeController.getInstance(currentAccount).clearCurrentNode();
                    finishFragment();
                } else if (id == 1) {
                    // search
                } else if (id == 2) {
                    // settings icon → node settings
                    if (fullNode != null) {
                        NodeSettingsActivity settings = new NodeSettingsActivity(fullNode.node.id);
                        presentFragment(settings);
                    }
                }
            }
        });
        actionBar.createMenu().addItem(2, R.drawable.ic_ab_other); // settings icon

        // Flat vertical layout (mirrors the working NodesListActivity): list + emptyView
        // as weighted siblings (GONE collapses), tabBar fixed at bottom. No FrameLayout
        // overlay and no elevation — those were intercepting all touch events.
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout root = (FrameLayout) fragmentView;

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);

        // Chat list (weighted)
        chatListView = new ListView(context);
        chatListView.setDividerHeight(0);
        chatListView.setDivider(null);
        chatListView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        adapter = new NodeChatsAdapter(context);
        chatListView.setAdapter(adapter);
        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            TL_nodes.TL_nodeChat chat = adapter.getItem(position);
            if (chat != null) openNodeChat(chat);
        });
        column.addView(chatListView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f));

        // Bottom tab bar — fixed 56dp. chatListView is the SOLE weighted child (mirrors NodesListActivity).
        LinearLayout tabBar = buildTabBar(context);
        column.addView(tabBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56));

        root.addView(column, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // Empty view — GONE by default, only shown when there are no chats; sits above the
        // tab bar (bottom margin 56) so it never intercepts tab taps. GONE => no interception.
        emptyView = buildEmptyView(context);
        root.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 0, 56));

        // FAB (create chat) — only for admins. Sits above the tab bar (72dp).
        fab = new ImageView(context);
        fab.setImageResource(R.drawable.floating_pencil);
        fab.setScaleType(ImageView.ScaleType.CENTER);
        fab.setBackground(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56),
                Theme.getColor(Theme.key_chats_actionBackground),
                Theme.getColor(Theme.key_chats_actionPressedBackground)));
        fab.setOnClickListener(v -> showCreateChatDialog(context));
        root.addView(fab, LayoutHelper.createFrame(56, 56, Gravity.END | Gravity.BOTTOM, 0, 0, 16, 72));

        // Register for updates
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.activeNodeChanged);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.nodeChatListUpdated);

        // Load chats
        if (fullNode != null) {
            chats.clear();
            chats.addAll(fullNode.chats);
            updateView();
        } else if (nodeId != 0) {
            loadFullNode();
        }

        return fragmentView;
    }

    private View buildEmptyView(Context context) {
        LinearLayout empty = new LinearLayout(context);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);
        empty.setVisibility(View.GONE);

        ImageView emptyImg = new ImageView(context);
        emptyImg.setImageResource(R.drawable.msg_info); // placeholder duck
        emptyImg.setColorFilter(Theme.getColor(Theme.key_emptyListPlaceholder));
        empty.addView(emptyImg, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

        TextView emptyTitle = new TextView(context);
        emptyTitle.setId(R.id.title);
        emptyTitle.setText(LocaleController.getString(R.string.NodesEmptyAdminTitle));
        emptyTitle.setTextSize(18);
        emptyTitle.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        emptyTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        emptyTitle.setGravity(Gravity.CENTER);
        empty.addView(emptyTitle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 8));

        TextView emptyDesc = new TextView(context);
        emptyDesc.setId(R.id.description);
        emptyDesc.setTextSize(14);
        emptyDesc.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        emptyDesc.setGravity(Gravity.CENTER);
        empty.addView(emptyDesc, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 0));

        return empty;
    }

    private LinearLayout buildTabBar(Context context) {
        LinearLayout tabBar = new LinearLayout(context);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        callsTab = buildTab(context, R.drawable.msg_calls, LocaleController.getString(R.string.NodesTabCalls), 0);
        chatsTab = buildTab(context, R.drawable.msg_groups, LocaleController.getString(R.string.NodesTabChats), 1);
        settingsTab = buildTab(context, R.drawable.msg_settings, LocaleController.getString(R.string.NodesTabSettings), 2);

        tabBar.addView(callsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        tabBar.addView(chatsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));
        tabBar.addView(settingsTab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));

        return tabBar;
    }

    private View buildTab(Context context, int iconRes, String label, int tabIndex) {
        LinearLayout tab = new LinearLayout(context);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setClickable(true);
        tab.setFocusable(true);
        tab.setBackground(Theme.getSelectorDrawable(false)); // visible ripple on tap

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER);
        icon.setColorFilter(tabIndex == currentTab
                ? Theme.getColor(Theme.key_featuredStickers_addButton)
                : Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        tab.addView(icon, LayoutHelper.createLinear(24, 24, Gravity.CENTER_HORIZONTAL));

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(10);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(tabIndex == currentTab
                ? Theme.getColor(Theme.key_featuredStickers_addButton)
                : Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        tab.addView(tv, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

        tab.setOnClickListener(v -> switchTab(tabIndex));
        return tab;
    }

    private void switchTab(int tab) {
        if (tab == 0) {
            presentFragment(new NodeCallsActivity(nodeId));
        } else if (tab == 1) {
            // Chats tab — already here
        } else if (tab == 2) {
            presentFragment(new NodeSettingsActivity(nodeId));
        }
    }

    private void openNodeChat(TL_nodes.TL_nodeChat chat) {
        // Node chats are served by the node server but are NOT synced into MessagesController
        // as regular TLRPC.Chat objects, so ChatActivity.onFragmentCreate() would return false
        // (chat not found) and silently abort. Build a synthetic channel and cache it first.
        org.telegram.messenger.MessagesController mc = org.telegram.messenger.MessagesController.getInstance(currentAccount);
        NodeController.registerNodeChat(chat.id);  // keep it out of the main chat list
        TLRPC.Chat existing = mc.getChat(chat.id);
        if (existing == null) {
            TLRPC.TL_channel ch = new TLRPC.TL_channel();
            ch.id = chat.id;
            ch.title = chat.title;
            ch.megagroup = chat.is_group;   // group → supergroup (members can post)
            ch.broadcast = !chat.is_group;  // channel → broadcast
            ch.left = false;                 // we are a member (node access already granted)
            ch.access_hash = 0;
            ch.date = chat.created_at;
            mc.putChat(ch, false);
        }

        Bundle args = new Bundle();
        args.putLong("chat_id", chat.id);
        org.telegram.ui.ChatActivity chatActivity = new org.telegram.ui.ChatActivity(args);
        presentFragment(chatActivity);
    }

    private void loadFullNode() {
        NodeController.getInstance(currentAccount).loadFullNode(nodeId, (fn, err) -> {
            if (fn != null) {
                fullNode = fn;
                chats.clear();
                chats.addAll(fn.chats);
                actionBar.setTitle(fn.node.title);
                actionBar.setSubtitle(LocaleController.formatString(R.string.NodesMembersOnline,
                        fn.node.members_count, fn.node.online_count));
            }
            updateView(); // always update — shows empty or error state if fn==null
        });
    }

    private void updateView() {
        boolean isEmpty = chats.isEmpty();
        chatListView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        boolean canCreate = NodeController.getInstance(currentAccount).canCreateChats();
        fab.setVisibility(canCreate ? View.VISIBLE : View.GONE);

        if (isEmpty) {
            // Update empty state text
            TextView title = emptyView.findViewById(R.id.title);
            TextView desc = emptyView.findViewById(R.id.description);
            if (canCreate) {
                if (title != null) title.setText(LocaleController.getString(R.string.NodesEmptyAdminTitle));
                if (desc != null) desc.setText(LocaleController.getString(R.string.NodesEmptyAdminDesc));
            } else {
                if (title != null) title.setText(LocaleController.getString(R.string.NodesEmptyMemberTitle));
                if (desc != null) desc.setText("");
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showCreateChatDialog(Context context) {
        if (fullNode == null) return;
        String[] types = {
                LocaleController.getString(R.string.NodesChannelType),
                LocaleController.getString(R.string.NodesGroupType)
        };
        new AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.NodesCreateChat))
                .setItems(types, (dialog, which) -> showCreateChatNameDialog(context, which == 1))
                .show();
    }

    private void showCreateChatNameDialog(Context context, boolean isGroup) {
        android.widget.EditText et = new android.widget.EditText(context);
        et.setHint(LocaleController.getString(R.string.NodesChatName));
        new AlertDialog.Builder(context)
                .setTitle(LocaleController.getString(R.string.NodesCreateChat))
                .setView(et)
                .setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> {
                    String title = et.getText().toString().trim();
                    if (!title.isEmpty()) {
                        createChat(title, isGroup);
                    }
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void createChat(String title, boolean isGroup) {
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_createChat req = new TL_nodes.TL_nodes_createChat();
        req.node_id = nodeId;
        req.title = title;
        req.is_group = isGroup;
        if (isGroup) req.flags |= 1;

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodeChat) {
                TL_nodes.TL_nodeChat chat = (TL_nodes.TL_nodeChat) res;
                NodeController.registerNodeChat(chat.id);
                chats.add(0, chat);
                if (fullNode != null) fullNode.chats.add(0, chat);
                updateView();
            }
        }));
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.activeNodeChanged) {
            TL_nodes.TL_nodes_fullNode fn = NodeController.getInstance(currentAccount).getCurrentFullNode();
            if (fn != null && fn.node.id == nodeId) {
                fullNode = fn;
                chats.clear();
                chats.addAll(fn.chats);
                updateView();
            }
        } else if (id == NotificationCenter.nodeChatListUpdated) {
            loadFullNode();
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.activeNodeChanged);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.nodeChatListUpdated);
    }

    // ─── Simple adapter for node chats ───────────────────────────────

    private class NodeChatsAdapter extends android.widget.BaseAdapter {
        private final Context context;

        NodeChatsAdapter(Context ctx) {
            this.context = ctx;
        }

        @Override public int getCount() { return chats.size(); }
        @Override public TL_nodes.TL_nodeChat getItem(int pos) { return chats.get(pos); }
        @Override public long getItemId(int pos) { return chats.get(pos).id; }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(10));
                // Clickable row with ripple; block children from stealing the touch
                row.setBackground(Theme.getSelectorDrawable(false));
                row.setClickable(true);
                row.setDescendantFocusability(android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS);

                BackupImageView av = new BackupImageView(context);
                av.setRoundRadius(AndroidUtilities.dp(21));
                av.setTag("avatar");
                row.addView(av, LayoutHelper.createLinear(42, 42, 0, 0, 12, 0));

                LinearLayout textCol = new LinearLayout(context);
                textCol.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTag("title");
                title.setTextSize(16);
                title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                title.setSingleLine();
                textCol.addView(title);

                row.addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));
                convertView = row;
            }

            TL_nodes.TL_nodeChat chat = getItem(position);
            LinearLayout row = (LinearLayout) convertView;
            // Direct click listener — reliable regardless of ListView item-click quirks
            row.setOnClickListener(v -> openNodeChat(chat));

            BackupImageView av = (BackupImageView) row.findViewWithTag("avatar");
            AvatarDrawable ad = new AvatarDrawable();
            ad.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            ad.setText(chat.title.length() > 0 ? String.valueOf(chat.title.charAt(0)) : "#");
            av.setImageDrawable(ad);

            TextView title = (TextView) row.findViewWithTag("title");
            // Show # prefix for channels, no prefix for groups
            String prefix = chat.is_group ? "" : "#";
            title.setText(prefix + chat.title);

            return convertView;
        }
    }
}
