package org.example;

import com.github.dockerjava.api.command.InspectContainerResponse;

public class ImageContainer {
    private final String image;
    private final InspectContainerResponse containerResponse;

    public ImageContainer(String image, InspectContainerResponse containerResponse){
        this.image = image;
        this.containerResponse = containerResponse;
    }

    public InspectContainerResponse getContainerResponse() {
        return containerResponse;
    }

    public String getImage() {
        return image;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ImageContainer){
            if(!image.equals(((ImageContainer) obj).image))
                return false;
            return containerResponse.equals(((ImageContainer) obj).containerResponse);
        }
        return false;
    }


}
