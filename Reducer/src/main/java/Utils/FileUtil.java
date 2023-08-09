package Utils;

import dtjvms.DTPlatform;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUtil {

    public static void copyFile(String srcPath, String tgtPath, String srcFile, String tgtFile){
        File src = new File(srcPath + DTPlatform.FILE_SEPARATOR + srcFile);
        File tgt = new File(tgtPath + DTPlatform.FILE_SEPARATOR + tgtFile);
        try {
            Files.copy(src.toPath(), tgt.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
