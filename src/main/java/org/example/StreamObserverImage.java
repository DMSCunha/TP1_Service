package org.example;

import ClienteServiceServerStub.Image;
import io.grpc.stub.StreamObserver;

public class StreamObserverImage implements StreamObserver<Image> {

    private boolean isCompleted = false;


    public StreamObserverImage(){}

    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public void onNext(Image image) {

        //save byte chunks

        //save keywords
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

        //run docker to mark image

        isCompleted = true;
    }
}
