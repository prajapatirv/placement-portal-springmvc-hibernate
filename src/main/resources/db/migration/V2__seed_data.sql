insert into company (name, city, website) values
 ('Nimbus Tech', 'Ahmedabad', 'https://nimbus.example'),
 ('Kesar Systems', 'Gandhinagar', 'https://kesar.example'),
 ('Gujarat Cloudworks', 'Surat', 'https://gcw.example'),
 ('Saptarishi Analytics', 'Vadodara', 'https://saptarishi.example'),
 ('BlueLotus Software', 'Rajkot', 'https://bluelotus.example');

insert into job_posting (company_id, title, min_package_lpa, max_package_lpa, last_date, status)
select c.id, v.title, v.minp, v.maxp, cast(v.ld as date), v.st
from (values
 ('Nimbus Tech','Java Backend Intern',3.0,4.5,'2026-11-30','OPEN'),
 ('Nimbus Tech','QA Automation Trainee',2.8,3.6,'2026-09-15','CLOSED'),
 ('Kesar Systems','Spring Boot Developer Trainee',3.5,5.0,'2026-12-10','OPEN'),
 ('Kesar Systems','Data Analyst Intern',3.0,4.0,'2026-11-20','OPEN'),
 ('Gujarat Cloudworks','Cloud Support Engineer',3.2,4.8,'2026-12-05','OPEN'),
 ('Gujarat Cloudworks','DevOps Trainee',3.6,5.2,'2026-12-15','OPEN'),
 ('Gujarat Cloudworks','Java Full Stack Trainee',3.4,5.0,'2026-11-28','OPEN'),
 ('Saptarishi Analytics','Python Data Engineer Trainee',3.8,5.5,'2026-12-01','OPEN'),
 ('Saptarishi Analytics','BI Developer Intern',3.0,4.2,'2026-11-25','OPEN'),
 ('BlueLotus Software','Mobile App Trainee',3.0,4.4,'2026-12-12','OPEN'),
 ('BlueLotus Software','UI Engineer Intern',2.8,4.0,'2026-11-18','OPEN'),
 ('BlueLotus Software','Technical Support Associate',2.5,3.4,'2026-12-20','OPEN')
) as v(cname, title, minp, maxp, ld, st)
join company c on c.name = v.cname;

insert into student (name, email, branch, cgpa) values
 ('Asha Patel','asha@ppsu.example','CE',8.6), ('Rohan Shah','rohan@ppsu.example','IT',7.9),
 ('Meera Desai','meera@ppsu.example','CE',9.1), ('Kabir Mehta','kabir@ppsu.example','CS',8.2),
 ('Isha Joshi','isha@ppsu.example','IT',8.8), ('Dev Trivedi','dev@ppsu.example','ITE',7.4),
 ('Nisha Parmar','nisha@ppsu.example','CE',8.0), ('Yash Gandhi','yash@ppsu.example','CS',7.7);

insert into application (student_id, job_id, status)
select s.id, j.id, v.st
from (values
 ('asha@ppsu.example','Java Backend Intern','SHORTLISTED'), ('asha@ppsu.example','Spring Boot Developer Trainee','APPLIED'),
 ('asha@ppsu.example','Java Full Stack Trainee','APPLIED'), ('rohan@ppsu.example','Java Backend Intern','APPLIED'),
 ('rohan@ppsu.example','Cloud Support Engineer','APPLIED'), ('rohan@ppsu.example','DevOps Trainee','REJECTED'),
 ('meera@ppsu.example','Spring Boot Developer Trainee','SELECTED'), ('meera@ppsu.example','Data Analyst Intern','APPLIED'),
 ('meera@ppsu.example','BI Developer Intern','SHORTLISTED'), ('kabir@ppsu.example','QA Automation Trainee','REJECTED'),
 ('kabir@ppsu.example','Python Data Engineer Trainee','APPLIED'), ('kabir@ppsu.example','Mobile App Trainee','APPLIED'),
 ('isha@ppsu.example','UI Engineer Intern','APPLIED'), ('isha@ppsu.example','Technical Support Associate','APPLIED'),
 ('isha@ppsu.example','Java Full Stack Trainee','SHORTLISTED'), ('dev@ppsu.example','Mobile App Trainee','APPLIED'),
 ('dev@ppsu.example','Cloud Support Engineer','APPLIED'), ('nisha@ppsu.example','Data Analyst Intern','APPLIED'),
 ('nisha@ppsu.example','Python Data Engineer Trainee','SHORTLISTED'), ('yash@ppsu.example','Technical Support Associate','APPLIED')
) as v(email, title, st)
join student s on s.email = v.email
join job_posting j on j.title = v.title;
