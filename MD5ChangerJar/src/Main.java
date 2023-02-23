import org.apache.commons.cli.*;

import java.io.*;

public class Main {
    static int errorCount = 0;
    static String osName = System.getProperty("os.name");

    public static void main(String[] args) {
        errorCount = 0;
        Options opts = new Options();
        opts.addOption("h", false, "help");  //输入-h,可以显示所有参数及用法

        Option inputOption = Option.builder("fi")                                    //c就是opt
                .required(false)                                       //是否必须含有该参数
                .hasArg()                                               //带一个参数
                .argName("file")                                        //参数的名字
                .longOpt("file_input")
                .desc("Input file Path")            //描述
                .build();
        opts.addOption(inputOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cl;
        String inputPath = "";

        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("Options", opts);
                } else {
                    inputPath = cl.getOptionValue("fi");
                    func(new File(inputPath));
                }
            } else {
                System.out.println("the paramter is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (errorCount > 0) {
            System.out.println("\033[31;4m" + "mission completed! error count " + errorCount + "\033[0m");
        } else {
            System.out.println("\033[32;4m" + "mission completed! error count " + errorCount + "\033[0m");
        }

    }

    public static boolean exMD5Change(File exFile) {
        StringBuffer stringBuffer = new StringBuffer(exFile.getAbsolutePath());
        String oldMD5 = "";
        String newMD5 = "";
        try {
            stringBuffer.append("\n   oldMD5=");
            stringBuffer.append(oldMD5 = getMD5(exFile));
            String tpPath = exFile.getAbsolutePath();
            if (tpPath.contains(" ")) {
                tpPath = tpPath.replaceAll(" ", "\" \"");
            }
            String[] command = getCmd(tpPath);
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            if (p.exitValue() == 0) {
                stringBuffer.append("\n   newMD5=").append(newMD5 = getMD5(exFile));
            } else {
                System.out.println("\033[31;4m" + "exitValue=" + p.exitValue() + "\033[0m");
            }
        } catch (Exception e) {
            System.out.println("\033[31;4m" + e.getMessage() + "\033[0m");
        }

        boolean flag = false;
        stringBuffer.append("\n");
        if ("".equals(oldMD5) || "".equals(newMD5) || oldMD5.equals(newMD5)) {
            stringBuffer.append("\033[31;4m" + "Fail!" + "\033[0m");
        } else {
            stringBuffer.append("\033[32;4m" + "Done!" + "\033[0m");
            flag = true;
        }
        System.out.println(stringBuffer);
        return flag;
    }

    private static void func(File file) {
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (f.isDirectory())    //若是目录，则递归打印该目录下的文件
                func(f);
            if (f.isFile() && !f.getName().endsWith(".jsc")) {      //排除jsc
                if (!exMD5Change(f)) {
                    errorCount++;
                }
            }
        }
    }

    private static String[] getCmd(String tpPath) {
        if (osName.contains("Windows")) {
            return new String[]{"cmd", "/c", "echo.", ">>", tpPath};
        } else {
            return new String[]{"echo \"\n\" >> " + tpPath};
        }
    }

    private static String getMD5(File file) {
        if (osName.contains("Windows")) {
            return getWindowsMD5(file);
        } else {
            return getMacMD5(file);
        }
    }

    private static String getMacMD5(File file) {
        String md5Str = "err";
        try {
//            String md5Cmd = "md5 " + ;
            String[] md5Cmd = {"md5", file.getAbsolutePath()};

            Process md5CmdProcess = Runtime.getRuntime().exec(md5Cmd);
            md5CmdProcess.waitFor();
            if (md5CmdProcess.exitValue() == 0) {
                InputStream md5Is = md5CmdProcess.getInputStream();
                BufferedReader md5Reader = new BufferedReader(new InputStreamReader(md5Is));
                md5Str = md5Reader.readLine().split("=")[1];
            }
        } catch (Exception e) {

        }
        return md5Str;
    }

    private static String getWindowsMD5(File file) {
        String md5Str = "err";
        try {
            String[] md5Cmd = {"cmd", "/c", "certutil", "-hashfile", file.getAbsolutePath(), "MD5"};

            Process md5CmdProcess = Runtime.getRuntime().exec(md5Cmd);
            md5CmdProcess.waitFor();
            if (md5CmdProcess.exitValue() == 0) {
//                InputStream md5Is = md5CmdProcess.getInputStream();
//                LineNumberReader md5Reader = new LineNumberReader(new InputStreamReader(md5Is));
//                md5Reader.setLineNumber(2);
//                md5Str = md5Reader.readLine();
//                System.out.println("md5Str:" + md5Str);
                InputStream md5Is = md5CmdProcess.getInputStream();
                BufferedReader md5Reader = new BufferedReader(new InputStreamReader(md5Is));
//                md5Str = md5Reader.readLine();
                for (int i = 0; i < 2; i++) {
                    if (i == 1) {
                        md5Str = md5Reader.readLine();
                    } else {
                        md5Reader.readLine();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("md5Str error:" + e.getMessage());
        }
        return md5Str;
    }

}