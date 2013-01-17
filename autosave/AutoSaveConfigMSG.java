package autosave;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AutoSaveConfigMSG
{

    private FileConfiguration configmsg;
    protected String messageBroadcastPre = "&9AutoSaving";
    protected String messageBroadcastPost = "&9AutoSave Complete";
    protected String messageStatusFail = "&9AutoSave has stopped, check the server logs for more info";
    protected String messageStatusNotRun = "&9AutoSave is running but has not yet saved.";
    protected String messageStatusSuccess = "&9AutoSave is running and last saved at ${DATE}.";
    protected String messageStatusOff = "&9AutoSave is not running (disabled)";
    protected String messageInsufficientPermissions = "&cYou do not have access to that command.";
    protected String messageStopping = "&9AutoSave Stopping";
    protected String messageStarting = "&9AutoSave Starting";
    protected String messageInfoNaN = "&cYou must enter a valid number, ex: 300";
    protected String messageInfoChangeSuccess = "&9${VARIABLE} has been updated.";
    protected String messageInfoLookup = "&9${VARIABLE} is ${VALUE}";
    protected String messageInfoListLookup = "&9${VARIABLE} is set to [${VALUE}]";
    protected String messageInfoInvalid = "&cYou must enter a valid setting (${VALIDSETTINGS})";
    protected String messageVersion = "&9AutoSave v${VERSION}, Instance ${UUID}";
    protected String messageWarning = "&9Warning, AutoSave will commence soon.";
    protected String messageBroadcastBackupPre = "&9AutoBackuping";
    protected String messageBroadcastBackupPost = "&9AutoBackup Complete";
    protected String messageBackupWarning = "&9Warning, AutoBackup will commence soon";


    public AutoSaveConfigMSG(FileConfiguration configmsg)
    {
        this.configmsg = configmsg;
    }

    public void loadmsg()
    {
        this.configmsg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/configmsg.yml"));
        this.messageBroadcastPre = this.configmsg.get("broadcast.pre", this.messageBroadcastPre).toString();
        this.messageBroadcastPost = this.configmsg.get("broadcast.post", this.messageBroadcastPost).toString();
        this.messageBroadcastBackupPre = this.configmsg.get("broadcastbackup.pre", this.messageBroadcastBackupPre).toString();
        this.messageBroadcastBackupPost = this.configmsg.get("broadcastbackup.post", this.messageBroadcastBackupPost).toString();
        this.messageStatusFail = this.configmsg.get("status.fail", this.messageStatusFail).toString();
        this.messageStatusNotRun = this.configmsg.get("status.notrun", this.messageStatusNotRun).toString();
        this.messageStatusSuccess = this.configmsg.get("status.success", this.messageStatusSuccess).toString();
        this.messageStatusOff = this.configmsg.get("status.off", this.messageStatusOff).toString();
        this.messageInsufficientPermissions = this.configmsg.get("insufficentpermissions", this.messageInsufficientPermissions).toString();
        this.messageStopping = this.configmsg.get("stopping", this.messageStopping).toString();
        this.messageStarting = this.configmsg.get("starting", this.messageStarting).toString();
        this.messageInfoNaN = this.configmsg.get("info.nan", this.messageInfoNaN).toString();
        this.messageInfoChangeSuccess = this.configmsg.get("info.changesuccess", this.messageInfoChangeSuccess).toString();
        this.messageInfoLookup = this.configmsg.get("infolookup", this.messageInfoLookup).toString();
        this.messageInfoListLookup = this.configmsg.get("infolistlookup", this.messageInfoListLookup).toString();
        this.messageInfoInvalid = this.configmsg.get("infoinvalid", this.messageInfoInvalid).toString();
        this.messageVersion = this.configmsg.get("version", this.messageVersion).toString();
        this.messageWarning = this.configmsg.get("warning", this.messageWarning).toString();
        this.messageBackupWarning = this.configmsg.get("warningbackup", this.messageBackupWarning).toString();
        this.configmsg = new YamlConfiguration();
        this.configmsg.set("broadcast.pre", this.messageBroadcastPre);
        this.configmsg.set("broadcast.post", this.messageBroadcastPost);
        this.configmsg.set("broadcastbackup.pre", this.messageBroadcastBackupPre);
        this.configmsg.set("broadcastbackup.post", this.messageBroadcastBackupPost);
        this.configmsg.set("status.fail", this.messageStatusFail);
        this.configmsg.set("status.notrun", this.messageStatusNotRun);
        this.configmsg.set("status.success", this.messageStatusSuccess);
        this.configmsg.set("status.off", this.messageStatusOff);
        this.configmsg.set("insufficentpermissions", this.messageInsufficientPermissions);
        this.configmsg.set("stopping", this.messageStopping);
        this.configmsg.set("starting", this.messageStarting);
        this.configmsg.set("info.nan", this.messageInfoNaN);
        this.configmsg.set("info.changesuccess", this.messageInfoChangeSuccess);
        this.configmsg.set("infolookup", this.messageInfoLookup);
        this.configmsg.set("infolistlookup", this.messageInfoListLookup);
        this.configmsg.set("infoinvalid", this.messageInfoInvalid);
        this.configmsg.set("version", this.messageVersion);
        this.configmsg.set("warning", this.messageWarning);
        this.configmsg.set("warningbackup", this.messageBackupWarning);

        try
        {
            this.configmsg.save(new File("plugins/AutoSaveWorld/configmsg.yml"));
        }
        catch (IOException var2)
        {
            var2.printStackTrace();
        }
    }
}
