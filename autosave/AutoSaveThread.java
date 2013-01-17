package autosave;

import autosave.AutoSave;
import autosave.AutoSaveConfig;
import autosave.AutoSaveConfigMSG;
import autosave.Generic;
import autosave.Mode;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

public class AutoSaveThread extends Thread
{

    protected final Logger log = Logger.getLogger("Minecraft");
    private boolean run = true;
    private AutoSave plugin = null;
    private AutoSaveConfig config;
    private AutoSaveConfigMSG configmsg;
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$autosave$Mode;


    AutoSaveThread(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg)
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
            this.log.info(String.format("[%s] AutoSaveThread Started: Interval is %d seconds, Warn Times are %s", new Object[] {this.plugin.getDescription().getName(), Integer.valueOf(this.config.varInterval), Generic.join(",", this.config.varWarnTimes)}));

            while (this.run)
            {
                if (this.config.varInterval == 0)
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
                    for (int i = 0; i < this.config.varInterval; ++i)
                    {
                        try
                        {
                            if (!this.run)
                            {
                                if (this.config.varDebug)
                                {
                                    this.log.info(String.format("[%s] Graceful quit of AutoSaveThread", new Object[] {this.plugin.getDescription().getName()}));
                                }

                                return;
                            }

                            boolean e = false;
                            Iterator var4 = this.config.varWarnTimes.iterator();

                            while (var4.hasNext())
                            {
                                int w = ((Integer)var4.next()).intValue();

                                if (w != 0 && w + i == this.config.varInterval)
                                {
                                    e = true;
                                }
                            }

                            if (e)
                            {
                                if (this.config.varDebug)
                                {
                                    this.log.info(String.format("[%s] Warning Time Reached: %d seconds to go.", new Object[] {this.plugin.getDescription().getName(), Integer.valueOf(this.config.varInterval - i)}));
                                }

                                this.plugin.getServer().broadcastMessage(Generic.parseColor(this.configmsg.messageWarning));
                                this.log.info(String.format("[%s] %s", new Object[] {this.plugin.getDescription().getName(), this.configmsg.messageWarning}));
                            }

                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException var6)
                        {
                            this.log.info("Could not sleep!");
                        }
                    }

                    switch ($SWITCH_TABLE$autosave$Mode()[this.config.varMode.ordinal()])
                    {
                        case 1:
                            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable()
                            {
                                public void run()
                                {
                                    AutoSaveThread.this.plugin.performSave();
                                    AutoSaveThread.this.plugin.lastSave = new Date();
                                }
                            });
                            break;

                        case 2:
                            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
                            {
                                public void run()
                                {
                                    AutoSaveThread.this.plugin.performSave();
                                    AutoSaveThread.this.plugin.lastSave = new Date();
                                }
                            });
                            break;

                        default:
                            this.log.warning(String.format("[%s] Invalid configuration mode!", new Object[] {this.plugin.getDescription().getName()}));
                    }
                }
            }
        }
    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$autosave$Mode()
    {
        if ($SWITCH_TABLE$autosave$Mode != null)
        {
            return $SWITCH_TABLE$autosave$Mode;
        }
        else
        {
            int[] var0 = new int[Mode.values().length];

            try
            {
                var0[Mode.ASYNCHRONOUS.ordinal()] = 1;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                var0[Mode.SYNCHRONOUS.ordinal()] = 2;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }

            $SWITCH_TABLE$autosave$Mode = var0;
            return var0;
        }
    }
}
