package com.example.orangetoolzpro.service;

import com.example.orangetoolzpro.domain.Customer;
import com.example.orangetoolzpro.domain.InvalidCustomer;
import com.example.orangetoolzpro.repository.CustomerRepository;
import com.example.orangetoolzpro.repository.InvalidCustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CustomerService {
    private static final long LINES_PER_THREAD =  25000;
    private static final long INVALID_CUSTOMER_PER_THREAD =  1000;
    private static final long VALID_CUSTOMER_PER_THREAD =  100000;

    private CustomerRepository customerRepository;
    private InvalidCustomerRepository invalidCustomerRepository;

    public CustomerService(CustomerRepository customerRepository,
                           InvalidCustomerRepository invalidCustomerRepository){
        this.customerRepository = customerRepository;
        this.invalidCustomerRepository = invalidCustomerRepository;
    }

    /*
    * Processes the text file
    */
    public void saveCustomers(MultipartFile file){

        ArrayList<String> lines = getLinesFromFile(file);
        if(lines.size() > 0){
            long numberOfProcessingThreadsNeeded = lines.size()/ LINES_PER_THREAD ;
            if(numberOfProcessingThreadsNeeded <= 0)
                numberOfProcessingThreadsNeeded = 1;
            System.out.println("lines.length = " + lines.size());
            System.out.println("numberOfProcessingThreadsNeeded = " + (int)numberOfProcessingThreadsNeeded);

            // Create threads to process the files.
            ExecutorService executor = Executors.newFixedThreadPool((int) numberOfProcessingThreadsNeeded);
            long count = 0;
            for (long i = 0; i < lines.size(); i = i + LINES_PER_THREAD) {
                count++;
                System.out.println("start = " + i + "    end = " + (i + LINES_PER_THREAD));
                Runnable worker = new ProcessorRunnable(lines, i, i + LINES_PER_THREAD,
                        this,
                        this.customerRepository,
                        this.invalidCustomerRepository);
                executor.execute(worker);
            }
            executor.shutdown();
            System.out.println("Created thread = " + count);
            while (!executor.isTerminated()) {
                // empty body
            }
            System.out.println("\nFinished all threads");
        }
    }
    public ArrayList<String> getLinesFromFile(MultipartFile file){
        ArrayList<String> lines = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            while((line=reader.readLine())!=null) {
                System.out.println(line);
                lines.add(line);
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        return lines;
    }
    public Customer prepareCustomer(String line){
        String data[] = line.split(",");

        Customer customer = null;
        if(data.length == 8){
            customer = new Customer();
            customer.setFirstName(data[0]);
            customer.setLastName(data[1]);
            customer.setCity(data[2]);
            customer.setCountryCode(data[3]);
            customer.setZipCode(data[4]);
            customer.setPhone(data[5]);
            customer.setEmail(data[6]);
            customer.setIp(data[7]);
        }
        return customer;
    }
    public InvalidCustomer prepareInvalidCustomer(String line){
        String data[] = line.split(",");

        InvalidCustomer invalidCustomer = null;
        if(data.length == 8){
            invalidCustomer = new InvalidCustomer();
            invalidCustomer.setFirstName(data[0]);
            invalidCustomer.setLastName(data[1]);
            invalidCustomer.setCity(data[2]);
            invalidCustomer.setCountryCode(data[3]);
            invalidCustomer.setZipCode(data[4]);
            invalidCustomer.setPhone(data[5]);
            invalidCustomer.setEmail(data[6]);
            invalidCustomer.setIp(data[7]);
        }
        return invalidCustomer;
    }


    /*
     * Exports data from DB
     */
    public void exportCustomer(String type){

        int fileCount = 1;
        StringBuilder stringBuilder = new StringBuilder();
        long numberOfProcessingThreadsNeeded = 1;
        long counter = 1, loopBoundary = 1;
        List<InvalidCustomer> invalidCustomers = null;
        List<Customer> customers = null;
        String fileName = "";

        if(type.equalsIgnoreCase("invalid")){
            invalidCustomers = (List<InvalidCustomer>) this.invalidCustomerRepository.findAll();
            numberOfProcessingThreadsNeeded = invalidCustomers.size() / INVALID_CUSTOMER_PER_THREAD ;
            counter = INVALID_CUSTOMER_PER_THREAD;
            loopBoundary = invalidCustomers.size();
            fileName = "invalidUser.txt";

            System.out.println("invalidCustomers count = " + invalidCustomers.size());
            System.out.println("numberOfProcessingThreadsNeeded = " + numberOfProcessingThreadsNeeded);

        }else if(type.equalsIgnoreCase("valid")){
            customers = (List<Customer>) this.customerRepository.findAll();
            numberOfProcessingThreadsNeeded = customers.size() / VALID_CUSTOMER_PER_THREAD ;
            counter = VALID_CUSTOMER_PER_THREAD;
            loopBoundary = customers.size();
            fileName = "validUser.txt";

            System.out.println("customers count = " + customers.size());
            System.out.println("numberOfProcessingThreadsNeeded = " + numberOfProcessingThreadsNeeded);
        }

        if(invalidCustomers != null || customers != null){
            ArrayList<Runnable> workers = new ArrayList<>();
            if(numberOfProcessingThreadsNeeded <= 0)
                numberOfProcessingThreadsNeeded = 1;

            // Create threads to get data from DB
            ExecutorService executor = Executors.newFixedThreadPool((int) numberOfProcessingThreadsNeeded);

            long count = 0;
            for (long i = 0; i < loopBoundary; i = i + counter) {
                count++;
                System.out.println("start = " + i + "    end = " + (i + counter));
                Runnable worker = new ExporterRunnable(
                        invalidCustomers, customers,
                        (int)i, (int)(i + counter), type, new StringBuilder(),
                        this,
                        this.customerRepository,
                        this.invalidCustomerRepository);
                workers.add(worker);
                executor.execute(worker);
            }
            executor.shutdown();
            System.out.println("Created thread = " + count);
            while (!executor.isTerminated()) {
                // empty body
            }
            System.out.println("\nFinished all threads");

            // Write the data in the file
            if(type.equalsIgnoreCase("invalid")){
                for(int i =0; i<workers.size(); i++){
                    ExporterRunnable exporterRunnable = (ExporterRunnable)workers.get(i);
                    stringBuilder.append(exporterRunnable.getData().toString());
                }
                String data = stringBuilder.toString();
                writeInFile(data, fileName);
            }else if(type.equalsIgnoreCase("valid")){
                fileCount = workers.size();
                for(int i =0; i<workers.size(); i++){
                    ExporterRunnable exporterRunnable = (ExporterRunnable)workers.get(i);
                    writeInFile(exporterRunnable.getData().toString(), i + "_" + fileName);
                }
                prepareZipFile("validCustomerCompressed.zip", fileCount, "validUser.txt");
            }
        }
    }
    public void prepareLineFromInvalidCustomer(StringBuilder stringBuilder, List<InvalidCustomer> invalidCustomers, int start, int end){
        for(int i=start; i<end; i++){
            try{
                InvalidCustomer invalidCustomer = invalidCustomers.get(i);
                if(invalidCustomer!=null){
                    stringBuilder.append(invalidCustomer.getFirstName() + ",");
                    stringBuilder.append(invalidCustomer.getLastName() + ",");
                    stringBuilder.append(invalidCustomer.getCity() + ",");
                    stringBuilder.append(invalidCustomer.getCountryCode() + ",");
                    stringBuilder.append(invalidCustomer.getZipCode() + ",");
                    stringBuilder.append(invalidCustomer.getPhone() + ",");
                    stringBuilder.append(invalidCustomer.getEmail() + ",");
                    stringBuilder.append(invalidCustomer.getIp());
                }
                stringBuilder.append(System.getProperty("line.separator"));
            }catch (Exception e){
                System.out.println("Problem i = " + i);
            }
        }
    }
    public void prepareLineFromCustomer(StringBuilder stringBuilder, List<Customer> customers, int start, int end){
        for(int i=start; i<end; i++){
            try{
                Customer customer = customers.get(i);
                if(customer!=null){
                    stringBuilder.append(customer.getFirstName() + ",");
                    stringBuilder.append(customer.getLastName() + ",");
                    stringBuilder.append(customer.getCity() + ",");
                    stringBuilder.append(customer.getCountryCode() + ",");
                    stringBuilder.append(customer.getZipCode() + ",");
                    stringBuilder.append(customer.getPhone() + ",");
                    stringBuilder.append(customer.getEmail() + ",");
                    stringBuilder.append(customer.getIp());
                }
                stringBuilder.append(System.getProperty("line.separator"));
            }catch (Exception e){
                System.out.println("Problem i = " + i);
            }
        }
    }

    private void writeInFile(String data, String fileName){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(data);
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void prepareZipFile(String zipFileName, int fileCount, String fileNameToZip){
        try{
            final FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for(int i =0; i<fileCount; i++){
                File file = new File(i + "_" + fileNameToZip);
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
