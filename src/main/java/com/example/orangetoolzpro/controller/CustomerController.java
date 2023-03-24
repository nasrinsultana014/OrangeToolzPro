package com.example.orangetoolzpro.controller;

import com.example.orangetoolzpro.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
@Controller
@RequestMapping("/customer")
public class CustomerController {
    private CustomerService customerService;
    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    @PostMapping("/uploadFile")
    public String uploadCustomer(@RequestParam("file") MultipartFile file){
        if(file.getSize() > 0){
            this.customerService.saveCustomers(file);
        }
        return "home";
    }

    @GetMapping
    public ResponseEntity<Resource> exportData(@RequestParam("type") String type){
        System.out.println("type = " + type);
        this.customerService.exportCustomer(type);
        String fileName = "";

        if("invalid".equalsIgnoreCase(type)) {
            fileName = "invalidUser.txt";
        } else if("valid".equalsIgnoreCase(type)){
            fileName = "validCustomerCompressed.zip";
        }
        if(!"".equalsIgnoreCase(fileName)){
            try {
                File file = new File(fileName);
                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
                HttpHeaders header = new HttpHeaders();
                header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
                header.add("Cache-Control", "no-cache, no-store, must-revalidate");
                header.add("Pragma", "no-cache");
                header.add("Expires", "0");
                return ResponseEntity.ok()
                        .headers(header)
                        .contentLength(file.length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } catch (Exception e) {
                return null;
            }
        }else{
            return null;
        }
    }
}
