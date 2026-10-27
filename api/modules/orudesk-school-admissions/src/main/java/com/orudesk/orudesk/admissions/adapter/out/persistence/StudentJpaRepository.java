package com.orudesk.orudesk.admissions.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface StudentJpaRepository extends JpaRepository<StudentJpaEntity, UUID> {
}
