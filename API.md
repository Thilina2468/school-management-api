# School Management API

Base URL: `http://localhost:8080/api/v1`

## Setup

1. Start MariaDB
2. Create database: `CREATE DATABASE school_management_db;`
3. Run the app
4. Seed permissions:

```sql
USE school_management_db;

INSERT INTO role_permissions (role, permissions) VALUES 
('STUDENT', '["VIEW_OWN_PROFILE", "UPDATE_OWN_PROFILE", "VIEW_OWN_MARKS"]'),
('TEACHER', '["VIEW_OWN_PROFILE", "UPDATE_OWN_PROFILE", "VIEW_ALL_STUDENTS", "VIEW_STUDENT_MARKS", "CREATE_MARK", "UPDATE_MARK", "DELETE_MARK", "DELETE_STUDENT", "DELETE_TEACHER", "VIEW_OWN_MODULES", "UPDATE_OWN_MODULES", "ASSIGN_MODULE"]');
```

---

## AUTH

### Register Student
```
POST /auth/register/student
No auth required

Body:
{
  "name": "John Doe",
  "email": "john@school.com",
  "password": "secret123"
}

201: Student registered successfully
409: Email already exists
400: Validation error (blank name/email/password, invalid email)
```

### Register Teacher
```
POST /auth/register/teacher
No auth required

Body:
{
  "name": "Ms. Smith",
  "email": "smith@school.com",
  "password": "teach456",
  "modules": ["Mathematics", "Physics"]
}

201: Teacher registered successfully
409: Email already exists
400: Validation error
```

### Login
```
POST /auth/login
No auth required

Body:
{
  "email": "john@school.com",
  "password": "secret123"
}

200: Login successful (returns JWT token)
401: Invalid email or password
```

---

## STUDENTS

### Get All Students
```
GET /students
Permission: VIEW_ALL_STUDENTS (teacher only)

200: Students fetched successfully
404: No students found
403: Access denied (student tries)
```

### Get Student by Code
```
GET /students/{studentCode}
Permission: VIEW_OWN_PROFILE (own) or VIEW_ALL_STUDENTS (any)

200: Student fetched successfully
404: Student not found
403: Access denied (student views other student)
```

### Update Student Profile
```
PUT /students/{studentCode}
Permission: UPDATE_OWN_PROFILE
Ownership: Students can only update their own profile, teachers can update any

Body:
{
  "name": "John Updated",
  "email": "newemail@school.com"
}

200: Student updated successfully
404: Student not found
409: Email already exists
403: Access denied
```

### Delete Student
```
DELETE /students/{studentCode}
Permission: DELETE_STUDENT (teacher only)

200: Student deleted successfully
404: Student not found
403: Access denied (student tries)
```

### Update Student Status
```
PATCH /students/{studentCode}/status
Permission: DELETE_STUDENT (teacher only)

Body:
{
  "status": "EXPELLED"
}

Valid statuses: ACTIVE, EXPELLED, INACTIVE

200: Student updated successfully
404: Student not found
400: Invalid status
403: Access denied
```

### Assign Modules to Student
```
POST /students/{studentCode}/modules
Permission: ASSIGN_MODULE (teacher only)

Body:
{
  "modules": ["Mathematics", "Physics"]
}

200: Student updated successfully
404: Student not found
403: Access denied
```

### Get Student Marks
```
GET /students/{studentCode}/marks
Permission: VIEW_OWN_MARKS (own) or VIEW_STUDENT_MARKS (any)

200: Marks fetched successfully
404: Student not found / No marks found
403: Access denied (student views other student's marks)
```

---

## TEACHERS

### Get All Teachers
```
GET /teachers
Any logged-in user

200: Teachers fetched successfully
404: No teachers found
```

### Get Teacher by Code
```
GET /teachers/{teacherCode}
Any logged-in user

200: Teacher fetched successfully
404: Teacher not found
```

### Update Teacher Profile
```
PUT /teachers/{teacherCode}
Permission: UPDATE_OWN_PROFILE
Ownership: Own profile only

Body:
{
  "name": "Ms. Smith Updated",
  "email": "newemail@school.com",
  "modules": ["Mathematics", "Physics", "Chemistry"]
}

200: Teacher updated successfully
404: Teacher not found
409: Email already exists
403: Access denied
```

### Delete Teacher
```
DELETE /teachers/{teacherCode}
Permission: DELETE_TEACHER

200: Teacher deleted successfully
404: Teacher not found
403: Access denied
```

### Get Teacher Modules
```
GET /teachers/{teacherCode}/modules
Permission: VIEW_OWN_MODULES
Ownership: Own modules only

200: Returns modules array
404: Teacher not found
403: Access denied
```

---

## MARKS

### Create Mark
```
POST /marks
Permission: CREATE_MARK (teacher only)
Validation: Teacher must teach the modules being graded

Body:
{
  "studentCode": "STD-001",
  "marks": {
    "Mathematics": 85,
    "Physics": 72
  }
}

Stored as:
{
  "Mathematics": { "score": 85, "updatedBy": "TCH-001" },
  "Physics": { "score": 72, "updatedBy": "TCH-001" }
}

201: Mark created successfully
409: Mark record already exists for this student
404: Student not found
403: Access denied / You don't teach one or more of these modules
```

### Get All Marks
```
GET /marks
Permission: VIEW_STUDENT_MARKS (teacher only)

200: Marks fetched successfully
404: No marks found
403: Access denied
```

### Get Mark by Code
```
GET /marks/{markCode}
Permission: VIEW_OWN_MARKS (own) or VIEW_STUDENT_MARKS (any)

200: Mark fetched successfully
404: Mark not found
403: Access denied (student views other student's mark)
```

### Update Mark
```
PUT /marks/{markCode}
Permission: UPDATE_MARK (teacher only)
Validation: Teacher must teach the modules being graded
Note: Merges with existing marks, doesn't replace

Body:
{
  "studentCode": "STD-001",
  "marks": {
    "Mathematics": 92
  }
}

200: Mark updated successfully
404: Mark not found
403: Access denied / You don't teach one or more of these modules
```

### Delete Mark
```
DELETE /marks/{markCode}
Permission: DELETE_MARK (teacher only)

200: Mark deleted successfully
404: Mark not found
403: Access denied
```

---

## PERMISSIONS

### Student Permissions
| Permission | What it allows |
|---|---|
| VIEW_OWN_PROFILE | View own student profile |
| UPDATE_OWN_PROFILE | Update own name/email |
| VIEW_OWN_MARKS | View own marks |

### Teacher Permissions
| Permission | What it allows |
|---|---|
| VIEW_OWN_PROFILE | View own teacher profile |
| UPDATE_OWN_PROFILE | Update own name/email/modules |
| VIEW_ALL_STUDENTS | List all students, view any student |
| VIEW_STUDENT_MARKS | View any student's marks |
| CREATE_MARK | Create marks (own modules only) |
| UPDATE_MARK | Update marks (own modules only) |
| DELETE_MARK | Delete any mark |
| DELETE_STUDENT | Delete students, change student status |
| DELETE_TEACHER | Delete teachers |
| VIEW_OWN_MODULES | View own modules list |
| UPDATE_OWN_MODULES | Update own modules list |
| ASSIGN_MODULE | Assign modules to students |

---

## RESPONSE FORMAT

All responses follow this format:
```json
{
  "status": 200,
  "message": "Student fetched successfully",
  "data": { ... }
}
```

## CUSTOM CODES

- Students: STD-001, STD-002, ...
- Teachers: TCH-001, TCH-002, ...
- Marks: MRK-001, MRK-002, ...

## TEST ORDER

Run in this order for clean testing:

1. Register Student (STD-001)
2. Register Student 2 (STD-002)
3. Register Teacher (TCH-001)
4. Login Student (save token)
5. Login Teacher (save token)
6. Teacher: Assign modules to STD-001
7. Teacher: Create marks for STD-001
8. Student: View own marks
9. Teacher: Update marks
10. Teacher: Get all students
11. Student: View own profile
12. Student: Update own profile
13. Teacher: Update student status
14. Teacher: Delete STD-002
15. Run edge cases
