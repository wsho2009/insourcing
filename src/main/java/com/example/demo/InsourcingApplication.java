package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import common.ocr.OcrProcess;

@SpringBootApplication
public class InsourcingApplication implements CommandLineRunner {

    @Autowired
    private InsourcingConfig config;

	public static void main(String[] args) {
		SpringApplication.run(InsourcingApplication.class, args);
	}
	 
    @Override
    public void run(String... strings) throws Exception {
		OcrProcess.run(config);
   	}
}
