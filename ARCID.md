# Architectural Design Choice
This document describes the architectural design choices made for the project. It outlines the key decisions regarding the structure, components, and technologies used in the system.

Structure 
```bash
src
└── com
    └── library
        ├── app         (Main.java, background schedulers)
        ├── config      (Database connections, JDBC utilities)
        ├── domain      (Entities including LibraryConfig, Enums, Interfaces)
        ├── exception   (Custom business exceptions)
        ├── repository  (Data access interfaces and MySQL implementations)
        ├── service     (Business logic, validations)
        ├── ui          (GUI forms, dialogs, terminal tester)
        └── util        (Helpers: PasswordHasher, DateHelper)
```

Even though your plan states that the domain package holds "interface kontrak" (like Auditable and Searchable), those are business/domain contracts. A book being searchable or a transaction being auditable are rules of your business.

The Repository<T, ID> interface, however, is an infrastructure contract. It dictates how data is persisted and retrieved (save, findById, findAll). Therefore, it belongs at the root of the data access layer.


# ENUMS
on writting enums first word must contains what its use about
so change from Role to UserRole