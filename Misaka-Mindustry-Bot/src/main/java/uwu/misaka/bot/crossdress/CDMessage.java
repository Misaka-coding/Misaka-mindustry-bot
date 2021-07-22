package uwu.misaka.bot.crossdress;

import java.util.ArrayList;

public class CDMessage {
    public ArrayList<Base.CDAttachment> attachments = new ArrayList<>();
    public ArrayList<Long> pingedUsers = new ArrayList<>();
    public ArrayList<Long> pingedRoles = new ArrayList<>();
    String message;
    long authorID;
    long channelID;
    long serverID;

    public CDMessage(String message, Long authorID, Long channelID) {
        this.message = message;
        this.authorID = authorID;
        this.channelID = channelID;
        serverID = -1;
    }

    public CDMessage(String message, Long authorID, Long channelID, Long serverID) {
        this.message = message;
        this.authorID = authorID;
        this.channelID = channelID;
        this.serverID = serverID;
    }

    public CDMessage addPingedUser(Long user) {
        pingedUsers.add(user);
        return this;
    }

    public CDMessage addPingedRole(Long role) {
        pingedRoles.add(role);
        return this;
    }

    public CDMessage addAttachments(Base.CDAttachment attachment) {
        this.attachments.add(attachment);
        return this;
    }
}
