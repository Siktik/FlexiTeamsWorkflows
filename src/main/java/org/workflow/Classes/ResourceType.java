package org.workflow.Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceType {

    private String name;
    private boolean unlimited = false;

    public ResourceType(String name, boolean unlimited){
        this.name= name;
        setUnlimited(unlimited);
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "ResourceType{" +
                "name='" + name + '\'' +
                ", unlimited=" + unlimited +
                '}';
    }
}
