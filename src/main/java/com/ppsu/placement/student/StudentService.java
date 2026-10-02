package com.ppsu.placement.student;

import com.ppsu.placement.application.ApplicationRepository;
import com.ppsu.placement.common.ConflictException;
import com.ppsu.placement.common.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository students;
    private final ApplicationRepository applications;

    public StudentService(StudentRepository students, ApplicationRepository applications) {
        this.students = students;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public List<StudentView> list() {
        return students.findAll(Sort.by("name")).stream().map(StudentService::toView).toList();
    }

    @Transactional(readOnly = true)
    public StudentForm getForm(Long id) {
        Student s = find(id);
        return new StudentForm(s.getName(), s.getEmail(), s.getBranch(), s.getCgpa());
    }

    @Transactional
    public Long create(StudentForm form) {
        String email = form.email().trim().toLowerCase();
        if (students.existsByEmailIgnoreCase(email))
            throw new ConflictException("A student with email " + email + " already exists.");
        return students.save(new Student(form.name().trim(), email, form.branch().trim().toUpperCase(),
                form.cgpa())).getId();
    }

    @Transactional
    public void update(Long id, StudentForm form) {
        Student s = find(id);
        String email = form.email().trim().toLowerCase();
        if (students.existsByEmailIgnoreCaseAndIdNot(email, id))
            throw new ConflictException("A student with email " + email + " already exists.");
        s.setName(form.name().trim());
        s.setEmail(email);
        s.setBranch(form.branch().trim().toUpperCase());
        s.setCgpa(form.cgpa());
    }

    @Transactional
    public void delete(Long id) {
        Student s = find(id);
        long apps = applications.countByStudentId(id);
        if (apps > 0)
            throw new ConflictException("Cannot delete " + s.getName() + ": " + apps + " application(s) exist.");
        students.delete(s);
    }

    private Student find(Long id) {
        return students.findById(id).orElseThrow(() -> new NotFoundException("Student " + id));
    }

    private static StudentView toView(Student s) {
        return new StudentView(s.getId(), s.getName(), s.getEmail(), s.getBranch(), s.getCgpa());
    }
}
