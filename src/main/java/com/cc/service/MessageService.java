package com.cc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.modals.AddmissionManager;
import com.cc.modals.Agent;
import com.cc.modals.Message;
import com.cc.modals.Student;

import com.cc.repository.AgentRepository;
import com.cc.repository.MessageRepository;
import com.cc.repository.StudentRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AgentRepository agentRepository;

    public Message sendMessage(Long agentId, Long studentId, String content, String senderRole) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        AddmissionManager manager = student.getAdmissionManager();
        if (manager == null) {
            throw new RuntimeException("Student does not have an assigned Admission Manager");
        }

        Message message = new Message(content, student, agent, manager, senderRole);
        return messageRepository.save(message);
    }

    public List<Message> getMessagesForStudent(Long studentId) {
        return messageRepository.findByStudentIdOrderByTimestampAsc(studentId);
    }

    public List<com.cc.DTO.MessageDTO> getMessagesForManager(Long managerId) {
        List<Message> messages = messageRepository.findByAdmissionManagerIdOrderByTimestampDesc(managerId);
        return messages.stream().map(msg -> new com.cc.DTO.MessageDTO(
                msg.getId(),
                msg.getContent(),
                msg.getTimestamp(),
                msg.getSenderRole(),
                msg.isRead(),
                msg.getStudent().getId(),
                msg.getStudent().getFullName(),
                msg.getStudent().getApplicationId(),
                msg.getAgent().getId(),
                msg.getAgent().getFullName(),
                msg.getAdmissionManager().getId(),
                msg.getAdmissionManager().getFullName())).collect(java.util.stream.Collectors.toList());
    }

    public List<com.cc.DTO.MessageDTO> getMessagesForAgent(Long agentId) {
        List<Message> messages = messageRepository.findByAgentIdOrderByTimestampDesc(agentId);
        return messages.stream().map(msg -> new com.cc.DTO.MessageDTO(
                msg.getId(),
                msg.getContent(),
                msg.getTimestamp(),
                msg.getSenderRole(),
                msg.isRead(),
                msg.getStudent().getId(),
                msg.getStudent().getFullName(),
                msg.getStudent().getApplicationId(),
                msg.getAgent().getId(),
                msg.getAgent().getFullName(),
                msg.getAdmissionManager().getId(),
                msg.getAdmissionManager().getFullName())).collect(java.util.stream.Collectors.toList());
    }

    public void markMessagesAsReadForStudent(Long studentId, String readerRole) {
        List<Message> messages = messageRepository.findByStudentIdOrderByTimestampAsc(studentId);
        boolean updated = false;
        for (Message msg : messages) {
            if (!msg.isRead() && !msg.getSenderRole().equals(readerRole)) {
                msg.setRead(true);
                updated = true;
            }
        }
        if (updated) {
            messageRepository.saveAll(messages);
        }
    }
}
