package com.example.machine.Controller;

import com.example.machine.entity.Machine;
import com.example.machine.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("machines")
public class MachineController {
     @Autowired
    private MachineRepository MachineRepository;

    @GetMapping("/all")
    public Iterable<Machine> getAllMachines(){
        return MachineRepository.findAll();
    }
    @GetMapping("/{id}")
    public Machine getMachinebyId(@PathVariable(name = "id") Long id){
        return MachineRepository.findById(id).get();
    }
    @PostMapping("/add")
    public Machine addMachine(@RequestBody Machine machine){
        machine.setEtat(true);
        return MachineRepository.save(machine);
    }
    @DeleteMapping("/{id}")
    public void deleteMachine(@PathVariable(name = "id") Long id){
        MachineRepository.deleteById(id);
    }
    @PutMapping("/{id}")
    public Machine updateMachine(@PathVariable(name = "id") Long id, @RequestBody Machine machine){
        machine.setId(id);
        return MachineRepository.save(machine);
    }
    @GetMapping("/count")
    public Long countMachine(){
        return MachineRepository.count();
    }
    @GetMapping("/search")
    public Iterable<Machine> searchMachine(@RequestParam(name = "etat") boolean etat){
        return MachineRepository.findByEtat(etat);
    }
    @GetMapping("/searchMachine")
    public Iterable<Machine> searchMachine(@RequestParam(name = "name") String name){
        return MachineRepository.searchMachineByName(name);
    }
}
