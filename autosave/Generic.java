package autosave;

import java.util.List;
import org.bukkit.ChatColor;

public class Generic
{

    public static boolean stringArrayContains(String base, String[] comparesWith)
    {
        for (int i = 0; i < comparesWith.length; ++i)
        {
            try
            {
                if (base.compareTo(comparesWith[i]) == 0)
                {
                    return true;
                }
            }
            catch (NullPointerException var4)
            {
                return false;
            }
        }

        return false;
    }

    public static String join(String glue, List s)
    {
        try
        {
            if (s == null)
            {
                return "";
            }
            else
            {
                int npe = s.size();

                if (npe == 0)
                {
                    return null;
                }
                else
                {
                    StringBuilder out = new StringBuilder();
                    out.append(s.get(0).toString());

                    for (int x = 1; x < npe; ++x)
                    {
                        out.append(glue).append(s.get(x).toString());
                    }

                    return out.toString();
                }
            }
        }
        catch (NullPointerException var5)
        {
            return "";
        }
    }

    public static String parseColor(String message)
    {
        message = message.replaceAll("&0", "" + ChatColor.BLACK);
        message = message.replaceAll("&1", "" + ChatColor.DARK_BLUE);
        message = message.replaceAll("&2", "" + ChatColor.DARK_GREEN);
        message = message.replaceAll("&3", "" + ChatColor.DARK_AQUA);
        message = message.replaceAll("&4", "" + ChatColor.DARK_RED);
        message = message.replaceAll("&5", "" + ChatColor.DARK_PURPLE);
        message = message.replaceAll("&6", "" + ChatColor.GOLD);
        message = message.replaceAll("&7", "" + ChatColor.GRAY);
        message = message.replaceAll("&8", "" + ChatColor.DARK_GRAY);
        message = message.replaceAll("&9", "" + ChatColor.BLUE);
        message = message.replaceAll("(?i)&a", "" + ChatColor.GREEN);
        message = message.replaceAll("(?i)&b", "" + ChatColor.AQUA);
        message = message.replaceAll("(?i)&c", "" + ChatColor.RED);
        message = message.replaceAll("(?i)&d", "" + ChatColor.LIGHT_PURPLE);
        message = message.replaceAll("(?i)&e", "" + ChatColor.YELLOW);
        message = message.replaceAll("(?i)&f", "" + ChatColor.WHITE);
        return message;
    }

    public static String stripColor(String message)
    {
        message = message.replaceAll("&0", "");
        message = message.replaceAll("&1", "");
        message = message.replaceAll("&2", "");
        message = message.replaceAll("&3", "");
        message = message.replaceAll("&4", "");
        message = message.replaceAll("&5", "");
        message = message.replaceAll("&6", "");
        message = message.replaceAll("&7", "");
        message = message.replaceAll("&8", "");
        message = message.replaceAll("&9", "");
        message = message.replaceAll("(?i)&a", "");
        message = message.replaceAll("(?i)&b", "");
        message = message.replaceAll("(?i)&c", "");
        message = message.replaceAll("(?i)&d", "");
        message = message.replaceAll("(?i)&e", "");
        message = message.replaceAll("(?i)&f", "");
        return message;
    }
}
