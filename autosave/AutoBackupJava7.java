package autosave;

import autosave.CopyFileVisitor;
import java.io.File;
import java.nio.file.Files;

public class AutoBackupJava7 extends Thread
{

    public void run() {}

    public void do7backup(File sourceLocation, File targetLocation)
    {
        try
        {
            Files.walkFileTree(sourceLocation.toPath(), new CopyFileVisitor(targetLocation));
        }
        catch (Exception var4)
        {
            ;
        }
    }
}
