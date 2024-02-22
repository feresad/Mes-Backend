package com.example.machine.Controller;

import com.example.machine.entity.Machine;
import com.example.machine.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RefreshScope
@RequestMapping("machines")
public class MachineController {
     @Autowired
    private MachineRepository MachineRepository;

    @GetMapping("/all")
    public Iterable<Machine> getAllMachines(){
        return MachineRepository.findAll();
    }
}
