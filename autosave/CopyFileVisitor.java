package autosave;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

public class CopyFileVisitor extends SimpleFileVisitor
{

    private final Path targetPath;
    private Path sourcePath = null;


    public CopyFileVisitor(File targetPath)
    {
        this.targetPath = targetPath.toPath();
    }

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
    {
        if (this.sourcePath == null)
        {
            this.sourcePath = dir;
        }
        else
        {
            Files.createDirectories(this.targetPath.resolve(this.sourcePath.relativize(dir)), new FileAttribute[0]);
        }

        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        Files.copy(file, this.targetPath.resolve(this.sourcePath.relativize(file)), new CopyOption[0]);
        return FileVisitResult.CONTINUE;
    }
}
