package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParallelExecution {

    private int ID;
    private static int idCounter = 0;

    private Task endingOnTask;
    private Task startingOnTask;
    private String name;

    public ParallelExecution(String name, Task endingOnTask, Task startingOnTask) {
        this.ID = ++ idCounter;
        this.endingOnTask = endingOnTask;
        this.startingOnTask = startingOnTask;
        this.name= name;
    }

    @Override
    public String toString() {
        return "ParallelExecution{" +
                "ID=" + ID +
                ", endingOnTask=" + endingOnTask +
                ", startingOnTask=" + startingOnTask +
                ", name='" + name + '\'' +
                '}';
    }
}
