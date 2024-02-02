package org.workflow.Classes;

import lombok.Getter;

@Getter
public class Resource {

    private static int idCounter=0;
    private int id;
    private String name;
    private String type;

    public Resource(String name, String type) {
        this.id=idCounter++;
        this.name=name;
        this.type= type;
        //System.out.println(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
