package com.example.orangetoolzpro.service;

import com.example.orangetoolzpro.domain.Customer;
import com.example.orangetoolzpro.domain.InvalidCustomer;
import com.example.orangetoolzpro.repository.CustomerRepository;
import com.example.orangetoolzpro.repository.InvalidCustomerRepository;

import java.util.ArrayList;
import java.util.List;

public class ExporterRunnable implements Runnable {

    private CustomerService customerService;
    private CustomerRepository customerRepository;
    private InvalidCustomerRepository invalidCustomerRepository;

    private List<InvalidCustomer> invalidCustomers;
    private List<Customer> customers;
    private int start;
    private int end;
    private String task = "";
    private StringBuilder stringBuilder;

    public ExporterRunnable(List<InvalidCustomer> invalidCustomers, List<Customer> customers, int start, int end, String task, StringBuilder stringBuilder,
                             CustomerService customerService,
                             CustomerRepository customerRepository,
                             InvalidCustomerRepository invalidCustomerRepository){
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.invalidCustomerRepository = invalidCustomerRepository;

        this.invalidCustomers = invalidCustomers;
        this.customers = customers;
        this.start = start;
        this.end = end;
        this.task = task;
        this.stringBuilder = stringBuilder;
    }

    @Override
    public void run() {
        if("invalid".equalsIgnoreCase(task)){
            this.customerService.prepareLineFromInvalidCustomer(
                    this.stringBuilder,this.invalidCustomers, this.start, this.end);
        }else if("valid".equalsIgnoreCase(task)){
            this.customerService.prepareLineFromCustomer(
                    this.stringBuilder,this.customers, this.start, this.end);
        }
    }

    public StringBuilder getData(){
        return this.stringBuilder;
    }
}
