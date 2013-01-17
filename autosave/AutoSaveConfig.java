package autosave;

import autosave.Mode;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AutoSaveConfig
{

    private FileConfiguration config;
    protected String valueOn = "on";
    protected String valueOff = "off";
    protected UUID varUuid;
    protected int varInterval = 300;
    protected List varWarnTimes = null;
    protected boolean varBroadcast = true;
    protected boolean varDebug = false;
    protected List varWorlds = null;
    protected Mode varMode;
    protected String extpath;
    protected boolean backupEnabled;
    protected int backupInterval;
    protected int MaxNumberOfBackups;
    protected boolean backupBroadcast;
    protected boolean donotbackuptointfld;
    protected boolean backuppluginsfolder;
    protected boolean slowbackup;
    protected boolean javanio;
    protected List extfolders;
    protected boolean backuptoextfolders;
    protected long datesec;
    protected int numberofbackups;
    protected List backupnames;
    protected int numberofbackupsext;
    protected List backupnamesext;


    public AutoSaveConfig(FileConfiguration config)
    {
        this.varMode = Mode.SYNCHRONOUS;
        this.backupEnabled = false;
        this.backupInterval = 21600;
        this.MaxNumberOfBackups = 30;
        this.backupBroadcast = true;
        this.donotbackuptointfld = true;
        this.backuppluginsfolder = true;
        this.slowbackup = false;
        this.javanio = false;
        this.backuptoextfolders = false;
        this.numberofbackups = 0;
        this.numberofbackupsext = 0;
        this.config = config;
    }

    public void loadbackupextfolderconfig()
    {
        this.config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
        this.extfolders = this.config.getStringList("extfolders");
        this.config = new YamlConfiguration();
        this.config.set("help", "write absolute paths to this file");
        this.config.set("extfolders", this.extfolders);

        try
        {
            this.config.save(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
        }
        catch (IOException var2)
        {
            var2.printStackTrace();
        }
    }

    public void savebackupextfolderconfig()
    {
        this.config = new YamlConfiguration();
        this.config.set("help", "write absolute paths to this file");
        this.config.set("extfolders", this.extfolders);

        try
        {
            this.config.save(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
        }
        catch (IOException var2)
        {
            var2.printStackTrace();
        }
    }

    public void load()
    {
        if ((new File("plugins/autosaveworld/config.yml")).exists())
        {
            YamlConfiguration.loadConfiguration(new File("plugins/autosaveworld/config.yml"));
        }
        else
        {
            this.config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/config.yml"));
        }

        this.valueOn = this.config.getString("value.on", this.valueOn);
        this.valueOff = this.config.getString("value.off", this.valueOff);
        this.varDebug = this.config.getBoolean("var.debug", this.varDebug);
        this.varBroadcast = this.config.getBoolean("var.broadcast", this.varBroadcast);
        this.varInterval = this.config.getInt("var.interval", this.varInterval);
        this.varMode = Mode.valueOf(this.config.getString("var.mode", this.varMode.name()));
        this.varWorlds = this.config.getStringList("var.worlds");

        if (this.varWorlds.size() == 0)
        {
            this.varWorlds.add("*");
            this.config.set("var.worlds", this.varWorlds);
        }

        this.varWarnTimes = this.config.getIntegerList("var.warntime");

        if (this.varWarnTimes.size() == 0)
        {
            this.varWarnTimes.add(Integer.valueOf(0));
            this.config.set("var.warntime", this.varWarnTimes);
        }

        this.varUuid = UUID.fromString(this.config.getString("var.uuid", UUID.randomUUID().toString()));
        this.backupEnabled = this.config.getBoolean("backup.enabled", this.backupEnabled);
        this.backupInterval = this.config.getInt("backup.interval", this.backupInterval);
        this.slowbackup = this.config.getBoolean("backup.slowbackup", this.slowbackup);
        this.MaxNumberOfBackups = this.config.getInt("backup.MaxNumberOfBackups", 30);
        this.backupBroadcast = this.config.getBoolean("backup.broadcast", this.backupBroadcast);
        this.backuptoextfolders = this.config.getBoolean("backup.toextfolders", this.backuptoextfolders);
        this.donotbackuptointfld = this.config.getBoolean("backup.disableintfolder", this.donotbackuptointfld);
        this.backuppluginsfolder = this.config.getBoolean("backup.pluginsfolder", this.backuppluginsfolder);
        this.save();
    }

    public void getbackupdate()
    {
        this.config = new YamlConfiguration();
        this.config.set("Backuped at: ", new Date());

        try
        {
            this.config.save(new File("plugins/AutoSaveWorld/backups/" + this.datesec + "/backupinfo.yml"));
        }
        catch (IOException var2)
        {
            var2.printStackTrace();
        }
    }

    public void getbackupdateext()
    {
        this.config = new YamlConfiguration();
        this.config.set("Backuped at: ", new Date());

        try
        {
            this.config.save(new File(this.extpath + "/backups/" + this.datesec + "/backupinfo.yml"));
        }
        catch (IOException var2)
        {
            var2.printStackTrace();
        }
    }

    public void loadConfigBackup()
    {
        this.config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/backups.yml"));
        this.numberofbackups = this.config.getInt("NOB", 0);
        this.backupnames = this.config.getLongList("listnames");
    }

    public void loadConfigBackupExt()
    {
        this.config = YamlConfiguration.loadConfiguration(new File(this.extpath + "/backups.yml"));
        this.numberofbackupsext = this.config.getInt("NOB", 0);
        this.backupnamesext = this.config.getLongList("listnames");
    }

    public void saveConfigBackupExt()
    {
        this.config = new YamlConfiguration();
        this.config.set("NOB", Integer.valueOf(this.numberofbackupsext));
        this.config.set("listnames", this.backupnamesext);

        try
        {
            this.config.save(new File(this.extpath + "/backups.yml"));
        }
        catch (IOException var2)
        {
            ;
        }
    }

    public void saveConfigBackup()
    {
        this.config = new YamlConfiguration();
        this.config.set("NOB", Integer.valueOf(this.numberofbackupsext));
        this.config.set("listnames", this.backupnames);

        try
        {
            this.config.save(new File("plugins/AutoSaveWorld/backups.yml"));
        }
        catch (IOException var2)
        {
            ;
        }
    }

    public void save()
    {
        this.config = new YamlConfiguration();
        this.config.set("value.on", this.valueOn);
        this.config.set("value.off", this.valueOff);
        this.config.set("var.debug", Boolean.valueOf(this.varDebug));
        this.config.set("var.broadcast", Boolean.valueOf(this.varBroadcast));
        this.config.set("var.interval", Integer.valueOf(this.varInterval));
        this.config.set("var.mode", this.varMode.name());
        this.config.set("var.worlds", this.varWorlds);
        this.config.set("var.warntime", this.varWarnTimes);
        this.config.set("backup.enabled", Boolean.valueOf(this.backupEnabled));
        this.config.set("backup.interval", Integer.valueOf(this.backupInterval));
        this.config.set("backup.MaxNumberOfBackups", Integer.valueOf(this.MaxNumberOfBackups));
        this.config.set("backup.broadcast", Boolean.valueOf(this.backupBroadcast));
        this.config.set("backup.toextfolders", Boolean.valueOf(this.backuptoextfolders));
        this.config.set("backup.disableintfolder", Boolean.valueOf(this.donotbackuptointfld));
        this.config.set("backup.pluginsfolder", Boolean.valueOf(this.backuppluginsfolder));
        this.config.set("backup.slowbackup", Boolean.valueOf(this.slowbackup));

        try
        {
            if ((new File("plugins/autosaveworld/config.yml")).exists())
            {
                (new File("plugins/autosaveworld/config.yml")).delete();
            }

            if ((new File("plugins/autosaveworld")).exists())
            {
                (new File("plugins/autosaveworld")).delete();
            }

            this.config.save(new File("plugins/AutoSaveWorld/config.yml"));
        }
        catch (IOException var2)
        {
            ;
        }
    }
}
