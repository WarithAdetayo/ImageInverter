package com.encentral.inverter.impl;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.encentral.inverter.api.IImageInverter;
import com.encentral.inverter.impl.actor.ImageInverterActor;

public class DefaultImageInverter implements IImageInverter {
    @Override
    public void invertImage(String imagePath, String outputFilename) {
        ActorSystem akkaSystem = ActorSystem.create("system");
        ActorRef imageInverterActor = akkaSystem.actorOf(ImageInverterActor.create(), "ImageInverterActor");

        imageInverterActor.tell(new ImageInverterActor.ImagePath(imagePath, outputFilename), ActorRef.noSender());
    }
}
