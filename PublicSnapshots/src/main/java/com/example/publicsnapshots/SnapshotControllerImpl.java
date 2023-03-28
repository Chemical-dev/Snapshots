package com.example.publicsnapshots;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/snapshot")
public class SnapshotControllerImpl {

    @Autowired
    private SnapshotServiceImpl snapshotService;

    @GetMapping("/new")
    public List<PublicSnapShots> getSnapShots(){
        return snapshotService.getSnapshot();
    }

    @PutMapping("/new")
    public void modifySnapShots(){
        snapshotService.modifyAttribute();
    }

    @GetMapping("/new1")
    public List<String> lambda(){
        return snapshotService.ListFunctions();
    }

    @GetMapping("/new2")
    public void vpc(){
        snapshotService.getVpcs();
    }

    @GetMapping("/bucket")
    public void getBuckets(){
        snapshotService.getBucketPolicy();
    }
}
