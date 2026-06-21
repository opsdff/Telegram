package org.telegram.tgnet.tl;

import org.telegram.tgnet.InputSerializedData;
import org.telegram.tgnet.OutputSerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class TL_nodes {

    // ─── Base types ───────────────────────────────────────────────────

    public static class TL_node extends TLObject {
        public static final int constructor = 0x4e0d1001;

        public long id;
        public long owner_id;
        public String title;
        public int members_count;
        public int online_count;
        public int created_at;
        public int flags;
        public String description;     // flags.0
        public long photo_id;          // flags.1
        public boolean is_public;      // flags.2
        public String link;            // flags.3
        public String invite_hash;     // flags.4
        public boolean restrict_content; // flags.5
        public boolean is_owner;       // flags.6
        public boolean is_member;      // flags.7
        public boolean verified;       // flags.8

        public static TL_node TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_node.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_node result = new TL_node();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt64(exception);
            owner_id = stream.readInt64(exception);
            title = stream.readString(exception);
            members_count = stream.readInt32(exception);
            online_count = stream.readInt32(exception);
            created_at = stream.readInt32(exception);
            if ((flags & 1) != 0) description = stream.readString(exception);
            if ((flags & 2) != 0) photo_id = stream.readInt64(exception);
            is_public = (flags & 4) != 0;
            if ((flags & 8) != 0) link = stream.readString(exception);
            if ((flags & 16) != 0) invite_hash = stream.readString(exception);
            restrict_content = (flags & 32) != 0;
            is_owner = (flags & 64) != 0;
            is_member = (flags & 128) != 0;
            verified = (flags & 256) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_public ? flags | 4 : flags & ~4;
            flags = restrict_content ? flags | 32 : flags & ~32;
            flags = is_owner ? flags | 64 : flags & ~64;
            flags = is_member ? flags | 128 : flags & ~128;
            flags = verified ? flags | 256 : flags & ~256;
            stream.writeInt32(flags);
            stream.writeInt64(id);
            stream.writeInt64(owner_id);
            stream.writeString(title);
            stream.writeInt32(members_count);
            stream.writeInt32(online_count);
            stream.writeInt32(created_at);
            if ((flags & 1) != 0) stream.writeString(description);
            if ((flags & 2) != 0) stream.writeInt64(photo_id);
            if ((flags & 8) != 0) stream.writeString(link);
            if ((flags & 16) != 0) stream.writeString(invite_hash);
        }
    }

    public static class TL_nodeRole extends TLObject {
        public static final int constructor = 0x4e0d1002;

        public int flags;
        public long id;
        public long node_id;
        public String title;
        public int color;
        public long permissions;
        public int admin_groups_limit;
        public int admin_channels_limit;
        public int ord;
        public int members_count;
        public String icon;     // flags.0
        public boolean is_default; // flags.1
        public boolean is_owner;   // flags.2

        public static TL_nodeRole TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodeRole.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodeRole result = new TL_nodeRole();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt64(exception);
            node_id = stream.readInt64(exception);
            title = stream.readString(exception);
            color = stream.readInt32(exception);
            permissions = stream.readInt64(exception);
            admin_groups_limit = stream.readInt32(exception);
            admin_channels_limit = stream.readInt32(exception);
            ord = stream.readInt32(exception);
            members_count = stream.readInt32(exception);
            if ((flags & 1) != 0) icon = stream.readString(exception);
            is_default = (flags & 2) != 0;
            is_owner = (flags & 4) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_default ? flags | 2 : flags & ~2;
            flags = is_owner ? flags | 4 : flags & ~4;
            stream.writeInt32(flags);
            stream.writeInt64(id);
            stream.writeInt64(node_id);
            stream.writeString(title);
            stream.writeInt32(color);
            stream.writeInt64(permissions);
            stream.writeInt32(admin_groups_limit);
            stream.writeInt32(admin_channels_limit);
            stream.writeInt32(ord);
            stream.writeInt32(members_count);
            if ((flags & 1) != 0) stream.writeString(icon);
        }
    }

    public static class TL_nodeChat extends TLObject {
        public static final int constructor = 0x4e0d1003;

        public int flags;
        public long id;
        public long node_id;
        public String title;
        public int created_at;
        public ArrayList<Long> visible_role_ids = new ArrayList<>();
        public ArrayList<Long> send_role_ids = new ArrayList<>();
        public boolean is_group;      // flags.0
        public boolean is_public;     // flags.1
        public String link;           // flags.2
        public long photo_id;         // flags.3
        public boolean visible_all;   // flags.4
        public boolean send_only_members; // flags.5
        public boolean restrict_content;  // flags.6

        public static TL_nodeChat TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodeChat.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodeChat result = new TL_nodeChat();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt64(exception);
            node_id = stream.readInt64(exception);
            title = stream.readString(exception);
            created_at = stream.readInt32(exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) visible_role_ids.add(stream.readInt64(exception));
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) send_role_ids.add(stream.readInt64(exception));
            is_group = (flags & 1) != 0;
            is_public = (flags & 2) != 0;
            if ((flags & 4) != 0) link = stream.readString(exception);
            if ((flags & 8) != 0) photo_id = stream.readInt64(exception);
            visible_all = (flags & 16) != 0;
            send_only_members = (flags & 32) != 0;
            restrict_content = (flags & 64) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_group ? flags | 1 : flags & ~1;
            flags = is_public ? flags | 2 : flags & ~2;
            flags = visible_all ? flags | 16 : flags & ~16;
            flags = send_only_members ? flags | 32 : flags & ~32;
            flags = restrict_content ? flags | 64 : flags & ~64;
            stream.writeInt32(flags);
            stream.writeInt64(id);
            stream.writeInt64(node_id);
            stream.writeString(title);
            stream.writeInt32(created_at);
            stream.writeInt32(visible_role_ids.size());
            for (Long v : visible_role_ids) stream.writeInt64(v);
            stream.writeInt32(send_role_ids.size());
            for (Long v : send_role_ids) stream.writeInt64(v);
            if ((flags & 4) != 0) stream.writeString(link);
            if ((flags & 8) != 0) stream.writeInt64(photo_id);
        }
    }

    public static class TL_nodeMember extends TLObject {
        public static final int constructor = 0x4e0d1004;

        public int flags;
        public long user_id;
        public int joined_at;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public boolean anonymous;   // flags.0
        public String first_name;   // flags.1
        public String last_name;    // flags.2
        public String bio;          // flags.3
        public long photo_id;       // flags.4

        public static TL_nodeMember TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodeMember.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodeMember result = new TL_nodeMember();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            user_id = stream.readInt64(exception);
            joined_at = stream.readInt32(exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) role_ids.add(stream.readInt64(exception));
            anonymous = (flags & 1) != 0;
            if ((flags & 2) != 0) first_name = stream.readString(exception);
            if ((flags & 4) != 0) last_name = stream.readString(exception);
            if ((flags & 8) != 0) bio = stream.readString(exception);
            if ((flags & 16) != 0) photo_id = stream.readInt64(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = anonymous ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(user_id);
            stream.writeInt32(joined_at);
            stream.writeInt32(role_ids.size());
            for (Long v : role_ids) stream.writeInt64(v);
            if ((flags & 2) != 0) stream.writeString(first_name);
            if ((flags & 4) != 0) stream.writeString(last_name);
            if ((flags & 8) != 0) stream.writeString(bio);
            if ((flags & 16) != 0) stream.writeInt64(photo_id);
        }
    }

    public static class TL_nodeProfile extends TLObject {
        public static final int constructor = 0x4e0d1005;

        public int flags;
        public long user_id;
        public int show_main_profile;
        public int share_contact;
        public String first_name;  // flags.0
        public String last_name;   // flags.1
        public String bio;         // flags.2
        public long photo_id;      // flags.3

        public static TL_nodeProfile TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodeProfile.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodeProfile result = new TL_nodeProfile();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            user_id = stream.readInt64(exception);
            show_main_profile = stream.readInt32(exception);
            share_contact = stream.readInt32(exception);
            if ((flags & 1) != 0) first_name = stream.readString(exception);
            if ((flags & 2) != 0) last_name = stream.readString(exception);
            if ((flags & 4) != 0) bio = stream.readString(exception);
            if ((flags & 8) != 0) photo_id = stream.readInt64(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(user_id);
            stream.writeInt32(show_main_profile);
            stream.writeInt32(share_contact);
            if ((flags & 1) != 0) stream.writeString(first_name);
            if ((flags & 2) != 0) stream.writeString(last_name);
            if ((flags & 4) != 0) stream.writeString(bio);
            if ((flags & 8) != 0) stream.writeInt64(photo_id);
        }
    }

    public static class TL_nodeInviteLink extends TLObject {
        public static final int constructor = 0x4e0d1006;

        public int flags;
        public String link;
        public int usage;
        public int created_at;
        public ArrayList<Long> allowed_role_ids = new ArrayList<>();
        public long assign_role_id;  // flags.0
        public int expires;          // flags.1
        public int usage_limit;      // flags.2
        public String name;          // flags.3
        public boolean revoked;      // flags.4

        public static TL_nodeInviteLink TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodeInviteLink.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodeInviteLink result = new TL_nodeInviteLink();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            link = stream.readString(exception);
            usage = stream.readInt32(exception);
            created_at = stream.readInt32(exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) allowed_role_ids.add(stream.readInt64(exception));
            if ((flags & 1) != 0) assign_role_id = stream.readInt64(exception);
            if ((flags & 2) != 0) expires = stream.readInt32(exception);
            if ((flags & 4) != 0) usage_limit = stream.readInt32(exception);
            if ((flags & 8) != 0) name = stream.readString(exception);
            revoked = (flags & 16) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = revoked ? flags | 16 : flags & ~16;
            stream.writeInt32(flags);
            stream.writeString(link);
            stream.writeInt32(usage);
            stream.writeInt32(created_at);
            stream.writeInt32(allowed_role_ids.size());
            for (Long v : allowed_role_ids) stream.writeInt64(v);
            if ((flags & 1) != 0) stream.writeInt64(assign_role_id);
            if ((flags & 2) != 0) stream.writeInt32(expires);
            if ((flags & 4) != 0) stream.writeInt32(usage_limit);
            if ((flags & 8) != 0) stream.writeString(name);
        }
    }

    public static class TL_nodeLocalAdmin extends TLObject {
        public static final int constructor = 0x4e0d1007;

        public int flags;
        public long user_id;
        public long promoted_by;  // flags.0
        public boolean is_owner;  // flags.1
        public long role_id;      // flags.2

        public static TL_nodeLocalAdmin TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodeLocalAdmin.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodeLocalAdmin result = new TL_nodeLocalAdmin();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            user_id = stream.readInt64(exception);
            if ((flags & 1) != 0) promoted_by = stream.readInt64(exception);
            is_owner = (flags & 2) != 0;
            if ((flags & 4) != 0) role_id = stream.readInt64(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_owner ? flags | 2 : flags & ~2;
            stream.writeInt32(flags);
            stream.writeInt64(user_id);
            if ((flags & 1) != 0) stream.writeInt64(promoted_by);
            if ((flags & 4) != 0) stream.writeInt64(role_id);
        }
    }

    // ─── Voice/Video types ────────────────────────────────────────────

    public static class TL_rtcIceServer extends TLObject {
        public static final int constructor = 0x4e0d1201;

        public int flags;
        public String urls;
        public String username;   // flags.0
        public String credential; // flags.1

        public static TL_rtcIceServer TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_rtcIceServer.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_rtcIceServer result = new TL_rtcIceServer();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            urls = stream.readString(exception);
            if ((flags & 1) != 0) username = stream.readString(exception);
            if ((flags & 2) != 0) credential = stream.readString(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeString(urls);
            if ((flags & 1) != 0) stream.writeString(username);
            if ((flags & 2) != 0) stream.writeString(credential);
        }
    }

    public static class TL_voiceParticipant extends TLObject {
        public static final int constructor = 0x4e0d1202;

        public int flags;
        public long user_id;
        public int joined_at;
        public boolean muted;    // flags.0
        public boolean video;    // flags.1
        public boolean screen;   // flags.2
        public boolean hand;     // flags.3
        public boolean speaking; // flags.4

        public static TL_voiceParticipant TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_voiceParticipant.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_voiceParticipant result = new TL_voiceParticipant();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            user_id = stream.readInt64(exception);
            joined_at = stream.readInt32(exception);
            muted = (flags & 1) != 0;
            video = (flags & 2) != 0;
            screen = (flags & 4) != 0;
            hand = (flags & 8) != 0;
            speaking = (flags & 16) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = muted ? flags | 1 : flags & ~1;
            flags = video ? flags | 2 : flags & ~2;
            flags = screen ? flags | 4 : flags & ~4;
            flags = hand ? flags | 8 : flags & ~8;
            flags = speaking ? flags | 16 : flags & ~16;
            stream.writeInt32(flags);
            stream.writeInt64(user_id);
            stream.writeInt32(joined_at);
        }
    }

    public static class TL_voiceRoom extends TLObject {
        public static final int constructor = 0x4e0d1203;

        public int flags;
        public long id;
        public long node_id;
        public String title;
        public int participants_count;
        public int created_at;
        public ArrayList<Long> visible_role_ids = new ArrayList<>();
        public long chat_id;       // flags.0
        public boolean video;      // flags.1
        public boolean e2e;        // flags.2
        public boolean visible_all; // flags.3
        public boolean active;     // flags.4
        public boolean is_call;    // flags.5

        public static TL_voiceRoom TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_voiceRoom.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_voiceRoom result = new TL_voiceRoom();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt64(exception);
            node_id = stream.readInt64(exception);
            title = stream.readString(exception);
            participants_count = stream.readInt32(exception);
            created_at = stream.readInt32(exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) visible_role_ids.add(stream.readInt64(exception));
            if ((flags & 1) != 0) chat_id = stream.readInt64(exception);
            video = (flags & 2) != 0;
            e2e = (flags & 4) != 0;
            visible_all = (flags & 8) != 0;
            active = (flags & 16) != 0;
            is_call = (flags & 32) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = video ? flags | 2 : flags & ~2;
            flags = e2e ? flags | 4 : flags & ~4;
            flags = visible_all ? flags | 8 : flags & ~8;
            flags = active ? flags | 16 : flags & ~16;
            flags = is_call ? flags | 32 : flags & ~32;
            stream.writeInt32(flags);
            stream.writeInt64(id);
            stream.writeInt64(node_id);
            stream.writeString(title);
            stream.writeInt32(participants_count);
            stream.writeInt32(created_at);
            stream.writeInt32(visible_role_ids.size());
            for (Long v : visible_role_ids) stream.writeInt64(v);
            if ((flags & 1) != 0) stream.writeInt64(chat_id);
        }
    }

    // ─── Result containers ────────────────────────────────────────────

    public static class TL_nodes_nodes extends TLObject {
        public static final int constructor = 0x4e0d1101;

        public ArrayList<TL_node> nodes = new ArrayList<>();

        public static TL_nodes_nodes TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_nodes.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_nodes result = new TL_nodes_nodes();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_node n = TL_node.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (n != null) nodes.add(n);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(nodes.size());
            for (TL_node n : nodes) n.serializeToStream(stream);
        }
    }

    public static class TL_nodes_fullNode extends TLObject {
        public static final int constructor = 0x4e0d1102;

        public int flags;
        public TL_node node;
        public ArrayList<TL_nodeRole> roles = new ArrayList<>();
        public ArrayList<TL_nodeChat> chats = new ArrayList<>();
        public ArrayList<TL_nodeMember> members = new ArrayList<>();
        public ArrayList<TLRPC.User> users = new ArrayList<>();
        public ArrayList<Long> my_role_ids = new ArrayList<>();
        public TL_nodeProfile my_profile; // flags.0

        public static TL_nodes_fullNode TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_fullNode.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_fullNode result = new TL_nodes_fullNode();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            node = TL_node.TLdeserialize(stream, stream.readInt32(exception), exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeRole r = TL_nodeRole.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (r != null) roles.add(r);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeChat c = TL_nodeChat.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (c != null) chats.add(c);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeMember m = TL_nodeMember.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (m != null) members.add(m);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TLRPC.User u = TLRPC.User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (u != null) users.add(u);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) my_role_ids.add(stream.readInt64(exception));
            if ((flags & 1) != 0) my_profile = TL_nodeProfile.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            node.serializeToStream(stream);
            stream.writeInt32(roles.size());
            for (TL_nodeRole r : roles) r.serializeToStream(stream);
            stream.writeInt32(chats.size());
            for (TL_nodeChat c : chats) c.serializeToStream(stream);
            stream.writeInt32(members.size());
            for (TL_nodeMember m : members) m.serializeToStream(stream);
            stream.writeInt32(users.size());
            for (TLRPC.User u : users) u.serializeToStream(stream);
            stream.writeInt32(my_role_ids.size());
            for (Long v : my_role_ids) stream.writeInt64(v);
            if ((flags & 1) != 0) my_profile.serializeToStream(stream);
        }
    }

    public static class TL_nodes_members extends TLObject {
        public static final int constructor = 0x4e0d1103;

        public int count;
        public ArrayList<TL_nodeMember> members = new ArrayList<>();
        public ArrayList<TLRPC.User> users = new ArrayList<>();

        public static TL_nodes_members TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_members.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_members result = new TL_nodes_members();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            count = stream.readInt32(exception);
            int c = stream.readInt32(exception);
            for (int i = 0; i < c; i++) {
                TL_nodeMember m = TL_nodeMember.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (m != null) members.add(m);
            }
            c = stream.readInt32(exception);
            for (int i = 0; i < c; i++) {
                TLRPC.User u = TLRPC.User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (u != null) users.add(u);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(count);
            stream.writeInt32(members.size());
            for (TL_nodeMember m : members) m.serializeToStream(stream);
            stream.writeInt32(users.size());
            for (TLRPC.User u : users) u.serializeToStream(stream);
        }
    }

    public static class TL_nodes_roles extends TLObject {
        public static final int constructor = 0x4e0d1104;
        public ArrayList<TL_nodeRole> roles = new ArrayList<>();

        public static TL_nodes_roles TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_roles.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_roles result = new TL_nodes_roles();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeRole r = TL_nodeRole.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (r != null) roles.add(r);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(roles.size());
            for (TL_nodeRole r : roles) r.serializeToStream(stream);
        }
    }

    public static class TL_nodes_chats extends TLObject {
        public static final int constructor = 0x4e0d1105;
        public ArrayList<TL_nodeChat> chats = new ArrayList<>();

        public static TL_nodes_chats TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_chats.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_chats result = new TL_nodes_chats();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeChat c = TL_nodeChat.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (c != null) chats.add(c);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chats.size());
            for (TL_nodeChat c : chats) c.serializeToStream(stream);
        }
    }

    public static class TL_nodes_inviteLinks extends TLObject {
        public static final int constructor = 0x4e0d1106;
        public ArrayList<TL_nodeInviteLink> links = new ArrayList<>();

        public static TL_nodes_inviteLinks TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_inviteLinks.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_inviteLinks result = new TL_nodes_inviteLinks();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeInviteLink l = TL_nodeInviteLink.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (l != null) links.add(l);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(links.size());
            for (TL_nodeInviteLink l : links) l.serializeToStream(stream);
        }
    }

    public static class TL_nodes_searchResults extends TLObject {
        public static final int constructor = 0x4e0d1107;
        public ArrayList<TL_node> joined = new ArrayList<>();
        public ArrayList<TL_node> similar = new ArrayList<>();

        public static TL_nodes_searchResults TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_searchResults.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_searchResults result = new TL_nodes_searchResults();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_node n = TL_node.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (n != null) joined.add(n);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_node n = TL_node.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (n != null) similar.add(n);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(joined.size());
            for (TL_node n : joined) n.serializeToStream(stream);
            stream.writeInt32(similar.size());
            for (TL_node n : similar) n.serializeToStream(stream);
        }
    }

    public static class TL_nodes_memberProfile extends TLObject {
        public static final int constructor = 0x4e0d1108;

        public int flags;
        public TL_nodeProfile profile;
        public TLRPC.User user;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public long main_user_id; // flags.0

        public static TL_nodes_memberProfile TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_memberProfile.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_memberProfile result = new TL_nodes_memberProfile();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            profile = TL_nodeProfile.TLdeserialize(stream, stream.readInt32(exception), exception);
            user = TLRPC.User.TLdeserialize(stream, stream.readInt32(exception), exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) role_ids.add(stream.readInt64(exception));
            if ((flags & 1) != 0) main_user_id = stream.readInt64(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            profile.serializeToStream(stream);
            user.serializeToStream(stream);
            stream.writeInt32(role_ids.size());
            for (Long v : role_ids) stream.writeInt64(v);
            if ((flags & 1) != 0) stream.writeInt64(main_user_id);
        }
    }

    public static class TL_nodes_nodePreview extends TLObject {
        public static final int constructor = 0x4e0d1109;

        public int flags;
        public TL_node node;
        public ArrayList<TLRPC.User> members = new ArrayList<>();
        public String description; // flags.0

        public static TL_nodes_nodePreview TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_nodePreview.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_nodePreview result = new TL_nodes_nodePreview();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            node = TL_node.TLdeserialize(stream, stream.readInt32(exception), exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TLRPC.User u = TLRPC.User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (u != null) members.add(u);
            }
            if ((flags & 1) != 0) description = stream.readString(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            node.serializeToStream(stream);
            stream.writeInt32(members.size());
            for (TLRPC.User u : members) u.serializeToStream(stream);
            if ((flags & 1) != 0) stream.writeString(description);
        }
    }

    public static class TL_nodes_chatAdmins extends TLObject {
        public static final int constructor = 0x4e0d110a;

        public ArrayList<TL_nodeLocalAdmin> node_admins = new ArrayList<>();
        public ArrayList<TL_nodeLocalAdmin> local_admins = new ArrayList<>();
        public ArrayList<TLRPC.User> users = new ArrayList<>();

        public static TL_nodes_chatAdmins TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_chatAdmins.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_chatAdmins result = new TL_nodes_chatAdmins();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeLocalAdmin a = TL_nodeLocalAdmin.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (a != null) node_admins.add(a);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_nodeLocalAdmin a = TL_nodeLocalAdmin.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (a != null) local_admins.add(a);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TLRPC.User u = TLRPC.User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (u != null) users.add(u);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(node_admins.size());
            for (TL_nodeLocalAdmin a : node_admins) a.serializeToStream(stream);
            stream.writeInt32(local_admins.size());
            for (TL_nodeLocalAdmin a : local_admins) a.serializeToStream(stream);
            stream.writeInt32(users.size());
            for (TLRPC.User u : users) u.serializeToStream(stream);
        }
    }

    public static class TL_nodes_voiceRoom extends TLObject {
        public static final int constructor = 0x4e0d1204;

        public int flags;
        public TL_voiceRoom room;
        public String rtc_url;
        public String rtc_token;
        public ArrayList<TL_rtcIceServer> ice_servers = new ArrayList<>();
        public ArrayList<TL_voiceParticipant> participants = new ArrayList<>();
        public ArrayList<TLRPC.User> users = new ArrayList<>();
        public boolean can_publish; // flags.0
        public long call_id;        // flags.1

        public static TL_nodes_voiceRoom TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_voiceRoom.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_voiceRoom result = new TL_nodes_voiceRoom();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            room = TL_voiceRoom.TLdeserialize(stream, stream.readInt32(exception), exception);
            rtc_url = stream.readString(exception);
            rtc_token = stream.readString(exception);
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_rtcIceServer s = TL_rtcIceServer.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (s != null) ice_servers.add(s);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_voiceParticipant p = TL_voiceParticipant.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (p != null) participants.add(p);
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TLRPC.User u = TLRPC.User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (u != null) users.add(u);
            }
            can_publish = (flags & 1) != 0;
            if ((flags & 2) != 0) call_id = stream.readInt64(exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = can_publish ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            room.serializeToStream(stream);
            stream.writeString(rtc_url);
            stream.writeString(rtc_token);
            stream.writeInt32(ice_servers.size());
            for (TL_rtcIceServer s : ice_servers) s.serializeToStream(stream);
            stream.writeInt32(participants.size());
            for (TL_voiceParticipant p : participants) p.serializeToStream(stream);
            stream.writeInt32(users.size());
            for (TLRPC.User u : users) u.serializeToStream(stream);
            if ((flags & 2) != 0) stream.writeInt64(call_id);
        }
    }

    public static class TL_nodes_voiceRooms extends TLObject {
        public static final int constructor = 0x4e0d1205;
        public ArrayList<TL_voiceRoom> rooms = new ArrayList<>();

        public static TL_nodes_voiceRooms TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_nodes_voiceRooms.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_nodes_voiceRooms result = new TL_nodes_voiceRooms();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                TL_voiceRoom r = TL_voiceRoom.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (r != null) rooms.add(r);
            }
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(rooms.size());
            for (TL_voiceRoom r : rooms) r.serializeToStream(stream);
        }
    }

    public static class TL_updateNodeCall extends TLObject {
        public static final int constructor = 0x4e0d1206;

        public int flags;
        public long call_id;
        public long room_id;
        public long from_id;
        public String action;
        public boolean video; // flags.0

        public static TL_updateNodeCall TLdeserialize(InputSerializedData stream, int constructor, boolean exception) {
            if (constructor != TL_updateNodeCall.constructor) {
                if (exception) throw new RuntimeException("can't parse magic " + Integer.toHexString(constructor));
                return null;
            }
            TL_updateNodeCall result = new TL_updateNodeCall();
            result.readParams(stream, exception);
            return result;
        }

        @Override
        public void readParams(InputSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            call_id = stream.readInt64(exception);
            room_id = stream.readInt64(exception);
            from_id = stream.readInt64(exception);
            action = stream.readString(exception);
            video = (flags & 1) != 0;
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = video ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(call_id);
            stream.writeInt64(room_id);
            stream.writeInt64(from_id);
            stream.writeString(action);
        }
    }

    // ─── Node methods ─────────────────────────────────────────────────

    public static class TL_nodes_createNode extends TLObject {
        public static final int constructor = 0x4e0d2001;
        public int flags;
        public String title;
        public String description;  // flags.0
        public long photo_id;       // flags.1

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_fullNode.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeString(title);
            if ((flags & 1) != 0) stream.writeString(description);
            if ((flags & 2) != 0) stream.writeInt64(photo_id);
        }
    }

    public static class TL_nodes_getNodes extends TLObject {
        public static final int constructor = 0x4e0d2002;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_nodes.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_nodes_getFullNode extends TLObject {
        public static final int constructor = 0x4e0d2003;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_fullNode.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_editNode extends TLObject {
        public static final int constructor = 0x4e0d2004;
        public int flags;
        public long node_id;
        public String title;        // flags.0
        public String description;  // flags.1
        public long photo_id;       // flags.2

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_fullNode.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            if ((flags & 1) != 0) stream.writeString(title);
            if ((flags & 2) != 0) stream.writeString(description);
            if ((flags & 4) != 0) stream.writeInt64(photo_id);
        }
    }

    public static class TL_nodes_deleteNode extends TLObject {
        public static final int constructor = 0x4e0d2005;
        public int flags;
        public long node_id;
        public boolean for_all; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = for_all ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_leaveNode extends TLObject {
        public static final int constructor = 0x4e0d2006;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_setNodeType extends TLObject {
        public static final int constructor = 0x4e0d2007;
        public int flags;
        public long node_id;
        public boolean is_public; // flags.0
        public String link;       // flags.1

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_fullNode.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_public ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            if ((flags & 2) != 0) stream.writeString(link);
        }
    }

    public static class TL_nodes_checkPublicLink extends TLObject {
        public static final int constructor = 0x4e0d2008;
        public String link;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(link);
        }
    }

    public static class TL_nodes_resolveNode extends TLObject {
        public static final int constructor = 0x4e0d2009;
        public String link;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_nodePreview.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(link);
        }
    }

    public static class TL_nodes_joinNode extends TLObject {
        public static final int constructor = 0x4e0d200a;
        public String link;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_fullNode.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(link);
        }
    }

    public static class TL_nodes_toggleContentProtection extends TLObject {
        public static final int constructor = 0x4e0d200b;
        public int flags;
        public long node_id;
        public boolean enabled; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = enabled ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_searchNodes extends TLObject {
        public static final int constructor = 0x4e0d200c;
        public String q;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_searchResults.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(q);
        }
    }

    // ─── Profile & members methods ────────────────────────────────────

    public static class TL_nodes_getNodeProfile extends TLObject {
        public static final int constructor = 0x4e0d200d;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeProfile.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_updateNodeProfile extends TLObject {
        public static final int constructor = 0x4e0d200e;
        public int flags;
        public long node_id;
        public int show_main_profile;
        public int share_contact;
        public String first_name;  // flags.0
        public String last_name;   // flags.1
        public String bio;         // flags.2
        public long photo_id;      // flags.3

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeProfile.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            stream.writeInt32(show_main_profile);
            stream.writeInt32(share_contact);
            if ((flags & 1) != 0) stream.writeString(first_name);
            if ((flags & 2) != 0) stream.writeString(last_name);
            if ((flags & 4) != 0) stream.writeString(bio);
            if ((flags & 8) != 0) stream.writeInt64(photo_id);
        }
    }

    public static class TL_nodes_getMembers extends TLObject {
        public static final int constructor = 0x4e0d200f;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_members.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_inviteMembers extends TLObject {
        public static final int constructor = 0x4e0d2010;
        public long node_id;
        public ArrayList<Long> user_ids = new ArrayList<>();

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
            stream.writeInt32(user_ids.size());
            for (Long id : user_ids) stream.writeInt64(id);
        }
    }

    public static class TL_nodes_removeMember extends TLObject {
        public static final int constructor = 0x4e0d2011;
        public long node_id;
        public long user_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
            stream.writeInt64(user_id);
        }
    }

    public static class TL_nodes_getMemberProfile extends TLObject {
        public static final int constructor = 0x4e0d2012;
        public long node_id;
        public long user_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_memberProfile.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
            stream.writeInt64(user_id);
        }
    }

    // ─── Roles methods ────────────────────────────────────────────────

    public static class TL_nodes_getRoles extends TLObject {
        public static final int constructor = 0x4e0d2013;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_roles.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_createRole extends TLObject {
        public static final int constructor = 0x4e0d2014;
        public int flags;
        public long node_id;
        public String title;
        public int color;
        public long permissions;
        public int admin_groups_limit;
        public int admin_channels_limit;
        public ArrayList<Long> user_ids = new ArrayList<>();
        public String icon; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeRole.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            stream.writeString(title);
            stream.writeInt32(color);
            stream.writeInt64(permissions);
            stream.writeInt32(admin_groups_limit);
            stream.writeInt32(admin_channels_limit);
            stream.writeInt32(user_ids.size());
            for (Long id : user_ids) stream.writeInt64(id);
            if ((flags & 1) != 0) stream.writeString(icon);
        }
    }

    public static class TL_nodes_editRole extends TLObject {
        public static final int constructor = 0x4e0d2015;
        public int flags;
        public long role_id;
        public String title;              // flags.0
        public int color;                 // flags.1
        public long permissions;          // flags.2
        public int admin_groups_limit;    // flags.3
        public int admin_channels_limit;  // flags.4
        public String icon;               // flags.5

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeRole.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(role_id);
            if ((flags & 1) != 0) stream.writeString(title);
            if ((flags & 2) != 0) stream.writeInt32(color);
            if ((flags & 4) != 0) stream.writeInt64(permissions);
            if ((flags & 8) != 0) stream.writeInt32(admin_groups_limit);
            if ((flags & 16) != 0) stream.writeInt32(admin_channels_limit);
            if ((flags & 32) != 0) stream.writeString(icon);
        }
    }

    public static class TL_nodes_deleteRole extends TLObject {
        public static final int constructor = 0x4e0d2016;
        public long role_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(role_id);
        }
    }

    public static class TL_nodes_getRoleUsers extends TLObject {
        public static final int constructor = 0x4e0d2017;
        public long role_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_members.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(role_id);
        }
    }

    public static class TL_nodes_assignRole extends TLObject {
        public static final int constructor = 0x4e0d2018;
        public long role_id;
        public ArrayList<Long> user_ids = new ArrayList<>();

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(role_id);
            stream.writeInt32(user_ids.size());
            for (Long id : user_ids) stream.writeInt64(id);
        }
    }

    public static class TL_nodes_unassignRole extends TLObject {
        public static final int constructor = 0x4e0d2019;
        public long role_id;
        public long user_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(role_id);
            stream.writeInt64(user_id);
        }
    }

    public static class TL_nodes_reorderRoles extends TLObject {
        public static final int constructor = 0x4e0d201a;
        public long node_id;
        public ArrayList<Long> role_ids = new ArrayList<>();

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
            stream.writeInt32(role_ids.size());
            for (Long id : role_ids) stream.writeInt64(id);
        }
    }

    // ─── Chat methods ─────────────────────────────────────────────────

    public static class TL_nodes_createChat extends TLObject {
        public static final int constructor = 0x4e0d201b;
        public int flags;
        public long node_id;
        public String title;
        public boolean is_group; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeChat.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_group ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            stream.writeString(title);
        }
    }

    public static class TL_nodes_getChats extends TLObject {
        public static final int constructor = 0x4e0d201c;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_chats.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_editChatVisibility extends TLObject {
        public static final int constructor = 0x4e0d201d;
        public int flags;
        public long chat_id;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public boolean visible_all; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeChat.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = visible_all ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(chat_id);
            stream.writeInt32(role_ids.size());
            for (Long id : role_ids) stream.writeInt64(id);
        }
    }

    public static class TL_nodes_setChatType extends TLObject {
        public static final int constructor = 0x4e0d201e;
        public int flags;
        public long chat_id;
        public boolean is_public; // flags.0
        public String link;       // flags.1

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeChat.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = is_public ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(chat_id);
            if ((flags & 2) != 0) stream.writeString(link);
        }
    }

    public static class TL_nodes_setGroupSendRoles extends TLObject {
        public static final int constructor = 0x4e0d201f;
        public int flags;
        public long chat_id;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public boolean only_members; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeChat.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = only_members ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(chat_id);
            stream.writeInt32(role_ids.size());
            for (Long id : role_ids) stream.writeInt64(id);
        }
    }

    public static class TL_nodes_getChatAdmins extends TLObject {
        public static final int constructor = 0x4e0d2020;
        public long chat_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_chatAdmins.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(chat_id);
        }
    }

    public static class TL_nodes_addLocalAdmin extends TLObject {
        public static final int constructor = 0x4e0d2021;
        public long chat_id;
        public long user_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(chat_id);
            stream.writeInt64(user_id);
        }
    }

    public static class TL_nodes_removeLocalAdmin extends TLObject {
        public static final int constructor = 0x4e0d2022;
        public long chat_id;
        public long user_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(chat_id);
            stream.writeInt64(user_id);
        }
    }

    // ─── Invite links methods ─────────────────────────────────────────

    public static class TL_nodes_createInviteLink extends TLObject {
        public static final int constructor = 0x4e0d2023;
        public int flags;
        public long node_id;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public long chat_id;         // flags.0
        public long assign_role_id;  // flags.1
        public int expires;          // flags.2
        public int usage_limit;      // flags.3
        public String name;          // flags.4

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodeInviteLink.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            stream.writeInt32(role_ids.size());
            for (Long id : role_ids) stream.writeInt64(id);
            if ((flags & 1) != 0) stream.writeInt64(chat_id);
            if ((flags & 2) != 0) stream.writeInt64(assign_role_id);
            if ((flags & 4) != 0) stream.writeInt32(expires);
            if ((flags & 8) != 0) stream.writeInt32(usage_limit);
            if ((flags & 16) != 0) stream.writeString(name);
        }
    }

    public static class TL_nodes_getInviteLinks extends TLObject {
        public static final int constructor = 0x4e0d2024;
        public int flags;
        public long node_id;
        public long chat_id; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_inviteLinks.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            if ((flags & 1) != 0) stream.writeInt64(chat_id);
        }
    }

    public static class TL_nodes_revokeInviteLink extends TLObject {
        public static final int constructor = 0x4e0d2025;
        public String link;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(link);
        }
    }

    // ─── Voice/call methods ───────────────────────────────────────────

    public static class TL_nodes_createVoiceRoom extends TLObject {
        public static final int constructor = 0x4e0d2026;
        public int flags;
        public long node_id;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public long chat_id;    // flags.0
        public String title;    // flags.1
        public boolean video;   // flags.2
        public boolean e2e;     // flags.3
        public boolean visible_all; // flags.4

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = video ? flags | 4 : flags & ~4;
            flags = e2e ? flags | 8 : flags & ~8;
            flags = visible_all ? flags | 16 : flags & ~16;
            stream.writeInt32(flags);
            stream.writeInt64(node_id);
            stream.writeInt32(role_ids.size());
            for (Long id : role_ids) stream.writeInt64(id);
            if ((flags & 1) != 0) stream.writeInt64(chat_id);
            if ((flags & 2) != 0) stream.writeString(title);
        }
    }

    public static class TL_nodes_getVoiceRooms extends TLObject {
        public static final int constructor = 0x4e0d2027;
        public long node_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRooms.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(node_id);
        }
    }

    public static class TL_nodes_joinVoiceRoom extends TLObject {
        public static final int constructor = 0x4e0d2028;
        public int flags;
        public long room_id;
        public boolean video; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = video ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(room_id);
        }
    }

    public static class TL_nodes_leaveVoiceRoom extends TLObject {
        public static final int constructor = 0x4e0d2029;
        public long room_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(room_id);
        }
    }

    public static class TL_nodes_getVoiceRoom extends TLObject {
        public static final int constructor = 0x4e0d202a;
        public long room_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(room_id);
        }
    }

    public static class TL_nodes_voiceRoomSetMedia extends TLObject {
        public static final int constructor = 0x4e0d202b;
        public int flags;
        public long room_id;
        public TLRPC.Bool muted;  // flags.0
        public TLRPC.Bool video;  // flags.1
        public TLRPC.Bool screen; // flags.2
        public TLRPC.Bool hand;   // flags.3

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt64(room_id);
            if ((flags & 1) != 0) muted.serializeToStream(stream);
            if ((flags & 2) != 0) video.serializeToStream(stream);
            if ((flags & 4) != 0) screen.serializeToStream(stream);
            if ((flags & 8) != 0) hand.serializeToStream(stream);
        }
    }

    public static class TL_nodes_editVoiceRoomVisibility extends TLObject {
        public static final int constructor = 0x4e0d202c;
        public int flags;
        public long room_id;
        public ArrayList<Long> role_ids = new ArrayList<>();
        public boolean visible_all; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = visible_all ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(room_id);
            stream.writeInt32(role_ids.size());
            for (Long id : role_ids) stream.writeInt64(id);
        }
    }

    public static class TL_nodes_endVoiceRoom extends TLObject {
        public static final int constructor = 0x4e0d202d;
        public long room_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(room_id);
        }
    }

    public static class TL_nodes_startCall extends TLObject {
        public static final int constructor = 0x4e0d202e;
        public int flags;
        public long user_id;
        public boolean video; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = video ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(user_id);
        }
    }

    public static class TL_nodes_acceptCall extends TLObject {
        public static final int constructor = 0x4e0d202f;
        public int flags;
        public long call_id;
        public boolean video; // flags.0

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TL_nodes_voiceRoom.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            flags = video ? flags | 1 : flags & ~1;
            stream.writeInt32(flags);
            stream.writeInt64(call_id);
        }
    }

    public static class TL_nodes_declineCall extends TLObject {
        public static final int constructor = 0x4e0d2030;
        public long call_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(call_id);
        }
    }

    public static class TL_nodes_discardCall extends TLObject {
        public static final int constructor = 0x4e0d2031;
        public long call_id;

        @Override
        public TLObject deserializeResponse(InputSerializedData stream, int constructor, boolean exception) {
            return TLRPC.Bool.TLdeserialize(stream, constructor, exception);
        }

        @Override
        public void serializeToStream(OutputSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(call_id);
        }
    }

    // ─── Permissions constants ────────────────────────────────────────

    public static final long PERM_CHANGE_NODE_INFO    = 1L;
    public static final long PERM_CREATE_OWN_CHATS    = 2L;
    public static final long PERM_ADMIN_GROUPS        = 4L;
    public static final long PERM_ADMIN_CHANNELS      = 8L;
    public static final long PERM_DELETE_CHATS        = 16L;
    public static final long PERM_PIN_CHATS           = 32L;
    public static final long PERM_MANAGE_FOLDERS      = 64L;
    public static final long PERM_BAN_USERS           = 128L;
    public static final long PERM_ADD_MEMBERS         = 256L;
    public static final long PERM_REMAIN_ANONYMOUS    = 512L;
    public static final long PERM_MANAGE_ROLES        = 1024L;

    public static final long PERM_MEMBER_DEFAULT = PERM_CREATE_OWN_CHATS | PERM_ADD_MEMBERS;
    public static final long PERM_OWNER_ALL      = 0x7FFL;

    // show_main_profile / share_contact values
    public static final int PRIVACY_NOBODY    = 0;
    public static final int PRIVACY_CONTACTS  = 1;
    public static final int PRIVACY_EVERYBODY = 2;
}
