package org.workflow.printer;

import jdk.jfr.Description;

public class Printer {
    @Description("print Output, first Param is Source (f.e. Name), second Param is Message")
    public static void print(String source, String message){
        System.out.println(source+": "+ message);
    }
}
