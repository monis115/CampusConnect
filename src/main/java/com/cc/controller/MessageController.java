package com.cc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.modals.Message;
import com.cc.service.MessageService;

@RestController
@RequestMapping("/api/messages")

public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Map<String, Object> payload) {
        Long agentId = Long.valueOf(payload.get("agentId").toString());
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        String content = (String) payload.get("content");
        String senderRole = (String) payload.get("senderRole");

        Message message = messageService.sendMessage(agentId, studentId, content, senderRole);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Message>> getMessagesForStudent(@PathVariable Long studentId) {
        List<Message> messages = messageService.getMessagesForStudent(studentId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<com.cc.DTO.MessageDTO>> getMessagesForManager(@PathVariable Long managerId) {
        List<com.cc.DTO.MessageDTO> messages = messageService.getMessagesForManager(managerId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<com.cc.DTO.MessageDTO>> getMessagesForAgent(@PathVariable Long agentId) {
        List<com.cc.DTO.MessageDTO> messages = messageService.getMessagesForAgent(agentId);
        return ResponseEntity.ok(messages);
    }
}
