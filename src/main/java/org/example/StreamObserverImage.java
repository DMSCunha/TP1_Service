package org.example;

import ClienteServiceServerStub.Image;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import io.grpc.stub.StreamObserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamObserverImage implements StreamObserver<Image> {

    private boolean isCompleted = false;
    private final Map<Integer, ImageContainer> imageContainerMap;
    private final DockerClient dockerClient;
    private final HostConfig hostConfig;
    private final int counterId;

    private String keywords;
    private String imageName;
    private final List<byte[]> bytesList = new ArrayList<>();

    public StreamObserverImage(Map<Integer, ImageContainer> imageContainerMap, int counterId,
                               DockerClient dockerClient, HostConfig hostconfig) {

        this.imageContainerMap = imageContainerMap;
        this.counterId = counterId;
        this.dockerClient = dockerClient;
        this.hostConfig = hostconfig;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public void onNext(Image image) {

        //save byte chunks
        bytesList.add(image.getImageBytes().toByteArray());

        //save keywords
        this.keywords = image.getKeywords();

        //save image name
        this.imageName = image.getName();
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }

    @Override
    public void onCompleted() {

        //guardar o ficheiro com array de array de bytes recebido
        try(FileOutputStream fileOut = new FileOutputStream(imageName)){
            for(byte[]aux : bytesList)
                fileOut.write(aux);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //criar e correr container para marcar a imagem recebida
        CreateContainerResponse containerResponse = dockerClient
                .createContainerCmd("tp1:markimage")
                .withName("container"+counterId)
                .withHostConfig(hostConfig)
                .withCmd(imageName,imageName,keywords)
                .exec();
        dockerClient.startContainerCmd(containerResponse.getId()).exec();

        //associar id ao container
        InspectContainerResponse inspectContainerResponse = dockerClient
                .inspectContainerCmd("container"+counterId).exec();

        imageContainerMap.put(counterId, new ImageContainer(imageName,inspectContainerResponse));

        isCompleted = true;
    }
}
