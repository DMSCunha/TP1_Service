package org.example;

import ClienteServiceServerStub.*;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import io.grpc.stub.StreamObserver;

public class ServiceClient extends ClientServiceGrpc.ClientServiceImplBase {

    private DockerClient dockerClient;
    private HostConfig hostconfig;

    public ServiceClient(DockerClient dockerClient, HostConfig hostconfig){
        this.dockerClient = dockerClient;
        this.hostconfig = hostconfig;
    }

    @Override
    public void isDone(Id request, StreamObserver<Status> responseObserver) {
        int id = request.getId();

        //check if docker has already finish work

            //if not
            responseObserver.onNext(Status.newBuilder().setStatus(false).build());
            responseObserver.onCompleted();

        //if yes
        responseObserver.onNext(Status.newBuilder().setStatus(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Image> sendImage(StreamObserver<Id> responseObserver) {

        //new id and add entry in data structer

        //send StreamObserver
        return super.sendImage(responseObserver);
    }

    @Override
    public void getImage(Id request, StreamObserver<MarkImage> responseObserver) {

        int id = request.getId();

        //check if id exists

            //if not, error and return

        //if yes, get image

        //send bytes chunks to client
        //while(......){}
            responseObserver.onNext(MarkImage.newBuilder().setImageBytes().build());

        //remove from data structer

        responseObserver.onCompleted();
    }
}
