package com.zentrix.policy.dto;

import com.zentrix.policy.PolicyAssignment;

import java.time.LocalDateTime;

public record PolicyAssignmentResponse(
        Integer id, Integer policyId, String policyName, String policyType,
        Integer deviceId, Integer groupId, LocalDateTime assignedAt
) {
    public static PolicyAssignmentResponse from(PolicyAssignment assignment) {
        return new PolicyAssignmentResponse(
                assignment.getId(),
                assignment.getPolicy().getId(),
                assignment.getPolicy().getName(),
                assignment.getPolicy().getType().name(),
                assignment.getDevice() != null ? assignment.getDevice().getId() : null,
                assignment.getGroup() != null ? assignment.getGroup().getId() : null,
                assignment.getAssignedAt()
        );
    }
}
