package com.example.oms;

import java.lang.foreign.Arena;

public class Main {

    private static final int MAX_ORDER_SIZE = 1024 * 1024; // 1MB
    private static final int RECORD_SIZE = 256; // 256 bytes
    public static void main(String[] args) {
        System.out.println("Startiung Order Management System...");


        try {
            int availableCores = Runtime.getRuntime().availableProcessors();
            System.out.println("Available CPU cores: " + availableCores);

            System.out.println("Allocationg off heap storage");
            Arena sharedArena = Arena.ofShared();

            //check if int to long conversion is slow
            MemorySegment offHeapStorage = sharedArena.allocate(MAX_ORDER_SIZE * RECORD_SIZE);

            HighSpeedOMS oms = new HighSpeedOMS(offHeapStorage);
            oms.start();

            Thread executionThread = new Thread(() ->  {
                try (AffinityLock affinityLock = AffinityLock.acquireCore()) {
                    System.out.println("Execution thread started on CPU core: " + affinityLock.cpuId());
                    oms.processOrders();


                } catch (Exception e) {
                    System.err.println("Failed to pin thread to a dedicated CPU core: " + e.getMessage());
                    
                } finally{
                    System.out.println("Execution thread finished.");
                }
            });

        } catch (Throwable e) {
            System.err.println("Error while initializing Order Management System: " );
            e.printStackTrace();
            System.exit(1);
        }
    }
}