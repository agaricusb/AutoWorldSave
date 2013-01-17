package autosave;

import autosave.AutoBackupJava7;
import autosave.AutoSave;
import autosave.AutoSaveConfig;
import autosave.AutoSaveConfigMSG;
import autosave.Generic;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.World;

public class AutoBackupThread extends Thread
{

    protected final Logger log = Logger.getLogger("Minecraft");
    private boolean run = true;
    private AutoSave plugin = null;
    private AutoSaveConfig config;
    private AutoSaveConfigMSG configmsg;
    public long datesec;
    private List tempnames = new ArrayList();
    public boolean niochecked = false;
    private AutoBackupJava7 backup7thread;


    private int backupWorlds(List worldNames, boolean ext)
    {
        boolean all = false;

        if (this.config.varWorlds.contains("*"))
        {
            all = true;
        }

        int i;
        List worlds;
        World world;
        Iterator var7;

        if (ext)
        {
            i = 0;
            worlds = this.plugin.getServer().getWorlds();
            var7 = worlds.iterator();

            while (var7.hasNext())
            {
                world = (World)var7.next();

                if (worldNames.contains(world.getName()) || all)
                {
                    this.plugin.debug(String.format("Backuping world: %s", new Object[] {world.getName()}));

                    try
                    {
                        this.copyDirectory(new File((new File(".")).getCanonicalPath() + File.separator + world.getName()), new File(this.config.extpath + "/backups" + File.separator + this.datesec + File.separator + world.getName()), false);
                    }
                    catch (IOException var9)
                    {
                        var9.printStackTrace();
                    }

                    ++i;
                }
            }

            return i;
        }
        else
        {
            i = 0;
            worlds = this.plugin.getServer().getWorlds();
            var7 = worlds.iterator();

            while (var7.hasNext())
            {
                world = (World)var7.next();

                if (worldNames.contains(world.getName()) || all)
                {
                    this.plugin.debug(String.format("Backuping world: %s", new Object[] {world.getName()}));

                    try
                    {
                        this.copyDirectory(new File((new File(".")).getCanonicalPath() + File.separator + world.getName()), new File((new File(".")).getCanonicalPath() + File.separator + "plugins/AutoSaveWorld/backups" + File.separator + this.datesec + File.separator + world.getName()), false);
                    }
                    catch (IOException var10)
                    {
                        var10.printStackTrace();
                    }

                    ++i;
                }
            }

            return i;
        }
    }

    public void copyDirectory(File sourceLocation, File targetLocation, boolean pluginflag) throws IOException
    {
        if (this.config.javanio && !pluginflag)
        {
            if (this.config.varDebug)
            {
                this.plugin.debug("Java 7 backup running");
            }

            try
            {
                if (this.backup7thread == null || !this.backup7thread.isAlive())
                {
                    this.backup7thread = new AutoBackupJava7();
                    this.backup7thread.start();
                }

                if (this.config.javanio)
                {
                    if (this.config.slowbackup)
                    {
                        this.backup7thread.setPriority(1);
                    }

                    this.backup7thread.do7backup(sourceLocation, targetLocation);
                }

                if (this.config.slowbackup)
                {
                    this.backup7thread.setPriority(10);
                }
            }
            catch (Exception var8)
            {
                var8.printStackTrace();
            }
        }
        else
        {
            if (this.config.varDebug && !pluginflag)
            {
                this.plugin.debug("Java 6 backup running");
            }

            if (sourceLocation.isDirectory())
            {
                if (!targetLocation.exists())
                {
                    targetLocation.mkdirs();
                }

                String[] e = sourceLocation.list();

                for (int out = 0; out < e.length; ++out)
                {
                    if (!e[out].equalsIgnoreCase("backups"))
                    {
                        this.copyDirectory(new File(sourceLocation, e[out]), new File(targetLocation, e[out]), pluginflag);
                    }
                }
            }
            else
            {
                try
                {
                    FileInputStream var10 = new FileInputStream(sourceLocation);
                    FileOutputStream var11 = new FileOutputStream(targetLocation);
                    byte[] buf = new byte[10240];
                    int len;

                    while ((len = var10.read(buf)) > 0)
                    {
                        var11.write(buf, 0, len);
                    }

                    var10.close();
                    var11.close();
                }
                catch (IOException var9)
                {
                    System.out.println("Failed to backup file " + sourceLocation);
                }
            }
        }
    }

    public void deleteDirectory(File file)
    {
        if (file.exists())
        {
            if (file.isDirectory())
            {
                File[] var5;
                int var4 = (var5 = file.listFiles()).length;

                for (int var3 = 0; var3 < var4; ++var3)
                {
                    File f = var5[var3];
                    this.deleteDirectory(f);
                }

                file.delete();
            }
            else
            {
                file.delete();
            }
        }
    }

    public void performBackup()
    {
        if (this.config.slowbackup)
        {
            this.setPriority(1);
        }

        if (this.plugin.backupInProgress)
        {
            this.plugin.warn("Multiple concurrent backups attempted! Backup interval is likely too short!");
        }
        else
        {
            this.plugin.saveInProgress = true;
            this.plugin.backupInProgress = true;
            this.datesec = System.currentTimeMillis();
            byte saved = 0;
            int var9;

            if (!this.config.donotbackuptointfld || !this.config.backuptoextfolders)
            {
                this.config.loadConfigBackup();
                this.tempnames.clear();
                Iterator var4 = this.config.backupnames.iterator();
                long i;

                while (var4.hasNext())
                {
                    i = ((Long)var4.next()).longValue();

                    if ((new File("plugins/AutoSaveWorld/backups/" + i)).exists())
                    {
                        this.tempnames.add(Long.valueOf(i));
                    }
                }

                this.config.backupnames.clear();
                var4 = this.tempnames.iterator();

                while (var4.hasNext())
                {
                    i = ((Long)var4.next()).longValue();
                    this.config.backupnames.add(Long.valueOf(i));
                }

                this.config.numberofbackups = this.config.backupnames.size();

                if (this.config.MaxNumberOfBackups != 0 && this.config.numberofbackups >= this.config.MaxNumberOfBackups)
                {
                    try
                    {
                        this.deleteDirectory(new File((new File(".")).getCanonicalPath() + File.separator + "plugins/AutoSaveWorld/backups/" + ((Long)this.config.backupnames.get(0)).toString()));
                    }
                    catch (IOException var8)
                    {
                        var8.printStackTrace();
                    }

                    this.config.backupnames.remove(0);
                    --this.config.numberofbackups;
                }

                this.plugin.broadcastb(this.configmsg.messageBroadcastBackupPre);
                var9 = saved + this.backupWorlds(this.config.varWorlds, false);
                this.plugin.debug(String.format("Backup %d Worlds", new Object[] {Integer.valueOf(var9)}));
                this.config.backupnames.add(Long.valueOf(this.datesec));
                ++this.config.numberofbackups;
                this.config.saveConfigBackup();
                this.config.datesec = this.datesec;
                this.config.getbackupdate();

                if (this.config.backuppluginsfolder)
                {
                    try
                    {
                        this.copyDirectory(new File((new File(".")).getCanonicalPath() + File.separator + "plugins"), new File("plugins/AutoSaveWorld/backups/" + this.datesec + File.separator + "plugins"), true);
                    }
                    catch (IOException var7)
                    {
                        var7.printStackTrace();
                    }
                }
            }

            if (this.config.backuptoextfolders)
            {
                if (this.config.varDebug)
                {
                    this.plugin.debug("start extbackup");
                }

                this.config.loadbackupextfolderconfig();

                if (this.config.extfolders.size() != 0)
                {
                    for (int var10 = 0; var10 < this.config.extfolders.size(); ++var10)
                    {
                        this.config.extpath = (String)this.config.extfolders.get(var10);

                        if (this.config.varDebug)
                        {
                            this.plugin.debug("Path is:" + this.config.extpath);
                        }

                        this.config.loadConfigBackupExt();
                        this.tempnames.clear();
                        Iterator var5 = this.config.backupnamesext.iterator();
                        long e;

                        while (var5.hasNext())
                        {
                            e = ((Long)var5.next()).longValue();

                            if ((new File(this.config.extpath + "/backups/" + e)).exists())
                            {
                                this.tempnames.add(Long.valueOf(e));
                            }
                        }

                        this.config.backupnamesext.clear();
                        var5 = this.tempnames.iterator();

                        while (var5.hasNext())
                        {
                            e = ((Long)var5.next()).longValue();
                            this.config.backupnamesext.add(Long.valueOf(e));
                        }

                        this.config.numberofbackupsext = this.config.backupnamesext.size();

                        if (this.config.varDebug)
                        {
                            this.plugin.debug("configuring done");
                        }

                        if (this.config.MaxNumberOfBackups != 0 && this.config.numberofbackupsext >= this.config.MaxNumberOfBackups)
                        {
                            this.deleteDirectory(new File(this.config.extpath + "/backups/" + ((Long)this.config.backupnamesext.get(0)).toString()));
                            this.config.backupnamesext.remove(0);
                            --this.config.numberofbackupsext;
                        }

                        saved = 0;
                        var9 = saved + this.backupWorlds(this.config.varWorlds, true);
                        this.plugin.debug(String.format("Backup %d Worlds", new Object[] {Integer.valueOf(var9)}));
                        this.config.backupnamesext.add(Long.valueOf(this.datesec));
                        ++this.config.numberofbackupsext;
                        this.config.saveConfigBackupExt();
                        this.config.datesec = this.datesec;
                        this.config.getbackupdateext();

                        if (this.config.backuppluginsfolder)
                        {
                            try
                            {
                                this.copyDirectory(new File((new File(".")).getCanonicalPath() + File.separator + "plugins"), new File(this.config.extpath + "/backups" + File.separator + this.datesec + File.separator + "plugins"), true);
                            }
                            catch (IOException var6)
                            {
                                var6.printStackTrace();
                            }
                        }
                    }
                }
            }

            this.plugin.broadcastb(this.configmsg.messageBroadcastBackupPost);
            this.plugin.saveInProgress = false;
            this.plugin.backupInProgress = false;

            if (this.config.varDebug)
            {
                this.plugin.debug("Full backup time: " + (System.currentTimeMillis() - this.datesec) + " milliseconds");
            }

            if (this.config.slowbackup)
            {
                this.setPriority(5);
            }
        }
    }

    AutoBackupThread(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg)
    {
        this.plugin = plugin;
        this.config = config;
        this.configmsg = configmsg;
    }

    public void setRun(boolean run)
    {
        this.run = run;
    }

    public void run()
    {
        if (this.config != null)
        {
            this.log.info(String.format("[%s] AutoBackupThread Started: Interval is %d seconds, Warn Times are %s", new Object[] {this.plugin.getDescription().getName(), Integer.valueOf(this.config.backupInterval), Generic.join(",", this.config.varWarnTimes)}));

            while (this.run)
            {
                if (this.config.backupInterval == 0)
                {
                    try
                    {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException var5)
                    {
                        ;
                    }
                }
                else
                {
                    for (int e = 0; e < this.config.backupInterval; ++e)
                    {
                        try
                        {
                            if (!this.run)
                            {
                                if (this.config.varDebug)
                                {
                                    this.log.info(String.format("[%s] Graceful quit of AutoBackupThread", new Object[] {this.plugin.getDescription().getName()}));
                                }

                                return;
                            }

                            boolean e1 = false;
                            Iterator var4 = this.config.varWarnTimes.iterator();

                            while (var4.hasNext())
                            {
                                int w = ((Integer)var4.next()).intValue();

                                if (w != 0 && w + e == this.config.backupInterval)
                                {
                                    e1 = true;
                                }
                            }

                            if (e1)
                            {
                                if (this.config.varDebug)
                                {
                                    this.log.info(String.format("[%s] Warning Time Reached: %d seconds to go.", new Object[] {this.plugin.getDescription().getName(), Integer.valueOf(this.config.backupInterval - e)}));
                                }

                                this.plugin.getServer().broadcastMessage(Generic.parseColor(this.configmsg.messageBackupWarning));
                                this.log.info(String.format("[%s] %s", new Object[] {this.plugin.getDescription().getName(), this.configmsg.messageBackupWarning}));
                            }

                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException var8)
                        {
                            this.log.info("Could not sleep!");
                        }
                    }

                    if (!this.niochecked)
                    {
                        try
                        {
                            Files.class.getMethods();
                            this.config.javanio = true;
                            this.log.info(String.format("[%s] java.nio found, using normal backup mode", new Object[] {this.plugin.getDescription().getName()}));
                        }
                        catch (NoClassDefFoundError var6)
                        {
                            this.log.info(String.format("[%s] no java.nio found, using old backup mode", new Object[] {this.plugin.getDescription().getName()}));
                            this.config.javanio = false;

                            if (this.config.varDebug)
                            {
                                this.plugin.debug("No class");
                            }
                        }
                        catch (SecurityException var7)
                        {
                            this.log.info(String.format("[%s] no java.nio found, using old backup mode", new Object[] {this.plugin.getDescription().getName()}));
                            this.config.javanio = false;
                        }

                        this.niochecked = true;
                    }

                    if (this.config.backupEnabled)
                    {
                        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable()
                        {
                            public void run()
                            {
                                AutoBackupThread.this.performBackup();
                            }
                        });
                    }
                }
            }
        }
    }
}
