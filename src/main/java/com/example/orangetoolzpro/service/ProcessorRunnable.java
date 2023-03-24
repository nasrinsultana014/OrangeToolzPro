package com.example.orangetoolzpro.service;

import com.example.orangetoolzpro.domain.Customer;
import com.example.orangetoolzpro.domain.InvalidCustomer;
import com.example.orangetoolzpro.repository.CustomerRepository;
import com.example.orangetoolzpro.repository.InvalidCustomerRepository;

import java.util.ArrayList;
import java.util.List;

public class ProcessorRunnable implements Runnable {
    private CustomerService customerService;
    private CustomerRepository customerRepository;
    private InvalidCustomerRepository invalidCustomerRepository;

    private ArrayList<String> linesToProcess;
    private long start;
    private long end;

    public ProcessorRunnable(ArrayList<String> linesToProcess, long start, long end,
                             CustomerService customerService,
                             CustomerRepository customerRepository,
                             InvalidCustomerRepository invalidCustomerRepository){
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.invalidCustomerRepository = invalidCustomerRepository;

        this.linesToProcess = linesToProcess;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {

        for(long i=this.start; (i<this.end && i<linesToProcess.size()); i++){
            Customer customer  = this.customerService.prepareCustomer(linesToProcess.get((int)i));
            if(customer!=null){
                try{
                    this.customerRepository.save(customer);
                }catch (Exception e){
                    InvalidCustomer invalidCustomer  = this.customerService.prepareInvalidCustomer(linesToProcess.get((int)i));
                    if(invalidCustomer != null){
                        this.invalidCustomerRepository.save(invalidCustomer);
                    }
                }
            }
        }
    }
}
