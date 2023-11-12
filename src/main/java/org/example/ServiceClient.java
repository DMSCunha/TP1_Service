package org.example;

import ClienteServiceServerStub.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ServiceClient extends ClientServiceGrpc.ClientServiceImplBase {

    private final DockerClient dockerClient;
    private final HostConfig hostconfig;
    private int counterId = 0;

    private final Map<Integer, ImageContainer> imageContainerMap= new HashMap<>();


    public ServiceClient(DockerClient dockerClient, HostConfig hostconfig){
        this.dockerClient = dockerClient;
        this.hostconfig = hostconfig;
    }

    @Override
    public void isDone(Id request, StreamObserver<Status> responseObserver) {

        //se o id existir
        if(imageContainerMap.containsKey(request.getId())){

            //saber o estado do container
            InspectContainerResponse.ContainerState state =
                    imageContainerMap.get(request.getId()).getContainerResponse().getState();

            if(state.getExitCodeLong() == 0){
                //Se já acabou
                responseObserver.onNext(Status.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            }
            else{
                //Se nao acabou
                responseObserver.onNext(Status.newBuilder().setStatus(false).build());
                responseObserver.onCompleted();
            }
        }
        //se o id nao existir
        else {
            responseObserver.onError(new Throwable("No image with that id!"));
        }
    }

    @Override
    public StreamObserver<Image> sendImage(StreamObserver<Id> responseObserver) {

        //send StreamObserver
        return new StreamObserverImage(imageContainerMap,counterId++,dockerClient,hostconfig);
    }

    @Override
    public void getImage(Id request, StreamObserver<MarkImage> responseObserver) {

        //se o id nao existir
        if(!imageContainerMap.containsKey(request.getId())){
            responseObserver.onError(new Throwable("No image with that id!"));
            return;
        }
        //se o id existir
        ImageContainer imagePath = imageContainerMap.get(request.getId());




        //byte images
        Path filePath = Paths.get(imagePath.getImage());
        //get image
        byte[] imageData;
        try {
            imageData = Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //partir a image em chuncks
            //divide image by 4
        List<byte[]> byteList= divideArray(imageData, 4);

        /*
        //transform byte list in byteString
        ByteString byteSeq0 = ByteString.copyFrom(byteList.get(0));
        ByteString byteSeq1 = ByteString.copyFrom(byteList.get(1));
        ByteString byteSeq2 = ByteString.copyFrom(byteList.get(2));
        ByteString byteSeq3 = ByteString.copyFrom(byteList.get(3));
         */

        //enviar os chuncks
        for(int i = 0; i < 4; i++){
            responseObserver.onNext(
                    MarkImage.newBuilder()
                            .setImageBytes(ByteString.copyFrom(byteList.get(i)))
                            .build());
        }

        //remove from data structer
        imageContainerMap.remove(request.getId());

        responseObserver.onCompleted();
    }

    public static List<byte[]> divideArray(byte[] source, int chunksize) {

        List<byte[]> result = new ArrayList<>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }

        return result;
    }

}
