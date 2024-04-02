package com.example.consommation.Controller;

import com.example.consommation.Repository.ConsoRepository;
import com.example.consommation.entity.consommation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consommations")
public class ConsomController {
    @Autowired
    private ConsoRepository consomRepository;

    @GetMapping("/all")
    public Object getConsom(){
        return consomRepository.findAll();
    }
   @GetMapping("/{id}")
    public Object getConsom(@PathVariable(name = "id") Long id){
        return consomRepository.findById(id);
    }
    @PostMapping("/add")
    public consommation addConsom(@RequestBody consommation consommation){
        return consomRepository.save(consommation);
    }
    @DeleteMapping("/{id}")
    public void deleteConsom(@PathVariable(name = "id") Long id){
        consomRepository.deleteById(id);
    }
    @GetMapping("/count")
    public Long countConsom(){
        return consomRepository.count();
    }
    @PutMapping("/{id}")
    public consommation updateConsom(@PathVariable(name = "id") Long id, @RequestBody consommation consommation){
        consommation.setId(id);
        return consomRepository.save(consommation);
    }
    //get liste des consommation par id machine
    @GetMapping("/machine/{idMachine}")
    public Iterable<consommation> getConsomByMachine(@PathVariable(name = "idMachine") Long idMachine){
        return consomRepository.findByMachineId(idMachine);
    }
}
