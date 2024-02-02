package org.workflow.Classes;

public class Qualifikation {

    private static int idCounter=0;
    private int id;
    private String name;

    public Qualifikation(String name){
        this.id=idCounter++;
        this.name=name;
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
        return "Qualifikation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
