package org.example;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) {
        String parentPath = args[0];
        String configFilePath = args[1];
        String needCheckRFile = args[2];
        if (null == parentPath) {
            System.out.println("请输入参数");
            return;
        }
        readConfig(parentPath, configFilePath);
        if (null != needCheckRFile) {
            checkRFile(parentPath);
        }
    }

    //Duplicate class android.support.v4.graphics.drawable.IconCompatParcelizer found in modules classes (classes.jar) and core-1.9.0-runtime (androidx.core:core:1.9.0)
    public static void readConfig(String pathname, String configFilePath) {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
            /* 读入TXT文件 */
            File filename = new File(configFilePath); // 要读取以上路径的input。txt文件
            System.out.println("filename = " + filename.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
            int errorCount = 0;
            while ((line = br.readLine()) != null) {
//                line = br.readLine(); // 一次读入一行数据
                System.out.println("line = " + line);
                line = line.trim();
                if (line.startsWith("Duplicate class ")) {
                    int endIndex = line.indexOf(" found in ");
                    String fileName = line.substring(16, endIndex);
                    System.out.println("fileName = " + fileName);
                    StringBuilder delFilePath = new StringBuilder();
                    delFilePath.append(pathname);
                    String[] names = fileName.split("\\.");
                    for (String s : names) {
                        delFilePath.append(File.separator);
                        delFilePath.append(s);
                    }
                    delFilePath.append(".class");
//                    fileName = pathname + File.separator + fileName.replaceAll("\\.", "\\");
                    System.out.println("delFilePath = " + delFilePath);
                    if (!delFile(delFilePath.toString())) {
                        errorCount++;
                    }
                }
            }
            if (errorCount == 0) {
                System.out.println("\033[32;4m readConfig Comp Failed! error " + errorCount + " \033[0m");
            } else {
                System.out.println("\033[31;4m readConfig Comp Done! error " + errorCount + " \033[0m");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkRFile(String parentPath) {
        func(new File(parentPath));
    }

    private static void func(File file) {
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (f.isDirectory())    //若是目录，则递归打印该目录下的文件
                func(f);
            if (f.isFile()) {      //排除jsc
                String path = f.getAbsolutePath();
                if (path.contains("android") && !path.contains("gms")) {
                    if (f.getName().equals("R.class") || f.getName().startsWith("R$")) {
                        delFile(path);
                    }
                }
            }
        }
    }

    public static boolean delFile(String filePath) {
        boolean flag = false;
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    System.out.println(file.getAbsolutePath() + "\033[32;4m is deleted! \033[0m");
                    flag = true;
                } else {
                    System.out.println("\033[31;4m Delete operation is failed. \033[0m");
                }
            } else {
                System.out.println("\033[31;4m Delete failed. \033[0m");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


}