package com.hxl.plugin.springboot.invoke;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constant {
    public static final String LIB_NAME = "spring-invoke-starter.jar";
    public static final String CLASSPATH_LIB_PATH = "/lib/" + LIB_NAME;
    //工作目录
    public static final Path CONFIG_WORK_HOME = Paths.get(System.getProperty("user.home"), ".config", "spring-invoke", "invoke");
    //lib目录
    public static final Path CONFIG_LIB_PATH = Paths.get(CONFIG_WORK_HOME.toString(), "lib", LIB_NAME);
    //文件所目录
    public static final Path CONFIG_CONTROLLER_SETTING = Paths.get(CONFIG_WORK_HOME.toString(), "controller-setting");
}