package com.orudesk.orudesk.admissions.adapter.out.persistence;

import com.orudesk.orudesk.admissions.application.port.in.StudentListItem;
import com.orudesk.orudesk.admissions.application.port.out.ReadStudentsPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
class StudentReadAdapter implements ReadStudentsPort {

    private final EntityManager entityManager;

    StudentReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * "in :schools" with a collection: Hibernate expands it to one bound parameter per school. Two kinds of
     * filter stack here: the tenant filter Hibernate adds, and the schools this user may see.
     */
    @Override
    public List<StudentListItem> listAt(Collection<UUID> schoolIds, String className, int limit) {
        String hql = "from StudentSummaryJpaEntity s where s.schoolId in :schools"
                + (className == null ? "" : " and s.className = :className")
                + " order by s.schoolId, s.className, s.section nulls first, s.lastName, s.firstName, s.id";
        var query = entityManager.createQuery(hql, StudentSummaryJpaEntity.class).setParameter("schools", schoolIds);
        if (className != null) {
            query.setParameter("className", className);
        }
        return query.setMaxResults(limit).getResultList().stream()
                .map(row -> new StudentListItem(row.getId(), row.getSchoolId(), row.getFirstName(), row.getLastName(),
                        row.getAdmissionNumber(), row.getClassName(), row.getSection()))
                .toList();
    }
}
