package com.example.ragsearch.controller;

import com.example.ragsearch.model.Workspace;
import com.example.ragsearch.model.WorkspaceRequest;
import com.example.ragsearch.service.WorkspaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<List<Workspace>> listWorkspaces() {
        return ResponseEntity.ok(workspaceService.listWorkspaces());
    }

    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody WorkspaceRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.createWorkspace(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> archiveWorkspace(@PathVariable String workspaceId) {
        workspaceService.archiveWorkspace(workspaceId);
        return ResponseEntity.noContent().build();
    }
}
