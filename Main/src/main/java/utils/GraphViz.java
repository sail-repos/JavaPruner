package utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;

public class GraphViz {

    public static String cmd = "dot";

    public static void convertDotStringToPng(String dotContent){
    }

    public static void convertDotFilePathToPng(String filePath){

        File dotFile = new File(filePath);
        if (dotFile.isFile() && dotFile.exists()){
            convertDotFileToPng(dotFile);
        } else {

            System.err.println(filePath + " is not a file or exists!");
        }
    }

    public static void convertDotFileToPng(File file){

        ArrayList<String> options = new ArrayList<>();
        options.add(cmd);
        options.add(file.getAbsolutePath());
        options.add("-T png");
        options.add("-o");
        options.add(file.getAbsolutePath().replace(".dot", ".png"));

        String cmds = StringUtils.join(options, " ");
        execute(cmds);
    }

    public static void execute(String cmd){

        try {

            Process process = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            new Thread(() -> {

                BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String line = null ;
                    while ((line = inputReader.readLine()) !=  null ){
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{

                    try {
                        inputStream.close();
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            try {
                String line = null;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    errorStream.close();
                    errorReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            process.waitFor();
            process.destroy();
        } catch (Exception e){

            e.printStackTrace();
        }
    }
}
