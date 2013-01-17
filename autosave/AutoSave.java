package autosave;

import autosave.AutoBackupThread;
import autosave.AutoSaveConfig;
import autosave.AutoSaveConfigMSG;
import autosave.AutoSaveThread;
import autosave.Generic;
import autosave.Mode;
import autosave.ThreadType;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class AutoSave extends JavaPlugin implements Listener
{

    private static final Logger log = Logger.getLogger("Minecraft");
    private AutoSaveThread saveThread = null;
    private AutoBackupThread backupThread = null;
    private AutoSaveConfigMSG configmsg;
    private AutoSaveConfig config;
    protected Date lastSave = null;
    protected int numPlayers = 0;
    protected boolean saveInProgress = false;
    protected boolean backupInProgress = false;
    protected Boolean bukkitHasSetAutoSave;
    private String world;
    private String perm;
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$autosave$ThreadType;


    public void onDisable()
    {
        this.config.save();
        this.performSave();

        if (this.config.varMode == Mode.ASYNCHRONOUS)
        {
            Iterator var2 = this.getServer().getWorlds().iterator();

            while (var2.hasNext())
            {
                World timeA = (World)var2.next();

                if (this.bukkitHasSetAutoSave.booleanValue())
                {
                    timeA.setAutoSave(true);
                }
                else
                {
                    ((CraftWorld)timeA).getHandle().savingDisabled = false;
                }
            }
        }

        long timeA1 = 0L;

        if (this.config.varDebug)
        {
            timeA1 = System.currentTimeMillis();
        }

        if (this.config.varDebug)
        {
            log.info(String.format("[%s] Stopping Save Thread", new Object[] {this.getDescription().getName()}));
        }

        this.stopThread(ThreadType.SAVE);
        this.stopThread(ThreadType.BACKUP);

        if (this.config.varDebug)
        {
            long timeB = System.currentTimeMillis();
            long millis = timeB - timeA1;
            long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            log.info(String.format("[%s] Version %s was disabled in %d seconds", new Object[] {this.getDescription().getName(), this.getDescription().getVersion(), Long.valueOf(durationSeconds)}));
        }
        else
        {
            log.info(String.format("[%s] Version %s is disabled!", new Object[] {this.getDescription().getName(), this.getDescription().getVersion()}));
        }
    }

    public void onEnable()
    {
        this.config = new AutoSaveConfig(this.getConfig());
        this.configmsg = new AutoSaveConfigMSG(this.getConfig());
        this.config.load();
        this.configmsg.loadmsg();
        this.config.loadbackupextfolderconfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        String vr = this.getServer().getVersion().toString();
        char[] vrt = vr.toCharArray();
        System.out.println(vr);
        System.out.println(vrt);
        int tmpp = 0;
        String vrp = "v";

        for (int world = 0; world < vr.length(); ++world)
        {
            if (vrt[world] == 58 && vrt[world - 1] == 67 && vrt[world - 2] == 77)
            {
                while (vrt[world] < 48 || vrt[world] > 57)
                {
                    ++world;
                }

                tmpp = world;
            }
        }

        System.out.println(tmpp);

        for (; vrt[tmpp] >= 48 && vrt[tmpp] <= 57 || vrt[tmpp] == 46; ++tmpp)
        {
            if (vrt[tmpp] == 46)
            {
                vrp = vrp + "_";
            }
            else
            {
                vrp = vrp + vrt[tmpp];
            }
        }

        vrp.replaceAll("[.]", "_");
        System.out.println(vrp);

        try
        {
            ClassLoader.getSystemClassLoader().loadClass("org.bukkit.craftbukkit." + vrp + ".CraftWorld");
        }
        catch (ClassNotFoundException var9)
        {
            var9.printStackTrace();
        }

        try
        {
            Server.class.getMethod("savePlayers", new Class[0]);
            World.class.getMethod("save", new Class[0]);
        }
        catch (NoSuchMethodException var8)
        {
            log.severe(String.format("[%s] ERROR: Server version is incompatible with %s!", new Object[] {this.getDescription().getName(), this.getDescription().getName()}));
            log.severe(String.format("[%s] Could not find method \"%s\", disabling!", new Object[] {this.getDescription().getName(), var8.getMessage()}));
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        try
        {
            World.class.getMethod("setAutoSave", new Class[] {Boolean.TYPE});
            this.bukkitHasSetAutoSave = Boolean.valueOf(true);
        }
        catch (NoSuchMethodException var7)
        {
            this.bukkitHasSetAutoSave = Boolean.valueOf(false);
        }

        if (this.config.varMode == Mode.ASYNCHRONOUS)
        {
            Iterator var6 = this.getServer().getWorlds().iterator();

            while (var6.hasNext())
            {
                World var10 = (World)var6.next();

                if (this.bukkitHasSetAutoSave.booleanValue())
                {
                    var10.setAutoSave(false);
                }
                else
                {
                    ((CraftWorld)var10).getHandle().savingDisabled = true;
                }
            }
        }

        this.startThread(ThreadType.SAVE);
        this.startThread(ThreadType.BACKUP);
        log.info(String.format("[%s] Version %s is enabled: %s", new Object[] {this.getDescription().getName(), this.getDescription().getVersion(), this.config.varUuid.toString()}));
    }

    public boolean hasRight(Player player, String perm, String world)
    {
        world = player.getWorld().toString();

        if (this.getServer().getPluginManager().getPlugin("bPermissions") != null && ApiLayer.hasPermission(world, CalculableType.USER, player.toString(), perm))
        {
            return true;
        }
        else if (player.hasPermission(perm))
        {
            return true;
        }
        else
        {
            if (this.getServer().getPluginManager().getPlugin("PermissionsEx") != null)
            {
                PermissionUser user = PermissionsEx.getUser(player);

                if (user.has(perm))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        String commandName = command.getName().toLowerCase();
        Player player = null;

        if (sender instanceof Player)
        {
            player = (Player)sender;

            if (commandName.equals("autosave"))
            {
                if (args.length >= 1)
                {
                    this.perm = "autosave." + args[0];
                }
                else
                {
                    this.perm = "autosave.save";
                }
            }
            else if (commandName.equals("autobackup"))
            {
                if (args.length >= 1)
                {
                    this.perm = "autobackup." + args[0];
                }
                else
                {
                    this.perm = "autobackup.backup";
                }
            }

            if (!player.isOp() && !this.hasRight(player, this.perm, this.world))
            {
                this.sendMessage(sender, this.configmsg.messageInsufficientPermissions);
                return true;
            }
        }
        else if (!(sender instanceof ConsoleCommandSender))
        {
            this.sendMessage(sender, this.configmsg.messageInsufficientPermissions);
            return true;
        }

        boolean newSetting;
        int var16;

        if (commandName.equals("autosave"))
        {
            if (args.length == 0)
            {
                this.performSave();
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("easteregg"))
            {
                this.sendMessage(sender, "Maybe something will be here later... ");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reloadmsg"))
            {
                this.configmsg.loadmsg();
                this.sendMessage(sender, "&9Messages loaded");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("help"))
            {
                this.sendMessage(sender, "&f/save&7 - &3Saves all players & worlds");
                this.sendMessage(sender, "&f/save loadconfig&7 - &3Loads config from file config.yml");
                this.sendMessage(sender, "&f/save help&7 - &3Displays this dialogue");
                this.sendMessage(sender, "&f/save toggle&7 - &3Toggles the AutoSave system");
                this.sendMessage(sender, "&f/save reloadmsg&7 - &3Load messages from file configmsg.yml");
                this.sendMessage(sender, "&f/save status&7 - &3Reports thread status and last run time");
                this.sendMessage(sender, "&f/save interval&7 [value] - &3Sets & retrieves the save interval");
                this.sendMessage(sender, "&f/save broadcast&7 [on|off] - &3Sets & retrieves the broadcast value");
                this.sendMessage(sender, "&f/save warn&7 [value] - &3Sets & retrieves the warn time in seconds");
                this.sendMessage(sender, "&f/save version&7 - &3Prints the version of AutoSave");
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("loadconfig"))
            {
                this.config.load();
            }
            else
            {
                if (args.length == 1 && args[0].equalsIgnoreCase("toggle"))
                {
                    if (this.saveThread == null)
                    {
                        this.sendMessage(sender, this.configmsg.messageStarting);
                        return this.startThread(ThreadType.SAVE);
                    }

                    this.sendMessage(sender, this.configmsg.messageStopping);
                    return this.stopThread(ThreadType.SAVE);
                }

                if (args.length == 1 && args[0].equalsIgnoreCase("status"))
                {
                    if (this.saveThread != null)
                    {
                        if (this.saveThread.isAlive())
                        {
                            if (this.lastSave == null)
                            {
                                this.sendMessage(sender, this.configmsg.messageStatusNotRun);
                                return true;
                            }

                            this.sendMessage(sender, this.configmsg.messageStatusSuccess.replaceAll("\\$\\{DATE\\}", this.lastSave.toString()));
                            return true;
                        }

                        this.sendMessage(sender, this.configmsg.messageStatusFail);
                        return true;
                    }

                    this.sendMessage(sender, this.configmsg.messageStatusOff);
                }
                else if (args.length >= 1 && args[0].equalsIgnoreCase("interval"))
                {
                    if (args.length == 1)
                    {
                        this.sendMessage(sender, this.configmsg.messageInfoLookup.replaceAll("\\$\\{VARIABLE\\}", "Interval").replaceAll("\\$\\{VALUE\\}", String.valueOf(this.config.varInterval)));
                        return true;
                    }

                    if (args.length == 2)
                    {
                        try
                        {
                            var16 = Integer.parseInt(args[1]);
                            this.config.varInterval = var16;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Interval"));
                            return true;
                        }
                        catch (NumberFormatException var12)
                        {
                            this.sendMessage(sender, this.configmsg.messageInfoNaN);
                            return false;
                        }
                    }
                }
                else if (args.length >= 1 && args[0].equalsIgnoreCase("warn"))
                {
                    if (args.length == 1)
                    {
                        this.sendMessage(sender, this.configmsg.messageInfoListLookup.replaceAll("\\$\\{VARIABLE\\}", "Warn").replaceAll("\\$\\{VALUE\\}", Generic.join(", ", this.config.varWarnTimes)));
                        return true;
                    }

                    if (args.length == 2)
                    {
                        try
                        {
                            ArrayList var17 = new ArrayList();
                            String[] var11;
                            int var10 = (var11 = args[1].split(",")).length;

                            for (int var9 = 0; var9 < var10; ++var9)
                            {
                                String s = var11[var9];
                                var17.add(Integer.valueOf(Integer.parseInt(s)));
                            }

                            this.config.varWarnTimes = var17;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Warn"));
                            return true;
                        }
                        catch (NumberFormatException var13)
                        {
                            this.sendMessage(sender, this.configmsg.messageInfoNaN);
                            return false;
                        }
                    }
                }
                else if (args.length >= 1 && args[0].equalsIgnoreCase("broadcast"))
                {
                    if (args.length == 1)
                    {
                        this.sendMessage(sender, this.configmsg.messageInfoLookup.replaceAll("\\$\\{VARIABLE\\}", "Broadcast").replaceAll("\\$\\{VALUE\\}", String.valueOf(this.config.varBroadcast ? this.config.valueOn : this.config.valueOff)));
                        return true;
                    }

                    if (args.length == 2)
                    {
                        newSetting = false;

                        if (args[1].equalsIgnoreCase(this.config.valueOn))
                        {
                            newSetting = true;
                        }
                        else
                        {
                            if (!args[1].equalsIgnoreCase(this.config.valueOff))
                            {
                                this.sendMessage(sender, this.configmsg.messageInfoInvalid.replaceAll("\\$\\{VALIDSETTINGS\\}", String.format("%s, %s", new Object[] {this.config.valueOn, this.config.valueOff})));
                                return false;
                            }

                            newSetting = false;
                        }

                        this.config.varBroadcast = newSetting;
                        this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "AutoSave Broadcast"));
                        return true;
                    }
                }
                else if (args.length >= 1 && args[0].equalsIgnoreCase("debug"))
                {
                    if (args.length == 1)
                    {
                        this.sendMessage(sender, this.configmsg.messageInfoLookup.replaceAll("\\$\\{VARIABLE\\}", "Debug").replaceAll("\\$\\{VALUE\\}", String.valueOf(this.config.varDebug ? this.config.valueOn : this.config.valueOff)));
                        return true;
                    }

                    if (args.length == 2)
                    {
                        newSetting = false;

                        if (args[1].equalsIgnoreCase(this.config.valueOn))
                        {
                            newSetting = true;
                        }
                        else
                        {
                            if (!args[1].equalsIgnoreCase(this.config.valueOff))
                            {
                                this.sendMessage(sender, this.configmsg.messageInfoInvalid.replaceAll("\\$\\{VALIDSETTINGS\\}", String.format("%s, %s", new Object[] {this.config.valueOn, this.config.valueOff})));
                                return false;
                            }

                            newSetting = false;
                        }

                        this.config.varDebug = newSetting;
                        this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Debug"));
                        return true;
                    }
                }
                else
                {
                    if (args.length == 2 && args[0].equalsIgnoreCase("addworld"))
                    {
                        this.config.varWorlds.add(args[1]);
                        this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Worlds"));
                        return true;
                    }

                    if (args.length == 2 && args[0].equalsIgnoreCase("remworld"))
                    {
                        this.config.varWorlds.remove(args[1]);
                        this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Worlds"));
                        return true;
                    }

                    if (args.length == 1 && args[0].equalsIgnoreCase("world"))
                    {
                        this.sendMessage(sender, this.configmsg.messageInfoListLookup.replaceAll("\\$\\{VARIABLE\\}", "Worlds").replaceAll("\\$\\{VALUE\\}", Generic.join(", ", this.config.varWorlds)));
                        return true;
                    }

                    if (args.length == 1 && args[0].equalsIgnoreCase("version"))
                    {
                        this.sendMessage(sender, String.format("%s%s", new Object[] {ChatColor.BLUE, this.configmsg.messageVersion.replaceAll("\\$\\{VERSION\\}", this.getDescription().getVersion()).replaceAll("\\$\\{UUID\\}", this.config.varUuid.toString())}));
                        return true;
                    }
                }
            }
        }
        else if (commandName.equals("autobackup"))
        {
            if (args.length == 0)
            {
                if (!this.backupThread.niochecked)
                {
                    try
                    {
                        Files.class.getMethods();
                        this.config.javanio = true;
                        log.info(String.format("[%s] java.nio found, using normal backup mode", new Object[] {this.getDescription().getName()}));
                    }
                    catch (NoClassDefFoundError var14)
                    {
                        log.info(String.format("[%s] no java.nio found, using old backup mode", new Object[] {this.getDescription().getName()}));
                        this.config.javanio = false;

                        if (this.config.varDebug)
                        {
                            this.debug("No class");
                        }
                    }
                    catch (SecurityException var15)
                    {
                        log.info(String.format("[%s] no java.nio found, using old backup mode", new Object[] {this.getDescription().getName()}));
                        this.config.javanio = false;
                    }

                    this.backupThread.niochecked = true;
                }

                if (this.config.backupEnabled)
                {
                    this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
                    {
                        public void run()
                        {
                            AutoSave.this.backupThread.performBackup();
                        }
                    });
                }

                return true;
            }

            if (args.length >= 1 && args[0].equalsIgnoreCase("devdebug"))
            {
                if (args[1].equalsIgnoreCase("javanio"))
                {
                    if (args.length == 2)
                    {
                        this.sendMessage(sender, "Java nio: " + this.config.javanio);
                    }

                    if (args[2].equalsIgnoreCase("On"))
                    {
                        this.config.javanio = true;
                    }
                    else if (args[2].equalsIgnoreCase("Off"))
                    {
                        this.config.javanio = false;
                    }

                    return true;
                }

                if (args[1].equalsIgnoreCase("slowbackup"))
                {
                    if (args.length == 2)
                    {
                        this.sendMessage(sender, "Slowbackup: " + this.config.slowbackup);
                    }

                    if (args[2].equalsIgnoreCase("On"))
                    {
                        this.config.slowbackup = true;
                    }
                    else if (args[2].equalsIgnoreCase("Off"))
                    {
                        this.config.slowbackup = false;
                    }

                    return true;
                }

                return false;
            }

            if (args.length >= 1 && args[0].equalsIgnoreCase("slowbackup"))
            {
                if (args.length == 1)
                {
                    if (this.config.slowbackup)
                    {
                        this.sendMessage(sender, "Slowbackup is on");
                        return true;
                    }

                    this.sendMessage(sender, "Slowbackup is on");
                    return true;
                }

                if (args.length == 2)
                {
                    if (args[1].equalsIgnoreCase("On"))
                    {
                        this.config.slowbackup = true;
                        this.sendMessage(sender, "Slowbackup is enabled");
                    }
                    else if (args[1].equalsIgnoreCase("Off"))
                    {
                        this.config.slowbackup = false;
                        this.sendMessage(sender, "Slowbackup is disabled");
                    }

                    return true;
                }
            }
            else
            {
                if (args.length >= 1 && args[0].equalsIgnoreCase("addextfolder"))
                {
                    if (args.length == 1)
                    {
                        this.sendMessage(sender, "&9Please specify external folder path");
                    }
                    else
                    {
                        this.config.extfolders.add(args[1]);
                        this.config.savebackupextfolderconfig();
                        this.sendMessage(sender, "&9Folder added");
                    }

                    return true;
                }

                if (args.length >= 1 && args[0].equalsIgnoreCase("enabled"))
                {
                    if (args.length == 1)
                    {
                        if (this.config.backupEnabled)
                        {
                            this.sendMessage(sender, "&9AutoBackup is enabled");
                        }
                        else
                        {
                            this.sendMessage(sender, "&9AutoBackup is disabled");
                        }

                        return true;
                    }

                    if (args.length == 2)
                    {
                        if (args[1].equalsIgnoreCase("on"))
                        {
                            this.config.backupEnabled = true;
                            this.sendMessage(sender, "&9AutoBackup started");
                            return true;
                        }

                        if (args[1].equalsIgnoreCase("off"))
                        {
                            this.config.backupEnabled = false;
                            this.sendMessage(sender, "&9AutoBackup stopped");
                            return true;
                        }
                    }
                }
                else
                {
                    if (args.length == 1 && args[0].equalsIgnoreCase("help"))
                    {
                        this.sendMessage(sender, "&f/backup&7 - &3Backup all worlds");
                        this.sendMessage(sender, "&f/backup enabled {on/of}&7 - &3Show status of autobackup {enable or disable autobackup}");
                        this.sendMessage(sender, "&f/backup maxnumberbackups {number, 0 for infinite}&7 - &3Shown maximum number of backups {set maximum nubmer of backups}");
                        this.sendMessage(sender, "&f/backup interval {seconds}&7 - &3Show interval between backups {set interval between backups}");
                        this.sendMessage(sender, "&f/backup broadcast {on/off}&7 - &3Show broadcast status {enabled or disable broadcast}");
                        this.sendMessage(sender, "&f/backup backuptoextfolders {on/off}&7 - &3{Enable or disable backup to external folders (!!!do not forget to add paths to file backupextfoldersconfig.yml!!!)}");
                        this.sendMessage(sender, "&f/backup addextfolder {absolute path}&7 - - &3Add folder path to backupextfoldersconfig.yml");
                        this.sendMessage(sender, "&f/backup donotbackuptointfolder {on/off}&7 - &3 {Disable or enable backup to internal folder if backup to external folder is active(on - disable, off - enable)}");
                        this.sendMessage(sender, "&f/backup backuppluginsfolder {on/off}&7 - &3 {Disable or enable backup of plugins folder}");
                        this.sendMessage(sender, "&f/backup slowbackup {on/off}&7 - &3 {Enable or disable slowbackup}");
                        return true;
                    }

                    if (args.length >= 1 && args[0].equalsIgnoreCase("donotbackuptointfolder"))
                    {
                        if (args.length == 1)
                        {
                            if (this.config.donotbackuptointfld)
                            {
                                this.sendMessage(sender, "Backup to internal folder is disabled");
                            }
                            else
                            {
                                this.sendMessage(sender, "Backup to internal folder is enabled");
                            }

                            return true;
                        }

                        if (args.length == 2)
                        {
                            newSetting = false;

                            if (args[1].equalsIgnoreCase(this.config.valueOn))
                            {
                                newSetting = true;
                            }
                            else
                            {
                                if (!args[1].equalsIgnoreCase(this.config.valueOff))
                                {
                                    this.sendMessage(sender, this.configmsg.messageInfoInvalid.replaceAll("\\$\\{VALIDSETTINGS\\}", String.format("%s, %s", new Object[] {this.config.valueOn, this.config.valueOff})));
                                    return false;
                                }

                                newSetting = false;
                            }

                            this.config.donotbackuptointfld = newSetting;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "AutoBackup Broadcast"));
                            return true;
                        }
                    }
                    else if (args.length >= 1 && args[0].equalsIgnoreCase("backuppluginsfolder"))
                    {
                        if (args.length == 1)
                        {
                            if (this.config.backuppluginsfolder)
                            {
                                this.sendMessage(sender, "Bakup plugins folder is enabled");
                            }
                            else
                            {
                                this.sendMessage(sender, "Bakup plugins folder is disabled");
                            }

                            return true;
                        }

                        if (args.length == 2)
                        {
                            if (args[1].equalsIgnoreCase("On"))
                            {
                                this.config.backuppluginsfolder = true;
                                this.sendMessage(sender, "Backup plugins folder set to enabled");
                            }
                            else if (args[1].equalsIgnoreCase("Off"))
                            {
                                this.config.backuppluginsfolder = false;
                                this.sendMessage(sender, "Backup plugins folder set to disabled");
                            }
                        }
                    }
                    else if (args.length >= 1 && args[0].equalsIgnoreCase("broadcast"))
                    {
                        if (args.length == 1)
                        {
                            this.sendMessage(sender, this.configmsg.messageInfoLookup.replaceAll("\\$\\{VARIABLE\\}", "Broadcast").replaceAll("\\$\\{VALUE\\}", String.valueOf(this.config.backupBroadcast ? this.config.valueOn : this.config.valueOff)));
                            return true;
                        }

                        if (args.length == 2)
                        {
                            newSetting = false;

                            if (args[1].equalsIgnoreCase(this.config.valueOn))
                            {
                                newSetting = true;
                            }
                            else
                            {
                                if (!args[1].equalsIgnoreCase(this.config.valueOff))
                                {
                                    this.sendMessage(sender, this.configmsg.messageInfoInvalid.replaceAll("\\$\\{VALIDSETTINGS\\}", String.format("%s, %s", new Object[] {this.config.valueOn, this.config.valueOff})));
                                    return false;
                                }

                                newSetting = false;
                            }

                            this.config.backupBroadcast = newSetting;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "AutoBackup Broadcast"));
                            return true;
                        }
                    }
                    else if (args.length >= 1 && args[0].equalsIgnoreCase("interval"))
                    {
                        if (args.length == 1)
                        {
                            this.sendMessage(sender, "&9Interval is " + String.valueOf(this.config.backupInterval));
                            return true;
                        }

                        if (args.length == 2)
                        {
                            var16 = Integer.parseInt(args[1]);
                            this.config.backupInterval = var16;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Interval"));
                            return true;
                        }
                    }
                    else if (args.length >= 1 && args[0].equalsIgnoreCase("maxnumberofbackups"))
                    {
                        if (args.length == 1)
                        {
                            this.sendMessage(sender, "&9Maximum number of backups is " + this.config.MaxNumberOfBackups);
                            return true;
                        }

                        if (args.length == 2)
                        {
                            var16 = Integer.parseInt(args[1]);
                            this.config.MaxNumberOfBackups = var16;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Maximum number of backups"));
                            return true;
                        }
                    }
                    else if (args.length >= 1 && args[0].equalsIgnoreCase("backuptoextfolders"))
                    {
                        if (args.length == 1)
                        {
                            if (this.config.backuptoextfolders)
                            {
                                this.sendMessage(sender, "Backup to external folders is on");
                            }
                            else
                            {
                                this.sendMessage(sender, "Backup to external folders is off");
                            }

                            return true;
                        }

                        if (args.length == 2)
                        {
                            newSetting = false;

                            if (args[1].equalsIgnoreCase(this.config.valueOn))
                            {
                                newSetting = true;
                            }
                            else
                            {
                                if (!args[1].equalsIgnoreCase(this.config.valueOff))
                                {
                                    this.sendMessage(sender, this.configmsg.messageInfoInvalid.replaceAll("\\$\\{VALIDSETTINGS\\}", String.format("%s, %s", new Object[] {this.config.valueOn, this.config.valueOff})));
                                    return false;
                                }

                                newSetting = false;
                            }

                            this.config.backuptoextfolders = newSetting;
                            this.sendMessage(sender, this.configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "AutoBackup save to external folders"));
                            return true;
                        }
                    }
                }
            }
        }
        else
        {
            this.sendMessage(sender, String.format("Unknown command \"%s\" handled by %s", new Object[] {commandName, this.getDescription().getName()}));
        }

        return false;
    }

    protected boolean startThread(ThreadType type)
    {
        switch ($SWITCH_TABLE$autosave$ThreadType()[type.ordinal()])
        {
            case 1:
                if (this.saveThread == null || !this.saveThread.isAlive())
                {
                    this.saveThread = new AutoSaveThread(this, this.config, this.configmsg);
                    this.saveThread.start();
                }

                return true;

            case 2:
                if (this.backupThread == null || !this.backupThread.isAlive())
                {
                    this.backupThread = new AutoBackupThread(this, this.config, this.configmsg);
                    this.backupThread.start();
                }

                return true;

            default:
                return false;
        }
    }

    protected boolean stopThread(ThreadType type)
    {
        switch ($SWITCH_TABLE$autosave$ThreadType()[type.ordinal()])
        {
            case 1:
                if (this.saveThread == null)
                {
                    return true;
                }
                else
                {
                    this.saveThread.setRun(false);

                    try
                    {
                        this.saveThread.join(5000L);
                        this.saveThread = null;
                        return true;
                    }
                    catch (InterruptedException var4)
                    {
                        this.warn("Could not stop AutoSaveThread", var4);
                        return false;
                    }
                }

            case 2:
                if (this.backupThread == null)
                {
                    return true;
                }
                else
                {
                    this.backupThread.setRun(false);

                    try
                    {
                        this.backupThread.join(5000L);
                        this.backupThread = null;
                        return true;
                    }
                    catch (InterruptedException var3)
                    {
                        this.warn("Could not stop AutoBackupThread", var3);
                        return false;
                    }
                }

            default:
                return false;
        }
    }

    private void savePlayers()
    {
        this.debug("Saving players");
        this.getServer().savePlayers();
    }

    private int saveWorlds()
    {
        int i = 0;
        List worlds = this.getServer().getWorlds();

        for (Iterator var4 = worlds.iterator(); var4.hasNext(); ++i)
        {
            World world = (World)var4.next();
            this.debug(String.format("Saving world: %s", new Object[] {world.getName()}));
            world.save();
        }

        return i;
    }

    public void performSave()
    {
        if (this.saveInProgress)
        {
            this.warn("Multiple concurrent saves attempted! Save interval is likely too short!");
        }
        else if (this.getServer().getOnlinePlayers().length == 0)
        {
            this.debug("Skipping save, no players online.");
        }
        else
        {
            this.saveInProgress = true;
            this.broadcasta(this.configmsg.messageBroadcastPre);
            this.savePlayers();
            this.debug("Saved Players");
            byte saved = 0;
            int saved1 = saved + this.saveWorlds();
            this.debug(String.format("Saved %d Worlds", new Object[] {Integer.valueOf(saved1)}));
            this.lastSave = new Date();
            this.broadcasta(this.configmsg.messageBroadcastPost);
            this.saveInProgress = false;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (this.config.varDebug)
        {
            this.debug("Check for last leave");
            this.debug("Players online = " + this.getServer().getOnlinePlayers().length);
        }

        if (this.getServer().getOnlinePlayers().length == 1)
        {
            this.performSave();

            if (this.config.varDebug)
            {
                this.debug("Last player has quit, autosaving");
            }
        }
    }

    public void sendMessage(CommandSender sender, String message)
    {
        if (!message.equals(""))
        {
            sender.sendMessage(Generic.parseColor(message));
        }
    }

    public void broadcasta(String message)
    {
        if (!message.equals("") && this.config.varBroadcast)
        {
            this.getServer().broadcastMessage(Generic.parseColor(message));
            log.info(String.format("[%s] %s", new Object[] {this.getDescription().getName(), Generic.stripColor(message)}));
        }
    }

    public void broadcastb(String message)
    {
        if (!message.equals("") && this.config.backupBroadcast)
        {
            this.getServer().broadcastMessage(Generic.parseColor(message));
            log.info(String.format("[%s] %s", new Object[] {this.getDescription().getName(), Generic.stripColor(message)}));
        }
    }

    public void debug(String message)
    {
        if (this.config.varDebug)
        {
            log.info(String.format("[%s] %s", new Object[] {this.getDescription().getName(), Generic.stripColor(message)}));
        }
    }

    public void warn(String message)
    {
        log.warning(String.format("[%s] %s", new Object[] {this.getDescription().getName(), Generic.stripColor(message)}));
    }

    public void warn(String message, Exception e)
    {
        log.log(Level.WARNING, String.format("[%s] %s", new Object[] {this.getDescription().getName(), Generic.stripColor(message)}), e);
    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$autosave$ThreadType()
    {
        if ($SWITCH_TABLE$autosave$ThreadType != null)
        {
            return $SWITCH_TABLE$autosave$ThreadType;
        }
        else
        {
            int[] var0 = new int[ThreadType.values().length];

            try
            {
                var0[ThreadType.BACKUP.ordinal()] = 2;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                var0[ThreadType.SAVE.ordinal()] = 1;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }

            $SWITCH_TABLE$autosave$ThreadType = var0;
            return var0;
        }
    }
}
