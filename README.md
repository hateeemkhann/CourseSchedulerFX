# CourseSchedulerFX 📅

A desktop timetable and course scheduling management system built with JavaFX, Maven, and SQL Server.

## Overview

CourseSchedulerFX helps academic institutions manage courses and automate timetable scheduling:
- **Timetable Scheduling** — Create, modify, and optimize class schedules and special sessions
- **Course & Faculty Allocation** — Assign teachers to courses, manage workload, and track availability
- **Batch & Department Tracking** — Organize academic batches, semesters, and department hierarchies
- **Classroom & Slot Management** — Room capacity planning and automated timeslot collision detection
- **Data Structures Integration** — Custom trees, graphs, and queues for efficient scheduling algorithms
- **Admin Dashboard** — Role-based administrative dashboard for complete institutional control

## Project Structure

```
CourseSchedulerFX/
├── src/
│   ├── main/
│   │   ├── java/com/example/courseschedulerfx/
│   │   │   ├── Main.java                   # Application entry point
│   │   │   ├── controllers/                # JavaFX FXML controllers
│   │   │   │   ├── ScheduleManagementController.java
│   │   │   │   ├── CourseManagementController.java
│   │   │   │   ├── TeacherManagementController.java
│   │   │   │   ├── BatchManagementController.java
│   │   │   │   ├── ClassRoomManagementController.java
│   │   │   │   └── ...
│   │   │   ├── DAO/                        # Database access objects
│   │   │   │   ├── DBConnection.java       # SQL Server connection manager
│   │   │   │   ├── ScheduleDAO.java
│   │   │   │   ├── CourseDAO.java
│   │   │   │   └── ...
│   │   │   ├── datastructures/             # Custom scheduling data structures
│   │   │   │   ├── Treee.java
│   │   │   │   ├── LinkedListt.java
│   │   │   │   ├── Stackk.java
│   │   │   │   └── Queuee.java
│   │   │   └── model/                      # Entity data models
│   │   └── resources/                      # FXML views and CSS styles
├── pom.xml                                 # Maven dependencies and build configuration
└── README.md
```

## Setup

1. **Clone the repo and navigate to the project folder**
   ```bash
   git clone https://github.com/hateeemkhann/CourseSchedulerFX.git
   cd CourseSchedulerFX
   ```

2. **Build the project with Maven**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```
   *(Or run `Main.java` directly from IntelliJ IDEA)*

## Tech Stack

- **Language**: Java 11+
- **GUI Framework**: JavaFX 17 (FXML, Scene Builder)
- **Build Tool**: Apache Maven
- **Database**: Microsoft SQL Server / Azure SQL
- **Connectivity**: JDBC (`mssql-jdbc`)
- **IDE**: IntelliJ IDEA
