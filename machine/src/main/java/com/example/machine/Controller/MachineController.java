package com.example.machine.Controller;

import com.example.machine.entity.Machine;
import com.example.machine.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("machines")
public class MachineController {
    private MachineRepository machineRepository;
    @GetMapping("/all")
    // get all machine by findall in the repository
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

}
