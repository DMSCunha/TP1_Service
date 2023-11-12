package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;


import java.net.URI;

import ResgisterServiceStub.*;
import ResgisterServiceStub.Void;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;

public class Main{

    private static String svcIp; private static int svcPort;
    //private static String svcIp = "34.175.157.230"; private static int svcPort = 8500;
    //private static String svcIp = "localhost"; private static int svcPort = 8500;
    private static String myIp; private static int myPort;
    //private static String myIp = "1"; private static int myPort = 1;
    private static RegisterServiceGrpc.RegisterServiceBlockingStub registerServiceBlockingStub;
    private static ManagedChannel registerChannel;
    private static final String volumePath = "/usr/datafiles";
    private static String pathVolDir;
    private static DockerClient dockerClient;


    //args[0] = unix:///var/run/docker.sock
    //args[1] = 320.120.3.1:1500 -> register address
    //args[2] = 320.120.3.1:7500 -> my address
    //args[3] = /usr/...         -> diretoria do volume
    public static void main(String[] args) {
        try{
            String HOST_URI;
            if(args.length == 4){
                HOST_URI = args[0];
                svcIp = getSvcIp(args[1]);
                svcPort = getSvcPort(args[1]);
                myIp = getSvcIp(args[2]);
                myPort = getSvcPort(args[2]);
                pathVolDir = args[3];
            }else{
                System.out.println("Invalid number of arguments");
                return;
            }

            //conecção ao docker
            dockerClient = DockerClientBuilder
                    .getInstance()
                    .withDockerHttpClient(
                            new ApacheDockerHttpClient.Builder()
                                    .dockerHost(URI.create(HOST_URI)).build()
                    )
                    .build();
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withBinds(new Bind(pathVolDir, new Volume(volumePath)));


            //canal de comunicação com o register
            registerChannel = ManagedChannelBuilder.forAddress(svcIp,svcPort)
                    .usePlaintext()
                    .build();

            //blocking stub para comunicar com o resgister
            registerServiceBlockingStub = RegisterServiceGrpc.newBlockingStub(registerChannel);

            //perguntar se tá vivo
            Status status = registerServiceBlockingStub.isAlive(Void.newBuilder().build());
            if(!status.getStatus()){
                System.out.println("ERROR: server resgister not available");
                return;
            }

            //conectar-se ao register
            Ack ack = registerServiceBlockingStub.connect(Address.newBuilder()
                    .setIp(myIp)
                    .setPort(myPort)
                    .build());
            if(!ack.getAck()){
                System.out.println("ERROR: server register ack false");
                return;
            }


            //começar o serviço
            io.grpc.Server svc = ServerBuilder
                .forPort(myPort)
                .addService(new ServiceClient(dockerClient, hostConfig))
                .build();
            svc.start();
            System.out.println("Server started, listening on " + myPort);

            svc.awaitTermination();
            svc.shutdown();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getSvcIp(String svcAddress){
        return svcAddress.substring(0,svcAddress.indexOf(":"));
    }

    public static int getSvcPort(String svcAddress) {
        return Integer.parseInt(svcAddress.substring(svcAddress.indexOf(":")+1));
    }
}