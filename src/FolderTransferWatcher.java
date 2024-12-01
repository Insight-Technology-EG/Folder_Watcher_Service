import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.walk;

public class FolderTransferWatcher  {

    public static void main(String[] args) {
        String sourcePath = "C:\\Users\\AtefMagdy\\Desktop\\Batchs";
        String destinationPath = "C:\\Users\\AtefMagdy\\Desktop";

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(sourcePath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Monitoring folder: " + sourcePath);

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path createdPath = path.resolve((Path) event.context());
                        if (Files.isDirectory(createdPath)) {
                            System.out.println("New folder detected: " + createdPath);

                            if (isFolderStable(createdPath)) {
                                String folderName = createdPath.getFileName().toString();
                                Path destinationFolder = Paths.get(destinationPath, folderName);

                                transferFolder(createdPath, destinationFolder);
                            } else {
                                System.out.println("Folder copy incomplete or unstable: " + createdPath);
                            }
                        }
                    }
                }

                if (!key.reset()) break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFolderStable(Path folderPath) {
        try {
            long previousSize = -1;
            int maxRetries = 5;
            int retryInterval = 5;

            for (int i = 0; i < maxRetries; i++) {
                long currentSize = calculateFolderSize(folderPath);

                if (currentSize == previousSize) {
                    System.out.println("Folder is stable: " + folderPath);
                    return true;
                }

                previousSize = currentSize;
                System.out.println("Folder is not stable, checking again...");
                TimeUnit.SECONDS.sleep(retryInterval);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Folder stability check interrupted: " + e.getMessage());
        }

        return false;
    }

    private static long calculateFolderSize(Path folderPath) {
        final long[] size = {0};

        try {
            Files.walkFileTree(folderPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size[0] += attrs.size();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error calculating folder size: " + e.getMessage());
        }

        return size[0];
    }

    private static void transferFolder(Path source, Path destination) {
        try {
            // Copy folder and its contents
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = destination.resolve(source.relativize(dir));
                    Files.createDirectories(targetPath); // Create directories at the destination
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = destination.resolve(source.relativize(file));
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING); // Copy files
                    return FileVisitResult.CONTINUE;
                }
            });

            if (isTransferSuccessful(source, destination)) {
                Files.walkFileTree(source, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });

                System.out.println("Folder successfully moved to: " + destination);
            } else {
                System.err.println("Transfer verification failed. Folder not deleted: " + source);
            }
        } catch (IOException e) {
            System.err.println("Failed to move folder: " + e.getMessage());
        }
    }

    private static boolean isTransferSuccessful(Path source, Path destination) {
        try {
            // Compare contents of source and destination
            return walk(source).allMatch(srcPath -> {
                Path destPath = destination.resolve(source.relativize(srcPath));
                return Files.exists(destPath) && (Files.isDirectory(srcPath) || compareFileSize(srcPath, destPath));
            });
        } catch (IOException e) {
            System.err.println("Error verifying transfer: " + e.getMessage());
            return false;
        }
    }

    private static boolean compareFileSize(Path sourceFile, Path destFile) {
        try {
            return Files.size(sourceFile) == Files.size(destFile);
        } catch (IOException e) {
            System.err.println("Error comparing file sizes: " + e.getMessage());
            return false;
        }
    }


}
